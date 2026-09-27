package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.metadatacenter.util.http.PagedListResponse;

import java.util.Map;

/**
 * One authority's answer to a name search, in CEDAR's body envelope.
 *
 * <p>{@code found}, {@code page} and {@code pageSize} are the fields this route answered with before
 * it took an offset. They are kept beside the envelope so a client that still sends {@code page}
 * and {@code pageSize}, the published embeddable editor among them, reads what it always read.
 * {@code page} is the offset divided by the limit, which is exact whenever the offset falls on a
 * page boundary, as it always does for such a client.
 */
public final class AuthoritySearchPage extends PagedListResponse {

  private final boolean found;
  private final Map<String, ?> results;
  private final int page;
  private final int pageSize;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final Object errors;

  AuthoritySearchPage(AuthoritySearchAnswer answer, String requestUrl, int limit, int offset) {
    this.found = answer.found();
    this.results = answer.results();
    this.page = offset / limit;
    this.pageSize = limit;
    this.errors = answer.errors();
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

  public int getPage() {
    return page;
  }

  public int getPageSize() {
    return pageSize;
  }

  public Object getErrors() {
    return errors;
  }
}
