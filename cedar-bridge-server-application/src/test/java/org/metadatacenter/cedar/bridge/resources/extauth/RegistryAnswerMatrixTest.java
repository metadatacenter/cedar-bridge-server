package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.metadatacenter.cedar.bridge.BridgeServerApplication;
import org.metadatacenter.cedar.bridge.BridgeServerConfiguration;
import org.metadatacenter.cedar.bridge.resources.SubstanceRegistry;
import org.metadatacenter.config.environment.CedarEnvironmentSource;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every external authority that asks a registry, each request it makes, answered in every way a
 * registry answers.
 *
 * <p>The six authorities read their registries' answers each for themselves, and they disagreed
 * about what a failure is. Four turned a registry they could not reach into a 500, where the transport
 * had said 503. RRID and ORCID read the body before the status, so a registry's HTML error page became
 * a 500. Four answered "not found" for an identifier the registry had failed to look up, PubMed
 * answered a search NCBI refused with an empty page, and a 429 or a 504, which CEDAR names no constant
 * for, went out as a 500 from all six. An ORCID that refused this server's credentials was a 500 too.
 *
 * <p>The rule each must keep: a registry that does not answer is a 503; a refusal of the request
 * itself is answered with the registry's own status; a 200 that cannot be read is a 502; and a
 * registry that refuses the deployment's credentials or access (401 or 403), limits its requests
 * (429) or fails (5xx) is a 502, since the caller can do nothing about any of them. No answer is
 * therefore a 401, and none carries CEDAR's authentication challenge. Each authority keeps its own
 * way of saying that a registry does not hold an identifier: four answer 200 with {@code found}
 * false, two answer 404. Which is right is a separate question, and nothing here settles it.
 *
 * <p>Every answer other than a 200 is the shared error body. A refusal names the registry and its
 * status and carries the authority's answer in {@code objects.authorityAnswer}, where {@code found}
 * is false and a lookup echoes the identifier it was asked for.
 */
@Tag("integration")
public class RegistryAnswerMatrixTest {

  private static final HttpServer REGISTRY = startRegistry();
  private static final String REGISTRY_BASE = "http://127.0.0.1:" + REGISTRY.getAddress().getPort();

  /** A registry answer: a status and a body, or no answer at all. */
  private enum Answer {
    OK(200, null), UNREADABLE(200, "<html><body>Maintenance</body></html>"),
    BAD_REQUEST(400, "{\"error\":\"bad query\"}"), UNAUTHORIZED(401, "{\"error\":\"key refused\"}"),
    FORBIDDEN(403, "<html>Forbidden</html>"), NOT_FOUND(404, "{\"error\":\"not found\"}"),
    GONE(410, "{\"error\":\"withdrawn\"}"),
    TOO_MANY_REQUESTS(429, "{\"error\":\"slow down\"}"), SERVER_ERROR(500, "<html>Oops</html>"),
    BAD_GATEWAY(502, "<html>Bad gateway</html>"), UNAVAILABLE(503, "<html>Down</html>"),
    GATEWAY_TIMEOUT(504, "<html>Timed out</html>"), NO_ANSWER(0, null);

    final int status;
    final String body;

    Answer(int status, String body) {
      this.status = status;
      this.body = body;
    }
  }

  /** One request an authority makes: the route that makes it, and how the registry sees it. */
  private record Step(String authority, String route, String registryRequest) {
    @Override
    public String toString() {
      return authority + " " + route + " (" + registryRequest + ")";
    }
  }

  private static final List<Step> STEPS = List.of(
      new Step("doi", "search", "search"), new Step("doi", "details", "details"),
      new Step("nih-grant", "search", "search"), new Step("nih-grant", "details", "details"),
      new Step("pmid", "search", "search"), new Step("pmid", "details", "details"),
      new Step("rrid", "search", "search"), new Step("rrid", "details", "details"),
      new Step("ror", "search", "search"), new Step("ror", "details", "details"),
      new Step("orcid", "search", "search"), new Step("orcid", "details", "details"),
      new Step("orcid", "search", "token"));

