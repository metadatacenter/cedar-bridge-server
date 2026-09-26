package org.metadatacenter.cedar.bridge.resources.extauth;

import com.codahale.metrics.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.metadatacenter.util.http.CedarError;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.http.PagedQuery;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;

/**
 * The bridge's external-authority surface: two routes, seven authorities.
 *
 * <p>These were seven resource classes of near-identical shape, and everything they shared was
 * shared by having been copied — the parameters, the pagination defaults, the rule rejecting a bad
 * page, the wording of that rejection, and the response envelope. Six of the seven were still
 * identical; ROR's rejection had drifted into a different shape and different words. Nothing
 * reported it, because until {@code ExternalAuthorityContractTest} nothing tested any of it.
 *
 * <p>Everything above an authority now lives here once. An {@link ExternalAuthority} answers for
 * one registry and says nothing about routing, validation or the envelope.
 *
 * <p>{@code /{authority}/search-by-name} and {@code /{authority}/{id}} both match a two-segment
 * path; JAX-RS prefers the literal, so the search route wins where it applies. That is how each of
 * the seven already worked, one path down.
 *
 * <h2>These routes are anonymous on purpose, and they are not free</h2>
 *
 * <p>Neither method resolves a user. That is deliberate and inherited: all seven classes this
 * replaced were open, because the registries behind them are public, and third-party deployments of
 * the embeddable editor reach them without a CEDAR session. {@code DataCiteResource}, registered in
 * the same application, asserts {@code LoggedIn} on every route, so the difference is a choice
 * rather than an omission.
 *
 * <h2>A registry that stops answering stops being asked</h2>
 *
 * <p>Each authority sits behind its own {@link AuthorityCircuitBreaker}. A registry that accepts
 * connections and never replies costs a full response timeout per request, and a held worker thread
 * with it, so after a few answerless calls in a row this surface stops asking that one and answers
 * 503 with {@code Retry-After} until a single probe finds it working again. The other six are
 * unaffected, and a registry that answers -- including one answering 500 -- is never cut off.
 *
 * <p>What a reader should not assume is that a public registry makes the route free. Three of the
 * seven authorities reach their registry on credentials the deployment holds:
 * {@code RridAuthority} sends the configured {@code apikey} header, {@code PubMedAuthority} appends
 * the configured {@code api_key} parameter, and {@code OrcidAuthority} uses the configured client
 * credentials. An anonymous caller therefore spends CEDAR's quota at ORCID, PubMed and RRID, and can
 * use this service as an unauthenticated relay to them.
 *
 * <p>The cost is bounded by whatever those three registries allow the deployment per period, and
 * nothing here bounds it further: there is no rate limit, no per-caller accounting, and no way to
 * tell one caller from another. An operator setting quotas should size them for the open internet
 * rather than for CEDAR's user count.
 */
@Path("/ext-auth/{authority}")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "External authorities")
public class ExternalAuthorityResource extends CedarMicroserviceResource {

  /** The default page size all seven have always used. */
  private static final int DEFAULT_PAGE_SIZE = 100;

  /**
   * The largest page an authority is asked for. Before there was one, a caller could ask a registry
   * for any number of entries on the deployment's credentials.
   */
  static final int MAX_PAGE_SIZE = 500;

  /**
   * The refusal for the zero-based {@code page} and {@code pageSize} the route took before it took
   * {@code limit} and {@code offset}, kept for the clients that still send them.
   *
   * <p>{@code pageSize} is rejected at {@code <= 1} rather than {@code < 1}, which is surprising —
   * a page of one is refused — and is what all seven did. The contract test pins it, so changing
   * it has to be a decision.
   */
  private static final String PAGINATION_ERROR =
      "Invalid pagination parameters: page must be >= 0, pageSize must be > 1";

  private final Map<String, ExternalAuthority> authoritiesBySegment = new LinkedHashMap<>();

  /**
   * One breaker per authority, so a registry that has stopped answering stops being asked.
   *
   * <p>Per authority rather than one for the surface: ORCID being down says nothing about ROR, and
   * a shared breaker would take the other six down with whichever one failed.
   */
  private final Map<String, AuthorityCircuitBreaker> breakersBySegment = new LinkedHashMap<>();

