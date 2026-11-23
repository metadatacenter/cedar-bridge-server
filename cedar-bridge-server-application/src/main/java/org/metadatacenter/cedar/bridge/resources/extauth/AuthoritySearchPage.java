package org.metadatacenter.cedar.bridge.resources.extauth;

import org.metadatacenter.util.http.PagedListResponse;

import java.util.Map;

/**
 * One authority's answer to a name search, in CEDAR's body envelope.
 *
 * <p>{@code results} holds one entry per matching term, keyed by the term's IRI. {@code found} says
 * whether the registry offered any. The embeddable editor reads {@code found} and {@code results};
 * it sends only {@code q} and so reads the first page at the default limit.
 *
 * <p>A page holds only what a registry offered. A registry's refusal is answered in the shared error
 * body instead, so no page carries a refusal's reasons.
 */
public final class AuthoritySearchPage extends PagedListResponse {

  private final boolean found;
  private final Map<String, ?> results;

  AuthoritySearchPage(AuthoritySearchAnswer answer, String requestUrl, int limit, int offset) {
    this.found = answer.found();
    this.results = answer.results();
    if (answer.totalCount() != null) {
      page(requestUrl, answer.totalCount(), limit, offset, answer.countCapped());
    } else {
      // The authority cannot know its total. Report the least it could be: everything up to this
      // page, and one more when the page came back full, so a next link appears exactly when there
      // may be more to read.
      int returned = answer.results().size();
      page(requestUrl, (long) offset + returned + (returned >= limit ? 1 : 0), limit, offset, true);
    }
  }

  public boolean isFound() {
    return found;
  }

  public Map<String, ?> getResults() {
    return results;
  }
}
