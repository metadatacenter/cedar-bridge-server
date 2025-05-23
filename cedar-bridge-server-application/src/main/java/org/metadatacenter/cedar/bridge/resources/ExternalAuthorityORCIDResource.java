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
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.http.UrlUtil;
import org.metadatacenter.util.json.JsonMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.constant.CedarQueryParameters.QP_Q;
import static org.metadatacenter.constant.HttpConstants.*;

@Path("/ext-auth/orcid")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityORCIDResource extends CedarMicroserviceResource {

  private final static String ORCID_V3_PREFIX = "v3.0/";
  private final static String ORCID_API_V3_RECORD_SUFFIX = "/record";
  private final static String ORCID_API_V3_EXPANDED_SEARCH_PREFIX = ORCID_V3_PREFIX + "expanded-search/?q=%s";
  private final static String ORCID_API_V3_SIMPLE_SEARCH_PREFIX = ORCID_V3_PREFIX + "search/?q=";
  private final static String ORCID_TOKEN_SUFFIX = "oauth/token";
  private static final String ORCID_TOKEN_GRANT_TYPE = "client_credentials";
  private static final String ORCID_TOKEN_SCOPE = "/read-public";

  private static String ORCID_TOKEN_PREFIX;
  private static String ORCID_API_PREFIX;
  private static String CLIENT_ID;
  private static String CLIENT_SECRET;

  private static String accessToken;
  private static long expiryTime;
  private static final ReentrantLock lock = new ReentrantLock();

  private static String ORCID_ID_PREFIX;

  public ExternalAuthorityORCIDResource(CedarConfig cedarConfig) {
    super(cedarConfig);
    ORCID_TOKEN_PREFIX = cedarConfig.getExternalAuthorities().getOrcid().getTokenPrefix();
    ORCID_API_PREFIX = cedarConfig.getExternalAuthorities().getOrcid().getApiPrefix();
    CLIENT_ID = cedarConfig.getExternalAuthorities().getOrcid().getClientId();
    CLIENT_SECRET = cedarConfig.getExternalAuthorities().getOrcid().getClientSecret();
    ensureOrcidIdPrefixInitialized();
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response geORCIDDetails(@PathParam(PP_ID) String orcId) throws CedarException {
    String extractedOrcId = orcId.contains("/") ? orcId.substring(orcId.lastIndexOf('/') + 1) : orcId;
    String url = ORCID_API_PREFIX + ORCID_V3_PREFIX + UrlUtil.urlEncode(extractedOrcId) + ORCID_API_V3_RECORD_SUFFIX;

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
      myResponse.put("id", getId(apiResponseNode));
      myResponse.put("name", getBestORCIDName(apiResponseNode));
    } else {
      myResponse.put("name", null);
      myResponse.put("errors", getORCIDErrors(apiResponseNode));
    }

    return CedarResponse.status(CedarResponseStatus.fromStatusCode(statusCode)).entity(myResponse).build();
  }

  @GET
  @Timed
  @Path("/search-by-name")
  public Response searchByName(@QueryParam(QP_Q) String searchTerm) throws CedarException {
    String searchTermEncoded = UrlUtil.urlEncode(searchTerm);

    String solrQuery = String.format(
        "{!edismax qf=\"given-and-family-names^50.0 family-name^10.0 given-names^10.0 credit-name^10.0 other-names^5" +
            ".0 text^1.0\" " +
            "pf=\"given-and-family-names^50.0\" " +
            "bq=\"current-institution-affiliation-name:[* TO *]^100.0 past-institution-affiliation-name:[* TO *]^70\"" +
            " mm=1}%s",
        searchTerm
    );

    String url = String.format(
        ORCID_API_PREFIX + ORCID_API_V3_EXPANDED_SEARCH_PREFIX,
        UrlUtil.urlEncode(solrQuery)
    ) + "&start=0&rows=50";

    System.out.println(url);

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
    //myResponse.put("rawResponse", apiResponseNode);

    Map<String, Map<String, String>> orcidSearchNames = new HashMap<>();
    if (statusCode == HttpConstants.OK) {
      orcidSearchNames = getORCIDSearchNames(apiResponseNode);
      myResponse.put("found", !orcidSearchNames.isEmpty());
    } else {
      myResponse.put("found", false);
      myResponse.put("errors", getORCIDErrors(apiResponseNode));
    }
    myResponse.put("results", orcidSearchNames);

    return CedarResponse.status(CedarResponseStatus.fromStatusCode(statusCode)).entity(myResponse).build();
  }

  private void ensureOrcidIdPrefixInitialized() {
    if (ORCID_ID_PREFIX == null) {
      lock.lock();
      try {
        if (ORCID_ID_PREFIX == null) { // Double-check inside lock
          determineOrcidIdPrefix();
        }
      } finally {
        lock.unlock();
      }
    }
  }

  private static void determineOrcidIdPrefix() {
    String url = ORCID_API_PREFIX + ORCID_API_V3_SIMPLE_SEARCH_PREFIX + "stanford";

    try {
      HttpResponse response = ProxyUtil.proxyGet(url, getAdditionalHeadersMap());
      String responseString = EntityUtils.toString(response.getEntity(), CharEncoding.UTF_8);
      JsonNode jsonResponse = JsonMapper.MAPPER.readTree(responseString);
      JsonNode firstResult = jsonResponse.path("result").path(0);
      JsonNode orcidIdentifier = firstResult.path("orcid-identifier");

      String uri = orcidIdentifier.path("uri").asText();
      String path = orcidIdentifier.path("path").asText();

      if (uri.endsWith(path)) {
        ORCID_ID_PREFIX = uri.substring(0, uri.length() - path.length());
      } else {
        throw new RuntimeException("Could not determine ORCID ID prefix.");
      }
    } catch (IOException | CedarException e) {
      throw new RuntimeException("Error retrieving ORCID ID prefix", e);
    }
  }

  private String getId(JsonNode apiResponseNode) {
    JsonNode idWrapperNode = apiResponseNode.get("orcid-identifier");
    if (idWrapperNode != null && idWrapperNode.isObject()) {
      JsonNode idNode = idWrapperNode.get("uri");
      if (idNode != null && idNode.isTextual()) {
        return idNode.textValue();
      }
    }
    return null;
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

  private Map<String, Map<String, String>> getORCIDSearchNames(JsonNode apiResponseNode) {
    Map<String, Map<String, String>> idToInfoMap = new LinkedHashMap<>(); // Preserve response order

    JsonNode expandedResultNode = apiResponseNode.get("expanded-result");
    if (expandedResultNode != null && expandedResultNode.isArray()) {
      for (JsonNode item : expandedResultNode) {
        String orcidId = null;
        JsonNode orcidIdNode = item.get("orcid-id");
        if (orcidIdNode != null) {
          orcidId = orcidIdNode.textValue();
        }

        if (orcidId != null) {
          orcidId = ORCID_ID_PREFIX + orcidId;
          String name = null;

          // 1. Try "credit-name" first
          JsonNode creditNameNode = item.get("credit-name");
          if (creditNameNode != null && !creditNameNode.asText().trim().isEmpty()) {
            name = creditNameNode.textValue();
          }

          // 2. Fall back to "given-names" + "family-names"
          if (name == null || name.trim().isEmpty()) {
            JsonNode givenNamesNode = item.get("given-names");
            JsonNode familyNamesNode = item.get("family-names");
            String given = givenNamesNode != null ? givenNamesNode.textValue() : null;
            String family = familyNamesNode != null ? familyNamesNode.textValue() : null;
            if (given != null && family != null) {
              name = given + " " + family;
            }
          }

          // 3. Fall back to first value in "other-name" array
          if ((name == null || name.trim().isEmpty()) && item.has("other-name")) {
            JsonNode otherNamesNode = item.get("other-name");
            if (otherNamesNode != null && otherNamesNode.isArray() && !otherNamesNode.isEmpty()) {
              name = otherNamesNode.get(0).textValue();
            }
          }

          if (name != null && !name.trim().isEmpty()) {
            // Collect institution-name entries
            String details = "";
            JsonNode institutionsNode = item.get("institution-name");
            if (institutionsNode != null && institutionsNode.isArray()) {
              List<String> institutions = new ArrayList<>();
              for (JsonNode inst : institutionsNode) {
                if (inst != null && !inst.asText().trim().isEmpty()) {
                  institutions.add(inst.asText());
                }
              }
              details = String.join(", ", institutions);
            }

            // Build result object
            Map<String, String> value = new HashMap<>();
            value.put("name", name);
            value.put("details", details);

            idToInfoMap.put(orcidId, value);
          }
        }
      }
    }

    return idToInfoMap;
  }


  private static Map<String, String> getAdditionalHeadersMap() {

    String bearerToken = getAccessToken();

    Map<String, String> additionalHeaders = new HashMap<>();
    additionalHeaders.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    additionalHeaders.put(HttpHeaders.AUTHORIZATION, HTTP_AUTH_HEADER_BEARER_PREFIX + bearerToken);
    return additionalHeaders;
  }

  public static String getAccessToken() {
    if (accessToken == null || System.currentTimeMillis() > expiryTime) {
      lock.lock();
      try {
        if (accessToken == null || System.currentTimeMillis() > expiryTime) {
          refreshToken();
        }
      } finally {
        lock.unlock();
      }
    }
    return accessToken;
  }

  private static void refreshToken() {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTP_HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);
    headers.put(HTTP_HEADER_ACCEPT, CONTENT_TYPE_APPLICATION_JSON);

    String body = String.format(
        "client_id=%s&client_secret=%s&grant_type=%s&scope=%s",
        URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8),
        URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8),
        URLEncoder.encode(ORCID_TOKEN_GRANT_TYPE, StandardCharsets.UTF_8),
        URLEncoder.encode(ORCID_TOKEN_SCOPE, StandardCharsets.UTF_8)
    );

    String url = ORCID_TOKEN_PREFIX + ORCID_TOKEN_SUFFIX;

    try {
      HttpResponse response = ProxyUtil.proxyPost(url, headers, body);
      int statusCode = response.getStatusLine().getStatusCode();

      if (statusCode != 200) {
        throw new RuntimeException("Failed to retrieve token. HTTP status: " + statusCode);
      }

      String responseString = EntityUtils.toString(response.getEntity());
      JsonNode jsonResponse = JsonMapper.MAPPER.readTree(responseString);

      accessToken = jsonResponse.get("access_token").asText();
      long expiresIn = jsonResponse.get("expires_in").asLong();
      expiryTime = System.currentTimeMillis() + (expiresIn * 1000);

    } catch (IOException | CedarProcessingException e) {
      throw new RuntimeException("Error while fetching access token", e);
    }
  }

  private String getBestORCIDName(JsonNode apiResponseNode) {
    JsonNode personNode = apiResponseNode.get("person");
    if (personNode != null) {
      JsonNode nameNode = personNode.get("name");
      if (nameNode != null) {
        JsonNode givenNamesNode = nameNode.get("given-names");
        JsonNode familyNameNode = nameNode.get("family-name");
        JsonNode creditNameNode = nameNode.get("credit-name");

        if (creditNameNode != null && !creditNameNode.isNull()) {
          return creditNameNode.get("value").asText(); // Prefer credit name if available
        }
        if (givenNamesNode != null && familyNameNode != null) {
          return givenNamesNode.get("value").asText() + " " + familyNameNode.get("value").asText();
        }
        if (givenNamesNode != null) {
          return givenNamesNode.get("value").asText();
        }
        if (familyNameNode != null) {
          return familyNameNode.get("value").asText();
        }
      }
    }
    return null;
  }


}
