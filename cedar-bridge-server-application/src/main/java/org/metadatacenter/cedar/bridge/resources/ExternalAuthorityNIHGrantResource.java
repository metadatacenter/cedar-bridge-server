package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Path("/ext-auth/nih-grant")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityNIHGrantResource extends CedarMicroserviceResource {

  private static final String NIH_REPORTER_API = "https://api.reporter.nih.gov/v2/projects/search";
  private static final String NIH_REPORTER_IRI_PREFIX = "https://reporter.nih.gov/project-details/";
  private static final int DEFAULT_PAGE_SIZE = 100;

  public ExternalAuthorityNIHGrantResource(CedarConfig cedarConfig) {
    super(cedarConfig);
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response getGrantDetails(@PathParam("id") String requested) throws CedarException {
    Map<String, Object> out = new HashMap<>();
    out.put("requestedId", requested);

    String grantId = extractGrantId(requested);
    if (grantId == null) {
      out.put("found", false);
      return CedarResponse.ok().entity(out).build();
    }

    String body = String.format("{\"criteria\":{\"project_nums\":[\"%s\"]}}", grantId);

    try {
      HttpResponse r = ProxyUtil.proxyPost(NIH_REPORTER_API, defaultHeaders(), body);
      int code = r.getStatusLine().getStatusCode();

      if (code == HttpConstants.OK) {
        String respBody = EntityUtils.toString(r.getEntity());
        JsonNode root = JsonMapper.MAPPER.readTree(respBody);
        JsonNode results = root.path("results");

        if (results.isArray() && results.size() > 0) {
          JsonNode first = results.get(0);
          String projectNum = asTextOrNull(first, "project_num");
          String projectId = asTextOrNull(first, "project_id");
          String title = asTextOrNull(first, "project_title");
          String org = asTextOrNull(first, "org_name");
          String pi = first.path("principal_investigators").isArray() && first.path("principal_investigators").size() > 0
              ? asTextOrNull(first.path("principal_investigators").get(0), "full_name")
              : null;

          if (projectId != null && title != null) {
            out.put("found", true);
            out.put("id", NIH_REPORTER_IRI_PREFIX + projectId);
            out.put("name", title);
            out.put("details", buildDetails(title, org, pi, projectNum));
          } else {
            out.put("found", false);
          }
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
  public Response searchByName(@QueryParam("q") String query,
                               @QueryParam("page") Integer page,
                               @QueryParam("pageSize") Integer pageSize) throws CedarException {

    final int pageVal = (page != null) ? page : 0;
    final int pageSizeVal = (pageSize != null) ? pageSize : DEFAULT_PAGE_SIZE;

    if (pageVal < 0 || pageSizeVal <= 1) {
      return CedarResponse.status(CedarResponseStatus.BAD_REQUEST)
          .entity("Invalid pagination parameters: page must be >= 0 and pageSize must be > 1")
          .build();
    }

    Map<String, Object> response = new HashMap<>();
    if (query == null || query.isBlank()) {
      response.put("found", false);
      response.put("results", Collections.emptyMap());
      response.put("page", pageVal);
      response.put("pageSize", pageSizeVal);
      return CedarResponse.ok().entity(response).build();
    }

    // Ask NIH for more than we need, to allow local filtering
    int nihLimit = pageSizeVal * 5; // buffer
    int nihOffset = pageVal * pageSizeVal; // approximate alignment

    String body = String.format(
        "{\"criteria\":{\"project_title\":\"%s\"},\"offset\":%d,\"limit\":%d}",
        query, nihOffset, nihLimit);

    try {
      HttpResponse r = ProxyUtil.proxyPost(NIH_REPORTER_API, defaultHeaders(), body);
      int code = r.getStatusLine().getStatusCode();

      List<Map.Entry<String, Map<String, Object>>> filtered = new ArrayList<>();
      if (code == HttpConstants.OK) {
        String respBody = EntityUtils.toString(r.getEntity());
        JsonNode root = JsonMapper.MAPPER.readTree(respBody);
        JsonNode hits = root.path("results");

        if (hits.isArray()) {
          for (JsonNode hit : hits) {
            String projectNum = asTextOrNull(hit, "project_num");
            String projectId = asTextOrNull(hit, "project_id");
            String title = asTextOrNull(hit, "project_title");
            String org = asTextOrNull(hit, "org_name");
            String pi = hit.path("principal_investigators").isArray() && hit.path("principal_investigators").size() > 0
                ? asTextOrNull(hit.path("principal_investigators").get(0), "full_name")
                : null;

            if (title != null && title.toLowerCase().contains(query.toLowerCase())) {
              if (projectId != null) {
                String iri = NIH_REPORTER_IRI_PREFIX + projectId;
                Map<String, Object> entry = new HashMap<>();
                entry.put("name", title);
                entry.put("details", buildDetails(title, org, pi, projectNum));
                filtered.add(Map.entry(iri, entry));
              }
            }
          }
        }
      }

      // Apply pagination on filtered results
      int start = pageVal * pageSizeVal;
      int end = Math.min(start + pageSizeVal, filtered.size());
      Map<String, Object> results = new LinkedHashMap<>();
      if (start < end) {
        for (int i = start; i < end; i++) {
          Map.Entry<String, Map<String, Object>> e = filtered.get(i);
          results.put(e.getKey(), e.getValue());
        }
      }

      response.put("found", !results.isEmpty());
      response.put("results", results);
      response.put("page", pageVal);
      response.put("pageSize", pageSizeVal);
      return CedarResponse.status(CedarResponseStatus.fromStatusCode(code)).entity(response).build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String extractGrantId(String any) {
    if (any == null || any.isBlank()) return null;
    return any.trim().toUpperCase(Locale.ROOT);
  }

  private String buildDetails(String title, String org, String pi, String grantNum) {
    StringBuilder sb = new StringBuilder();
    sb.append(title);
    if (org != null && !org.isBlank()) sb.append(" at ").append(org);
    if (pi != null && !pi.isBlank()) sb.append(" (PI: ").append(pi).append(")");
    if (grantNum != null && !grantNum.isBlank()) sb.append("; Grant ").append(grantNum);
    return sb.toString();
  }

  private static Map<String, String> defaultHeaders() {
    Map<String, String> h = new HashMap<>();
    h.put("Accept", MediaType.APPLICATION_JSON);
    h.put("Content-Type", MediaType.APPLICATION_JSON);
    return h;
  }

  private static String asTextOrNull(JsonNode n, String field) {
    JsonNode v = n.path(field);
    return (v.isMissingNode() || v.isNull()) ? null : v.asText(null);
  }
}