  /** The authorities that say "not found" as a 200 with {@code found} false. */
  private static final List<String> FOUND_FALSE_FOR_MISSING = List.of("doi", "nih-grant", "pmid", "rrid");

  private static final Map<String, String> DETAIL_IDS = Map.of(
      "doi", "10.1234%2Fabc", "nih-grant", "R01GM000001", "pmid", "123", "rrid", "AB_1",
      "ror", "05a28rw58", "orcid", "0000-0001-2345-6789");

  /** The registry each authority asks, as a refusal names it. */
  private static final Map<String, String> REGISTRIES = Map.of(
      "doi", "DataCite", "nih-grant", "NIH RePORTER", "pmid", "NCBI E-utilities", "rrid", "SciCrunch",
      "ror", "ROR", "orcid", "ORCID");

  /** The two authorities that keep the registry's own answer, and its reasons, in a refused lookup. */
  private static final List<String> PASS_THE_REGISTRYS_ANSWER_THROUGH = List.of("ror", "orcid");

  private record Fault(String authority, String registryRequest, Answer answer, String body) {
    Fault(String authority, String registryRequest, Answer answer) { this(authority, registryRequest, answer, null); }
  }

  private static final AtomicReference<Fault> FAULT = new AtomicReference<>();

  static Stream<Arguments> verificationCases() {
    Map<String, String> iris = Map.of("doi", "https://doi.org/10.1234/abc",
        "nih-grant", "https://reporter.nih.gov/project-details/1", "pmid", "https://pubmed.ncbi.nlm.nih.gov/123",
        "rrid", "https://identifiers.org/RRID:AB_1", "ror", "https://ror.org/05a28rw58",
        "orcid", "https://orcid.org/0000-0001-2345-6789");
    return iris.entrySet().stream().flatMap(entry -> Stream.of(Answer.OK, Answer.NOT_FOUND, Answer.UNREADABLE,
        Answer.SERVER_ERROR).map(answer -> Arguments.of(entry.getKey(), entry.getValue(), answer)));
  }

