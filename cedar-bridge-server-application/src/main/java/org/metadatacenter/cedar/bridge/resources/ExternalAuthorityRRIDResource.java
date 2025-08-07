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
import java.util.Map;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.constant.CedarQueryParameters.QP_Q;

@Path("/ext-auth/rrid")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityRRIDResource extends CedarMicroserviceResource {

  private static String IDENTIFIERS_ORG_RRID_PREFIX = "https://identifiers.org/RRID:";
  private static String SCICRUNCH_RESOLVER_API = "https://scicrunch.org/resolver/";

  private static String rridApiPrefix;
  private static String rridApiKey;

  public ExternalAuthorityRRIDResource(CedarConfig cedarConfig) {
    super(cedarConfig);
    rridApiPrefix = cedarConfig.getExternalAuthorities().getRrid().getApiPrefix();
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
  public Response searchByName(@QueryParam(QP_Q) String nameFragment) throws CedarException {

    String requestBody = "{\n" +
        "  \"size\": 10,\n" +
        "  \"query\": {\n" +
        "    \"bool\": {\n" +
        "      \"should\": [\n" +
        "        { \"term\": { \"item.name.aggregate\": { \"value\": \"" + nameFragment + "\", \"boost\": 100 } } },\n" +
        "        { \"prefix\": { \"item.name.aggregate\": { \"value\": \"" + nameFragment + "\", \"boost\": 10 } } },\n" +
        "        { \"wildcard\": { \"item.name.aggregate\": { \"value\": \"*" + nameFragment + "*\", \"boost\": 1 } } }\n" +
        "      ]\n" +
        "    }\n" +
        "  }\n" +
        "}";

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", MediaType.APPLICATION_JSON);
    headers.put("apikey", rridApiKey);

    try {
      HttpResponse proxyResponse = ProxyUtil.proxyPost(rridApiPrefix, headers, requestBody);
      int statusCode = proxyResponse.getStatusLine().getStatusCode();
      String responseString = EntityUtils.toString(proxyResponse.getEntity());
      JsonNode apiResponseNode = JsonMapper.MAPPER.readTree(responseString);

      Map<String, Object> response = new HashMap<>();
      response.put("found", statusCode == HttpConstants.OK);

      Map<String, Object> results = new HashMap<>();
      JsonNode hits = apiResponseNode.path("hits").path("hits");

      for (JsonNode hit : hits) {
        JsonNode itemNode = hit.path("_source").path("item");
        String identifier = itemNode.path("identifier").asText(null);
        String name = itemNode.path("name").asText(null);

        if (identifier != null && name != null) {
          String rridUrl = IDENTIFIERS_ORG_RRID_PREFIX + identifier;

          Map<String, Object> entry = new HashMap<>();
          entry.put("name", name);
          entry.put("details", null); // Placeholder, can be enriched later

          results.put(rridUrl, entry);
        }
      }
      response.put("results", results);
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

}