  public ExternalAuthorityResource(CedarConfig cedarConfig, List<ExternalAuthority> authorities) {
    super(cedarConfig);
    for (ExternalAuthority authority : authorities) {
      ExternalAuthority clash = authoritiesBySegment.put(authority.pathSegment(), authority);
      if (clash != null) {
        // Two authorities under one path is a wiring mistake that would otherwise surface as one
        // of them silently never being reachable.
        throw new IllegalArgumentException(
            "two authorities are registered under \"" + authority.pathSegment() + "\": "
                + clash.getClass().getSimpleName() + " and " + authority.getClass().getSimpleName());
      }
      breakersBySegment.put(authority.pathSegment(), new AuthorityCircuitBreaker(authority.pathSegment()));
    }
  }

  @GET
  @Timed
  @Path("/search-by-name")
  @Operation(summary = "Search an external registry by name",
      description = "These routes take no credentials. Neither builds a request context, so anyone who can reach this host can use them, and three of the seven authorities behind them spend credentials the deployment holds. Recorded here because a spec that claimed otherwise would be worse than one that says so. Search one external authority for entries matching a name, and return them with "
          + "the paging that produced them. The status is the authority's own, so an upstream refusal "
          + "is reported as that authority reported it. An authority that has not finished loading "
          + "answers 503 with Retry-After rather than an empty result.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "A page of matching entries, with the paging envelope and, "
          + "for clients that still page by number, `found`, `page` and `pageSize`",
          content = @Content(schema = @Schema(ref = "#/components/schemas/AuthoritySearchResults"))),
      @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = CedarError.class)),
          description = "The limit or offset is out of range, `page` is negative or `pageSize` is not greater "
              + "than one, or both kinds of paging were sent"),
      @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "No authority is served under this path segment"),
      @ApiResponse(responseCode = "503", description = "The authority is not ready yet; Retry-After says when to try again",
          content = @Content(schema = @Schema(implementation = CedarError.class)))
  })
  public Response searchByName(
      @Parameter(description = "Which registry to ask. One of `doi`, `nih-grant`, `orcid`, `comp-tox`, `pmid`, `ror`, `rrid`. A segment no authority is registered under answers 404 naming the ones that are.", required = true)
      @PathParam("authority") String segment,
      @Parameter(description = "The name to search for.")
      @QueryParam("q") String query,
      @Parameter(description = "How many entries to return, from 1 to 500. Defaults to 100.")
      @QueryParam("limit") Optional<Integer> limit,
      @Parameter(description = "How many matching entries to skip. Defaults to 0.")
      @QueryParam("offset") Optional<Integer> offset,
      @Parameter(description = "Zero-based page number, for clients that page by number. Cannot be sent with "
          + "`limit` or `offset`. Defaults to 0.")
      @QueryParam("page") Integer page,
      @Parameter(description = "Entries per page, for clients that page by number. Cannot be sent with `limit` "
          + "or `offset`. Defaults to 100, and must be greater than one and at most 500.")
      @QueryParam("pageSize") Integer pageSize) throws CedarException {

    ExternalAuthority authority = authoritiesBySegment.get(segment);
    if (authority == null) {
      return unknownAuthority(segment);
    }

    final int offsetVal;
    final int limitVal;
    boolean byNumber = page != null || pageSize != null;
    if (byNumber && (limit.isPresent() || offset.isPresent())) {
      return CedarResponse.badRequest()
          .message("Send either limit and offset or page and pageSize, not both").build();
    }
    if (byNumber) {
      int pageVal = (page != null) ? page : 0;
      int pageSizeVal = (pageSize != null) ? pageSize : DEFAULT_PAGE_SIZE;
      if (pageVal < 0 || pageSizeVal <= 1) {
        return CedarResponse.badRequest().message(PAGINATION_ERROR).build();
      }
      if (pageSizeVal > MAX_PAGE_SIZE) {
        return CedarResponse.badRequest()
            .message("Invalid pagination parameters: pageSize must be at most " + MAX_PAGE_SIZE).build();
      }
      offsetVal = pageVal * pageSizeVal;
      limitVal = pageSizeVal;
    } else {
      PagedQuery pagedQuery = new PagedQuery(DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE).limit(limit).offset(offset);
      pagedQuery.validate();
      offsetVal = pagedQuery.getOffset();
      limitVal = pagedQuery.getLimit();
    }

    AuthoritySearchAnswer answer;
    try {
      answer = breakersBySegment.get(segment).call(() -> authority.search(query, offsetVal, limitVal));
    } catch (AuthorityNotReadyException notReady) {
      return notReadyResponse(notReady);
    }

    AuthoritySearchPage body = new AuthoritySearchPage(answer, uriInfo.getRequestUri().toString(), limitVal,
        offsetVal);
    return CedarResponse.status(CedarResponseStatus.fromStatusCode(answer.statusCode())).entity(body).build();
  }

  @GET
  @Timed
  @Path("/{id}")
  @Operation(summary = "Resolve an identifier against an external registry",
      description = "These routes take no credentials. Neither builds a request context, so anyone who can reach this host can use them, and three of the seven authorities behind them spend credentials the deployment holds. Recorded here because a spec that claimed otherwise would be worse than one that says so. Look one identifier up in an external authority and return what it holds, with "
          + "`found` saying whether it resolved and `requestedId` echoing what was asked. The status "
          + "is the authority's own. This path and the search path both match two segments; the "
          + "literal `search-by-name` wins, so no authority can have an entry by that name.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "What the authority holds for the identifier",
          content = @Content(schema = @Schema(ref = "#/components/schemas/AuthorityDetails"))),
      @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Unauthorized"),
      @ApiResponse(responseCode = "404",
          description = "No authority is served under this path segment, or the authority does not "
              + "hold this identifier"),
      @ApiResponse(responseCode = "503", description = "The authority is not ready yet; Retry-After says when to try again",
          content = @Content(schema = @Schema(implementation = CedarError.class)))
  })
  public Response details(
      @Parameter(description = "Which registry to ask. One of `doi`, `nih-grant`, `orcid`, `comp-tox`, `pmid`, `ror`, `rrid`. A segment no authority is registered under answers 404 naming the ones that are.", required = true)
      @PathParam("authority") String segment,
      @Parameter(description = "The identifier to resolve, as that registry spells it.", required = true)
      @PathParam(PP_ID) String id) throws CedarException {

    ExternalAuthority authority = authoritiesBySegment.get(segment);
    if (authority == null) {
      return unknownAuthority(segment);
    }

    AuthorityDetailsAnswer answer;
    try {
      answer = breakersBySegment.get(segment).call(() -> authority.details(id));
    } catch (AuthorityNotReadyException notReady) {
      return notReadyResponse(notReady);
    }

    // `found` and `requestedId` are on every one of the seven answers, so an authority states
    // neither: what it knows is whether it resolved the identifier, not how that is reported.
    Map<String, Object> body = new HashMap<>(answer.body());
    body.put("found", answer.found());
    body.put("requestedId", id);

    return CedarResponse.status(CedarResponseStatus.fromStatusCode(answer.statusCode())).entity(body).build();
  }

  /**
   * A path segment no authority is registered under.
   *
   * <p>Previously a 404 from Jersey, since no resource declared the path at all. Now the route
   * exists for every segment, so the 404 is this one — and it can name what does exist, which is
   * more use to whoever mistyped it.
   */
  private Response unknownAuthority(String segment) {
    return CedarResponse.notFound()
        .message("No external authority is served under \"" + segment + "\". Known: "
            + String.join(", ", authoritiesBySegment.keySet()) + ".")
        .build();
  }

  private Response notReadyResponse(AuthorityNotReadyException notReady) {
    return CedarResponse.status(CedarResponseStatus.SERVICE_UNAVAILABLE)
        .header(HttpHeaders.RETRY_AFTER, Long.toString(notReady.getRetryAfterSeconds()))
        .message(notReady.getMessage())
        .build();
  }
}