  @ParameterizedTest @MethodSource("verificationCases")
  void semanticVerificationUsesAnExactAuthorityLookup(String authority, String iri, Answer answer) throws Exception {
    FAULT.set(new Fault(authority, "details", answer));
    String body = JsonMapper.STRICT_MAPPER.createObjectNode().put("iri", iri).toString();
    HttpResponse<String> response = CLIENT.send(HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + SERVER.getLocalPort() + "/ext-auth/" + authority + "/verify"))
        .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode(), response.body());
    JsonNode result = JsonMapper.STRICT_MAPPER.readTree(response.body());
    String expected = answer == Answer.OK ? "passed"
        : answer == Answer.NOT_FOUND && !"doi".equals(authority) ? "failed" : "unavailable";
    assertEquals(expected, result.path("status").asText(), response.body());
    assertEquals(iri, result.path("iri").asText());
  }

  static Stream<Arguments> verificationEnvelopeCases() {
    return Stream.of(
        Arguments.of("nih-grant", "https://reporter.nih.gov/project-details/1", "{}", "unavailable"),
        Arguments.of("nih-grant", "https://reporter.nih.gov/project-details/1", "{\"results\":{}}", "unavailable"),
        Arguments.of("nih-grant", "https://reporter.nih.gov/project-details/1", "{\"results\":[]}", "failed"),
        Arguments.of("nih-grant", "https://reporter.nih.gov/project-details/1", "{\"results\":[{\"project_id\":\"1\"}]}", "unavailable"),
        Arguments.of("rrid", "https://identifiers.org/RRID:AB_1", "{}", "unavailable"),
        Arguments.of("rrid", "https://identifiers.org/RRID:AB_1", "{\"hits\":{}}", "unavailable"),
        Arguments.of("rrid", "https://identifiers.org/RRID:AB_1", "{\"hits\":{\"hits\":[]}}", "failed"),
        Arguments.of("rrid", "https://identifiers.org/RRID:AB_1", "{\"hits\":{\"hits\":[{}]}}", "unavailable"),
        Arguments.of("rrid", "https://identifiers.org/RRID:AB_1", "{\"hits\":{\"hits\":[{\"_source\":{\"item\":{\"identifier\":\"AB_1\"}}}]}}", "unavailable"),
        Arguments.of("pmid", "https://pubmed.ncbi.nlm.nih.gov/123", "{}", "unavailable"),
        Arguments.of("pmid", "https://pubmed.ncbi.nlm.nih.gov/123", "{\"result\":{}}", "unavailable"),
        Arguments.of("pmid", "https://pubmed.ncbi.nlm.nih.gov/123", "{\"result\":{\"uids\":[]}}", "failed"),
        Arguments.of("pmid", "https://pubmed.ncbi.nlm.nih.gov/123", "{\"result\":{\"uids\":[\"123\"],\"123\":{}}}", "unavailable"),
        Arguments.of("pmid", "https://pubmed.ncbi.nlm.nih.gov/123", "{\"result\":{\"123\":{\"title\":\"\"}}}", "unavailable"),
        Arguments.of("doi", "https://doi.org/10.1234/abc", "{}", "unavailable"),
        Arguments.of("doi", "https://doi.org/10.1234/abc", "{\"data\":{\"attributes\":{\"doi\":\"10.1234/abc\"}}}", "unavailable"));
  }

  @ParameterizedTest @MethodSource("verificationEnvelopeCases")
  void semanticVerificationDistinguishesIncompleteRecordsFromExplicitAbsence(String authority, String iri,
      String registryBody, String expected) throws Exception {
    FAULT.set(new Fault(authority, "details", Answer.OK, registryBody));
    String body = JsonMapper.STRICT_MAPPER.createObjectNode().put("iri", iri).toString();
    HttpResponse<String> response = CLIENT.send(HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + SERVER.getLocalPort() + "/ext-auth/" + authority + "/verify"))
        .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode(), response.body());
    JsonNode result = JsonMapper.STRICT_MAPPER.readTree(response.body());
    assertEquals(expected, result.path("status").asText(), registryBody + " -> " + response.body());
    assertEquals("failed".equals(expected) ? "authorityMembership" : "semanticUnavailable", result.path("code").asText());
    assertEquals(iri, result.path("iri").asText());
    // The legacy details contract stays 200/found:false for those same incomplete envelopes.
    HttpResponse<String> details = CLIENT.send(HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + SERVER.getLocalPort() + "/ext-auth/" + authority + "/" + DETAIL_IDS.get(authority)))
        .GET().build(), HttpResponse.BodyHandlers.ofString());
    assertEquals(200, details.statusCode(), details.body());
    assertFalse(JsonMapper.STRICT_MAPPER.readTree(details.body()).path("found").asBoolean());
  }

  static {
    redirectEnvironment();
  }

  private static void redirectEnvironment() {
    Map<String, String> environment = new HashMap<>(CedarEnvironmentSource.getAll());
    environment.put("CEDAR_BRIDGE_HTTP_PORT", "0");
    environment.put("CEDAR_BRIDGE_ADMIN_PORT", "0");
    environment.put("CEDAR_BRIDGE_STOP_PORT", "0");
    // The server loads its CompTox registry as it starts. Point it at a port nothing listens on, so
    // it never reaches EPA's API, which CI configures.
    environment.put("CEDAR_COMP_TOX_API_PREFIX", "http://127.0.0.1:1/");
    environment.put("CEDAR_ROR_API_PREFIX", REGISTRY_BASE + "/ror/");
    CedarEnvironmentSource.setOverride(environment);
  }

  /** The six proxying authorities, each pointed at the registry stood up here. */
  public static class MatrixBridgeApplication extends BridgeServerApplication {
    @Override
    protected List<ExternalAuthority> createExternalAuthorities(SubstanceRegistry substanceRegistry) {
      return List.of(
          new DoiAuthority(REGISTRY_BASE + "/dois"),
          new NihGrantAuthority(REGISTRY_BASE + "/nih"),
          new PubMedAuthority(REGISTRY_BASE + "/eutils/", "key"),
          new RridAuthority(REGISTRY_BASE + "/rrid/search", REGISTRY_BASE + "/rrid/resolver/", "key"),
          new RorAuthority(cedarConfig),
          new OrcidAuthority(REGISTRY_BASE + "/orcid/", REGISTRY_BASE + "/orcid/api/", "id", "secret"));
    }
  }

  private static final DropwizardTestSupport<BridgeServerConfiguration> SERVER =
      new DropwizardTestSupport<>(MatrixBridgeApplication.class, ResourceHelpers.resourceFilePath("test-config.yml"));

  private static final HttpClient CLIENT = HttpClient.newHttpClient();

  @BeforeAll
  public static void startServer() throws Exception {
    redirectEnvironment();
    SERVER.before();
  }

  @AfterAll
  public static void stopServer() {
    SERVER.after();
    REGISTRY.stop(0);
  }

  @AfterEach
  public void disarm() {
    FAULT.set(null);
  }

  static Stream<Arguments> cases() {
    List<Arguments> cases = new ArrayList<>();
    for (Step step : STEPS) {
      for (Answer answer : Answer.values()) {
        cases.add(Arguments.of(step, answer));
      }
    }
    return cases.stream();
  }

  @ParameterizedTest(name = "{0}, answered {1}")
  @MethodSource("cases")
  public void theRouteAnswersAsTheRegistryDidOrSaysWhyNot(Step step, Answer answer) throws Exception {
    FAULT.set(new Fault(step.authority(), step.registryRequest(), answer));
    String path = "/ext-auth/" + step.authority()
        + (step.route().equals("search") ? "/search-by-name?q=cancer" : "/" + DETAIL_IDS.get(step.authority()));
    HttpResponse<String> response = CLIENT.send(HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + SERVER.getLocalPort() + path)).GET().build(),
        HttpResponse.BodyHandlers.ofString());
    FAULT.set(null);

    int expected;
    if (answer == Answer.OK) {
      expected = 200;
    } else if (answer == Answer.NO_ANSWER) {
      expected = 503;
    } else if (answer == Answer.UNREADABLE || step.registryRequest().equals("token")) {
      expected = 502;
    } else if (answer == Answer.NOT_FOUND && step.route().equals("details")
        && FOUND_FALSE_FOR_MISSING.contains(step.authority())) {
      expected = 200;
    } else if (deploymentOrRegistryFault(answer.status)) {
      expected = 502;
    } else {
      expected = answer.status;
    }
    assertEquals(expected, response.statusCode(), step + " answered " + response.body());
    assertTrue(response.headers().firstValue("WWW-Authenticate").isEmpty(),
        "no answer asks the caller to authenticate: " + response.headers().map());
    assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"),
        "the answer is JSON: " + response.headers().map());
    JsonNode body = JsonMapper.STRICT_MAPPER.readTree(response.body());
    boolean details = step.route().equals("details");
    String requestedId = details ? URLDecoder.decode(DETAIL_IDS.get(step.authority()), StandardCharsets.UTF_8) : null;
    if (details && expected == 200) {
      assertEquals(answer == Answer.OK, body.path("found").asBoolean(),
          "only an answer that holds the identifier is a find: " + body);
      if (answer != Answer.OK) {
        assertEquals(JsonMapper.STRICT_MAPPER.readTree("{\"found\":false,\"requestedId\":\"" + requestedId + "\"}"),
            body, "a registry that does not hold the identifier is a 200 that found nothing");
      }
    }
    if (expected == 200) {
      return;
    }

    JsonNode error = SharedErrorBody.of(response, expected);
    assertFalse(error.get("message").asText("").isBlank(), "the refusal says why: " + body);
    if (answer == Answer.UNREADABLE || answer == Answer.NO_ANSWER || step.registryRequest().equals("token")) {
      assertTrue(error.get("objects").isEmpty(), "no registry refusal to carry: " + body);
      return;
    }

    // The registry refused, and the shared error body says so.
    assertEquals("upstreamServerError", error.get("errorKey").asText(), body.toString());
    assertEquals(answer.status, error.at("/parameters/upstreamStatusCode").asInt(), body.toString());
    String registry = REGISTRIES.get(step.authority());
    assertEquals(registry, error.at("/parameters/upstreamService").asText(), body.toString());
    String explanation = answer.status >= 500 ? registry + " failed with HTTP " + answer.status + "."
        : answer.status == 401 || answer.status == 403 ? registry + " refused this deployment's access (HTTP "
            + answer.status + "). This is a problem with the deployment rather than one the caller can resolve."
        : answer.status == 429 ? registry + " is limiting this deployment's requests (HTTP 429)."
        : details && answer == Answer.NOT_FOUND ? registry + " holds no entry for \"" + requestedId + "\" (HTTP 404)."
        : registry + " refused the request with HTTP " + answer.status + ".";
    // ORCID reads its reason off a refusal that is JSON. ROR reads reasons only from a list, which none
    // of these refusals holds.
    String reason = step.authority().equals("orcid") && answer.body.startsWith("{")
        ? "\n" + JsonMapper.STRICT_MAPPER.readTree(answer.body).path("error").asText() : "";
    assertEquals(explanation + reason, error.get("message").asText());

    JsonNode said = error.at("/objects/authorityAnswer");
    assertTrue(said.isObject(), "the authority's answer is carried: " + body);
    assertFalse(said.path("found").asBoolean(true), "a refusal is not a find: " + body);
    if (!details) {
      // Only ORCID reads reasons off a refused search.
      assertEquals(step.authority().equals("orcid") ? Set.of("errors", "found", "results") : Set.of("found", "results"),
          keys(said), body.toString());
      assertTrue(said.path("results").isObject() && said.path("results").isEmpty(), body.toString());
      return;
    }
    assertEquals(requestedId, said.path("requestedId").asText(), body.toString());
    if (PASS_THE_REGISTRYS_ANSWER_THROUGH.contains(step.authority())) {
      assertEquals(Set.of("errors", "found", "name", "rawResponse", "requestedId"), keys(said), body.toString());
      JsonNode registryAnswer = answer.body.startsWith("{") ? JsonMapper.STRICT_MAPPER.readTree(answer.body)
          : JsonMapper.STRICT_MAPPER.nullNode();
      assertEquals(registryAnswer, said.get("rawResponse"), body.toString());
      assertTrue(said.get("name").isNull(), body.toString());
      assertTrue(said.get("errors").isArray(), body.toString());
    } else {
      assertEquals(Set.of("found", "requestedId"), keys(said), body.toString());
    }
  }

  /**
   * Whether a registry's status concerns the deployment or the registry rather than the request: a
   * refusal of the deployment's credentials or access, a limit on its requests, or a failure.
   */
  private static boolean deploymentOrRegistryFault(int status) {
    return status == 401 || status == 403 || status == 429 || status >= 500;
  }

  private static Set<String> keys(JsonNode node) {
    Set<String> keys = new TreeSet<>();
    node.fieldNames().forEachRemaining(keys::add);
    return keys;
  }

  // The registry

  private static HttpServer startRegistry() {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext("/", RegistryAnswerMatrixTest::answer);
      server.start();
      return server;
    } catch (IOException e) {
      throw new IllegalStateException("Could not bind the stub registry", e);
    }
  }

  /** The authority and the request a registry call is, from its path and body. */
  private static String[] identify(String path, String body) {
    if (path.startsWith("/dois/")) {
      return new String[]{"doi", "details"};
    }
    if (path.startsWith("/dois")) {
      return new String[]{"doi", "search"};
    }
    if (path.startsWith("/nih")) {
      if (body.contains("appl_ids")) {
        JsonNode query;
        try { query = JsonMapper.STRICT_MAPPER.readTree(body); }
        catch (IOException e) { throw new IllegalArgumentException(e); }
        assertEquals(1, query.path("criteria").path("appl_ids").get(0).asInt());
        return new String[]{"nih-grant", "details"};
      }
      return new String[]{"nih-grant", body.contains("project_nums") ? "details" : "search"};
    }
    if (path.startsWith("/eutils/esearch")) {
      return new String[]{"pmid", "search"};
    }
    if (path.startsWith("/eutils/esummary")) {
      return new String[]{"pmid", "details"};
    }
    if (path.startsWith("/rrid/search")) {
      return new String[]{"rrid", "search"};
    }
    if (path.startsWith("/rrid/resolver/")) {
      return new String[]{"rrid", "details"};
    }
    if (path.startsWith("/ror/organizations/")) {
      return new String[]{"ror", "details"};
    }
    if (path.startsWith("/ror/organizations")) {
      return new String[]{"ror", "search"};
    }
    if (path.startsWith("/orcid/oauth/token")) {
      return new String[]{"orcid", "token"};
    }
    if (path.startsWith("/orcid/api/v3.0/expanded-search")) {
      return new String[]{"orcid", "search"};
    }
    if (path.startsWith("/orcid/api/v3.0/search")) {
      return new String[]{"orcid", "prefix"};
    }
    if (path.startsWith("/orcid/api/v3.0/")) {
      return new String[]{"orcid", "details"};
    }
    return new String[]{"unknown", "unknown"};
  }

  /** What each registry answers when nothing is wrong: an empty search, a record for an identifier. */
  private static final Map<String, String> ANSWERS = Map.ofEntries(
      Map.entry("doi search", "{\"data\":[],\"meta\":{\"total\":0}}"),
      Map.entry("doi details", "{\"data\":{\"attributes\":{\"doi\":\"10.1234/abc\",\"titles\":[{\"title\":\"A dataset\"}]}}}"),
      Map.entry("nih-grant search", "{\"results\":[]}"),
      Map.entry("nih-grant details", "{\"results\":[{\"project_id\":\"1\",\"project_title\":\"A grant\"}]}"),
      Map.entry("pmid search", "{\"esearchresult\":{\"count\":\"0\",\"idlist\":[]}}"),
      Map.entry("pmid details", "{\"result\":{\"123\":{\"title\":\"A paper\"}}}"),
      Map.entry("rrid search", "{\"hits\":{\"hits\":[],\"total\":0}}"),
      Map.entry("rrid details", "{\"hits\":{\"hits\":[{\"_source\":{\"item\":{\"identifier\":\"AB_1\",\"name\":\"An antibody\"}}}]}}"),
      Map.entry("ror search", "{\"items\":[],\"number_of_results\":0}"),
      Map.entry("ror details", "{\"id\":\"https://ror.org/05a28rw58\",\"names\":[{\"value\":\"An institute\",\"types\":[\"ror_display\"]}]}"),
      Map.entry("orcid token", "{\"access_token\":\"token\",\"expires_in\":0}"),
      Map.entry("orcid prefix", "{\"result\":[{\"orcid-identifier\":{\"uri\":\"https://orcid.org/0000-0001\",\"path\":\"0000-0001\"}}]}"),
      Map.entry("orcid search", "{\"expanded-result\":[],\"num-found\":0}"),
      Map.entry("orcid details", "{\"orcid-identifier\":{\"uri\":\"https://orcid.org/0000-0001-2345-6789\"},\"person\":{\"name\":{\"given-names\":{\"value\":\"Ada\"},\"family-name\":{\"value\":\"Lovelace\"}}}}"));

  private static void answer(HttpExchange exchange) throws IOException {
    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    String[] request = identify(exchange.getRequestURI().getPath(), body);
    Fault fault = FAULT.get();
    Answer answer = fault != null && fault.authority().equals(request[0]) && fault.registryRequest().equals(request[1])
        ? fault.answer() : Answer.OK;
    if (answer == Answer.NO_ANSWER) {
      // The connection closes before a status line, as a registry that went down would.
      exchange.close();
      return;
    }
    String content = fault != null && fault.authority().equals(request[0]) && fault.registryRequest().equals(request[1]) && fault.body() != null
        ? fault.body() : answer == Answer.OK ? ANSWERS.getOrDefault(request[0] + " " + request[1], "{}") : answer.body;
    byte[] payload = content.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type",
        content.startsWith("<") ? "text/html" : "application/json");
    exchange.sendResponseHeaders(answer.status, payload.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(payload);
    }
  }
}
