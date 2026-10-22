package org.metadatacenter.cedar.bridge.resources.extauth;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarProcessingException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** When a registry stops being asked, and what it takes to be asked again. */
class AuthorityCircuitBreakerTest {

  private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-09-15T09:00:00Z"));
  private final AtomicInteger calls = new AtomicInteger();

  private AuthorityCircuitBreaker breaker() {
    return new AuthorityCircuitBreaker("orcid", Duration.ofSeconds(30), now::get);
  }

  private String answerless() throws CedarProcessingException {
    calls.incrementAndGet();
    throw new org.metadatacenter.exception.CedarDependencyUnavailableException("no answer", new java.net.SocketTimeoutException());
  }

  @Test
  void oneFailureDoesNotStopTheNextCall() throws Exception {
    AuthorityCircuitBreaker breaker = breaker();

    assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));
    assertEquals("ok", breaker.call(() -> "ok"));
    assertFalse(breaker.isOpen(), "a bad second is not a registry being down");
  }

  @Test
  void aRunOfFailuresStopsTheCallsAndReportsWhenToRetry() {
    AuthorityCircuitBreaker breaker = breaker();
    for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING; attempt++) {
      assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));
    }
    int reached = calls.get();
    assertTrue(breaker.isOpen());

    AuthorityNotReadyException refused =
        assertThrows(AuthorityNotReadyException.class, () -> breaker.call(this::answerless));

    assertEquals(reached, calls.get(), "an open breaker must not reach the registry at all");
    assertTrue(refused.getRetryAfterSeconds() > 0 && refused.getRetryAfterSeconds() <= 30);
  }

  @Test
  void aSuccessfulProbeResumesTheCalls() throws Exception {
    AuthorityCircuitBreaker breaker = breaker();
    for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING; attempt++) {
      assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));
    }

    now.set(now.get().plusSeconds(31));
    assertEquals("back", breaker.call(() -> "back"));

    assertFalse(breaker.isOpen());
    assertEquals("still working", breaker.call(() -> "still working"),
        "a closed breaker does not ration the calls after it");
  }

  @Test
  void aFailedProbeStartsTheWaitAgain() {
    AuthorityCircuitBreaker breaker = breaker();
    for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING; attempt++) {
      assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));
    }

    now.set(now.get().plusSeconds(31));
    assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));

    assertTrue(breaker.isOpen(), "the probe found it still down");
    int reached = calls.get();
    assertThrows(AuthorityNotReadyException.class, () -> breaker.call(this::answerless));
    assertEquals(reached, calls.get());
  }

  @Test
  void anAnswerIsNotAFailureWhateverItsStatus() throws Exception {
    AuthorityCircuitBreaker breaker = breaker();

    // A registry answering 500 has answered, and quickly. Each authority reports that as its own
    // status in the body rather than by throwing, so nothing here should ever see it as a failure.
    for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING * 3; attempt++) {
      assertEquals(500, (int) breaker.call(() -> 500));
    }

    assertFalse(breaker.isOpen());
  }

  @Test
  void anAuthorityStillLoadingIsNotARegistryThatStoppedAnswering() {
    AuthorityCircuitBreaker breaker = breaker();

    for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING * 2; attempt++) {
      assertThrows(AuthorityNotReadyException.class,
          () -> breaker.call(() -> {
            throw new AuthorityNotReadyException("loading", 5);
          }));
    }

    assertFalse(breaker.isOpen(), "PFAS loading its registry is not evidence about EPA CompTox");
  }
  @Test
  void responseParsingFailuresDoNotOpenTheBreaker() {
    AuthorityCircuitBreaker breaker = breaker();
    for (int attempt = 0; attempt < 10; attempt++) {
      assertThrows(RuntimeException.class, () -> breaker.call(() -> {
        throw new RuntimeException(new java.io.IOException("invalid response JSON"));
      }));
    }
    assertFalse(breaker.isOpen());
  }

  // ---- failures that happen here ----------------------------------------------------------------

  /** A call that fails before it reaches the registry, because its URL holds a space and cannot be sent. */
  private String unbuildable() throws CedarProcessingException {
    calls.incrementAndGet();
    RegistryReply.get("https://registry.example/a b", new HashMap<>());
    throw new AssertionError("a URL holding a space cannot have been sent");
  }

  @Test
  void aRequestThisServerCannotBuildIsNotARegistryFailure() throws Exception {
    AuthorityCircuitBreaker breaker = breaker();

    for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING * 2; attempt++) {
      CedarProcessingException failure = assertThrows(CedarProcessingException.class, () -> breaker.call(this::unbuildable));
      assertEquals(500, failure.getErrorPack().getStatusCode(), "the failure is this server's");
    }

    assertFalse(breaker.isOpen(), "a request that was never sent is no evidence that the registry is down");
    assertEquals("ok", breaker.call(() -> "ok"));
  }

  /**
   * Counted as a success, a request this server could not build reset a run of answerless calls, so a
   * registry that had stopped answering went on being asked.
   */
  @Test
  void aRequestThisServerCannotBuildDoesNotResetTheCount() {
    AuthorityCircuitBreaker breaker = breaker();
    for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING - 1; attempt++) {
      assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));
    }

    assertThrows(CedarProcessingException.class, () -> breaker.call(this::unbuildable));
    assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));

    assertTrue(breaker.isOpen(), "the run of answerless calls goes on counting");
  }

  /**
   * A malformed request let through a half-open breaker as its probe fails here, and learns nothing
   * about the registry. It neither starts the wait again nor closes the breaker, so the next request
   * probes in its place, and one answerless call opens the breaker again where a closed one would take
   * five. Counted as a success, it closed the breaker on a registry nobody had asked.
   */
  @Test
  void aMalformedProbeNeitherClosesNorOpensTheBreaker() {
    AuthorityCircuitBreaker breaker = breaker();
    for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING; attempt++) {
      assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));
    }
    now.set(now.get().plusSeconds(31));

    assertThrows(CedarProcessingException.class, () -> breaker.call(this::unbuildable));
    assertFalse(breaker.isOpen(), "a malformed probe does not start the wait again");

    int reached = calls.get();
    assertThrows(org.metadatacenter.exception.CedarDependencyUnavailableException.class,
        () -> breaker.call(this::answerless));
    assertEquals(reached + 1, calls.get(), "the next request is let through to probe");
    assertTrue(breaker.isOpen(), "a malformed probe does not close the breaker");
  }

  /** A registry that answers with something that cannot be read has answered, and is not down. */
  @Test
  void anAnswerThatCannotBeReadIsStillAnAnswer() {
    AuthorityCircuitBreaker breaker = breaker();
    for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING - 1; attempt++) {
      assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));
    }

    assertThrows(CedarProcessingException.class, () -> breaker.call(() -> {
      throw RegistryReply.unreadable(new java.io.IOException("not JSON"));
    }));
    assertThrows(CedarProcessingException.class, () -> breaker.call(this::answerless));

    assertFalse(breaker.isOpen(), "the answer ended the run of answerless calls");
  }

  // ---- answers given here, without asking -------------------------------------------------------

  /** Nothing listens on port 1 here, so every request to a registry there goes unanswered. */
  private static final String NOWHERE = "http://127.0.0.1:1/";

  /** One request to the route. */
  private interface RouteCall {
    Response call() throws Exception;
  }

  /**
   * One authority's requests: one its registry is asked, and those it answers here without asking,
   * each of which finds nothing.
   */
  private record Requests(String authority, RouteCall asked, List<RouteCall> answeredHere) {
    @Override
    public String toString() {
      return authority;
    }
  }

  /** The route over PubMed, NIH RePORTER and ORCID, none of whose registries answers. */
  private ExternalAuthorityResource route() {
    UriInfo requestUri = mock(UriInfo.class);
    when(requestUri.getRequestUriBuilder()).thenAnswer(call -> UriBuilder.fromUri("http://localhost/ext-auth"));
    return new ExternalAuthorityResource(mock(CedarConfig.class, RETURNS_DEEP_STUBS),
        List.of(new PubMedAuthority(NOWHERE + "eutils/", "key"), new NihGrantAuthority(NOWHERE + "nih"),
            new OrcidAuthority(NOWHERE + "orcid/", NOWHERE + "orcid/api/", "id", "secret")), now::get) {
      {
        // A search page links to the pages beside it, which it builds from the request.
        uriInfo = requestUri;
      }
    };
  }

  /**
   * PubMed's answer for an identifier that is not a PubMed ID, NIH's for a blank identifier, and the
   * three authorities' answers to a blank search.
   */
  private static List<Requests> requests(ExternalAuthorityResource route) {
    return List.of(
        new Requests("pmid", () -> route.details("pmid", "123"), List.of(
            () -> route.details("pmid", "10.1234/abc"), () -> route.details("pmid", "PMID:12a34"),
            () -> search(route, "pmid", " "), () -> search(route, "pmid", null))),
        new Requests("nih-grant", () -> route.details("nih-grant", "R01GM000001"), List.of(
            () -> route.details("nih-grant", " "),
            () -> search(route, "nih-grant", ""), () -> search(route, "nih-grant", null))),
        new Requests("orcid", () -> route.details("orcid", "0000-0001-2345-6789"), List.of(
            () -> search(route, "orcid", " "), () -> search(route, "orcid", null))));
  }

  private static Response search(ExternalAuthorityResource route, String authority, String query) throws Exception {
    return route.searchByName(authority, query, Optional.empty(), Optional.empty(), null, null);
  }

  /** Each request answered here, which finds nothing. */
  private static void answeredHere(Requests requests) throws Exception {
    for (RouteCall call : requests.answeredHere()) {
      Response response = call.call();
      assertEquals(200, response.getStatus(), requests.authority() + " answers without its registry");
      Object found = response.getEntity() instanceof AuthoritySearchPage page ? page.isFound()
          : ((Map<?, ?>) response.getEntity()).get("found");
      assertEquals(false, found, requests.authority());
    }
  }

  /** A request whose registry did not answer, which the breaker counts. */
  private static void unanswered(Requests requests) {
    CedarException failure = assertThrows(CedarException.class, () -> requests.asked().call(), requests.authority());
    assertEquals(503, failure.getErrorPack().getStatusCode(), requests.authority());
  }

  /** A request an open breaker refused, without asking the registry, with when to ask again. */
  private static void refused(Requests requests) throws Exception {
    Response response;
    try {
      response = requests.asked().call();
    } catch (CedarException asked) {
      throw new AssertionError(requests.authority() + "'s registry was asked: " + asked.getMessage(), asked);
    }
    assertEquals(503, response.getStatus(), requests.authority());
    assertTrue(response.getHeaderString(HttpHeaders.RETRY_AFTER) != null, requests.authority());
  }

  /**
   * An answer given here asks no registry, so it is no evidence that the registry answers. Counted as
   * one, an identifier that is not a PubMed ID, or a blank search, reset a run of answerless calls, so
   * a registry that had stopped answering went on being asked. The answer is also given while the
   * breaker is open, since giving it needs nothing of the registry.
   */
  @Test
  void anAnswerGivenHereDoesNotResetTheCount() throws Exception {
    for (Requests requests : requests(route())) {
      for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING - 1; attempt++) {
        unanswered(requests);
      }
      answeredHere(requests);
      unanswered(requests);

      refused(requests);
      answeredHere(requests);
    }
  }

  /**
   * An answer given here is not the probe of a half-open breaker. Taken for one, it closed the breaker
   * on a registry nobody had asked, and the next five requests each waited out the registry again.
   */
  @Test
  void anAnswerGivenHereNeitherClosesNorProbesAHalfOpenBreaker() throws Exception {
    List<Requests> all = requests(route());
    for (Requests requests : all) {
      for (int attempt = 0; attempt < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING; attempt++) {
        unanswered(requests);
      }
    }
    now.set(now.get().plusSeconds(31));

    for (Requests requests : all) {
      answeredHere(requests);
      unanswered(requests);
      refused(requests);
    }
  }

}
