package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.cedar.bridge.resources.doi.DataCiteHttpClient;
import org.metadatacenter.cedar.bridge.resources.doi.DoiAnnotationWriter;
import org.metadatacenter.cedar.bridge.resources.doi.DoiMinting;
import org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationStore;
import org.metadatacenter.cedar.bridge.resources.doi.DoiSuffixes;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.security.model.auth.CedarPermission;
import org.metadatacenter.util.artifact.InstanceArtifactDocument;
import org.metadatacenter.util.http.CedarError;
import org.metadatacenter.util.http.ProxyUtil;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.function.Supplier;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.constant.CedarQueryParameters.QP_SOURCE_ARTIFACT_ID;
import static org.metadatacenter.rest.assertion.GenericAssertions.LoggedIn;

/**
 * The DOI routes. Each authenticates its caller and checks the route's own preconditions, and
 * {@link DoiMinting} does the rest: it reads the artifact, asks DataCite, keeps the artifact's DOI record
 * and records a published DOI on the artifact.
 */
@Path("/datacite")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "DOIs")
@SecurityRequirement(name = "api_key")
public class DataCiteResource extends CedarMicroserviceResource {

  private final DoiMinting minting;

  public DataCiteResource(CedarConfig cedarConfig, DoiRegistrationStore registrations) {
    this(cedarConfig, CedarDataServices.getInstance(),
        new DataCiteHttpClient(cedarConfig.getBridgeConfig().getDataCite()), ProxyUtil::proxyPost, registrations,
        new DoiSuffixes());
  }

  /** The routes over the given collaborators, which a test replaces with ones it can script. */
  public DataCiteResource(CedarConfig cedarConfig, CedarDataServices dataServices, DataCiteHttpClient httpClient,
                          DoiAnnotationWriter annotationWriter, DoiRegistrationStore registrations,
                          Supplier<String> doiSuffixes) {
    super(cedarConfig, dataServices);
    this.minting = new DoiMinting(cedarConfig, dataServices, httpClient, annotationWriter, registrations, doiSuffixes);
  }

