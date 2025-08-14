package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Path("/ext-auth/pmid")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityPubMedResource extends CedarMicroserviceResource {

  private static final String PUBMED_NCBI_IRI_PREFIX = "https://pubmed.ncbi.nlm.nih.gov/";

  // NCBI E-utilities endpoints (JSON)
  private static final String EUTILS_BASE = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
  private static final String ESEARCH = EUTILS_BASE + "esearch.fcgi?db=pubmed&retmode=json";
  private static final String ESUMMARY = EUTILS_BASE + "esummary.fcgi?db=pubmed&retmode=json";

  // Optional: supply API key/email via config if you have them (improves rate limits)
  private static String ncbiApiKey;   // cedarConfig.externalAuthorities.pubmed.apiKey (optional)
  private static String ncbiTool;     // cedarConfig.externalAuthorities.pubmed.tool (optional)
  private static String ncbiEmail;    // cedarConfig.externalAuthorities.pubmed.email (optional)

  public ExternalAuthorityPubMedResource(CedarConfig cedarConfig) {
    super(cedarConfig);
    ncbiApiKey = null; // pm.getApiKey();
    ncbiTool = null; // pm.getTool();
    ncbiEmail = null; // pm.getEmail();
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response getPMIDDetails(@PathParam("id") String requested) throws CedarException {
    Map<String, Object> out = new HashMap<>();
    out.put("requestedId", requested);

    final String pmid = extractPMID(requested);
    if (pmid == null) {
      out.put("found", false);
      return CedarResponse.ok().entity(out).build();
    }

    String url = ESUMMARY + "&id=" + url(pmid) + addNcbiOptParams();
    try {
      HttpResponse r = ProxyUtil.proxyGet(url, defaultHeaders());
      int code = r.getStatusLine().getStatusCode();

      if (code == HttpConstants.OK) {
        String body = EntityUtils.toString(r.getEntity());
        JsonNode root = JsonMapper.MAPPER.readTree(body);
        JsonNode result = root.path("result").path(pmid);
        String title = asTextOrNull(result, "title");

        if (title != null && !title.isBlank()) {
          out.put("found", true);
          out.put("name", title);
          out.put("id", PUBMED_NCBI_IRI_PREFIX + pmid);
        } else {
          out.put("found", false);
        }
      } else {
        out.put("found", false);
      }
      return CedarResponse.ok().entity(out).build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @GET
  @Timed
  @Path("/search-by-name")
  public Response searchByName(@QueryParam("q") String nameFragment) throws CedarException {
    Map<String, Object> response = new HashMap<>();
    if (nameFragment == null || nameFragment.isBlank()) {
      response.put("found", false);
      response.put("results", Collections.emptyMap());
      return CedarResponse.ok().entity(response).build();
    }

    final String q = nameFragment.trim();
    final boolean looksLikePMID = q.chars().allMatch(Character::isDigit);

    // Run both paths in parallel: ID lookup (if numeric) and title search
    CompletableFuture<Map<String, Object>> idLookupFut =
        looksLikePMID ? CompletableFuture.supplyAsync(() -> lookupById(q))
            : CompletableFuture.completedFuture(Collections.emptyMap());

    CompletableFuture<Map<String, Object>> titleSearchFut =
        CompletableFuture.supplyAsync(() -> searchByTitle(q));

    Map<String, Object> mergedResults;
    int httpStatus = HttpConstants.OK;

    try {
      Map<String, Object> idResults = idLookupFut.join();
      Map<String, Object> titleResults = titleSearchFut.join();

      // Merge with ID results first, then title results (preserve insertion order)
      mergedResults = new LinkedHashMap<>();
      idResults.forEach(mergedResults::put);
      titleResults.forEach((k, v) -> mergedResults.putIfAbsent(k, v));

    } catch (Exception e) {
      // If either future threw, return safe empty result with 502
      response.put("found", false);
      response.put("results", Collections.emptyMap());
      return CedarResponse.status(CedarResponseStatus.fromStatusCode(502)).entity(response).build();
    }

    response.put("found", true);
    response.put("results", mergedResults);
    return CedarResponse.status(CedarResponseStatus.fromStatusCode(httpStatus)).entity(response).build();
  }

  private Map<String, Object> lookupById(String pmid) {
    Map<String, Object> results = new LinkedHashMap<>();
    String esumUrl = ESUMMARY + "&id=" + url(pmid) + addNcbiOptParams();

    try {
      HttpResponse eResp = ProxyUtil.proxyGet(esumUrl, defaultHeaders());
      if (eResp.getStatusLine().getStatusCode() != HttpConstants.OK) {
        return results;
      }

      String body = EntityUtils.toString(eResp.getEntity());
      JsonNode root = JsonMapper.MAPPER.readTree(body).path("result");
      JsonNode item = root.path(pmid);
      String title = asTextOrNull(item, "title");
      if (title == null || title.isBlank()) return results;

      String journal = asTextOrNull(item, "fulljournalname");
      String pubdate = asTextOrNull(item, "pubdate");
      String details = buildDetails(title, journal, pubdate, pmid);

      String key = PUBMED_NCBI_IRI_PREFIX + pmid + "/";
      Map<String, Object> entry = new HashMap<>();
      entry.put("name", title);
      entry.put("details", details);
      results.put(key, entry);
      return results;

    } catch (CedarProcessingException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, Object> searchByTitle(String raw) {
    Map<String, Object> results = new LinkedHashMap<>();

    // Only add wildcard if length >= 3 and the user didn’t supply one
    String term = raw;
    if (!raw.endsWith("*") && raw.length() >= 3) term = raw + "*";

    String esearchUrl = ESEARCH
        + "&retmax=10"
        + "&term=" + url(term) + "[Title]"
        + addNcbiOptParams();

    try {
      HttpResponse sResp = ProxyUtil.proxyGet(esearchUrl, defaultHeaders());
      if (sResp.getStatusLine().getStatusCode() != HttpConstants.OK) return results;

      String sBody = EntityUtils.toString(sResp.getEntity());
      JsonNode sRoot = JsonMapper.MAPPER.readTree(sBody);
      JsonNode idList = sRoot.path("esearchresult").path("idlist");
      if (!idList.isArray() || idList.size() == 0) return results;

      List<String> pmids = new ArrayList<>();
      idList.forEach(n -> pmids.add(n.asText()));

      String esumUrl = ESUMMARY + "&id=" + url(String.join(",", pmids)) + addNcbiOptParams();
      HttpResponse eResp = ProxyUtil.proxyGet(esumUrl, defaultHeaders());
      if (eResp.getStatusLine().getStatusCode() != HttpConstants.OK) return results;

      String eBody = EntityUtils.toString(eResp.getEntity());
      JsonNode eRoot = JsonMapper.MAPPER.readTree(eBody).path("result");

      for (String pmid : pmids) {
        JsonNode item = eRoot.path(pmid);
        String title = asTextOrNull(item, "title");
        if (title == null || title.isBlank()) continue;

        String journal = asTextOrNull(item, "fulljournalname");
        String pubdate = asTextOrNull(item, "pubdate");
        String details = buildDetails(title, journal, pubdate, pmid);

        String key = PUBMED_NCBI_IRI_PREFIX + pmid + "/";
        Map<String, Object> entry = new HashMap<>();
        entry.put("name", title);
        entry.put("details", details);
        results.put(key, entry);
      }
      return results;

    } catch (CedarProcessingException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String buildDetails(String title, String journal, String pubdate, String pmid) {
    String j = (journal == null || journal.isBlank()) ? "journal article" : ("a " + journal + " article");
    String y = (pubdate == null) ? "" : " (" + pubdate + ")";
    return String.format("%s is %s%s. PMID %s.", title, j, y, pmid);
  }

  private static String extractPMID(String any) {
    if (any == null || any.isBlank()) return null;
    String s = any.trim();

    // If URL, take the last path segment
    if (s.startsWith("http://") || s.startsWith("https://")) {
      int lastSlash = s.lastIndexOf('/');
      if (lastSlash >= 0 && lastSlash + 1 < s.length()) s = s.substring(lastSlash + 1);
    }

    if (s.toLowerCase(Locale.ROOT).startsWith("pubmed:")) s = s.substring("pubmed:".length());
    if (s.toUpperCase(Locale.ROOT).startsWith("PMID:")) s = s.substring("PMID:".length());

    // PubMed IDs are digits
    s = s.replaceAll("[^0-9]", "");
    return s.isEmpty() ? null : s;
  }

  private static Map<String, String> defaultHeaders() {
    Map<String, String> h = new HashMap<>();
    h.put("Accept", MediaType.APPLICATION_JSON);
    return h;
    // NCBI doesn't require apikey in headers; we pass it on the query string.
  }

  private static String addNcbiOptParams() {
    StringBuilder sb = new StringBuilder();
    if (ncbiApiKey != null && !ncbiApiKey.isBlank()) sb.append("&api_key=").append(url(ncbiApiKey));
    if (ncbiTool != null && !ncbiTool.isBlank()) sb.append("&tool=").append(url(ncbiTool));
    if (ncbiEmail != null && !ncbiEmail.isBlank()) sb.append("&email=").append(url(ncbiEmail));
    return sb.toString();
  }

  private static String url(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  private static String asTextOrNull(JsonNode n, String field) {
    JsonNode v = n.path(field);
    return (v.isMissingNode() || v.isNull()) ? null : v.asText(null);
  }
}
