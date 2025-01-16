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
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.http.UrlUtil;
import org.metadatacenter.util.json.JsonMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.constant.CedarQueryParameters.QP_Q;
import static org.metadatacenter.constant.HttpConstants.HTTP_AUTH_HEADER_BEARER_PREFIX;
import static org.metadatacenter.rest.assertion.GenericAssertions.LoggedIn;

@Path("/ext-auth/orcid")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityORCIDResource extends CedarMicroserviceResource {

  private final static String ORCID_API_V3_RECORD_SUFFIX = "/record";
  private final static String ORCID_API_V3_SEARCH_PREFIX = "expanded-search/?q=";
  private static String ORCID_API_PREFIX;

  public ExternalAuthorityORCIDResource(CedarConfig cedarConfig) {
    super(cedarConfig);
    ORCID_API_PREFIX = cedarConfig.getExternalAuthorities().getOrcid().getApiPrefix();
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response geORCIDDetails(@PathParam(PP_ID) String orcId) throws CedarException {
    CedarRequestContext c = buildRequestContext();
    c.must(c.user()).be(LoggedIn);

    String url = ORCID_API_PREFIX + UrlUtil.urlEncode(orcId) + ORCID_API_V3_RECORD_SUFFIX;

    HttpResponse proxyResponse = ProxyUtil.proxyGet(url, getAdditionalHeadersMap());
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
    myResponse.put("requestedId", orcId);
    myResponse.put("rawResponse", apiResponseNode);


    if (statusCode == HttpConstants.OK) {
      myResponse.put("name", getORCIDNames(apiResponseNode));
    } else {
      myResponse.put("errors", getORCIDErrors(apiResponseNode));
    }

    return CedarResponse.status(CedarResponseStatus.fromStatusCode(statusCode)).entity(myResponse).build();
  }

  @GET
  @Timed
  @Path("/search-by-name")
  public Response searchByName(@QueryParam(QP_Q) String searchTerm) throws CedarException {
    CedarRequestContext c = buildRequestContext();
    c.must(c.user()).be(LoggedIn);

    String url = ORCID_API_PREFIX + ORCID_API_V3_SEARCH_PREFIX + UrlUtil.urlEncode(searchTerm);

    HttpResponse proxyResponse = ProxyUtil.proxyGet(url, getAdditionalHeadersMap());
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
    myResponse.put("rawResponse", apiResponseNode);

    Map<String, String> orcidSearchNames = new HashMap<>();
    if (statusCode == HttpConstants.OK) {
      orcidSearchNames = getORCIDSearchNames(apiResponseNode);
      myResponse.put("found", orcidSearchNames.size() != 0);
    } else {
      myResponse.put("found", false);
      myResponse.put("errors", getORCIDErrors(apiResponseNode));
    }
    myResponse.put("results", orcidSearchNames);

    return CedarResponse.status(CedarResponseStatus.fromStatusCode(statusCode)).entity(myResponse).build();
  }

  private Map<String, Object> getORCIDNames(JsonNode apiResponseNode) {
    Map<String, Object> names = new HashMap<>();
    JsonNode personNode = apiResponseNode.get("person");
    if (personNode != null) {
      JsonNode namesNode = personNode.get("name");
      if (namesNode != null && namesNode.isObject()) {
        for (Iterator<String> it = namesNode.fieldNames(); it.hasNext(); ) {
          String key = it.next();
          JsonNode valueNode = namesNode.get(key);
          if (valueNode != null) {
            names.put(key, valueNode);
          }
        }
      }
    }
    return names;
  }

  private List<String> getORCIDErrors(JsonNode apiResponseNode) {
    List<String> errors = new ArrayList<>();
    JsonNode errorNode = apiResponseNode.get("user-message");
    if (errorNode != null && errorNode.isTextual()) {
      errors.add(errorNode.textValue());
    } else {
      JsonNode errorNode2 = apiResponseNode.get("error");
      if (errorNode2 != null && errorNode2.isTextual()) {
        errors.add(errorNode2.textValue());
      }
    }
    return errors;
  }

  private Map<String, String> getORCIDSearchNames(JsonNode apiResponseNode) {
    Map<String, String> idToNameMap = new HashMap<>();

    JsonNode expandedResultNode = apiResponseNode.get("expanded-result");
    if (expandedResultNode != null && expandedResultNode.isArray()) {
      for (JsonNode item : expandedResultNode) {
        String orcidId = null;
        JsonNode orcidIdNode = item.get("orcid-id");
        if (orcidIdNode != null) {
          orcidId = orcidIdNode.textValue();
        }

        if (orcidId != null) { // Ensure ORCID ID exists
          String name = null;

          // Try to concatenate "given-names" and "family-names"
          JsonNode givenNamesNode = item.get("given-names");
          JsonNode familyNamesNode = item.get("family-names");
          if (givenNamesNode != null && familyNamesNode != null) {
            name = givenNamesNode.textValue() + " " + familyNamesNode.textValue();
          }

          // Fall back to "credit-name" if full name is not available
          if (name == null || name.trim().isEmpty()) {
            JsonNode creditNameNode = item.get("credit-name");
            if (creditNameNode != null) {
              name = creditNameNode.textValue();
            }
          }

          // Fall back to the first value in "other-name" if available
          if ((name == null || name.trim().isEmpty()) && item.has("other-name")) {
            JsonNode otherNamesNode = item.get("other-name");
            if (otherNamesNode != null && otherNamesNode.isArray() && otherNamesNode.size() > 0) {
              name = otherNamesNode.get(0).textValue();
            }
          }

          // Only add to map if we have a name
          if (name != null && !name.trim().isEmpty()) {
            idToNameMap.put(orcidId, name);
          }
        }
      }
    }

    return idToNameMap.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            )
        );
  }

  private static Map<String, String> getAdditionalHeadersMap() {

    String bearerToken = "YOUR-TOKEN-HERE";

    Map<String, String> additionalHeaders = new HashMap<>();
    additionalHeaders.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    additionalHeaders.put(HttpHeaders.AUTHORIZATION, HTTP_AUTH_HEADER_BEARER_PREFIX + bearerToken);
    return additionalHeaders;
  }


}
