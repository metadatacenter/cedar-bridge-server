package org.metadatacenter.cedar.bridge.resources.extauth;

import org.metadatacenter.constant.HttpConstants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What an authority found for a name, before the envelope goes round it.
 *
 * <p>The envelope is the same for all seven and is built once, in {@link ExternalAuthorityResource}.
 * What an authority contributes is the terms, what it knows about how many there are, and, when the
 * registry refused, the status and the reason.
 *
 * <p>Registries differ in what they can say about a total. Most report one, and it is exact. One
 * can be reached only in part, and its count is then of what can be reached, with
 * {@code countCapped} saying there are more. One narrows what its registry returns after the fact
 * and cannot know the total at all, which a null {@code totalCount} records.
 *
 * @param statusCode  the status to answer with, which is the registry's own when it was not 200
 * @param results     term IRI to {@code {name, details}}, in the order the registry offered them
 * @param errors      whatever the registry said went wrong, or null
 * @param totalCount  how many terms match, or null when the authority cannot know
 * @param countCapped whether {@code totalCount} is a lower bound rather than the whole
 */
public record AuthoritySearchAnswer(int statusCode, Map<String, ?> results, Object errors, Long totalCount,
                                    boolean countCapped) {

  private static final Map<String, Object> NOTHING = new LinkedHashMap<>();

  public AuthoritySearchAnswer {
    results = (results == null) ? NOTHING : results;
  }

  /** Terms found, out of an exact total. */
  public static AuthoritySearchAnswer of(Map<String, ?> results, long totalCount) {
    return new AuthoritySearchAnswer(HttpConstants.OK, results, null, totalCount, false);
  }

  /** Terms found, out of a total that is only a lower bound when {@code countCapped} is true. */
  public static AuthoritySearchAnswer of(Map<String, ?> results, long totalCount, boolean countCapped) {
    return new AuthoritySearchAnswer(HttpConstants.OK, results, null, totalCount, countCapped);
  }

  /** Terms found, out of a total the authority cannot know. */
  public static AuthoritySearchAnswer ofUnknownTotal(Map<String, ?> results) {
    return new AuthoritySearchAnswer(HttpConstants.OK, results, null, null, false);
  }

  /** Nothing to offer, without the registry having been asked. */
  public static AuthoritySearchAnswer nothing() {
    return of(NOTHING, 0);
  }

  /** The registry refused, in its own words. */
  public static AuthoritySearchAnswer failed(int statusCode, Object errors) {
    return new AuthoritySearchAnswer(statusCode, NOTHING, errors, 0L, false);
  }

  /**
   * Whether this counts as a find.
   *
   * <p>Terms, and a registry that answered. A non-200 with terms cannot arise from the constructors
   * above, and would not be a find if it did.
   */
  public boolean found() {
    return statusCode == HttpConstants.OK && !results.isEmpty();
  }
}
