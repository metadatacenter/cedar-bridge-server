package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cedar.bridge.BridgeServerApplication;
import org.metadatacenter.cedar.bridge.BridgeServerConfiguration;
import org.metadatacenter.cedar.bridge.resources.SubstanceRegistry;
import org.metadatacenter.config.environment.CedarEnvironmentSource;
import org.metadatacenter.util.json.JsonMapper;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The external-authority search answered in CEDAR's body paging envelope.
 *
 * <p>Two things are under test. The route: how it reads either kind of paging, what it hands an
 * authority, and how it builds the envelope from whatever the authority knows about its total. And
 * the two authorities whose paging does the most work: DataCite, which pages by number and has to
 * serve an offset that falls inside a page, and ROR, which can be read only one upstream page deep.
 * Both read a registry stood up here, on a local port, rather than the real one.
 */
public class ExternalAuthorityPagingTest {

  private static final HttpServer REGISTRY = startRegistry();
  private static final String REGISTRY_BASE = "http://127.0.0.1:" + REGISTRY.getAddress().getPort();

  /** DataCite's answers: this many DOIs match whatever is asked. */
  private static final int DATACITE_TOTAL = 23;
  /** ROR's answers: the first upstream page holds this many, out of the larger number it reports. */
  private static volatile int rorOnPage = 20;
  private static volatile int rorReported = 20;

  private static final List<String> dataciteRequests = Collections.synchronizedList(new ArrayList<>());

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

  /** The route's authorities, replaced by ones this test can script or point at its registry. */
  public static class PagingBridgeApplication extends BridgeServerApplication {
    @Override
    protected List<ExternalAuthority> createExternalAuthorities(SubstanceRegistry substanceRegistry) {
      return List.of(SCRIPTED, new DoiAuthority(REGISTRY_BASE + "/dois"), new RorAuthority(cedarConfig));
    }
  }

  /** Answers whatever the test last told it to, and remembers what it was asked for. */
  static final class ScriptedAuthority implements ExternalAuthority {
    volatile AuthoritySearchAnswer answer = AuthoritySearchAnswer.nothing();
    volatile int lastOffset = -1;
    volatile int lastLimit = -1;

    @Override
    public String pathSegment() {
      return "scripted";
    }

    @Override
    public AuthoritySearchAnswer search(String query, int offset, int limit) {
      lastOffset = offset;
      lastLimit = limit;
      return answer;
    }

