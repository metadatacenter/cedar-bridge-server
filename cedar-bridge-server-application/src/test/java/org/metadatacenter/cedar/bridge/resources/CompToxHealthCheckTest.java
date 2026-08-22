package org.metadatacenter.cedar.bridge.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.OptionalLong;

/**
 * The bridge exposes eight resources and CompTox is one of them, so an EPA outage must not report
 * the whole server unfit. It used to: the check returned unhealthy whenever the registry was
 * unloaded, {@code cedar-services.sh health} exits non-zero unless every check passes, and the
 * runbook gates deploys on that — so a third party being down blocked our own deploy verification.
 *
 * <p>What replaces it is not silence. The condition still has to be legible in the health output,
 * and a thirty-second warm-up still has to read differently from an outage running for days.
 */
public class CompToxHealthCheckTest {

  private static final long NOW = 1_000_000_000_000L;

  private static SubstanceRegistry.LoadStatus loaded(int count) {
    return new SubstanceRegistry.LoadStatus(true, count, 0, null, 0L, 0L);
  }

  /** Before any attempt has finished: nothing failed yet, nothing scheduled. */
  private static SubstanceRegistry.LoadStatus firstLoadInFlight() {
    return new SubstanceRegistry.LoadStatus(false, 0, 0, null, 0L, 0L);
  }

  /** A failing loader still waiting out its backoff. */
  private static SubstanceRegistry.LoadStatus waiting(int attempts, String error, long backoffMs, long waitLeftMs) {
    return new SubstanceRegistry.LoadStatus(false, 0, attempts, error, backoffMs, NOW + waitLeftMs);
  }

  /** A failing loader whose next attempt is due, i.e. running now. */
  private static SubstanceRegistry.LoadStatus retrying(int attempts, String error, long backoffMs) {
    return new SubstanceRegistry.LoadStatus(false, 0, attempts, error, backoffMs, NOW - 5_000L);
  }

  @Nested
  @DisplayName("no registry state makes the server unfit")
  class NeverUnhealthy {

    @Test
    public void aLoadedRegistryIsHealthy() {
      Assertions.assertTrue(CompToxHealthCheck.resultFor(loaded(12345), NOW).isHealthy());
    }

    @Test
    public void anUnloadedRegistryIsStillHealthy() {
      Assertions.assertTrue(CompToxHealthCheck.resultFor(firstLoadInFlight(), NOW).isHealthy());
    }

    @Test
    public void aLongRunningUpstreamOutageIsStillHealthy() {
      Assertions.assertTrue(
          CompToxHealthCheck.resultFor(waiting(400, "HTTP 503", 600_000L, 600_000L), NOW).isHealthy(),
          "an EPA outage is a statement about EPA, not about this instance, and the loader retries forever");
    }
  }

  @Nested
  @DisplayName("the condition stays legible in the message")
  class Message {

    @Test
    public void aLoadedRegistryReportsItsSize() {
      Assertions.assertEquals("CompTox registry loaded: 12345 substances",
          CompToxHealthCheck.describe(loaded(12345), NOW));
    }

    @Test
    public void anUnloadedRegistrySaysWhatCallersGetMeanwhile() {
      Assertions.assertTrue(
          CompToxHealthCheck.describe(firstLoadInFlight(), NOW).contains("PFAS lookups return 503"));
    }

    @Test
    public void beforeAnyFailureItReadsAsWarmUpRatherThanFault() {
      String message = CompToxHealthCheck.describe(firstLoadInFlight(), NOW);
      Assertions.assertTrue(message.contains("still loading"));
      Assertions.assertFalse(message.contains("failed attempt"));
    }

    /**
     * The distinction the old message could not make: "not loaded yet" covered both a server up for
     * thirty seconds and one whose upstream had been gone for days.
     */
    @Test
    public void aSustainedOutageReadsDifferentlyFromAWarmUp() {
      String warmUp = CompToxHealthCheck.describe(firstLoadInFlight(), NOW);
      String outage = CompToxHealthCheck.describe(waiting(400, "HTTP 503", 600_000L, 600_000L), NOW);
      Assertions.assertNotEquals(warmUp, outage);
      Assertions.assertTrue(outage.contains("400 consecutive failed attempts"));
    }

    @Test
    public void theLastErrorIsCarried() {
      Assertions.assertTrue(
          CompToxHealthCheck.describe(waiting(3, "HTTP 503", 4_000L, 4_000L), NOW).contains("HTTP 503"));
    }

    @Test
    public void aSingleFailureIsNotPluralised() {
      String message = CompToxHealthCheck.describe(waiting(1, "HTTP 503", 1_000L, 1_000L), NOW);
      Assertions.assertTrue(message.contains("1 failed attempt"));
      Assertions.assertFalse(message.contains("attempts"));
    }

    @Test
    public void theNextAttemptIsAnnouncedWhileWaiting() {
      Assertions.assertTrue(CompToxHealthCheck.describe(waiting(5, "HTTP 503", 120_000L, 120_000L), NOW)
          .contains("Retrying in 120s"));
    }

    @Test
    public void anAttemptInProgressSaysSoRatherThanCountingDown() {
      String message = CompToxHealthCheck.describe(retrying(5, "HTTP 503", 16_000L), NOW);
      Assertions.assertTrue(message.contains("A retry is in progress"));
      Assertions.assertFalse(message.contains("Retrying in"));
    }
  }

  /**
   * The same figure drives the 503's {@code Retry-After}, which was a fixed 30 seconds — fair while
   * the first load is in flight, wrong once backoff has grown to ten minutes.
   */
  @Nested
  @DisplayName("how long a caller is told to wait")
  class RetryAfter {

    @Test
    public void isEmptyWhenNoRetryIsScheduled() {
      Assertions.assertEquals(OptionalLong.empty(), firstLoadInFlight().retryAfterSeconds(NOW));
    }

    @Test
    public void isTheRemainingWaitWhileTheLoaderIsWaiting() {
      Assertions.assertEquals(OptionalLong.of(120L), waiting(5, "x", 120_000L, 120_000L).retryAfterSeconds(NOW));
    }

    @Test
    public void roundsUpSoItNeverUnderstatesTheWait() {
      Assertions.assertEquals(OptionalLong.of(2L), waiting(2, "x", 4_000L, 1_500L).retryAfterSeconds(NOW));
    }

    @Test
    public void carriesTheFullBackoffRatherThanAFixedGuess() {
      Assertions.assertEquals(OptionalLong.of(600L), waiting(9, "x", 600_000L, 600_000L).retryAfterSeconds(NOW));
    }

    /**
     * The case that prompted this: against the live EPA outage, each failing request took longer
     * than its own backoff, so the next attempt was always already due and the remaining wait
     * collapsed to one second. Telling a client to retry in one second, for hours, is worse than the
     * fixed thirty this replaced — so a due attempt reports the interval that follows it instead.
     */
    @Test
    public void isTheBackoffIntervalWhenTheAttemptIsAlreadyDue() {
      Assertions.assertEquals(OptionalLong.of(16L), retrying(5, "x", 16_000L).retryAfterSeconds(NOW));
    }

    @Test
    public void isNeverBelowOneSecond() {
      Assertions.assertEquals(OptionalLong.of(1L), retrying(1, "x", 0L).retryAfterSeconds(NOW));
    }
  }
}
