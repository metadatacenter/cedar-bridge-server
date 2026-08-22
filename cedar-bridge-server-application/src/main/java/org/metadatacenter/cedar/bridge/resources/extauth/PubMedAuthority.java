package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.MediaType;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
  private static final String ESEARCH = EUTILS_BASE + "esearch.fcgi?db=pubmed&retmode=json";
  private static final String ESUMMARY = EUTILS_BASE + "esummary.fcgi?db=pubmed&retmode=json";

  /** NCBI asks callers to identify themselves, and raises the rate limit for those that do. */
  private static final String NCBI_TOOL = "CEDAR";
  private static final String NCBI_EMAIL = "admin@metadatacenter.org";

  /** What NCBI answered with when neither question could be put to it. */
  private static final int BAD_GATEWAY = 502;

  private final String ncbiApiKey;

  public PubMedAuthority(CedarConfig cedarConfig) {
    this.ncbiApiKey = cedarConfig.getExternalAuthorities().getPubMed().getApiKey();
  }

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public AuthoritySearchAnswer search(String query, int page, int pageSize) {
    if (query == null || query.isBlank()) {
      return AuthoritySearchAnswer.nothing();
    }

    final String q = query.trim();
    final boolean looksLikePmid = q.chars().allMatch(Character::isDigit);

    CompletableFuture<Map<String, Object>> byId = looksLikePmid
        ? CompletableFuture.supplyAsync(() -> lookupById(q))
        : CompletableFuture.completedFuture(new LinkedHashMap<>());
    CompletableFuture<Map<String, Object>> byTitle =
        CompletableFuture.supplyAsync(() -> searchByTitle(q, page, pageSize));

    try {
      Map<String, Object> merged = new LinkedHashMap<>(byId.join());
      byTitle.join().forEach(merged::putIfAbsent);
      return AuthoritySearchAnswer.of(merged);
    } catch (Exception e) {
      return AuthoritySearchAnswer.failed(BAD_GATEWAY, null);
    }
  }

  @Override
  public AuthorityDetailsAnswer details(String id) {
    final String pmid = extractPmid(id);
    if (pmid == null) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }

    try {
      ClassicHttpResponse response = ProxyUtil.proxyGet(summaryUrl(pmid), defaultHeaders());
      if (response.getCode() != HttpConstants.OK) {
        return AuthorityDetailsAnswer.notFound(new HashMap<>());
      }

      JsonNode result = JsonMapper.MAPPER.readTree(EntityUtils.toString(response.getEntity()))
          .path("result").path(pmid);
      String title = asTextOrNull(result, "title");
      if (title == null || title.isBlank()) {
        return AuthorityDetailsAnswer.notFound(new HashMap<>());
      }

      Map<String, Object> body = new HashMap<>();
      body.put("name", title);
      body.put("id", PUBMED_NCBI_IRI_PREFIX + pmid);
      return AuthorityDetailsAnswer.found(body);
    } catch (CedarProcessingException | IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /** The one publication an identifier names, as a single term or none. */
  private Map<String, Object> lookupById(String pmid) {
    Map<String, Object> results = new LinkedHashMap<>();
    try {
      ClassicHttpResponse response = ProxyUtil.proxyGet(summaryUrl(pmid), defaultHeaders());
      if (response.getCode() != HttpConstants.OK) {
        return results;
      }
      JsonNode item = JsonMapper.MAPPER.readTree(EntityUtils.toString(response.getEntity()))
          .path("result").path(pmid);
      addTerm(results, pmid, item);
      return results;
    } catch (CedarProcessingException | IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Publications whose title matches, in two calls: the identifiers, then their summaries.
   *
   * <p>A trailing wildcard is added only from three characters, so a one- or two-letter fragment
   * does not ask NCBI to match most of PubMed.
   */
  private Map<String, Object> searchByTitle(String raw, int page, int pageSize) {
    Map<String, Object> results = new LinkedHashMap<>();

    String term = (!raw.endsWith("*") && raw.length() >= 3) ? raw + "*" : raw;
    String esearchUrl = ESEARCH + "&retstart=" + (page * pageSize) + "&retmax=" + pageSize
        + "&term=" + url(term) + "[Title]" + ncbiOptionalParams();

    try {
      ClassicHttpResponse searchResponse = ProxyUtil.proxyGet(esearchUrl, defaultHeaders());
      if (searchResponse.getCode() != HttpConstants.OK) {
        return results;
      }

      JsonNode idList = JsonMapper.MAPPER.readTree(EntityUtils.toString(searchResponse.getEntity()))
          .path("esearchresult").path("idlist");
      if (!idList.isArray() || idList.isEmpty()) {
        return results;
      }

      List<String> pmids = new ArrayList<>();
      idList.forEach(node -> pmids.add(node.asText()));

      ClassicHttpResponse summaryResponse =
          ProxyUtil.proxyGet(summaryUrl(String.join(",", pmids)), defaultHeaders());
      if (summaryResponse.getCode() != HttpConstants.OK) {
        return results;
      }

      JsonNode summaries = JsonMapper.MAPPER.readTree(EntityUtils.toString(summaryResponse.getEntity()))
          .path("result");
      for (String pmid : pmids) {
        addTerm(results, pmid, summaries.path(pmid));
      }
      return results;
    } catch (CedarProcessingException | IOException | ParseException e) {
      throw new RuntimeException(e);
    }
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
    return ESUMMARY + "&id=" + url(ids) + ncbiOptionalParams();
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
