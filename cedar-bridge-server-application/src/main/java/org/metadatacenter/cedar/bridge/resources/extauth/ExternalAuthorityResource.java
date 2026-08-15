package org.metadatacenter.cedar.bridge.resources.extauth;

import com.codahale.metrics.annotation.Timed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
 */
@Path("/ext-auth/{authority}")
@Produces(MediaType.APPLICATION_JSON)
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
  public Response searchByName(@PathParam("authority") String segment,
                               @QueryParam("q") String query,
                               @QueryParam("page") Integer page,
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
  public Response details(@PathParam("authority") String segment,
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
