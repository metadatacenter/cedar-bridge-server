package org.metadatacenter.cedar.bridge.resources;

import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.util.http.CedarResponse;

/**
 * An error status from a service this server asks on the deployment's account, and the answer the
 * caller receives for it.
 *
 * <p>The DOI routes ask DataCite, and the external-authority routes ask seven registries. Both answer
 * a service's error in the shared error body, with {@code upstreamServerError}, and both take its
 * status from the rule here. A refusal of the request itself keeps the service's status, so the
 * caller can act on it, whether the service holds no such entry or refused what it was sent. Every
 * other error concerns the deployment or the service rather than the request, and answers 502, since
 * the caller can do nothing about it. These are a refusal of the deployment's credentials or access
 * (401 or 403), a limit on the deployment's requests (429), and the service failing (5xx). A status
 * below 400 is no error at all, and no answer a route can use, so it answers 502 too.
 *
 * <p>A 401 passed on would also mislead the caller. Every 401 CEDAR answers carries CEDAR's
 * authentication challenge, which asks the caller to authenticate to CEDAR although CEDAR asked for
 * no credentials.
 *
 * @param service the service, as {@code parameters.upstreamService} identifies it
 * @param status  the status the service answered with
 */
public record UpstreamRefusal(String service, int status) {

  /** The parameter that identifies the service. */
  public static final String SERVICE_PARAMETER = "upstreamService";

  /** The parameter that carries the service's status. */
  public static final String STATUS_PARAMETER = "upstreamStatusCode";

  /** What a service's status says went wrong. */
  public enum Kind {
    /** The service refused the request itself, with a 4xx other than 401, 403 or 429. */
    REQUEST_REFUSED,
    /** The service refused the deployment's credentials or access, with 401 or 403. */
    ACCESS_REFUSED,
    /** The service is limiting the deployment's requests, with 429. */
    RATE_LIMITED,
    /** The service failed, with a 5xx or a higher status. */
    FAILED,
    /** The service answered with a status below 400, which no route can use. */
    UNUSABLE
  }

  public Kind kind() {
    if (status < 400) {
      return Kind.UNUSABLE;
    }
    if (status >= 500) {
      return Kind.FAILED;
    }
    if (status == 401 || status == 403) {
      return Kind.ACCESS_REFUSED;
    }
    if (status == 429) {
      return Kind.RATE_LIMITED;
    }
    return Kind.REQUEST_REFUSED;
  }

  /** The status the caller receives: the service's own for a refusal of the request, and 502 otherwise. */
  public int answerStatus() {
    return kind() == Kind.REQUEST_REFUSED ? status : CedarResponseStatus.BAD_GATEWAY.getStatusCode();
  }

  /**
   * The shared error body for this status. The caller adds what it carries of the service's answer.
   *
   * @param explanation what happened, in the words of the route that asked
   * @param reasons     the reasons the service gave, one per line, or an empty string when it gave none
   */
  public CedarResponse.CedarResponseBuilder answer(String explanation, String reasons) {
    return CedarResponse.status(answerStatus())
        .errorKey(CedarErrorKey.UPSTREAM_SERVER_ERROR)
        .message(reasons.isEmpty() ? explanation : explanation + "\n" + reasons)
        .parameter(STATUS_PARAMETER, status)
        .parameter(SERVICE_PARAMETER, service);
  }
}
