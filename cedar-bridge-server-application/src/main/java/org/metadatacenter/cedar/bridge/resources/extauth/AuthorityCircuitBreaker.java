package org.metadatacenter.cedar.bridge.resources.extauth;

import org.metadatacenter.exception.CedarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Stops asking one registry that has stopped answering.
 *
 * <p>A registry that is down does not refuse a connection; it accepts one and never replies, so
 * every request spends the whole response timeout before failing. With nothing in front of it, a
 * dead third party costs that on every call for as long as it stays dead, and those calls hold
 * worker threads that requests to the other six authorities need.
 *
 * <p>One of these guards one authority. Nothing guards a call to another CEDAR service: the
 * artifact server is not optional, so a breaker there would turn a timeout followed by 503 into an
 * immediate 503 and nothing else, and it would open during a rolling restart.
 *
 * <p>Only a failure to get an answer counts. A registry that replies 500 has answered, quickly, and
 * is not the case this exists for; so has one that replies 404. Opening on those would stop asking a
 * registry that is working.
 *
 * <p>An open breaker reports itself through {@link AuthorityNotReadyException}, which the route
 * already answers as a 503 carrying {@code Retry-After}. That is the same thing PFAS says while its
 * registry is still loading, and it means the same thing here: no answer yet, ask again after.
 */
final class AuthorityCircuitBreaker {

  private static final Logger log = LoggerFactory.getLogger(AuthorityCircuitBreaker.class);

  /**
   * How many answerless calls in a row it takes to stop asking.
   *
   * <p>More than one, because a single timeout is ordinary: a registry, a network or a DNS server
   * has a bad second without being down. Few enough that a registry which is actually down costs a
   * handful of held threads rather than one per request until it returns.
   */
  static final int FAILURES_BEFORE_OPENING = 5;

  /** How long an open breaker refuses before letting one request through to look. */
  static final Duration OPEN_FOR = Duration.ofSeconds(30);

  private final String authority;
  private final Duration openFor;
  private final Supplier<Instant> clock;

  private int consecutiveFailures;
  private Instant openedAt;
  /** Held by the one request allowed through a half-open breaker, so only one probes at a time. */
  private final AtomicBoolean probing = new AtomicBoolean();

  AuthorityCircuitBreaker(String authority) {
    this(authority, OPEN_FOR, Instant::now);
  }

  AuthorityCircuitBreaker(String authority, Duration openFor, Supplier<Instant> clock) {
    this.authority = authority;
    this.openFor = openFor;
    this.clock = clock;
  }

  /** What one guarded call to an authority does. */
  interface Call<T> {
    T get() throws CedarException;
  }

  /**
   * Makes the call, unless this authority has stopped answering.
   *
   * @throws AuthorityNotReadyException if the breaker is open, before any call is attempted
   */
  <T> T call(Call<T> call) throws CedarException {
    boolean probe = admit();
    try {
      T answer = call.get();
      succeeded();
      return answer;
    } catch (AuthorityNotReadyException notReady) {
      // The authority's own loading state, which it answered immediately. Not evidence about the
      // registry, so it neither opens the breaker nor closes it.
      throw notReady;
    } catch (RuntimeException | CedarException failure) {
      failed();
      throw failure;
    } finally {
      if (probe) {
        probing.set(false);
      }
    }
  }

  /**
   * Whether this call may proceed, and whether it is the one probe a half-open breaker allows.
   *
   * <p>A second request arriving while the probe is still out is refused rather than joining it:
   * two requests through a breaker that is open is what it exists to prevent.
   */
  private synchronized boolean admit() {
    if (openedAt == null) {
      return false;
    }
    Duration openSoFar = Duration.between(openedAt, clock.get());
    if (openSoFar.compareTo(openFor) < 0) {
      throw refusal(openFor.minus(openSoFar));
    }
    if (!probing.compareAndSet(false, true)) {
      throw refusal(openFor);
    }
    return true;
  }

  private AuthorityNotReadyException refusal(Duration remaining) {
    return new AuthorityNotReadyException(
        "The " + authority + " registry has stopped answering and is not being asked again yet",
        Math.max(1, remaining.toSeconds()));
  }

  private synchronized void succeeded() {
    if (openedAt != null) {
      log.info("The {} registry answered again; resuming calls to it", authority);
    }
    consecutiveFailures = 0;
    openedAt = null;
  }

  private synchronized void failed() {
    consecutiveFailures++;
    if (consecutiveFailures >= FAILURES_BEFORE_OPENING && openedAt == null) {
      openedAt = clock.get();
      log.warn("The {} registry has failed {} calls in a row; not asking it again for {}s",
          authority, consecutiveFailures, openFor.toSeconds());
    } else if (openedAt != null) {
      // The half-open probe failed. Start the wait again from now.
      openedAt = clock.get();
    }
  }

  /** Whether calls are being refused, for a health check or a test. */
  synchronized boolean isOpen() {
    return openedAt != null && Duration.between(openedAt, clock.get()).compareTo(openFor) < 0;
  }
}
