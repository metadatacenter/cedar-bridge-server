package org.metadatacenter.cedar.bridge.resources.extauth;

import org.metadatacenter.constant.HttpConstants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What an authority knows about one identifier.
 *
 * <p>Every one of the seven answers {@code found}, {@code requestedId}, and — when it found
 * something — {@code id} and {@code name}. Beyond that they differ honestly: ORCID and ROR return
 * a researcher or organisation record, which the fields that show a record read, and when the
 * registry refuses, both pass its reasons through. So this carries the body rather than prescribing
 * it, and the resource adds the two keys every answer has.
 *
 * @param statusCode 200, or the registry's status when it refused, which the route answers as
 *                   {@code UpstreamRefusal} classifies it
 * @param found      whether the identifier resolved
 * @param body       everything this authority has to say about it, its own keys
 * @param definitive whether the answer establishes existence or absence, rather than a malformed response
 */
public record AuthorityDetailsAnswer(int statusCode, boolean found, Map<String, Object> body, boolean definitive) {

  /** Preserve the details API while distinguishing an incomplete registry answer for verification. */
  public AuthorityDetailsAnswer(int statusCode, boolean found, Map<String, Object> body) {
    this(statusCode, found, body, true);
  }

  public AuthorityDetailsAnswer {
    body = (body == null) ? new LinkedHashMap<>() : body;
  }

  public static AuthorityDetailsAnswer found(Map<String, Object> body) {
    return new AuthorityDetailsAnswer(HttpConstants.OK, true, body);
  }

  public static AuthorityDetailsAnswer notFound(Map<String, Object> body) {
    return new AuthorityDetailsAnswer(HttpConstants.OK, false, body);
  }

  /** A readable 200 lacked the envelope or record fields needed to establish existence or absence. */
  public static AuthorityDetailsAnswer incomplete(Map<String, Object> body) {
    return new AuthorityDetailsAnswer(HttpConstants.OK, false, body, false);
  }

  /**
   * The registry answered, and not with a record. The route answers it in the shared error body,
   * which carries this body in {@code objects.authorityAnswer}, with a status {@code UpstreamRefusal}
   * decides from the registry's.
   */
  public static AuthorityDetailsAnswer failed(int statusCode, Map<String, Object> body) {
    return new AuthorityDetailsAnswer(statusCode, false, body);
  }
}
