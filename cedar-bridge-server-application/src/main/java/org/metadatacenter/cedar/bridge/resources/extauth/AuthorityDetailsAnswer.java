package org.metadatacenter.cedar.bridge.resources.extauth;

import org.metadatacenter.constant.HttpConstants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What an authority knows about one identifier.
 *
 * <p>Every one of the seven answers {@code found}, {@code requestedId}, and — when it found
 * something — {@code id} and {@code name}. Beyond that they differ honestly: ORCID and ROR return
 * a researcher or organisation record, which the fields that show a record read, and ORCID also
 * passes the registry's own errors through. So this carries the body rather than prescribing it,
 * and the resource adds the two keys every answer has.
 *
 * @param statusCode the status to answer with, which is the registry's own when it was not 200
 * @param found      whether the identifier resolved
 * @param body       everything this authority has to say about it, its own keys
 */
public record AuthorityDetailsAnswer(int statusCode, boolean found, Map<String, Object> body) {

  public AuthorityDetailsAnswer {
    body = (body == null) ? new LinkedHashMap<>() : body;
  }

  public static AuthorityDetailsAnswer found(Map<String, Object> body) {
    return new AuthorityDetailsAnswer(HttpConstants.OK, true, body);
  }

  public static AuthorityDetailsAnswer notFound(Map<String, Object> body) {
    return new AuthorityDetailsAnswer(HttpConstants.OK, false, body);
  }

  /** The registry answered, and not with a record. Its status is passed on unchanged. */
  public static AuthorityDetailsAnswer failed(int statusCode, Map<String, Object> body) {
    return new AuthorityDetailsAnswer(statusCode, false, body);
  }
}
