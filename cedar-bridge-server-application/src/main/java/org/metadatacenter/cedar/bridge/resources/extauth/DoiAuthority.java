package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Publications and datasets, from DataCite. */
public class DoiAuthority implements ExternalAuthority {

  static final String PATH_SEGMENT = "doi";

  private static final String DATACITE_API_PREFIX = "https://api.datacite.org/dois";
  private static final String DOI_IRI_BASE = "https://doi.org/";

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public AuthoritySearchAnswer search(String query, int page, int pageSize) {
    // DataCite counts pages from one.
    final int apiPage = page + 1;
    final String q = (query == null) ? "" : query;

    // Titles only, so a search for a name does not match an abstract.
    String dataciteUrl = String.format("%s?query=titles.title:%s&page[number]=%d&page[size]=%d",
        DATACITE_API_PREFIX, q, apiPage, pageSize);

    Upstream upstream = get(dataciteUrl);
    if (!upstream.ok()) {
      // DataCite's own status is passed on, which is what this route has always done: a registry
      // that is down is not a search that found nothing.
      return AuthoritySearchAnswer.failed(upstream.statusCode(), null);
    }
    JsonNode root = upstream.document();

    Map<String, Object> results = new LinkedHashMap<>();
    JsonNode data = root.path("data");
    if (data.isArray()) {
      for (JsonNode itemNode : data) {
        JsonNode attributes = itemNode.path("attributes");
        String doi = attributes.path("doi").asText(null);
        String title = title(attributes);
        if (doi != null && title != null) {
          Map<String, Object> term = new HashMap<>();
          term.put("name", title);
          term.put("details", detailsSentence(attributes, title));
          results.put(DOI_IRI_BASE + doi, term);
        }
      }
    }

    return AuthoritySearchAnswer.of(results);
  }

  @Override
  public AuthorityDetailsAnswer details(String id) {
    Upstream upstream = get(DATACITE_API_PREFIX + "/" + extractBaseDoi(id));
    if (!upstream.ok()) {
      // Answered 200 with found=false however DataCite replied, which is what this route has
      // always done for an identifier — unlike search, where the status is passed on.
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }

    JsonNode attributes = upstream.document().path("data").path("attributes");
    String doi = attributes.path("doi").asText(null);
    String title = title(attributes);
    if (doi == null || title == null) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }

    Map<String, Object> body = new HashMap<>();
    body.put("id", DOI_IRI_BASE + doi);
    body.put("name", title);
    body.put("details", detailsSentence(attributes, title));
    return AuthorityDetailsAnswer.found(body);
  }

  /** What DataCite answered: its status, and the document when there is one. */
  private record Upstream(int statusCode, JsonNode document) {
    boolean ok() {
      return statusCode == HttpConstants.OK;
    }
  }

  private static Upstream get(String url) {
    try {
      ClassicHttpResponse response = ProxyUtil.proxyGet(url, new HashMap<>());
      int statusCode = response.getCode();
      if (statusCode != HttpConstants.OK) {
        return new Upstream(statusCode, null);
      }
      return new Upstream(statusCode, JsonMapper.MAPPER.readTree(EntityUtils.toString(response.getEntity())));
    } catch (CedarProcessingException | IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private static String title(JsonNode attributes) {
    JsonNode titles = attributes.path("titles");
    return titles.isArray() && titles.size() > 0 ? titles.get(0).path("title").asText(null) : null;
  }

  private static String extractBaseDoi(String doiId) {
    String id = doiId.trim();
    if (id.startsWith("https://doi.org/")) {
      id = id.substring("https://doi.org/".length());
    } else if (id.startsWith("http://doi.org/")) {
      id = id.substring("http://doi.org/".length());
    } else if (id.toLowerCase().startsWith("doi:")) {
      id = id.substring("doi:".length());
    }
    return id;
  }

  private static String detailsSentence(JsonNode attributes, String title) {
    String publisher = attributes.path("publisher").asText("");
    String pubYear = attributes.path("publicationYear").asText("");
    String resourceType = attributes.path("types").path("resourceTypeGeneral").asText("");

    StringBuilder sb = new StringBuilder(title);
    if (!publisher.isEmpty()) {
      sb.append(" was published by ").append(publisher);
    }
    if (!pubYear.isEmpty()) {
      sb.append(" in ").append(pubYear);
    }
    if (!resourceType.isEmpty()) {
      sb.append(" (resource type: ").append(resourceType).append(")");
    }
    sb.append(".");
    return sb.toString();
  }
}
