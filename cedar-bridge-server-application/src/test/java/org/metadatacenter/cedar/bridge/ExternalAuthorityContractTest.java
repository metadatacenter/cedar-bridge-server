package org.metadatacenter.cedar.bridge;

import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.metadatacenter.config.environment.CedarEnvironmentSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The contract every external authority answers, asserted over all seven at once.
 *
 * <p>The seven {@code /ext-auth/…} resources were written by copying one and substituting names,
 * and they had no test of any kind: the surface they share — two routes per authority, the same
 * query parameters, the same pagination rules, the same error — existed only as seven copies that
 * happened to agree. Nothing would have reported it if one had stopped agreeing.
 *
 * <p>Every assertion here is offline. Pagination is validated before the upstream registry is
 * called, so an invalid page reaches the shared rules and stops, which is what makes it possible
 * to prove a route exists, is wired, and enforces the contract without ORCID, DataCite, NCBI,
 * SciCrunch, NIH RePORTER or EPA CompTox being reachable — or being called at all.
 *
 * <p>Authorities are named by path segment, which is what the route actually is: PFAS is served
 * under {@code comp-tox} and NIH Grant under {@code nih-grant}.
 */
public class ExternalAuthorityContractTest {

  /** The seven path segments, which are the bridge's external-authority surface. */
  static final List<String> AUTHORITIES =
      List.of("orcid", "ror", "comp-tox", "pmid", "rrid", "nih-grant", "doi");

  /**
   * The authority that answers from a local registry rather than by proxying.
   *
   * <p>While the registry is cold — which it always is under test, since nothing populates it — it
   * has nothing to say about any request. It used to say so before reading the request, so a
   * malformed request during the loading window was answered 503; on the shared route the
   * pagination rules run first, so that request is now answered 400 like any other malformed one
   * and the 503 is what a well-formed request gets.
   */
  private static final String LOCAL_REGISTRY_AUTHORITY = "comp-tox";

  /**
   * The authorities whose rejection is CEDAR's own error object rather than a bare string.
   *
   * <p>Six built the 400 from a bare string, so a JSON API answered {@code text/plain}; ROR used
   * {@code CedarResponse.badRequest().errorMessage(…)}, the framework's structured error, and its
   * wording had drifted with it — a comma where the others have "and". Neither difference was
   * anyone's decision; they are what seven copies of one method turn into. The structured form
   * wins, and an authority joins this list as it moves onto the shared route.
   */
  private static final List<String> STRUCTURED_ERROR_AUTHORITIES = List.of("ror", LOCAL_REGISTRY_AUTHORITY);

  private static final int BAD_REQUEST = 400;
  private static final int SERVICE_UNAVAILABLE = 503;

  private static final String PAGINATION_ERROR =
      "Invalid pagination parameters: page must be >= 0 and pageSize must be > 1";

  private static final String PAGINATION_ERROR_STRUCTURED =
      "Invalid pagination parameters: page must be >= 0, pageSize must be > 1";

  private static final HttpClient CLIENT = HttpClient.newHttpClient();

  static {
    // Must run before the test support boots the server, which reads the port env vars. Ports are
    // distinct from the dev server and from every other booting test class.
    Map<String, String> environment = new HashMap<>(CedarEnvironmentSource.getAll());
    environment.put("CEDAR_BRIDGE_HTTP_PORT", "19029");
    environment.put("CEDAR_BRIDGE_ADMIN_PORT", "19129");
    environment.put("CEDAR_BRIDGE_STOP_PORT", "19229");
    CedarEnvironmentSource.setOverride(environment);
  }

  private static final DropwizardTestSupport<BridgeServerConfiguration> SERVER =
      new DropwizardTestSupport<>(BridgeServerApplicationTest.class,
          ResourceHelpers.resourceFilePath("test-config.yml"));

  @BeforeAll
  public static void startServer() throws Exception {
    SERVER.before();
  }

  @AfterAll
  public static void stopServer() {
    SERVER.after();
  }

  static Stream<String> authorities() {
    return AUTHORITIES.stream();
  }

  /** Every authority whose rejection is a bare string: the ones not yet on the shared route. */
  static Stream<String> plainTextRejectingAuthorities() {
    return AUTHORITIES.stream().filter(authority -> !STRUCTURED_ERROR_AUTHORITIES.contains(authority));
  }

  static Stream<String> structuredRejectingAuthorities() {
    return STRUCTURED_ERROR_AUTHORITIES.stream();
  }