  @GET
  @Timed
  @Path("/get-doi-metadata/{id}")
  @Operation(summary = "Get a DOI's metadata from DataCite",
      description = "Fetch what DataCite holds for a DOI, including affiliation detail. A refusal of the "
          + "request itself keeps DataCite's status, so a DOI DataCite does not know is a 404. Every refusal "
          + "DataCite makes is answered in the shared error body (upstreamServerError), with DataCite's own errors in "
          + "objects.dataCiteErrors and its status in parameters.upstreamStatusCode. DataCite integration can "
          + "be switched off by configuration, and every route here answers 400 when it is.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "The DOI's metadata, as DataCite returned it",
          content = @Content(schema = @Schema(ref = "#/components/schemas/DataCiteDoiMetadata"))),
      @ApiResponse(responseCode = "400", description = "DataCite integration is disabled (dataCiteDOIDisabled), "
          + "or DataCite refused the request (upstreamServerError)",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "401", description = UNAUTHORIZED,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "404", description = "DataCite holds no such DOI (upstreamServerError). The "
          + "body's objects.dataCiteErrors holds DataCite's errors.",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "406", description = NOT_ACCEPTABLE,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "429", description = RATE_LIMITED,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "500", description = INTERNAL_ERROR,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "502", description = DATACITE_FAILED,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "503", description = UNAVAILABLE,
          content = @Content(schema = @Schema(implementation = CedarError.class)))
  })
  public Response getDOIMetadata(
      @Parameter(description = "The DOI as a URL. Example: https://doi.org/10.82658/abcd-1234",
          required = true)
      @PathParam(PP_ID) String doiIdUrl) throws CedarException {
    CedarRequestContext c = buildRequestContext();
    c.must(c.user()).be(LoggedIn);

    Response disabled = minting.refusalWhileDisabled();
    if (disabled != null) {
      return disabled;
    }
    return minting.readMetadata(doiIdUrl);
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Path("/create-doi")
  @Operation(summary = "Begin minting a DOI for an artifact",
      description = "Return everything the workbench needs to open the DOI form for one artifact: "
          + "the DataCite metadata template, the source artifact, and either the draft DOI already "
          + "started for it or a metadata instance pre-filled with what can be derived. Reads only; "
          + "nothing is minted here. DataCite integration can be switched off by configuration, and every route here answers 400 when it is.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "The template, the artifact, and the draft or pre-filled metadata",
          content = @Content(schema = @Schema(ref = "#/components/schemas/DoiFormStart"))),
      @ApiResponse(responseCode = "400", description = "DataCite integration is disabled (dataCiteDOIDisabled), the "
          + "artifact is not eligible for a DOI, or the resource server refused a read (upstreamServerError)",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "401", description = "The request carries no valid credentials, the caller "
          + "cannot edit the source artifact (noWriteAccessToArtifact), or the resource server refused a read "
          + "(upstreamServerError)",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "403", description = "The caller cannot read the source artifact or the DataCite "
          + "template (upstreamServerError), or lacks the template read permission",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "404", description = "The source artifact or DataCite template was not found",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "406", description = NOT_ACCEPTABLE,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "409", description = ALREADY_HAS_DOI,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "429", description = "The caller, or the reads the workflow makes of the resource "
          + "server, exceeded the deployment's request rate",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "500", description = INTERNAL_ERROR,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "502", description = "DataCite could not be reached or refused the draft lookup, "
          + "whose errors are then in objects.dataCiteErrors, or the resource server's answer could not be used "
          + "(upstreamServerError)",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "503", description = UNAVAILABLE,
          content = @Content(schema = @Schema(implementation = CedarError.class)))
  })
  public Response createDOIStart(
      @Parameter(description = "Identifier of the artifact the DOI is for.", required = true)
      @QueryParam(QP_SOURCE_ARTIFACT_ID) String sourceArtifactId) throws CedarException {
    CedarRequestContext c = buildRequestContext();
    c.must(c.user()).be(LoggedIn);

    Response disabled = minting.refusalWhileDisabled();
    if (disabled != null) {
      return disabled;
    }
    c.must(c.user()).have(CedarPermission.TEMPLATE_READ);
    return minting.start(c, sourceArtifactId);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Path("/create-doi")
  @Operation(summary = "Mint a DOI for an artifact",
      description = "Register a DOI with DataCite from the metadata instance in the body, and record "
          + "it on the artifact. `state` chooses between a draft, which can still be changed, and a "
          + "published DOI, which is findable and permanent. The metadata is validated against the "
          + "DataCite template first, and a failure is returned rather than half-registered. CEDAR chooses "
          + "the DOI and records it for the artifact before it asks DataCite to register it, so a request "
          + "repeated after a timeout or a failed recording continues with the same DOI rather than "
          + "registering another. DataCite integration can be switched off by configuration, and every "
          + "route here answers 400 when it is.")
  @RequestBody(description = "The DataCite metadata to register, as an instance of the DataCite "
      + "metadata template. It is validated against that template before anything is sent to DataCite.",
      required = true,
      content = @Content(mediaType = MediaType.APPLICATION_JSON,
          schema = @Schema(implementation = InstanceArtifactDocument.class)))
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "The DOI, as registered",
          content = @Content(schema = @Schema(ref = "#/components/schemas/DoiRegistration")),
          headers = @Header(name = "Location", description = "The DOI as a resolvable URL.",
              schema = @Schema(type = "string", format = "uri"))),
      @ApiResponse(responseCode = "400", description = "DataCite integration is disabled (dataCiteDOIDisabled), "
          + "`state` is neither draft nor publish, the body is not JSON (malformedJsonRequestBody), the artifact is "
          + "not eligible, the metadata failed validation (invalidInput, with the report in "
          + "objects.validationResult), or DataCite refused the metadata (invalidInput, with DataCite's errors in "
          + "objects.dataCiteErrors). The resource server refusing a read is also reported (upstreamServerError).",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "401", description = "The request carries no valid credentials, the caller "
          + "cannot edit the source artifact (noWriteAccessToArtifact), or the resource server refused a read "
          + "(upstreamServerError)",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "403", description = "The caller cannot read the source artifact or the DataCite "
          + "template (upstreamServerError), or lacks the template read permission",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "404", description = "The source artifact or DataCite template was not found",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "406", description = NOT_ACCEPTABLE,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "409", description = ALREADY_HAS_DOI + " A draft request for such an artifact "
          + "records nothing, and a publish request records the DOI on the artifact and answers 201. DataCite refusing "
          + "the registration as a conflict is also a 409 (upstreamServerError, with DataCite's errors in "
          + "objects.dataCiteErrors).",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "415", description = "The body is not declared as JSON",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "429", description = "The caller, or the reads the workflow makes of the resource "
          + "server, exceeded the deployment's request rate",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "500", description = INTERNAL_ERROR,
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "502", description = DATACITE_FAILED + " The resource server's answer could not "
          + "be used (upstreamServerError), or the DOI was published but could not be recorded on the artifact, "
          + "which parameters.reconciliationRequired reports. Repeating the publish request records the DOI "
          + "parameters.doi names and registers nothing at DataCite.",
          content = @Content(schema = @Schema(implementation = CedarError.class))),
      @ApiResponse(responseCode = "503", description = UNAVAILABLE,
          content = @Content(schema = @Schema(implementation = CedarError.class)))
  })
  public Response createDOI(
      @Parameter(description = "Identifier of the artifact the DOI is for.", required = true)
      @QueryParam(QP_SOURCE_ARTIFACT_ID) String sourceArtifactId,
      @Parameter(description = "`draft` for a DOI that can still be changed, `publish` for a "
          + "findable and permanent one.", required = true)
      @QueryParam("state") String state,
      // Hidden from the scanner, not from the spec: the body is described by the @RequestBody above.
      // Left unannotated, the scanner resolves JsonNode into a component of its own, which the
      // explicit request body then never references, and the spec carries an empty JsonNode schema.
      @Parameter(hidden = true) JsonNode dataCiteInstance) throws CedarException {
    CedarRequestContext c = buildRequestContext();
    c.must(c.user()).be(LoggedIn);

    Response disabled = minting.refusalWhileDisabled();
    if (disabled != null) {
      return disabled;
    }
    Response invalidState = DoiMinting.refusalOfState(state);
    if (invalidState != null) {
      return invalidState;
    }
    c.must(c.user()).have(CedarPermission.TEMPLATE_READ);
    return minting.register(c, sourceArtifactId, state, dataCiteInstance);
  }

  private static final String UNAUTHORIZED = "The request carries no valid credentials";
  private static final String NOT_ACCEPTABLE = "The request does not accept a JSON response";
  private static final String RATE_LIMITED = "The deployment limits each user's request rate, and the caller "
      + "exceeded it";
  private static final String INTERNAL_ERROR = "Internal server error";
  private static final String UNAVAILABLE = "The graph database or another service the request depends on is "
      + "unavailable";
  private static final String DATACITE_FAILED = "DataCite could not be reached, failed, refused the repository "
      + "credentials this server holds, or limited this deployment's requests (upstreamServerError, with "
      + "DataCite's errors in objects.dataCiteErrors when it gave any).";
  private static final String ALREADY_HAS_DOI = "The artifact already has a findable DOI (doiAlreadyExists), which "
      + "parameters.doi names. When DataCite has published it and the artifact does not yet carry it, "
      + "parameters.reconciliationRequired is true.";
}
