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
  private static final int DEFAULT_PAGE_SIZE = 100;
  private static String rorApiPrefix;

  public ExternalAuthorityRORResource(CedarConfig cedarConfig) {
    super(cedarConfig);
    rorApiPrefix = cedarConfig.getExternalAuthorities().getRor().getApiPrefix();
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response getRORDetails(@PathParam(PP_ID) String rorId) throws CedarException {
    String url = rorApiPrefix + ROR_API_V2_ORGANIZATIONS_PREFIX + UrlUtil.urlEncode(rorId);

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
    myResponse.put("rawResponse", apiResponseNode);

    if (statusCode == HttpConstants.OK) {
      myResponse.put("id", getId(apiResponseNode));
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
  public Response searchByName(@QueryParam(QP_Q) String orgNameFragment,
                               @QueryParam("page") Integer page,
                               @QueryParam("pageSize") Integer pageSize) throws CedarException {
    if (orgNameFragment != null && !orgNameFragment.isEmpty() && !orgNameFragment.endsWith("*")) {
      orgNameFragment = orgNameFragment + "*";
    }

    if (page == null) page = 0;
    if (pageSize == null) pageSize = DEFAULT_PAGE_SIZE;

    if (page < 0 || pageSize <= 1) {
      return CedarResponse.badRequest()
          .errorMessage("Invalid pagination parameters: page must be >= 0, pageSize must be > 1")
          .build();
    }

    String url = rorApiPrefix + ROR_API_V2_ORGANIZATION_SEARCH_PREFIX + UrlUtil.urlEncode(orgNameFragment);

    HttpResponse proxyResponse = ProxyUtil.proxyGet(url, new HashMap<>());
    int statusCode = proxyResponse.getStatusLine().getStatusCode();
    JsonNode apiResponseNode;
    try {
      String apiResponseString = EntityUtils.toString(proxyResponse.getEntity(), CharEncoding.UTF_8);
      apiResponseNode = JsonMapper.MAPPER.readTree(apiResponseString);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Map<String, Object> myResponse = new HashMap<>();
    if (statusCode == HttpConstants.OK) {
      Map<String, Map<String, String>> rorSearchNames = getRORSearchNames(apiResponseNode);
      List<Map.Entry<String, Map<String, String>>> entries = new ArrayList<>(rorSearchNames.entrySet());
      int fromIndex = page * pageSize;
      int toIndex = Math.min(fromIndex + pageSize, entries.size());

      Map<String, Map<String, String>> paginatedResults = new HashMap<>();
      if (fromIndex < entries.size()) {
        for (Map.Entry<String, Map<String, String>> e : entries.subList(fromIndex, toIndex)) {
          paginatedResults.put(e.getKey(), e.getValue());
        }
      }
      myResponse.put("results", paginatedResults);
      myResponse.put("found", !paginatedResults.isEmpty());
      myResponse.put("page", page);
      myResponse.put("pageSize", pageSize);
    } else {
      myResponse.put("found", false);
      myResponse.put("page", page);
      myResponse.put("pageSize", pageSize);
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

  private Map<String, Map<String, String>> getRORSearchNames(JsonNode apiResponseNode) {
    Map<String, Map<String, String>> idToNameMap = new HashMap<>();
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
            Map<String, String> value = new HashMap<>();
            value.put("name", bestName);
            value.put("details", buildDetails(item));
            idToNameMap.put(rorId, value);
          }
        }
      }
    }
    return idToNameMap;
  }

  private String buildDetails(JsonNode item) {
    StringBuilder sb = new StringBuilder();

    JsonNode typesNode = item.get("types");
    if (typesNode != null && typesNode.isArray() && typesNode.size() > 0) {
      List<String> types = new ArrayList<>();
      for (JsonNode type : typesNode) {
        if (type != null && type.isTextual()) {
          types.add(type.textValue());
        }
      }
      if (!types.isEmpty()) {
        sb.append("Resource is of type ").append(String.join(", ", types));
      }
    }

    JsonNode countryNode = item.get("country");
    if (countryNode != null && countryNode.get("country_name") != null) {
      if (sb.length() > 0) sb.append(", ");
      sb.append("located in ").append(countryNode.get("country_name").textValue());
    }

    JsonNode linksNode = item.get("links");
    if (linksNode != null && linksNode.isArray() && linksNode.size() > 0) {
      JsonNode firstLink = linksNode.get(0);
      if (firstLink != null && firstLink.isTextual()) {
        if (sb.length() > 0) sb.append(", ");
        sb.append("with URL ").append(firstLink.textValue());
      }
    }
    return sb.length() > 0 ? sb.toString() : null;
  }

}
