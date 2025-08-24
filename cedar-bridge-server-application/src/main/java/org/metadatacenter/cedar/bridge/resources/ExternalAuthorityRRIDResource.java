package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.Map;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.constant.CedarQueryParameters.QP_Q;

@Path("/ext-auth/rrid")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityRRIDResource extends CedarMicroserviceResource {

  private static String SCICRUNCH_API_PREFIX = "https://api.scicrunch.io/elastic/v1/*_pr/_search";
  private static String IDENTIFIERS_ORG_RRID_PREFIX = "https://identifiers.org/RRID:";
  private static String SCICRUNCH_RESOLVER_API = "https://scicrunch.org/resolver/";
  private static final int DEFAULT_PAGE_SIZE = 100;

  private static String rridApiKey;

  public ExternalAuthorityRRIDResource(CedarConfig cedarConfig) {
    super(cedarConfig);
    rridApiKey = cedarConfig.getExternalAuthorities().getRrid().getApiKey();
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response getRRIDDetails(@PathParam(PP_ID) String rridId) throws CedarException {
    Map<String, Object> myResponse = new HashMap<>();

    final String baseRridId = extractBaseRRID(rridId);
    final String sciCrunchRridIri = SCICRUNCH_RESOLVER_API + baseRridId;
    final String sciCrunchResolverUrl = sciCrunchRridIri + ".json";

    myResponse.put("requestedId", rridId);

    Map<String, String> headers = new HashMap<>();
    headers.put("apikey", rridApiKey);

    try {
      HttpResponse proxyResponse = ProxyUtil.proxyGet(sciCrunchResolverUrl, headers);
      int statusCode = proxyResponse.getStatusLine().getStatusCode();

      if (statusCode == HttpConstants.OK) {
        HttpEntity entity = proxyResponse.getEntity();
        String responseBody = EntityUtils.toString(entity);
        JsonNode root = JsonMapper.MAPPER.readTree(responseBody);

        JsonNode hits = root.path("hits").path("hits");
        if (hits.isArray() && hits.size() > 0) {
          JsonNode item = hits.get(0).path("_source").path("item");
          String identifier = item.path("identifier").asText(null);
          String name = item.path("name").asText(null);

          if (identifier != null && name != null) {
            myResponse.put("found", true);
            myResponse.put("id", IDENTIFIERS_ORG_RRID_PREFIX + identifier);
            myResponse.put("name", name);
          } else {
            myResponse.put("found", false);
          }
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

    final int from = page * pageSize;
    final String q = (nameFragment == null) ? "" : nameFragment;

    // Build ES query body with pagination
    ObjectNode root = JsonMapper.MAPPER.createObjectNode();
    root.put("from", from);
    root.put("size", pageSize);

    ObjectNode query = root.putObject("query").putObject("bool");
    ArrayNode should = query.putArray("should");

    ObjectNode termVal = JsonMapper.MAPPER.createObjectNode();
    termVal.put("value", q);
    termVal.put("boost", 100);
    ObjectNode termField = JsonMapper.MAPPER.createObjectNode();
    termField.set("item.name.aggregate", termVal);
    ObjectNode term = JsonMapper.MAPPER.createObjectNode();
    term.set("term", termField);
    should.add(term);

    ObjectNode prefixVal = JsonMapper.MAPPER.createObjectNode();
    prefixVal.put("value", q);
    prefixVal.put("boost", 10);
    ObjectNode prefixField = JsonMapper.MAPPER.createObjectNode();
    prefixField.set("item.name.aggregate", prefixVal);
    ObjectNode prefix = JsonMapper.MAPPER.createObjectNode();
    prefix.set("prefix", prefixField);
    should.add(prefix);

    ObjectNode wildcardVal = JsonMapper.MAPPER.createObjectNode();
    wildcardVal.put("value", "*" + q + "*");
    wildcardVal.put("boost", 1);
    ObjectNode wildcardField = JsonMapper.MAPPER.createObjectNode();
    wildcardField.set("item.name.aggregate", wildcardVal);
    ObjectNode wildcard = JsonMapper.MAPPER.createObjectNode();
    wildcard.set("wildcard", wildcardField);
    should.add(wildcard);

    String requestBody;
    try {
      requestBody = JsonMapper.MAPPER.writeValueAsString(root);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize SciCrunch query body", e);
    }

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", MediaType.APPLICATION_JSON);
    headers.put("apikey", rridApiKey);

    try {
      HttpResponse proxyResponse = ProxyUtil.proxyPost(SCICRUNCH_API_PREFIX, headers, requestBody);
      int statusCode = proxyResponse.getStatusLine().getStatusCode();
      String responseString = EntityUtils.toString(proxyResponse.getEntity());
      JsonNode apiResponseNode = JsonMapper.MAPPER.readTree(responseString);

      Map<String, Object> response = new HashMap<>();
      response.put("page", page);
      response.put("pageSize", pageSize);

      if (statusCode == HttpConstants.OK) {
        JsonNode hits = apiResponseNode.path("hits").path("hits");

        Map<String, Object> results = new java.util.LinkedHashMap<>();
        if (hits.isArray()) {
          for (JsonNode hit : hits) {
            JsonNode itemNode = hit.path("_source").path("item");
            String identifier = itemNode.path("identifier").asText(null);
            String name = itemNode.path("name").asText(null);
            String details = buildDetails(itemNode);

            if (identifier != null && name != null) {
              String rridUrl = IDENTIFIERS_ORG_RRID_PREFIX + identifier;
              Map<String, Object> entry = new HashMap<>();
              entry.put("name", name);
              entry.put("details", details);

              results.put(rridUrl, entry);
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

  private String extractBaseRRID(String rridId) {
    String id = rridId;
    if (id.startsWith("http://") || id.startsWith("https://")) {
      id = id.substring(id.lastIndexOf('/') + 1);
    }
    return id.toUpperCase().startsWith("RRID:") ? id : "RRID:" + id;
  }

  private String buildDetails(JsonNode itemNode) {
    String name = itemNode.path("name").asText("");

    String type = "resource";
    for (JsonNode typeNode : itemNode.path("types")) {
      String t = typeNode.path("name").asText(null);
      if (t != null) {
        type = t;
        break;
      }
    }

    int xrefCount = itemNode.path("alternateIdentifiers").size();

    return String.format(
        "%s is a %s. Alternate identifiers exist in %d database(s).", name, type, xrefCount
    );
  }


}
