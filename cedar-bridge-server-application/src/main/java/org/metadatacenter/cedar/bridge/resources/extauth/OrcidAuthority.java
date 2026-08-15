package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.codec.CharEncoding;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.http.UrlUtil;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.metadatacenter.constant.HttpConstants.CONTENT_TYPE_APPLICATION_JSON;
import static org.metadatacenter.constant.HttpConstants.CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED;
import static org.metadatacenter.constant.HttpConstants.HTTP_AUTH_HEADER_BEARER_PREFIX;
import static org.metadatacenter.constant.HttpConstants.HTTP_HEADER_ACCEPT;
import static org.metadatacenter.constant.HttpConstants.HTTP_HEADER_CONTENT_TYPE;

/**
 * Researchers, from ORCID.
 *
 * <p>The only authority CEDAR authenticates to. It holds a client-credentials token, refreshes it
 * when it expires, and the two calls below carry it. It is also one of the two whose details
 * answer carries the registry's whole record, which the field that shows a researcher panel reads.
 */
public class OrcidAuthority implements ExternalAuthority {

  static final String PATH_SEGMENT = "orcid";

  private static final String ORCID_V3_PREFIX = "v3.0/";
  private static final String ORCID_API_V3_RECORD_SUFFIX = "/record";
  private static final String ORCID_API_V3_EXPANDED_SEARCH_PREFIX = ORCID_V3_PREFIX + "expanded-search/?q=%s";
  private static final String ORCID_API_V3_SIMPLE_SEARCH_PREFIX = ORCID_V3_PREFIX + "search/?q=";
  private static final String ORCID_TOKEN_SUFFIX = "oauth/token";
  private static final String ORCID_TOKEN_GRANT_TYPE = "client_credentials";
  private static final String ORCID_TOKEN_SCOPE = "/read-public";

  /**
   * How a name is weighed against what was typed.
   *
   * <p>An exact full name outranks a family name, which outranks a credit name, and an affiliation
   * — current far above past — lifts a researcher above one with none. Transcribed verbatim: the
   * weights are ORCID's own tuning, and this is a refactor.
   */
  private static final String EXPANDED_SEARCH_QUERY =
      "{!edismax qf=\"given-and-family-names^50.0 family-name^10.0 given-names^10.0 credit-name^10.0 "
          + "other-names^5.0 text^1.0\" pf=\"given-and-family-names^50.0\" "
          + "bq=\"current-institution-affiliation-name:[* TO *]^100.0 past-institution-affiliation-name:[* TO *]^70\" "
          + "mm=1}%s";

  private final String orcidTokenPrefix;
  private final String orcidApiPrefix;
  private final String clientId;
  private final String clientSecret;

  private final ReentrantLock lock = new ReentrantLock();
  private String accessToken;
  private long expiryTime;
  private String orcidIdPrefix;

  public OrcidAuthority(CedarConfig cedarConfig) {
    this.orcidTokenPrefix = cedarConfig.getExternalAuthorities().getOrcid().getTokenPrefix();
    this.orcidApiPrefix = cedarConfig.getExternalAuthorities().getOrcid().getApiPrefix();
    this.clientId = cedarConfig.getExternalAuthorities().getOrcid().getClientId();
    this.clientSecret = cedarConfig.getExternalAuthorities().getOrcid().getClientSecret();
  }

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public AuthoritySearchAnswer search(String query, int page, int pageSize) throws CedarException {
    if (query == null || query.trim().isEmpty()) {
      return AuthoritySearchAnswer.nothing();
    }

    String url = String.format(orcidApiPrefix + ORCID_API_V3_EXPANDED_SEARCH_PREFIX,
        UrlUtil.urlEncode(String.format(EXPANDED_SEARCH_QUERY, query)))
        + "&start=" + (page * pageSize) + "&rows=" + pageSize;

    ClassicHttpResponse proxyResponse = ProxyUtil.proxyGet(url, additionalHeaders());
    int statusCode = proxyResponse.getCode();
    JsonNode root = read(proxyResponse);

    if (statusCode != HttpConstants.OK) {
      return AuthoritySearchAnswer.failed(statusCode, errors(root));
    }
    return AuthoritySearchAnswer.of(searchNames(root));
  }

