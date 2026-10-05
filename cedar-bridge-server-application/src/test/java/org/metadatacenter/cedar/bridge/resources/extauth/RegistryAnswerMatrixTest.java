package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * <p>The rule each must keep: a registry that does not answer is a 503; a refusal is answered with the
 * registry's own status; a 200 that cannot be read is a 502; and a refusal of the credentials the
 * deployment holds is a 502, since the caller can do nothing about them. Each authority keeps its own
 * way of saying that a registry does not hold an identifier: four answer 200 with {@code found}
 * false, two relay the 404. Which is right is a separate question, and nothing here settles it.
 */
public class RegistryAnswerMatrixTest {

  private static final HttpServer REGISTRY = startRegistry();
  private static final String REGISTRY_BASE = "http://127.0.0.1:" + REGISTRY.getAddress().getPort();

  /** A registry answer: a status and a body, or no answer at all. */
  private enum Answer {
    OK(200, null), UNREADABLE(200, "<html><body>Maintenance</body></html>"),
    BAD_REQUEST(400, "{\"error\":\"bad query\"}"), UNAUTHORIZED(401, "{\"error\":\"key refused\"}"),
    FORBIDDEN(403, "<html>Forbidden</html>"), NOT_FOUND(404, "{\"error\":\"not found\"}"),
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

  private record Fault(String authority, String registryRequest, Answer answer) {
  }

  private static final AtomicReference<Fault> FAULT = new AtomicReference<>();

  static {
    redirectEnvironment();
  }

  private static void redirectEnvironment() {
    Map<String, String> environment = new HashMap<>(CedarEnvironmentSource.getAll());
    environment.put("CEDAR_BRIDGE_HTTP_PORT", "0");
    environment.put("CEDAR_BRIDGE_ADMIN_PORT", "0");
    environment.put("CEDAR_BRIDGE_STOP_PORT", "0");
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
    } else {
      expected = answer.status;
    }
    assertEquals(expected, response.statusCode(), step + " answered " + response.body());
    assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"),
        "the answer is JSON: " + response.headers().map());
    JsonNode body = JsonMapper.STRICT_MAPPER.readTree(response.body());
    if (step.route().equals("details") && expected == 200) {
      assertEquals(answer == Answer.OK, body.path("found").asBoolean(),
          "only an answer that holds the identifier is a find: " + body);
    }
    if (expected != 200) {
      assertFalse(body.path("found").asBoolean(false), "a failure is not a find: " + body);
    }
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
    String content = answer == Answer.OK ? ANSWERS.getOrDefault(request[0] + " " + request[1], "{}") : answer.body;
    byte[] payload = content.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type",
        content.startsWith("<") ? "text/html" : "application/json");
    exchange.sendResponseHeaders(answer.status, payload.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(payload);
    }
  }
}
