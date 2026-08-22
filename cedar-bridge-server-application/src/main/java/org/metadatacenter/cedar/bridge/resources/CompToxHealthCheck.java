package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.health.HealthCheck;

/**
 * Reports the CompTox registry's condition without failing the server for it.
 *
 * <p>This check used to return unhealthy whenever the registry was unloaded, which made an EPA
 * outage indistinguishable from a broken bridge. Three things are wrong with that here:
 *
 * <ul>
 *   <li>CompTox is one of eight resources this server exposes. DataCite, DOI, NIH Grant, ORCID,
 *       PubMed, ROR and RRID are unaffected by it, so failing the whole server misreports seven
 *       working capabilities as broken.</li>
 *   <li>The degradation is not silent. {@link ExternalAuthorityCompToxResource} already answers
 *       every PFAS request with a 503 and a {@code Retry-After} while the registry is unloaded, so
 *       no caller is misled and there is nothing for a health check to guard against.</li>
 *   <li>{@link SubstanceRegistryLoader} retries forever. The failure condition therefore means "a
 *       third party is down and we will keep trying", which is a statement about EPA rather than
 *       about this instance, and no action taken here can clear it.</li>
 * </ul>
 *
 * <p>The cost was concrete: {@code cedar-services.sh health} exits non-zero unless every check
 * passes, and the runbook gates deploys on it, so an EPA outage blocked our own deploy
 * verification. It also forced the runbook to document "bridge reads UNHEALTHY at first, this is
 * normal" — an instruction to ignore red on this service, which is how a real bridge fault would be
 * missed.
 *
 * <p>Contrast {@code TerminologyServerHealthCheck}, which is right to report unhealthy on the same
 * kind of trigger: the ontology catalogue is that server's entire job, so nothing useful remains
 * without it, and its degraded mode was silently serving a partial catalogue. Neither holds here.
 *
 * <p>So the condition is reported in the message, where monitoring and operators can still see it,
 * and the result stays healthy. If this should ever page someone, that belongs on a metric or a
 * separate non-gating endpoint, not on the binary that gates deploys.
 */
public class CompToxHealthCheck extends HealthCheck {
  private final SubstanceRegistry registry;

  public CompToxHealthCheck(SubstanceRegistry registry) {
    this.registry = registry;
  }

  @Override
  protected Result check() {
    return resultFor(registry.getLoadStatus(), System.currentTimeMillis());
  }

  /**
   * The result for a given status. Separated from {@link #check()} so the rule that matters — that
   * no registry state produces an unhealthy result — can be asserted without a clock, a
   * configuration or a live registry.
   */
  static Result resultFor(SubstanceRegistry.LoadStatus status, long nowMs) {
    return Result.healthy(describe(status, nowMs));
  }

  /** The health message for a given status. */
  static String describe(SubstanceRegistry.LoadStatus status, long nowMs) {
    if (status.loaded()) {
      return "CompTox registry loaded: " + status.substanceCount() + " substances";
    }

    StringBuilder sb = new StringBuilder("CompTox registry not loaded, so PFAS lookups return 503");
    if (status.failedAttempts() > 0) {
      sb.append("; ").append(status.failedAttempts())
          .append(status.failedAttempts() == 1 ? " failed attempt" : " consecutive failed attempts");
      if (status.lastError() != null) {
        sb.append(", last error: ").append(status.lastError());
      }
    } else {
      sb.append("; still loading from the EPA CTX API");
    }
    if (status.failedAttempts() > 0) {
      if (status.waitingToRetry(nowMs)) {
        status.retryAfterSeconds(nowMs).ifPresent(seconds -> sb.append(". Retrying in ").append(seconds).append("s"));
      } else {
        sb.append(". A retry is in progress");
      }
    }
    return sb.toString();
  }
}