    @Override
    public AuthorityDetailsAnswer details(String id) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }
  }

  private static final ScriptedAuthority SCRIPTED = new ScriptedAuthority();

  private static final DropwizardTestSupport<BridgeServerConfiguration> SERVER =
      new DropwizardTestSupport<>(PagingBridgeApplication.class, ResourceHelpers.resourceFilePath("test-config.yml"));

  private static final HttpClient CLIENT = HttpClient.newHttpClient();

  @BeforeAll
  public static void startServer() throws Exception {
    // Another class in this JVM may have replaced the environment override; restore ours.
    redirectEnvironment();
    SERVER.before();
  }

  @AfterAll
  public static void stopServer() {
    SERVER.after();
    REGISTRY.stop(0);
  }

  @BeforeEach
  public void reset() {
    SCRIPTED.answer = AuthoritySearchAnswer.nothing();
    SCRIPTED.lastOffset = -1;
    SCRIPTED.lastLimit = -1;
    dataciteRequests.clear();
    rorOnPage = 20;
    rorReported = 20;
  }

  // ---- the route: reading the request ----------------------------------------------------------

  @Test
  public void limitAndOffsetReachTheAuthorityAsGiven() throws Exception {
    get("/ext-auth/scripted/search-by-name?q=x&limit=7&offset=13");

    assertEquals(13, SCRIPTED.lastOffset);
    assertEquals(7, SCRIPTED.lastLimit);
  }

  @Test
  public void withoutPagingTheAuthorityIsAskedForTheFirstHundred() throws Exception {
    get("/ext-auth/scripted/search-by-name?q=x");

    assertEquals(0, SCRIPTED.lastOffset);
    assertEquals(100, SCRIPTED.lastLimit);
  }

  @Test
  public void aPageNumberBecomesTheOffsetItStandsFor() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.of(terms(0, 10), 50);

    JsonNode page = get("/ext-auth/scripted/search-by-name?q=x&page=2&pageSize=10");

    assertEquals(20, SCRIPTED.lastOffset);
    assertEquals(10, SCRIPTED.lastLimit);
    assertEquals(2, page.get("page").asInt(), "a client paging by number reads back its own page");
    assertEquals(10, page.get("pageSize").asInt());
    assertEquals(20, page.get("currentOffset").asLong());
  }

  @Test
  public void outOfRangePagingIsRefused() throws Exception {
    assertEquals(400, status("/ext-auth/scripted/search-by-name?q=x&limit=0"));
    assertEquals(400, status("/ext-auth/scripted/search-by-name?q=x&limit=501"));
    assertEquals(400, status("/ext-auth/scripted/search-by-name?q=x&offset=-1"));
    assertEquals(400, status("/ext-auth/scripted/search-by-name?q=x&pageSize=501"));
    assertEquals(200, status("/ext-auth/scripted/search-by-name?q=x&limit=500"));
    assertEquals(200, status("/ext-auth/scripted/search-by-name?q=x&pageSize=500"));
  }

  @Test
  public void thePageNumberRulesStillReadAsTheyAlwaysDid() throws Exception {
    HttpResponse<String> one = send("/ext-auth/scripted/search-by-name?q=x&pageSize=1");
    HttpResponse<String> negative = send("/ext-auth/scripted/search-by-name?q=x&page=-1");

    assertEquals(400, one.statusCode());
    assertTrue(one.body().contains("pageSize must be > 1"), one.body());
    assertEquals(400, negative.statusCode());
    assertTrue(negative.body().contains("page must be >= 0"), negative.body());
  }

  @Test
  public void bothKindsOfPagingTogetherAreRefusedBeforeAnAuthorityIsAsked() throws Exception {
    HttpResponse<String> response = send("/ext-auth/scripted/search-by-name?q=x&page=1&limit=10");

    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("not both"), response.body());
    assertEquals(-1, SCRIPTED.lastOffset, "the authority was asked anyway");
  }

  // ---- the route: building the envelope --------------------------------------------------------

  @Test
  public void anExactTotalFillsTheEnvelopeAndTheOldFields() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.of(terms(10, 10), 35);

    JsonNode page = get("/ext-auth/scripted/search-by-name?q=smith&limit=10&offset=10");

    assertEquals(35, page.get("totalCount").asLong());
    assertEquals(10, page.get("currentOffset").asLong());
    assertEquals(10, page.get("request").get("limit").asInt());
    assertEquals(10, page.get("request").get("offset").asInt());
    assertFalse(page.has("countCapped"));
    assertTrue(page.get("found").asBoolean());
    assertEquals(1, page.get("page").asInt());
    assertEquals(10, page.get("pageSize").asInt());
    assertEquals(10, page.get("results").size());
    assertFalse(page.has("errors"));
    Map<String, String> next = query(page.get("paging").get("next").asText());
    assertEquals("smith", next.get("q"));
    assertEquals("20", next.get("offset"));
    assertEquals("30", query(page.get("paging").get("last").asText()).get("offset"));
    assertEquals("0", query(page.get("paging").get("prev").asText()).get("offset"));
  }

  @Test
  public void resultsKeepTheOrderTheAuthorityGaveThem() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.of(terms(0, 5), 5);

    JsonNode page = get("/ext-auth/scripted/search-by-name?q=x");

    List<String> keys = new ArrayList<>();
    page.get("results").fieldNames().forEachRemaining(keys::add);
    assertEquals(List.of("https://t/0", "https://t/1", "https://t/2", "https://t/3", "https://t/4"), keys);
  }

  @Test
  public void aCappedTotalHasNoLastLink() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.of(terms(0, 10), 20, true);

    JsonNode page = get("/ext-auth/scripted/search-by-name?q=x&limit=10");

    assertTrue(page.get("countCapped").asBoolean());
    assertEquals(20, page.get("totalCount").asLong());
    assertFalse(page.get("paging").has("last"));
    assertTrue(page.get("paging").has("next"));
  }

  @Test
  public void anUnknownTotalLinksOnwardOnlyFromAFullPage() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.ofUnknownTotal(terms(0, 10));
    JsonNode full = get("/ext-auth/scripted/search-by-name?q=x&limit=10&offset=30");

    SCRIPTED.answer = AuthoritySearchAnswer.ofUnknownTotal(terms(0, 4));
    JsonNode partial = get("/ext-auth/scripted/search-by-name?q=x&limit=10&offset=40");

    assertTrue(full.get("countCapped").asBoolean());
    assertEquals(41, full.get("totalCount").asLong(), "everything so far, and one more");
    assertTrue(full.get("paging").has("next"));
    assertFalse(full.get("paging").has("last"));
    assertTrue(partial.get("countCapped").asBoolean());
    assertEquals(44, partial.get("totalCount").asLong());
    assertFalse(partial.get("paging").has("next"), "a short page is the last one");
  }

  @Test
  public void aRegistryRefusalKeepsItsStatusAndReasonInsideTheEnvelope() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.failed(409, List.of("quota exceeded"));

    HttpResponse<String> response = send("/ext-auth/scripted/search-by-name?q=x");
    JsonNode page = JsonMapper.STRICT_MAPPER.readTree(response.body());

    assertEquals(409, response.statusCode());
    assertFalse(page.get("found").asBoolean());
    assertEquals("quota exceeded", page.get("errors").get(0).asText());
    assertEquals(0, page.get("totalCount").asLong());
    assertEquals(0, page.get("results").size());
  }

  @Test
  public void nothingFoundIsAnEmptyPageNotAnError() throws Exception {
    JsonNode page = get("/ext-auth/scripted/search-by-name?q=x");

    assertFalse(page.get("found").asBoolean());
    assertEquals(0, page.get("totalCount").asLong());
    assertFalse(page.get("paging").has("next"));
  }

  // ---- DataCite: paging by number --------------------------------------------------------------

  @Test
  public void anAlignedOffsetAsksDataciteForOnePage() throws Exception {
    JsonNode page = get("/ext-auth/doi/search-by-name?q=climate&limit=5&offset=10");

    assertEquals(List.of("3:5"), dataciteRequests);
    assertEquals(List.of("10", "11", "12", "13", "14"), dataciteNumbers(page));
    assertEquals(DATACITE_TOTAL, page.get("totalCount").asLong());
    assertFalse(page.has("countCapped"));
  }

  @Test
  public void anOffsetInsideAPageIsServedFromTheTwoPagesItStraddles() throws Exception {
    JsonNode page = get("/ext-auth/doi/search-by-name?q=climate&limit=5&offset=7");

    assertEquals(List.of("2:5", "3:5"), dataciteRequests);
    assertEquals(List.of("7", "8", "9", "10", "11"), dataciteNumbers(page));
  }

  @Test
  public void aStraddleAtTheEndAsksForNoPageBeyondTheTotal() throws Exception {
    JsonNode page = get("/ext-auth/doi/search-by-name?q=climate&limit=5&offset=21");

    assertEquals(List.of("5:5"), dataciteRequests, "page 6 would start past the 23 DataCite holds");
    assertEquals(List.of("21", "22"), dataciteNumbers(page));
    assertFalse(page.get("paging").has("next"));
  }

  @Test
  public void walkingDataciteByNextLinksVisitsEveryDoiOnce() throws Exception {
    List<String> seen = new ArrayList<>();
    String next = "/ext-auth/doi/search-by-name?q=climate&limit=6";
    while (next != null) {
      JsonNode page = get(next);
      seen.addAll(dataciteNumbers(page));
      next = page.get("paging").has("next") ? pathOf(page.get("paging").get("next").asText()) : null;
    }

    List<String> expected = new ArrayList<>();
    for (int i = 0; i < DATACITE_TOTAL; i++) {
      expected.add(String.valueOf(i));
    }
    assertEquals(expected, seen);
  }

  @Test
  public void aPageNumberStillPagesDatacite() throws Exception {
    JsonNode page = get("/ext-auth/doi/search-by-name?q=climate&page=1&pageSize=10");

    assertEquals(List.of("2:10"), dataciteRequests);
    assertEquals(10, page.get("results").size());
    assertEquals("10", dataciteNumbers(page).get(0));
  }

  // ---- ROR: one upstream page deep -------------------------------------------------------------

  @Test
  public void rorSlicesItsOneUpstreamPageAndCountsWhatIsReachable() throws Exception {
    rorOnPage = 20;
    rorReported = 20;

    JsonNode page = get("/ext-auth/ror/search-by-name?q=stanford&limit=8&offset=16");

    assertEquals(4, page.get("results").size());
    assertEquals(20, page.get("totalCount").asLong());
    assertFalse(page.has("countCapped"));
    assertFalse(page.get("paging").has("next"));
  }

  @Test
  public void rorCapsItsCountWhenItReportsMoreThanCanBeReached() throws Exception {
    rorOnPage = 20;
    rorReported = 4_311;

    JsonNode page = get("/ext-auth/ror/search-by-name?q=university&limit=10");

    assertEquals(20, page.get("totalCount").asLong(), "the count is of what paging can reach");
    assertTrue(page.get("countCapped").asBoolean());
    assertFalse(page.get("paging").has("last"));
  }

  // ---- RRID: Elasticsearch's two ways of reporting a total -------------------------------------

  @Test
  public void rridReadsAPlainTotalAsExact() {
    AuthoritySearchAnswer answer = RridAuthority.answer(terms(0, 1), JsonMapper.STRICT_MAPPER.valueToTree(42));

    assertEquals(42L, answer.totalCount());
    assertFalse(answer.countCapped());
  }

  @Test
  public void rridReadsATrackedTotalByItsRelation() {
    ObjectNode exact = JsonMapper.STRICT_MAPPER.createObjectNode().put("value", 17).put("relation", "eq");
    ObjectNode atLeast = JsonMapper.STRICT_MAPPER.createObjectNode().put("value", 10_000).put("relation", "gte");

    assertFalse(RridAuthority.answer(terms(0, 1), exact).countCapped());
    assertEquals(17L, RridAuthority.answer(terms(0, 1), exact).totalCount());
    assertTrue(RridAuthority.answer(terms(0, 1), atLeast).countCapped());
    assertEquals(10_000L, RridAuthority.answer(terms(0, 1), atLeast).totalCount());
  }

  @Test
  public void anUnknownTotalIsNullNotZero() {
    assertNull(AuthoritySearchAnswer.ofUnknownTotal(terms(0, 1)).totalCount());
  }

  // ---- helpers ---------------------------------------------------------------------------------

  private static Map<String, Object> terms(int from, int n) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (int i = from; i < from + n; i++) {
      out.put("https://t/" + (i - from), Map.of("name", "Term " + i, "details", "d"));
    }
    return out;
  }

  private static List<String> dataciteNumbers(JsonNode page) {
    List<String> out = new ArrayList<>();
    page.get("results").fieldNames().forEachRemaining(iri -> out.add(iri.substring(iri.lastIndexOf('.') + 1)));
    return out;
  }

  private static JsonNode get(String pathAndQuery) throws Exception {
    HttpResponse<String> response = send(pathAndQuery);
    assertEquals(200, response.statusCode(), pathAndQuery + " -> " + response.body());
    return JsonMapper.STRICT_MAPPER.readTree(response.body());
  }

  private static int status(String pathAndQuery) throws Exception {
    return send(pathAndQuery).statusCode();
  }

  private static HttpResponse<String> send(String pathAndQuery) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://127.0.0.1:" + SERVER.getLocalPort() + pathAndQuery)).GET().build();
    return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static String pathOf(String link) {
    URI uri = URI.create(link);
    assertEquals(SERVER.getLocalPort(), uri.getPort(), "links point back at the server that served them");
    return uri.getRawPath() + "?" + uri.getRawQuery();
  }

  private static Map<String, String> query(String link) {
    Map<String, String> out = new HashMap<>();
    String raw = URI.create(link).getRawQuery();
    if (raw != null) {
      for (String pair : raw.split("&")) {
        String[] kv = pair.split("=", 2);
        out.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
            kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
      }
    }
    return out;
  }

  // ---- the stand-in registry -------------------------------------------------------------------

  private static HttpServer startRegistry() {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext("/dois", exchange -> {
        Map<String, String> q = query("http://x" + exchange.getRequestURI().toString());
        int number = Integer.parseInt(q.get("page[number]"));
        int size = Integer.parseInt(q.get("page[size]"));
        dataciteRequests.add(number + ":" + size);
        ObjectNode root = JsonMapper.STRICT_MAPPER.createObjectNode();
        ArrayNode data = root.putArray("data");
        for (int i = (number - 1) * size; i < Math.min(number * size, DATACITE_TOTAL); i++) {
          ObjectNode attributes = data.addObject().putObject("attributes");
          attributes.put("doi", "10.5555/doi." + i);
          attributes.putArray("titles").addObject().put("title", "Dataset " + i);
        }
        root.putObject("meta").put("total", DATACITE_TOTAL);
        respond(exchange, root);
      });
      server.createContext("/ror/", exchange -> {
        ObjectNode root = JsonMapper.STRICT_MAPPER.createObjectNode();
        root.put("number_of_results", rorReported);
        ArrayNode items = root.putArray("items");
        for (int i = 0; i < rorOnPage; i++) {
          ObjectNode item = items.addObject();
          item.put("id", "https://ror.org/0" + i);
          ObjectNode name = item.putArray("names").addObject();
          name.put("value", "Organization " + i);
          name.putArray("types").add("ror_display");
        }
        respond(exchange, root);
      });
      server.start();
      return server;
    } catch (Exception e) {
      throw new IllegalStateException("could not start the stand-in registry", e);
    }
  }

  private static void respond(com.sun.net.httpserver.HttpExchange exchange, JsonNode body) throws java.io.IOException {
    byte[] bytes = JsonMapper.STRICT_MAPPER.writeValueAsBytes(body);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }
}
