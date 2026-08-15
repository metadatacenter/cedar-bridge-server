package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.MediaType;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Awarded grants, from NIH RePORTER. */
public class NihGrantAuthority implements ExternalAuthority {

  static final String PATH_SEGMENT = "nih-grant";

  private static final String NIH_REPORTER_API = "https://api.reporter.nih.gov/v2/projects/search";
  private static final String NIH_REPORTER_IRI_PREFIX = "https://reporter.nih.gov/project-details/";

  /**
   * How much more than a page is asked for, because the results are filtered again here.
   *
   * <p>RePORTER matches a title loosely, so a page of its answers is not a page of answers that
   * contain what was typed. Asking for five times the page and narrowing locally is how this has
   * always worked — approximate, and the alternative is showing matches that do not match.
   */
  private static final int OVER_FETCH = 5;

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public AuthoritySearchAnswer search(String query, int page, int pageSize) {
    if (query == null || query.isBlank()) {
      return AuthoritySearchAnswer.nothing();
    }

    String body = String.format("{\"criteria\":{\"project_title\":\"%s\"},\"offset\":%d,\"limit\":%d}",
        query, page * pageSize, pageSize * OVER_FETCH);

    try {
      ClassicHttpResponse response = ProxyUtil.proxyPost(NIH_REPORTER_API, defaultHeaders(), body);
      int statusCode = response.getCode();

      List<Map.Entry<String, Map<String, Object>>> matching = new ArrayList<>();
      if (statusCode == HttpConstants.OK) {
        JsonNode root = JsonMapper.MAPPER.readTree(EntityUtils.toString(response.getEntity()));
        for (JsonNode hit : root.path("results")) {
          String title = asTextOrNull(hit, "project_title");
          String projectId = asTextOrNull(hit, "project_id");
          if (title != null && projectId != null && title.toLowerCase().contains(query.toLowerCase())) {
            matching.add(Map.entry(NIH_REPORTER_IRI_PREFIX + projectId, term(hit, title)));
          }
        }
      }

      // Paginate what survived the narrowing, not what RePORTER offered.
      Map<String, Object> results = new LinkedHashMap<>();
      int start = page * pageSize;
      int end = Math.min(start + pageSize, matching.size());
      for (int i = start; i < end; i++) {
        results.put(matching.get(i).getKey(), matching.get(i).getValue());
      }

      return statusCode == HttpConstants.OK
          ? AuthoritySearchAnswer.of(results)
          : AuthoritySearchAnswer.failed(statusCode, null);
    } catch (CedarProcessingException | IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public AuthorityDetailsAnswer details(String id) {
    if (id == null || id.isBlank()) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }

    String body = String.format("{\"criteria\":{\"project_nums\":[\"%s\"]}}", id.trim().toUpperCase(Locale.ROOT));

    try {
      ClassicHttpResponse response = ProxyUtil.proxyPost(NIH_REPORTER_API, defaultHeaders(), body);
      if (response.getCode() != HttpConstants.OK) {
        return AuthorityDetailsAnswer.notFound(new HashMap<>());
      }

      JsonNode results = JsonMapper.MAPPER.readTree(EntityUtils.toString(response.getEntity())).path("results");
      if (!results.isArray() || results.isEmpty()) {
        return AuthorityDetailsAnswer.notFound(new HashMap<>());
      }

      JsonNode first = results.get(0);
      String projectId = asTextOrNull(first, "project_id");
      String title = asTextOrNull(first, "project_title");
      if (projectId == null || title == null) {
        return AuthorityDetailsAnswer.notFound(new HashMap<>());
      }

      Map<String, Object> found = new HashMap<>(term(first, title));
      found.put("id", NIH_REPORTER_IRI_PREFIX + projectId);
      return AuthorityDetailsAnswer.found(found);
    } catch (CedarProcessingException | IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /** One project as a term: what it is called, and a line describing it. */
  private static Map<String, Object> term(JsonNode project, String title) {
    JsonNode investigators = project.path("principal_investigators");
    String pi = investigators.isArray() && !investigators.isEmpty()
        ? asTextOrNull(investigators.get(0), "full_name")
        : null;

    Map<String, Object> term = new HashMap<>();
    term.put("name", title);
    term.put("details",
        buildDetails(title, asTextOrNull(project, "org_name"), pi, asTextOrNull(project, "project_num")));
    return term;
  }

  private static String buildDetails(String title, String org, String pi, String grantNum) {
    StringBuilder sb = new StringBuilder();
    sb.append(title);
    if (org != null && !org.isBlank()) {
      sb.append(" at ").append(org);
    }
    if (pi != null && !pi.isBlank()) {
      sb.append(" (PI: ").append(pi).append(")");
    }
    if (grantNum != null && !grantNum.isBlank()) {
      sb.append("; Grant ").append(grantNum);
    }
    return sb.toString();
  }

  private static Map<String, String> defaultHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", MediaType.APPLICATION_JSON);
    headers.put("Content-Type", MediaType.APPLICATION_JSON);
    return headers;
  }

  private static String asTextOrNull(JsonNode node, String field) {
    JsonNode value = node.path(field);
    return (value.isMissingNode() || value.isNull()) ? null : value.asText(null);
  }
}
