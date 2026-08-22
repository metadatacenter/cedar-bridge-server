package org.metadatacenter.cedar.bridge.resources.extauth;

/**
 * An authority that cannot answer yet, and knows roughly when it will.
 *
 * <p>Only PFAS raises this: it answers from a registry loaded from EPA CompTox rather than by
 * proxying, so before that load completes it has nothing to say about any request. The wait is the
 * loader's own next attempt, which grows as it backs off — a client honouring {@code Retry-After}
 * then stops re-asking a question whose answer cannot have changed.
 */
public class AuthorityNotReadyException extends RuntimeException {

  private final long retryAfterSeconds;

  public AuthorityNotReadyException(String message, long retryAfterSeconds) {
    super(message);
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public long getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}
