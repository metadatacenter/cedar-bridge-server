package org.metadatacenter.cedar.bridge.resources.extauth;

import org.metadatacenter.constant.HttpConstants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What an authority found for a name, before the envelope goes round it.
 *
 * <p>The envelope — {@code found}, {@code page}, {@code pageSize}, and {@code errors} when there
 * are any — is the same for all seven and is built once, in {@link ExternalAuthorityResource}.
 * What an authority contributes is the terms and, when the registry refused, the status and the
 * reason.
 *
 * @param statusCode the status to answer with, which is the registry's own when it was not 200
 * @param results    term IRI to {@code {name, details}}, in the order the registry offered them
 * @param errors     whatever the registry said went wrong, or null
 */
public record AuthoritySearchAnswer(int statusCode, Map<String, ?> results, Object errors) {

  private static final Map<String, Object> NOTHING = new LinkedHashMap<>();

  public AuthoritySearchAnswer {
    results = (results == null) ? NOTHING : results;
  }

  /** Terms found, or none — the ordinary answer. */
  public static AuthoritySearchAnswer of(Map<String, ?> results) {
    return new AuthoritySearchAnswer(HttpConstants.OK, results, null);
  }

  /** Nothing to offer, without the registry having been asked. */
  public static AuthoritySearchAnswer nothing() {
    return of(NOTHING);
  }

  /** The registry refused, in its own words. */
  public static AuthoritySearchAnswer failed(int statusCode, Object errors) {
    return new AuthoritySearchAnswer(statusCode, NOTHING, errors);
  }

  /**
   * Whether this counts as a find.
   *
   * <p>Terms, and a registry that answered. A non-200 with terms cannot arise from the two
   * constructors above, and would not be a find if it did.
   */
  public boolean found() {
    return statusCode == HttpConstants.OK && !results.isEmpty();
  }
}
