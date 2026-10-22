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
   * The registry this authority asks, as a refusal identifies it.
   *
   * <p>A registry's refusal is answered in the shared error body, and its
   * {@code parameters.upstreamService} identifies the registry that refused rather than the path
   * segment the caller sent, so a refusal from the {@code doi} authority names DataCite.
   */
  String registryName();

  /**
   * The terms this authority offers for a name: at most {@code limit} of them, skipping the first
   * {@code offset}.
   *
   * <p>Called with pagination already defaulted and validated, so an implementation can use the
   * numbers as given. The offset need not be a multiple of the limit.
   */
  AuthoritySearchAnswer search(String query, int offset, int limit) throws CedarException;

  /** Everything this authority knows about one identifier. */
  AuthorityDetailsAnswer details(String id) throws CedarException;

  /**
   * Whether {@code id} could be one of this registry's identifiers, in any spelling {@link #details}
   * accepts. By default the registry decides, so every identifier could be.
   *
   * <p>One that could not, such as a DOI holding a space, is answered as an identifier the registry
   * does not hold, without asking the registry and without calling through the authority's circuit
   * breaker. No request is made for it, so it is no evidence about whether the registry is answering.
   */
  default boolean couldIdentify(String id) {
    return true;
  }

  /**
   * Whether a search for {@code query} could match anything this registry holds. By default the
   * registry decides, so every search could.
   *
   * <p>For a search that could not, the route calls {@link #search} without the authority's circuit
   * breaker. No request is made for it, so it is no evidence about whether the registry is answering.
   * An authority that overrides this must therefore answer such a search without asking its registry.
   */
  default boolean couldMatch(String query) {
    return true;
  }
}