  /** What a rejected request said, whichever of the two shapes it arrived in. */
  private static void assertRejectedForPagination(String authority, HttpResponse<String> response) {
    assertEquals(BAD_REQUEST, response.statusCode(), authority + " did not reject the request");
    String expected = STRUCTURED_ERROR_AUTHORITIES.contains(authority)
        ? PAGINATION_ERROR_STRUCTURED : PAGINATION_ERROR;
    assertTrue(response.body().contains(expected),
        authority + " worded the pagination error differently: " + response.body());
  }

  private static HttpResponse<String> get(String path) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + SERVER.getLocalPort() + path))
        .GET()
        .build();
    try {
      return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      throw new RuntimeException("probing " + path + " failed", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("probing " + path + " was interrupted", e);
    }
  }

  /**
   * A route that has vanished or changed verb answers 404 or 405, and this is the only assertion
   * that survives a resource being dropped from the application's registration list.
   */
  @ParameterizedTest(name = "{0} serves a search route")
  @MethodSource("authorities")
  public void everyAuthorityServesItsSearchRoute(String authority) {
    int status = get("/ext-auth/" + authority + "/search-by-name?q=x&page=-1").statusCode();

    assertTrue(status != 404 && status != 405,
        "/ext-auth/" + authority + "/search-by-name does not answer: " + status);
  }

  @ParameterizedTest(name = "{0} rejects a negative page")
  @MethodSource("authorities")
  public void everyAuthorityRejectsANegativePage(String authority) {
    assertRejectedForPagination(authority, get("/ext-auth/" + authority + "/search-by-name?q=x&page=-1"));
  }

  /**
   * Deliberately {@code <= 1} rather than {@code < 1}, which is what all seven have always done:
   * a page of one is rejected. Pinned because it is surprising, so a refactor cannot quietly
   * "fix" it into a behaviour change nobody asked for.
   */
  @ParameterizedTest(name = "{0} rejects a page size of one")
  @MethodSource("authorities")
  public void everyAuthorityRejectsAPageSizeOfOne(String authority) {
    assertRejectedForPagination(authority, get("/ext-auth/" + authority + "/search-by-name?q=x&pageSize=1"));
  }

  /**
   * The rejection body, as text from a JSON endpoint.
   *
   * <p>Split from the status assertions so the divergence is a named fact with a test attached to
   * each side, rather than a branch buried in a helper. An authority leaves this test and joins
   * the one below as it moves onto the shared route, and when the list is empty this one goes.
   */
  @ParameterizedTest(name = "{0} rejects with a bare string")
  @MethodSource("plainTextRejectingAuthorities")
  public void anAuthorityNotYetSharedRejectsWithABareString(String authority) {
    assertEquals(PAGINATION_ERROR, get("/ext-auth/" + authority + "/search-by-name?q=x&page=-1").body(),
        authority + " no longer answers a bare string");
  }

  @ParameterizedTest(name = "{0} rejects with a CEDAR error object")
  @MethodSource("structuredRejectingAuthorities")
  public void anAuthorityOnTheSharedRouteRejectsWithCedarsOwnErrorObject(String authority) {
    String body = get("/ext-auth/" + authority + "/search-by-name?q=x&page=-1").body();

    assertTrue(body.contains("\"statusCode\":400"),
        authority + " did not answer a CEDAR error object: " + body);
    assertTrue(body.contains(PAGINATION_ERROR_STRUCTURED),
        authority + " reworded its pagination error: " + body);
  }

  /**
   * The local registry says "come back later", and says when.
   *
   * <p>Asked with a well-formed request, since the shared route validates before any authority is
   * consulted. That ordering is the change: this used to answer 503 even to a malformed request,
   * because the readiness check came first.
   */
  @Test
  public void theLocalRegistryReportsItIsNotReadyAndWhenToRetry() {
    HttpResponse<String> response = get("/ext-auth/" + LOCAL_REGISTRY_AUTHORITY + "/search-by-name?q=x");

    assertEquals(SERVICE_UNAVAILABLE, response.statusCode(),
        LOCAL_REGISTRY_AUTHORITY + " did not report an unloaded registry");
    assertTrue(response.headers().firstValue("Retry-After").isPresent(),
        "a 503 from " + LOCAL_REGISTRY_AUTHORITY + " must say when to come back");
  }

  /**
   * A segment no authority is registered under.
   *
   * <p>Jersey answered this with a bare 404 when no resource declared the path. The shared route
   * matches every segment, so the 404 is now the resource's own and can name what does exist.
   */
  @Test
  public void anUnknownAuthorityIsNamedRatherThanJustRefused() {
    HttpResponse<String> response = get("/ext-auth/not-an-authority/search-by-name?q=x");

    assertEquals(404, response.statusCode(), "an unknown authority was not refused");
    assertTrue(response.body().contains(LOCAL_REGISTRY_AUTHORITY),
        "the refusal does not say which authorities exist: " + response.body());
  }
}
