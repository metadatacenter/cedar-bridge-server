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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  /**
   * The default page size all seven have always used.
   *
   * <p>{@code pageSize} is rejected at {@code <= 1} rather than {@code < 1}, which is surprising —
   * a page of one is refused — and is what all seven did. Transcribed rather than corrected: this
   * is a refactor, and the contract test pins it so that changing it has to be a decision.
   */
  private static final int DEFAULT_PAGE_SIZE = 100;

  private static final String PAGINATION_ERROR =
      "Invalid pagination parameters: page must be >= 0, pageSize must be > 1";

  private final Map<String, ExternalAuthority> authoritiesBySegment = new LinkedHashMap<>();

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
      @ApiResponse(responseCode = "200", description = "Matching entries, with `found`, `page` and `pageSize`"),
      @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = CedarError.class)),
          description = "`page` is negative or `pageSize` is not greater than one"),
      @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "No authority is served under this path segment"),
      @ApiResponse(responseCode = "503", description = "The authority is not ready yet; Retry-After says when to try again")
  })
  public Response searchByName(
      @Parameter(description = "Which registry to ask. One of `doi`, `nih-grant`, `orcid`, `comp-tox`, `pmid`, `ror`, `rrid`. A segment no authority is registered under answers 404 naming the ones that are.", required = true)
      @PathParam("authority") String segment,
      @Parameter(description = "The name to search for.")
      @QueryParam("q") String query,
      @Parameter(description = "Zero-based page number. Defaults to 0.")
      @QueryParam("page") Integer page,
      @Parameter(description = "Entries per page. Defaults to 100, and must be greater than one.")
      @QueryParam("pageSize") Integer pageSize) throws CedarException {

    ExternalAuthority authority = authoritiesBySegment.get(segment);
    if (authority == null) {
      return unknownAuthority(segment);
    }

    final int pageVal = (page != null) ? page : 0;
    final int pageSizeVal = (pageSize != null) ? pageSize : DEFAULT_PAGE_SIZE;

    if (pageVal < 0 || pageSizeVal <= 1) {
      return CedarResponse.badRequest().errorMessage(PAGINATION_ERROR).build();
    }

    AuthoritySearchAnswer answer;
    try {
      answer = authority.search(query, pageVal, pageSizeVal);
    } catch (AuthorityNotReadyException notReady) {
      return notReadyResponse(notReady);
    }

    Map<String, Object> body = new HashMap<>();
    body.put("found", answer.found());
    body.put("results", answer.results());
    body.put("page", pageVal);
    body.put("pageSize", pageSizeVal);
    if (answer.errors() != null) {
      body.put("errors", answer.errors());
    }

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
      @ApiResponse(responseCode = "200", description = "What the authority holds for the identifier"),
      @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Unauthorized"),
      @ApiResponse(responseCode = "404",
          description = "No authority is served under this path segment, or the authority does not "
              + "hold this identifier"),
      @ApiResponse(responseCode = "503", description = "The authority is not ready yet; Retry-After says when to try again")
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
      answer = authority.details(id);
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
        .errorMessage("No external authority is served under \"" + segment + "\". Known: "
            + String.join(", ", authoritiesBySegment.keySet()) + ".")
        .build();
  }

  private Response notReadyResponse(AuthorityNotReadyException notReady) {
    return CedarResponse.status(CedarResponseStatus.SERVICE_UNAVAILABLE)
        .header(HttpHeaders.RETRY_AFTER, Long.toString(notReady.getRetryAfterSeconds()))
        .entity(Map.of("message", notReady.getMessage()))
        .build();
  }
}
