package org.metadatacenter.cedar.bridge.resources.extauth;

import org.junit.jupiter.api.Test;
import org.metadatacenter.exception.CedarProcessingException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

}
