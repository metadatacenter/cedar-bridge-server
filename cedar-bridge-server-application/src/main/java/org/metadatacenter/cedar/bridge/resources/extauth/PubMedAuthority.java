package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.MediaType;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarProcessingException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Publications, from NCBI's E-utilities.
 *
 * <p>The only authority that asks two questions at once. Text that is all digits is both a
 * plausible PubMed ID and a plausible title search, so it runs as both, concurrently, and the
 * identifier's answer is offered first.
 */
public class PubMedAuthority implements ExternalAuthority {

  static final String PATH_SEGMENT = "pmid";

  private static final String PUBMED_NCBI_IRI_PREFIX = "https://pubmed.ncbi.nlm.nih.gov/";

  private static final String EUTILS_BASE = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
  private static final String ESEARCH = "esearch.fcgi?db=pubmed&retmode=json";
  private static final String ESUMMARY = "esummary.fcgi?db=pubmed&retmode=json";

  /** NCBI asks callers to identify themselves, and raises the rate limit for those that do. */
  private static final String NCBI_TOOL = "CEDAR";
  private static final String NCBI_EMAIL = "admin@metadatacenter.org";

  private final String eutilsBase;
  private final String ncbiApiKey;

  public PubMedAuthority(CedarConfig cedarConfig) {
    this(EUTILS_BASE, cedarConfig.getExternalAuthorities().getPubMed().getApiKey());
  }

  /** An authority reading another E-utilities endpoint, so a test can stand one up locally. */
  PubMedAuthority(String eutilsBase, String ncbiApiKey) {
    this.eutilsBase = eutilsBase;
    this.ncbiApiKey = ncbiApiKey;
  }

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public AuthoritySearchAnswer search(String query, int offset, int limit) throws CedarProcessingException {
    if (query == null || query.isBlank()) {
      return AuthoritySearchAnswer.nothing();
    }

    final String q = query.trim();
    // An exact identifier match leads the first page only; merged into every page, it repeated there.
    final boolean looksLikePmid = offset == 0 && q.chars().allMatch(Character::isDigit);

    CompletableFuture<Lookup> byId = looksLikePmid
        ? CompletableFuture.supplyAsync(() -> unchecked(() -> lookupById(q)))
        : CompletableFuture.completedFuture(Lookup.of(new LinkedHashMap<>(), 0));
    CompletableFuture<Lookup> byTitle =
        CompletableFuture.supplyAsync(() -> unchecked(() -> searchByTitle(q, offset, limit)));

    Lookup identified;
    Lookup titled;
    try {
      identified = byId.join();
      titled = byTitle.join();
    } catch (CompletionException e) {
      // Every failure was a 502 here, an NCBI that did not answer included, which is a 503.
      if (e.getCause() instanceof CedarProcessingException failure) {
        throw failure;
      }
      throw e;
    }
    for (Lookup lookup : List.of(titled, identified)) {
      if (!lookup.ok()) {
        return AuthoritySearchAnswer.failed(lookup.status(), null);
      }
    }
    Map<String, Object> merged = new LinkedHashMap<>(identified.terms());
    titled.terms().forEach(merged::putIfAbsent);
    // The identifier match is counted only when it is not also a title match.
    long extra = identified.terms().keySet().stream().filter(k -> !titled.terms().containsKey(k)).count();
    return AuthoritySearchAnswer.of(merged, titled.count() + extra);
  }

  /** What one lookup found, or the status NCBI refused it with. */
  private record Lookup(int status, Map<String, Object> terms, long count) {
    static Lookup of(Map<String, Object> terms, long count) {
      return new Lookup(HttpConstants.OK, terms, count);
    }

    static Lookup refused(int status) {
      return new Lookup(status, new LinkedHashMap<>(), 0);
    }

    boolean ok() {
      return status == HttpConstants.OK;
    }
  }

  private interface Call {
    Lookup get() throws CedarProcessingException;
  }

  /** A lookup run on another thread, which can throw only what is unchecked. */
  private static Lookup unchecked(Call call) {
    try {
      return call.get();
    } catch (CedarProcessingException e) {
      throw new CompletionException(e);
    }
  }

  @Override
  public AuthorityDetailsAnswer details(String id) throws CedarProcessingException {
    final String pmid = extractPmid(id);
    if (pmid == null) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }

    RegistryReply reply = RegistryReply.get(summaryUrl(pmid), defaultHeaders());
    if (reply.status() == HttpConstants.NOT_FOUND) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }
    if (!reply.ok()) {
      // A publication NCBI failed to look up is not one it does not hold.
      return AuthorityDetailsAnswer.failed(reply.status(), new HashMap<>());
    }

    JsonNode result = reply.json().path("result").path(pmid);
    String title = asTextOrNull(result, "title");
    if (title == null || title.isBlank()) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }

    Map<String, Object> body = new HashMap<>();
    body.put("name", title);
    body.put("id", PUBMED_NCBI_IRI_PREFIX + pmid);
    return AuthorityDetailsAnswer.found(body);
  }

  /** The one publication an identifier names, as a single term or none. */
  private Lookup lookupById(String pmid) throws CedarProcessingException {
    RegistryReply reply = RegistryReply.get(summaryUrl(pmid), defaultHeaders());
    if (!reply.ok()) {
      return Lookup.refused(reply.status());
    }
    Map<String, Object> results = new LinkedHashMap<>();
    addTerm(results, pmid, reply.json().path("result").path(pmid));
    return Lookup.of(results, results.size());
  }

  /**
   * Publications whose title matches, in two calls: the identifiers, then their summaries.
   *
   * <p>A trailing wildcard is added only from three characters, so a one- or two-letter fragment
   * does not ask NCBI to match most of PubMed.
   */
  private Lookup searchByTitle(String raw, int offset, int limit) throws CedarProcessingException {
    Map<String, Object> results = new LinkedHashMap<>();

    String term = (!raw.endsWith("*") && raw.length() >= 3) ? raw + "*" : raw;
    String esearchUrl = eutilsBase + ESEARCH + "&retstart=" + offset + "&retmax=" + limit
        + "&term=" + url(term) + "[Title]" + ncbiOptionalParams();

    // A refusal from either call was answered as a search that matched nothing.
    RegistryReply search = RegistryReply.get(esearchUrl, defaultHeaders());
    if (!search.ok()) {
      return Lookup.refused(search.status());
    }
    JsonNode esearch = search.json().path("esearchresult");
    long count = esearch.path("count").asLong(0);
    JsonNode idList = esearch.path("idlist");
    if (!idList.isArray() || idList.isEmpty()) {
      return Lookup.of(results, count);
    }

    List<String> pmids = new ArrayList<>();
    idList.forEach(node -> pmids.add(node.asText()));

    RegistryReply summary = RegistryReply.get(summaryUrl(String.join(",", pmids)), defaultHeaders());
    if (!summary.ok()) {
      return Lookup.refused(summary.status());
    }
    JsonNode summaries = summary.json().path("result");
    for (String pmid : pmids) {
      addTerm(results, pmid, summaries.path(pmid));
    }
    return Lookup.of(results, count);
  }

  /** One summary as a term, or nothing when it carries no title to show. */
  private static void addTerm(Map<String, Object> results, String pmid, JsonNode item) {
    String title = asTextOrNull(item, "title");
    if (title == null || title.isBlank()) {
      return;
    }
    Map<String, Object> term = new HashMap<>();
    term.put("name", title);
    term.put("details",
        buildDetails(title, asTextOrNull(item, "fulljournalname"), asTextOrNull(item, "pubdate"), pmid));
    results.put(PUBMED_NCBI_IRI_PREFIX + pmid + "/", term);
  }

  private String summaryUrl(String ids) {
    return eutilsBase + ESUMMARY + "&id=" + url(ids) + ncbiOptionalParams();
  }

  private static String buildDetails(String title, String journal, String pubdate, String pmid) {
    String describedJournal =
        (journal == null || journal.isBlank()) ? "journal article" : ("a " + journal + " article");
    String year = (pubdate == null) ? "" : " (" + pubdate + ")";
    return String.format("%s Is %s%s; PMID %s", title, describedJournal, year, pmid);
  }

  private static String extractPmid(String any) {
    if (any == null || any.isBlank()) {
      return null;
    }
    String s = any.trim();

    // A URL names the publication in its last segment.
    if (s.startsWith("http://") || s.startsWith("https://")) {
      int lastSlash = s.lastIndexOf('/');
      if (lastSlash >= 0 && lastSlash + 1 < s.length()) {
        s = s.substring(lastSlash + 1);
      }
    }

    if (s.toLowerCase(Locale.ROOT).startsWith("pubmed:")) {
      s = s.substring("pubmed:".length());
    }
    if (s.toUpperCase(Locale.ROOT).startsWith("PMID:")) {
      s = s.substring("PMID:".length());
    }

    // A PubMed ID is digits.
    s = s.replaceAll("[^0-9]", "");
    return s.isEmpty() ? null : s;
  }

  /** NCBI takes no key in a header; it goes on the query string. */
  private static Map<String, String> defaultHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", MediaType.APPLICATION_JSON);
    return headers;
  }

  private String ncbiOptionalParams() {
    StringBuilder sb = new StringBuilder();
    if (ncbiApiKey != null && !ncbiApiKey.isBlank()) {
      sb.append("&api_key=").append(url(ncbiApiKey));
    }
    sb.append("&tool=").append(url(NCBI_TOOL));
    sb.append("&email=").append(url(NCBI_EMAIL));
    return sb.toString();
  }

  private static String url(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  private static String asTextOrNull(JsonNode node, String field) {
    JsonNode value = node.path(field);
    return (value.isMissingNode() || value.isNull()) ? null : value.asText(null);
  }
}
