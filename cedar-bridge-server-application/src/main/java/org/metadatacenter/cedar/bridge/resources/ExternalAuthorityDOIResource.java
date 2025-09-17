package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpEntity;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.constant.CedarQueryParameters.QP_Q;

@Path("/ext-auth/doi")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityDOIResource extends CedarMicroserviceResource {

  private static final String DATACITE_API_PREFIX = "https://api.datacite.org/dois";
  private static final int DEFAULT_PAGE_SIZE = 100;

  public ExternalAuthorityDOIResource(CedarConfig cedarConfig) {
    super(cedarConfig);
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response getDOIDetails(@PathParam(PP_ID) String doiId) throws CedarException {
    Map<String, Object> myResponse = new HashMap<>();
    myResponse.put("requestedId", doiId);

    String normalizedDoi = extractBaseDOI(doiId);
    String dataciteUrl = DATACITE_API_PREFIX + "/" + normalizedDoi;

    try {
      HttpResponse proxyResponse = ProxyUtil.proxyGet(dataciteUrl, new HashMap<>());
      int statusCode = proxyResponse.getStatusLine().getStatusCode();

      if (statusCode == HttpConstants.OK) {
        HttpEntity entity = proxyResponse.getEntity();
        String responseBody = EntityUtils.toString(entity);
        JsonNode root = JsonMapper.MAPPER.readTree(responseBody);

        JsonNode attributes = root.path("data").path("attributes");
        String doi = attributes.path("doi").asText(null);
        String title = attributes.path("titles").isArray() && attributes.path("titles").size() > 0
            ? attributes.path("titles").get(0).path("title").asText(null)
            : null;
        String publisher = attributes.path("publisher").asText("");
        String pubYear = attributes.path("publicationYear").asText("");
        String resourceType = attributes.path("types").path("resourceTypeGeneral").asText("");

        if (doi != null && title != null) {
          myResponse.put("found", true);
          myResponse.put("id", "https://doi.org/" + doi);
          myResponse.put("name", title);
          myResponse.put("details", buildDetails(title, publisher, pubYear, resourceType));
        } else {
          myResponse.put("found", false);
        }
      } else {
        myResponse.put("found", false);
      }
      return CedarResponse.ok().entity(myResponse).build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @GET
  @Timed
  @Path("/search-by-name")
  public Response searchByName(@QueryParam(QP_Q) String nameFragment,
                               @QueryParam("page") Integer page,
                               @QueryParam("pageSize") Integer pageSize) throws CedarException {

    if (page == null) page = 0;
    if (pageSize == null) pageSize = DEFAULT_PAGE_SIZE;

    if (page < 0 || pageSize <= 1) {
      return CedarResponse.status(CedarResponseStatus.BAD_REQUEST)
          .entity("Invalid pagination parameters: page must be >= 0 and pageSize must be > 1")
          .build();
    }

    final int apiPage = page + 1; // DataCite uses 1-based pagination
    final String q = (nameFragment == null) ? "" : nameFragment;

    // restrict to titles only
    String dataciteUrl = String.format("%s?query=titles.title:%s&page[number]=%d&page[size]=%d",
        DATACITE_API_PREFIX, q, apiPage, pageSize);

    try {
      HttpResponse proxyResponse = ProxyUtil.proxyGet(dataciteUrl, new HashMap<>());
      int statusCode = proxyResponse.getStatusLine().getStatusCode();
      String responseString = EntityUtils.toString(proxyResponse.getEntity());
      JsonNode apiResponseNode = JsonMapper.MAPPER.readTree(responseString);

      Map<String, Object> response = new HashMap<>();
      response.put("page", page);
      response.put("pageSize", pageSize);

      if (statusCode == HttpConstants.OK) {
        Map<String, Object> results = new LinkedHashMap<>();

        JsonNode data = apiResponseNode.path("data");
        if (data.isArray()) {
          for (JsonNode itemNode : data) {
            JsonNode attrs = itemNode.path("attributes");
            String doi = attrs.path("doi").asText(null);
            String title = attrs.path("titles").isArray() && attrs.path("titles").size() > 0
                ? attrs.path("titles").get(0).path("title").asText(null)
                : null;
            String publisher = attrs.path("publisher").asText("");
            String pubYear = attrs.path("publicationYear").asText("");
            String resourceType = attrs.path("types").path("resourceTypeGeneral").asText("");

            if (doi != null && title != null) {
              String doiUrl = "https://doi.org/" + doi;
              Map<String, Object> entry = new HashMap<>();
              entry.put("name", title);
              entry.put("details", buildDetails(title, publisher, pubYear, resourceType));

              results.put(doiUrl, entry);
            }
          }
        }
        response.put("results", results);
        response.put("found", !results.isEmpty());
      } else {
        response.put("found", false);
        response.put("results", new HashMap<>());
      }
      return CedarResponse.status(CedarResponseStatus.fromStatusCode(statusCode)).entity(response).build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String extractBaseDOI(String doiId) {
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

  private String buildDetails(String title, String publisher, String pubYear, String resourceType) {
    StringBuilder sb = new StringBuilder();
    sb.append(title);
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
