package org.metadatacenter.cedar.bridge.resources.extauth;

import org.metadatacenter.exception.CedarException;

/**
 * One external authority, behind the single ext-auth route that serves all of them.
 *
 * <p>ORCID, ROR, PFAS, PubMed, RRID, NIH Grant and DOI were seven resource classes, each declaring
 * the same two routes under its own path and each carrying its own copy of everything those routes
 * do beyond calling a registry: the pagination defaults, the rule that rejects a bad page, the
 * wording of that rejection, and the response envelope. The copies had already drifted — ROR
 * rejected a request in a different shape and different words from the other six — and nothing
 * reported it, because nothing tested any of it.
 *
 * <p>What genuinely differs between two authorities is which registry answers and how its reply
 * becomes terms. That is what an implementation of this interface holds, and nothing else.
 * {@link ExternalAuthorityResource} owns the routes, the parameters, the validation and the
 * envelope, so an eighth authority is one class rather than a resource, a registration and a
 * consumer-side config key.
 */
public interface ExternalAuthority {

  /**
   * The path segment this authority is served under, which is the whole of its addressing.
   *
   * <p>Not the authority's name: PFAS answers under {@code comp-tox} and NIH Grant under
   * {@code nih-grant}, as they always have. These are the strings CEE's descriptors carry.
   */
  String pathSegment();

  /**
   * The terms this authority offers for a name, on the page asked for.
   *
   * <p>Called with pagination already defaulted and validated, so an implementation can use the
   * numbers as given.
   */
  AuthoritySearchAnswer search(String query, int page, int pageSize) throws CedarException;

  /** Everything this authority knows about one identifier. */
  AuthorityDetailsAnswer details(String id) throws CedarException;
}
