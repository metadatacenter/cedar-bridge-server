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
 * authority, how it builds the envelope from whatever the authority knows about its total, and how
 * either route answers a refusal, which is in the shared error body rather than the envelope. And
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
    volatile AuthorityDetailsAnswer detailsAnswer = AuthorityDetailsAnswer.notFound(new HashMap<>());
    volatile int lastOffset = -1;
    volatile int lastLimit = -1;

    @Override
    public String pathSegment() {
      return "scripted";
    }

    @Override
    public String registryName() {
      return "The scripted registry";
    }

    @Override
    public AuthoritySearchAnswer search(String query, int offset, int limit) {
      lastOffset = offset;
      lastLimit = limit;
      return answer;
    }

    @Override
    public AuthorityDetailsAnswer details(String id) {
      return detailsAnswer;
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
    SCRIPTED.detailsAnswer = AuthorityDetailsAnswer.notFound(new HashMap<>());
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
  public void thePagingLinksOfAPageNumberRequestCanBeFollowed() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.of(terms(0, 10), 50);

    JsonNode page = get("/ext-auth/scripted/search-by-name?q=x&page=2&pageSize=10");

    for (String rel : List.of("first", "prev", "next", "last")) {
      String link = page.get("paging").get(rel).asText();
      Map<String, String> query = query(link);
      assertFalse(query.containsKey("page") || query.containsKey("pageSize"),
          "the " + rel + " link pages by offset alone, but was " + link);
      assertEquals("x", query.get("q"));
      assertEquals(200, status(pathOf(link)), "the " + rel + " link " + link + " can be followed");
    }
    assertEquals("30", query(page.get("paging").get("next").asText()).get("offset"));
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

  /**
   * A registry's refusal of the search itself keeps its status and is answered in the shared error
   * body. What the route used to answer with is in objects.authorityAnswer, without the paging
   * envelope of a page the registry never gave.
   */
  @Test
  public void aRegistryRefusalOfASearchKeepsItsStatusInTheSharedErrorBody() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.failed(409, List.of("quota exceeded", "try later"));

    JsonNode error = refusal(send("/ext-auth/scripted/search-by-name?q=x"), 409, 409,
        "The scripted registry refused the request with HTTP 409.\nquota exceeded\ntry later");

    assertEquals(JsonMapper.STRICT_MAPPER.readTree(
            "{\"found\":false,\"results\":{},\"errors\":[\"quota exceeded\",\"try later\"]}"),
        error.at("/objects/authorityAnswer"));
  }

  @Test
  public void aSearchRefusalWithoutReasonsCarriesNone() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.failed(422, null);

    JsonNode error = refusal(send("/ext-auth/scripted/search-by-name?q=x"), 422, 422,
        "The scripted registry refused the request with HTTP 422.");

    assertEquals(JsonMapper.STRICT_MAPPER.readTree("{\"found\":false,\"results\":{}}"),
        error.at("/objects/authorityAnswer"));
  }

  /**
   * A registry that does not hold the identifier keeps the 404. The authority's answer, with found
   * false, the identifier asked for and the registry's own answer, is in objects.authorityAnswer.
   */
  @Test
  public void aRegistryRefusalOfALookupCarriesTheAuthoritysAnswer() throws Exception {
    Map<String, Object> said = new HashMap<>();
    said.put("rawResponse", Map.of("errors", List.of("no such record")));
    said.put("name", null);
    said.put("errors", List.of("no such record"));
    SCRIPTED.detailsAnswer = AuthorityDetailsAnswer.failed(404, said);

    JsonNode error = refusal(send("/ext-auth/scripted/R-1"), 404, 404,
        "The scripted registry holds no entry for \"R-1\" (HTTP 404).\nno such record");

    assertEquals(JsonMapper.STRICT_MAPPER.readTree("{\"found\":false,\"requestedId\":\"R-1\","
            + "\"rawResponse\":{\"errors\":[\"no such record\"]},\"name\":null,\"errors\":[\"no such record\"]}"),
        error.at("/objects/authorityAnswer"));
  }

  @Test
  public void aLookupRefusalOtherThanNotFoundSaysTheRegistryRefusedTheRequest() throws Exception {
    SCRIPTED.detailsAnswer = AuthorityDetailsAnswer.failed(410, new HashMap<>());

    JsonNode error = refusal(send("/ext-auth/scripted/R-1"), 410, 410,
        "The scripted registry refused the request with HTTP 410.");

    assertEquals(JsonMapper.STRICT_MAPPER.readTree("{\"found\":false,\"requestedId\":\"R-1\"}"),
        error.at("/objects/authorityAnswer"));
  }

  /**
   * A registry that refuses the deployment's credentials or access, limits its requests or fails has
   * told the caller nothing about the request, so either route answers 502. The body still names the
   * registry's status, carries the authority's answer, and says which of these happened. A registry's
   * 401 in particular never reaches the caller, nor does CEDAR's authentication challenge with it.
   */
  @Test
  public void aRegistryFaultTheCallerCannotRemedyIsABadGateway() throws Exception {
    Map<Integer, String> explanations = new LinkedHashMap<>();
    for (int status : List.of(401, 403)) {
      explanations.put(status, "The scripted registry refused this deployment's access (HTTP " + status
          + "). This is a problem with the deployment rather than one the caller can resolve.");
    }
    explanations.put(429, "The scripted registry is limiting this deployment's requests (HTTP 429).");
    for (int status : List.of(500, 501, 502, 503, 504, 599)) {
      explanations.put(status, "The scripted registry failed with HTTP " + status + ".");
    }
    for (Map.Entry<Integer, String> expected : explanations.entrySet()) {
      int status = expected.getKey();
      SCRIPTED.answer = AuthoritySearchAnswer.failed(status, List.of("said " + status));
      SCRIPTED.detailsAnswer = AuthorityDetailsAnswer.failed(status, new HashMap<>());

      JsonNode search = refusal(send("/ext-auth/scripted/search-by-name?q=x"), 502, status,
          expected.getValue() + "\nsaid " + status);
      assertEquals(JsonMapper.STRICT_MAPPER.readTree(
              "{\"found\":false,\"results\":{},\"errors\":[\"said " + status + "\"]}"),
          search.at("/objects/authorityAnswer"));
      JsonNode lookup = refusal(send("/ext-auth/scripted/R-1"), 502, status, expected.getValue());
      assertEquals(JsonMapper.STRICT_MAPPER.readTree("{\"found\":false,\"requestedId\":\"R-1\"}"),
          lookup.at("/objects/authorityAnswer"));
    }
  }

  /**
   * A status that is neither 200 nor a refusal is no answer this route can use, and the shared error
   * body cannot carry a status below 400, so each route answers 502 and names the registry's status.
   */
  @Test
  public void aStatusThatIsNeitherSuccessNorRefusalIsABadGateway() throws Exception {
    SCRIPTED.answer = AuthoritySearchAnswer.failed(302, null);
    SCRIPTED.detailsAnswer = AuthorityDetailsAnswer.failed(204, new HashMap<>());

    refusal(send("/ext-auth/scripted/search-by-name?q=x"), 502, 302,
        "The scripted registry answered HTTP 302, which this route cannot use.");
    JsonNode lookup = refusal(send("/ext-auth/scripted/R-1"), 502, 204,
        "The scripted registry answered HTTP 204, which this route cannot use.");
    assertEquals("R-1", lookup.at("/objects/authorityAnswer/requestedId").asText());
  }

  /** A segment no authority is registered under is refused in the shared error body, naming the known ones. */
  @Test
  public void anUnknownAuthorityIsRefusedInTheSharedErrorBody() throws Exception {
    for (String path : List.of("/ext-auth/nowhere/search-by-name?q=x", "/ext-auth/nowhere/R-1")) {
      HttpResponse<String> response = send(path);
      JsonNode error = SharedErrorBody.of(response, 404);
      assertTrue(error.get("errorKey").isNull(), error.toString());
      assertEquals("No external authority is served under \"nowhere\". Known: scripted, doi, ror.",
          error.get("message").asText());
      assertTrue(error.get("parameters").isEmpty(), error.toString());
      assertTrue(error.get("objects").isEmpty(), error.toString());
    }
  }

  /**
   * Every paging refusal is the shared error body. The route's own refusals say what was wrong. A
   * parameter that is not a number is refused by the framework before the route runs, with no message.
   */
  @Test
  public void everyPagingRefusalIsASharedErrorBody() throws Exception {
    for (String query : List.of("limit=0", "limit=501", "offset=-1", "pageSize=1", "page=-1", "pageSize=501",
        "page=1&limit=10")) {
      HttpResponse<String> response = send("/ext-auth/scripted/search-by-name?q=x&" + query);
      JsonNode error = SharedErrorBody.of(response, 400);
      assertFalse(error.get("message").asText("").isBlank(), query + " -> " + error);
    }
    for (String query : List.of("page=abc", "pageSize=abc", "limit=abc", "offset=abc")) {
      SharedErrorBody.of(send("/ext-auth/scripted/search-by-name?q=x&" + query), 400);
    }
    assertEquals(-1, SCRIPTED.lastOffset, "the authority was asked anyway");
  }

  @Test
  public void aRequestThatAcceptsNoJsonIsRefusedInTheSharedErrorBody() throws Exception {
    for (String path : List.of("/ext-auth/scripted/search-by-name?q=x", "/ext-auth/scripted/R-1")) {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("http://127.0.0.1:" + SERVER.getLocalPort() + path)).header("Accept", "text/html")
          .GET().build();
      SharedErrorBody.of(CLIENT.send(request, HttpResponse.BodyHandlers.ofString()), 406);
    }
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

  /** A registry's refusal: the status answered, the registry's own, and the message. */
  private static JsonNode refusal(HttpResponse<String> response, int status, int registryStatus, String message)
      throws Exception {
    JsonNode error = SharedErrorBody.of(response, status);
    assertEquals("upstreamServerError", error.get("errorKey").asText(), response.body());
    assertEquals(message, error.get("message").asText());
    assertEquals(registryStatus, error.at("/parameters/upstreamStatusCode").asInt(), response.body());
    assertEquals("The scripted registry", error.at("/parameters/upstreamService").asText(), response.body());
    assertFalse(error.at("/objects/authorityAnswer/found").asBoolean(true), response.body());
    return error;
  }

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
