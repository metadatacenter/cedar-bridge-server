package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.util.http.UrlUtil;

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
    this(cedarConfig.getExternalAuthorities().getOrcid().getTokenPrefix(),
        cedarConfig.getExternalAuthorities().getOrcid().getApiPrefix(),
        cedarConfig.getExternalAuthorities().getOrcid().getClientId(),
        cedarConfig.getExternalAuthorities().getOrcid().getClientSecret());
  }

  /** An authority reading other ORCID endpoints, so a test can stand them up locally. */
  OrcidAuthority(String orcidTokenPrefix, String orcidApiPrefix, String clientId, String clientSecret) {
    this.orcidTokenPrefix = orcidTokenPrefix;
    this.orcidApiPrefix = orcidApiPrefix;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public AuthoritySearchAnswer search(String query, int offset, int limit) throws CedarException {
    if (query == null || query.trim().isEmpty()) {
      return AuthoritySearchAnswer.nothing();
    }

    String url = String.format(orcidApiPrefix + ORCID_API_V3_EXPANDED_SEARCH_PREFIX,
        UrlUtil.urlEncode(String.format(EXPANDED_SEARCH_QUERY, query)))
        + "&start=" + offset + "&rows=" + limit;

    RegistryReply reply = RegistryReply.get(url, additionalHeaders());
    if (!reply.ok()) {
      JsonNode refusal = reply.refusal();
      return AuthoritySearchAnswer.failed(reply.status(), refusal == null ? List.of() : errors(refusal));
    }
    JsonNode root = reply.json();
    return AuthoritySearchAnswer.of(searchNames(root), root.path("num-found").asLong(0));
  }

  @Override
  public AuthorityDetailsAnswer details(String id) throws CedarException {
    String extracted = id.contains("/") ? id.substring(id.lastIndexOf('/') + 1) : id;
    String url = orcidApiPrefix + ORCID_V3_PREFIX + UrlUtil.urlEncode(extracted) + ORCID_API_V3_RECORD_SUFFIX;

    RegistryReply reply = RegistryReply.get(url, additionalHeaders());

    Map<String, Object> body = new HashMap<>();
    if (!reply.ok()) {
      // What ORCID said is passed on when it said it in JSON; its status is passed on regardless.
      JsonNode refusal = reply.refusal();
      body.put("rawResponse", refusal);
      body.put("name", null);
      body.put("errors", refusal == null ? List.of() : errors(refusal));
      return AuthorityDetailsAnswer.failed(reply.status(), body);
    }

    JsonNode root = reply.json();
    body.put("rawResponse", root);
    body.put("id", recordId(root));
    body.put("name", bestName(root));
    return AuthorityDetailsAnswer.found(body);
  }

  private Map<String, Map<String, String>> searchNames(JsonNode root) throws CedarProcessingException {
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
  private void ensureOrcidIdPrefixInitialized() throws CedarProcessingException {
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

  /**
   * Learns the prefix from a record ORCID returns. A record that does not show one is an answer this
   * cannot use, so a 502 like any other; it was a 500, as was every failure to ask.
   */
  private void determineOrcidIdPrefix() throws CedarProcessingException {
    String url = orcidApiPrefix + ORCID_API_V3_SIMPLE_SEARCH_PREFIX + "stanford";

    RegistryReply reply = RegistryReply.get(url, additionalHeaders());
    if (!reply.ok()) {
      throw RegistryReply.unreadable(new IOException("ORCID answered " + reply.status()
          + " when asked for the record that shows its identifier prefix"));
    }
    JsonNode orcidIdentifier = reply.json().path("result").path(0).path("orcid-identifier");

    String uri = orcidIdentifier.path("uri").asText();
    String path = orcidIdentifier.path("path").asText();

    if (path.isEmpty() || !uri.endsWith(path)) {
      throw RegistryReply.unreadable(new IOException("ORCID's record shows no identifier prefix"));
    }
    orcidIdPrefix = uri.substring(0, uri.length() - path.length());
  }

  private Map<String, String> additionalHeaders() throws CedarProcessingException {
    Map<String, String> additionalHeaders = new HashMap<>();
    additionalHeaders.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    additionalHeaders.put(HttpHeaders.AUTHORIZATION, HTTP_AUTH_HEADER_BEARER_PREFIX + accessToken());
    return additionalHeaders;
  }

  private String accessToken() throws CedarProcessingException {
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

  /**
   * Fetches the token the deployment's credentials buy. A refusal is a 502: the credentials are the
   * deployment's, so the caller can do nothing about them, and the status ORCID gave would tell it
   * otherwise. It was a 500.
   */
  private void refreshToken() throws CedarProcessingException {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTP_HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);
    headers.put(HTTP_HEADER_ACCEPT, CONTENT_TYPE_APPLICATION_JSON);

    String body = String.format("client_id=%s&client_secret=%s&grant_type=%s&scope=%s",
        URLEncoder.encode(clientId, StandardCharsets.UTF_8),
        URLEncoder.encode(clientSecret, StandardCharsets.UTF_8),
        URLEncoder.encode(ORCID_TOKEN_GRANT_TYPE, StandardCharsets.UTF_8),
        URLEncoder.encode(ORCID_TOKEN_SCOPE, StandardCharsets.UTF_8));

    RegistryReply reply = RegistryReply.post(orcidTokenPrefix + ORCID_TOKEN_SUFFIX, headers, body);
    if (!reply.ok()) {
      throw RegistryReply.unreadable(new IOException("ORCID refused the credentials this server holds, with "
          + reply.status()));
    }
    JsonNode jsonResponse = reply.json();
    JsonNode token = jsonResponse.get("access_token");
    JsonNode expiresIn = jsonResponse.get("expires_in");
    if (token == null || !token.isTextual() || expiresIn == null || !expiresIn.canConvertToLong()) {
      throw RegistryReply.unreadable(new IOException("ORCID's token answer carries no token"));
    }
    accessToken = token.asText();
    expiryTime = System.currentTimeMillis() + (expiresIn.asLong() * 1000);
  }
}
