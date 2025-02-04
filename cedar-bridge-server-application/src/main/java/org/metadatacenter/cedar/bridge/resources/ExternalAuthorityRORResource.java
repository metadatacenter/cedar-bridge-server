package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.CharEncoding;
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
import org.metadatacenter.util.http.UrlUtil;
import org.metadatacenter.util.json.JsonMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.constant.CedarQueryParameters.QP_Q;

@Path("/ext-auth/ror")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityRORResource extends CedarMicroserviceResource {

  private final static String ROR_API_V2_ORGANIZATIONS_PREFIX = "organizations/";
  private final static String ROR_API_V2_ORGANIZATION_SEARCH_PREFIX = "organizations?query=";
  private static String ROR_API_PREFIX;

  public ExternalAuthorityRORResource(CedarConfig cedarConfig) {
    super(cedarConfig);
    ROR_API_PREFIX = cedarConfig.getExternalAuthorities().getRor().getApiPrefix();
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response getRORDetails(@PathParam(PP_ID) String rorId) throws CedarException {
    String url = ROR_API_PREFIX + ROR_API_V2_ORGANIZATIONS_PREFIX + UrlUtil.urlEncode(rorId);

    HttpResponse proxyResponse = ProxyUtil.proxyGet(url, new HashMap<>());
    int statusCode = proxyResponse.getStatusLine().getStatusCode();

    JsonNode apiResponseNode;

    HttpEntity entity = proxyResponse.getEntity();
    try {
      String apiResponseString = EntityUtils.toString(entity, CharEncoding.UTF_8);
      apiResponseNode = JsonMapper.MAPPER.readTree(apiResponseString);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Map<String, Object> myResponse = new HashMap<>();
    myResponse.put("found", statusCode == HttpConstants.OK);
    myResponse.put("requestedId", rorId);
    myResponse.put("id", getId(apiResponseNode));
    //myResponse.put("rawResponse", apiResponseNode);

    if (statusCode == HttpConstants.OK) {
      myResponse.put("name", getBestRORName(apiResponseNode));
    } else {
      myResponse.put("name", null);
      myResponse.put("errors", getRORErrors(apiResponseNode));
    }

    return CedarResponse.status(CedarResponseStatus.fromStatusCode(statusCode)).entity(myResponse).build();
  }

  @GET
  @Timed
  @Path("/search-by-name")
  public Response searchByName(@QueryParam(QP_Q) String orgNameFragment) throws CedarException {
    if (orgNameFragment != null && !orgNameFragment.isEmpty() && !orgNameFragment.endsWith("*")) {
      orgNameFragment = orgNameFragment + "*";
    }

    String url = ROR_API_PREFIX + ROR_API_V2_ORGANIZATION_SEARCH_PREFIX + UrlUtil.urlEncode(orgNameFragment);

    HttpResponse proxyResponse = ProxyUtil.proxyGet(url, new HashMap<>());
    int statusCode = proxyResponse.getStatusLine().getStatusCode();

    JsonNode apiResponseNode;

    HttpEntity entity = proxyResponse.getEntity();
    try {
      String apiResponseString = EntityUtils.toString(entity, CharEncoding.UTF_8);
      apiResponseNode = JsonMapper.MAPPER.readTree(apiResponseString);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Map<String, Object> myResponse = new HashMap<>();

    if (statusCode == HttpConstants.OK) {
      Map<String, String> rorSearchNames = getRORSearchNames(apiResponseNode);
      myResponse.put("results", rorSearchNames);
      myResponse.put("found", !rorSearchNames.isEmpty());
    } else {
      myResponse.put("found", false);
    }

    return CedarResponse.status(CedarResponseStatus.fromStatusCode(statusCode)).entity(myResponse).build();
  }

  private String getBestRORName(JsonNode apiResponseNode) {
    JsonNode namesNode = apiResponseNode.get("names");
    if (namesNode != null && namesNode.isArray()) {
      String fallbackName = null;
      for (JsonNode name : namesNode) {
        JsonNode typesNode = name.get("types");
        JsonNode valueNode = name.get("value");
        if (typesNode != null && typesNode.isArray()) {
          for (JsonNode type : typesNode) {
            if ("ror_display".equals(type.textValue())) {
              if (valueNode != null) {
                return valueNode.textValue(); // Return immediately if "ror_display" is found
              }
            }
          }
        }
        if (valueNode != null && fallbackName == null) {
          fallbackName = valueNode.textValue(); // Capture fallback name
        }
      }
      return fallbackName; // Return fallback name if no "ror_display" is found
    }
    return null;
  }

  private String getId(JsonNode apiResponseNode) {
    JsonNode idNode = apiResponseNode.get("id");
    if (idNode != null && idNode.isTextual()) {
      return idNode.textValue();
    } else {
      return null;
    }
  }

  private List<String> getRORErrors(JsonNode apiResponseNode) {
    List<String> errors = new ArrayList<>();
    JsonNode errorsNode = apiResponseNode.get("errors");
    if (errorsNode != null && errorsNode.isArray()) {
      for (JsonNode error : errorsNode) {
        if (error != null) {
          errors.add(error.textValue());
        }
      }
    }
    return errors;
  }

  private Map<String, String> getRORSearchNames(JsonNode apiResponseNode) {
    Map<String, String> idToNameMap = new HashMap<>();
    JsonNode itemsNode = apiResponseNode.get("items");
    if (itemsNode != null && itemsNode.isArray()) {
      for (JsonNode item : itemsNode) {
        String rorId = null;
        JsonNode idNode = item.get("id");
        if (idNode != null) {
          rorId = idNode.textValue();
        }

        if (rorId != null) {
          String bestName = getBestRORName(item);
          if (bestName != null) {
            idToNameMap.put(rorId, bestName);
          }
        }
      }
    }
    return idToNameMap;
  }
}