  @Override
  public AuthorityDetailsAnswer details(String id) throws CedarException {
    String extracted = id.contains("/") ? id.substring(id.lastIndexOf('/') + 1) : id;
    String url = orcidApiPrefix + ORCID_V3_PREFIX + UrlUtil.urlEncode(extracted) + ORCID_API_V3_RECORD_SUFFIX;

    ClassicHttpResponse proxyResponse = ProxyUtil.proxyGet(url, additionalHeaders());
    int statusCode = proxyResponse.getCode();
    JsonNode root = read(proxyResponse);

    Map<String, Object> body = new HashMap<>();
    body.put("rawResponse", root);

    if (statusCode != HttpConstants.OK) {
      body.put("name", null);
      body.put("errors", errors(root));
      return AuthorityDetailsAnswer.failed(statusCode, body);
    }

    body.put("id", recordId(root));
    body.put("name", bestName(root));
    return AuthorityDetailsAnswer.found(body);
  }

  private static JsonNode read(ClassicHttpResponse proxyResponse) {
    try {
      return JsonMapper.MAPPER.readTree(EntityUtils.toString(proxyResponse.getEntity(), CharEncoding.UTF_8));
    } catch (IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, Map<String, String>> searchNames(JsonNode root) {
    // The prefix is discovered from ORCID itself, so it is resolved on the first search rather
    // than when this is constructed: asking for it at construction time makes the whole server's
    // startup depend on ORCID being reachable and on the credentials being valid.
    ensureOrcidIdPrefixInitialized();

    Map<String, Map<String, String>> idToInfo = new LinkedHashMap<>(); // Preserve response order

    JsonNode expandedResultNode = root.get("expanded-result");
    if (expandedResultNode == null || !expandedResultNode.isArray()) {
      return idToInfo;
    }

    for (JsonNode item : expandedResultNode) {
      JsonNode orcidIdNode = item.get("orcid-id");
      String orcidId = (orcidIdNode == null) ? null : orcidIdNode.textValue();
      if (orcidId == null) {
        continue;
      }

      String name = searchResultName(item);
      if (name == null || name.trim().isEmpty()) {
        continue;
      }

      Map<String, String> term = new HashMap<>();
      term.put("name", name);
      term.put("details", institutions(item));
      idToInfo.put(orcidIdPrefix + orcidId, term);
    }

    return idToInfo;
  }

  /**
   * What to call a researcher a search returned.
   *
   * <p>Their credit name if they have chosen one, then given and family names together, then the
   * first of any other names they list — a record without a credit name is still named rather than
   * dropped.
   */
  private static String searchResultName(JsonNode item) {
    JsonNode creditNameNode = item.get("credit-name");
    if (creditNameNode != null && !creditNameNode.asText().trim().isEmpty()) {
      return creditNameNode.textValue();
    }

    JsonNode givenNamesNode = item.get("given-names");
    JsonNode familyNamesNode = item.get("family-names");
    String given = (givenNamesNode == null) ? null : givenNamesNode.textValue();
    String family = (familyNamesNode == null) ? null : familyNamesNode.textValue();
    if (given != null && family != null) {
      return given + " " + family;
    }

    JsonNode otherNamesNode = item.get("other-name");
    if (otherNamesNode != null && otherNamesNode.isArray() && !otherNamesNode.isEmpty()) {
      return otherNamesNode.get(0).textValue();
    }
    return null;
  }

  private static String institutions(JsonNode item) {
    JsonNode institutionsNode = item.get("institution-name");
    if (institutionsNode == null || !institutionsNode.isArray()) {
      return "";
    }
    List<String> institutions = new ArrayList<>();
    for (JsonNode institution : institutionsNode) {
      if (institution != null && !institution.asText().trim().isEmpty()) {
        institutions.add(institution.asText());
      }
    }
    return String.join(", ", institutions);
  }

  /** The same question as {@link #searchResultName}, of a full record, whose shape differs. */
  private static String bestName(JsonNode root) {
    JsonNode personNode = root.get("person");
    if (personNode == null) {
      return null;
    }
    JsonNode nameNode = personNode.get("name");
    if (nameNode == null) {
      return null;
    }

    JsonNode givenNamesNode = nameNode.get("given-names");
    JsonNode familyNameNode = nameNode.get("family-name");
    JsonNode creditNameNode = nameNode.get("credit-name");

    if (creditNameNode != null && !creditNameNode.isNull()) {
      return creditNameNode.get("value").asText();
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
    return null;
  }

  private static String recordId(JsonNode root) {
    JsonNode idWrapperNode = root.get("orcid-identifier");
    if (idWrapperNode != null && idWrapperNode.isObject()) {
      JsonNode idNode = idWrapperNode.get("uri");
      if (idNode != null && idNode.isTextual()) {
        return idNode.textValue();
      }
    }
    return null;
  }

  private static List<String> errors(JsonNode root) {
    List<String> errors = new ArrayList<>();
    JsonNode userMessage = root.get("user-message");
    if (userMessage != null && userMessage.isTextual()) {
      errors.add(userMessage.textValue());
      return errors;
    }
    JsonNode error = root.get("error");
    if (error != null && error.isTextual()) {
      errors.add(error.textValue());
    }
    return errors;
  }

  /**
   * The prefix ORCID puts before an identifier to make it an IRI.
   *
   * <p>Read off a record ORCID itself returns rather than hard-coded, because it differs between
   * the sandbox and production.
   */
  private void ensureOrcidIdPrefixInitialized() {
    if (orcidIdPrefix != null) {
      return;
    }
    lock.lock();
    try {
      if (orcidIdPrefix != null) { // Double-check inside lock
        return;
      }
      determineOrcidIdPrefix();
    } finally {
      lock.unlock();
    }
  }

  private void determineOrcidIdPrefix() {
    String url = orcidApiPrefix + ORCID_API_V3_SIMPLE_SEARCH_PREFIX + "stanford";

    try {
      ClassicHttpResponse response = ProxyUtil.proxyGet(url, additionalHeaders());
      JsonNode jsonResponse =
          JsonMapper.MAPPER.readTree(EntityUtils.toString(response.getEntity(), CharEncoding.UTF_8));
      JsonNode orcidIdentifier = jsonResponse.path("result").path(0).path("orcid-identifier");

      String uri = orcidIdentifier.path("uri").asText();
      String path = orcidIdentifier.path("path").asText();

      if (!uri.endsWith(path)) {
        throw new RuntimeException("Could not determine ORCID ID prefix.");
      }
      orcidIdPrefix = uri.substring(0, uri.length() - path.length());
    } catch (IOException | ParseException | CedarException e) {
      throw new RuntimeException("Error retrieving ORCID ID prefix", e);
    }
  }

  private Map<String, String> additionalHeaders() {
    Map<String, String> additionalHeaders = new HashMap<>();
    additionalHeaders.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    additionalHeaders.put(HttpHeaders.AUTHORIZATION, HTTP_AUTH_HEADER_BEARER_PREFIX + accessToken());
    return additionalHeaders;
  }

  private String accessToken() {
    if (accessToken != null && System.currentTimeMillis() <= expiryTime) {
      return accessToken;
    }
    lock.lock();
    try {
      if (accessToken == null || System.currentTimeMillis() > expiryTime) {
        refreshToken();
      }
      return accessToken;
    } finally {
      lock.unlock();
    }
  }

  private void refreshToken() {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTP_HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);
    headers.put(HTTP_HEADER_ACCEPT, CONTENT_TYPE_APPLICATION_JSON);

    String body = String.format("client_id=%s&client_secret=%s&grant_type=%s&scope=%s",
        URLEncoder.encode(clientId, StandardCharsets.UTF_8),
        URLEncoder.encode(clientSecret, StandardCharsets.UTF_8),
        URLEncoder.encode(ORCID_TOKEN_GRANT_TYPE, StandardCharsets.UTF_8),
        URLEncoder.encode(ORCID_TOKEN_SCOPE, StandardCharsets.UTF_8));

    try {
      ClassicHttpResponse response = ProxyUtil.proxyPost(orcidTokenPrefix + ORCID_TOKEN_SUFFIX, headers, body);
      if (response.getCode() != HttpConstants.OK) {
        throw new RuntimeException("Failed to retrieve token. HTTP status: " + response.getCode());
      }

      JsonNode jsonResponse = JsonMapper.MAPPER.readTree(EntityUtils.toString(response.getEntity()));
      accessToken = jsonResponse.get("access_token").asText();
      expiryTime = System.currentTimeMillis() + (jsonResponse.get("expires_in").asLong() * 1000);
    } catch (IOException | ParseException | CedarProcessingException e) {
      throw new RuntimeException("Error while fetching access token", e);
    }
  }
}
