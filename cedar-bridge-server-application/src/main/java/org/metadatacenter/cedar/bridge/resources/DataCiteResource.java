package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.util.http.CedarError;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance;
import org.metadatacenter.cedar.bridge.resource.datacite.*;
import org.metadatacenter.cedar.bridge.resource.datacite.form.Attributes;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSchema;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.constant.LinkedData;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.id.CedarArtifactId;
import org.metadatacenter.id.CedarFQResourceId;
import org.metadatacenter.id.CedarTemplateId;
import org.metadatacenter.model.BiboStatus;
import org.metadatacenter.model.CedarResourceType;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.folderserver.basic.FolderServerArtifact;
import org.metadatacenter.model.request.ResourceType;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.FolderServiceSession;
import org.metadatacenter.server.ResourcePermissionServiceSession;
import org.metadatacenter.server.security.model.auth.CedarPermission;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.constant.CedarQueryParameters.QP_SOURCE_ARTIFACT_ID;
import static org.metadatacenter.rest.assertion.GenericAssertions.LoggedIn;

@Path("/datacite")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "DOIs")
@SecurityRequirement(name = "api_key")
public class DataCiteResource extends CedarMicroserviceResource {

  private static final Logger log = LoggerFactory.getLogger(DataCiteResource.class);

  private final String endpointUrl;
  private final String templateId;
  private final boolean dataciteEnabled;
  private final String basicAuth;
  private final DataCiteHttpClient httpClient;
  private final DoiAnnotationWriter doiAnnotationWriter;
  protected final org.metadatacenter.bridge.CedarDataServices dataServices;

  public DataCiteResource(CedarConfig cedarConfig) {
    this(cedarConfig, org.metadatacenter.bridge.CedarDataServices.getInstance());
  }

  public DataCiteResource(CedarConfig cedarConfig, org.metadatacenter.bridge.CedarDataServices dataServices) {
    this(cedarConfig, dataServices, new DataCiteHttpClient(cedarConfig.getBridgeConfig().getDataCite()),
        ProxyUtil::proxyPost);
  }

  DataCiteResource(CedarConfig cedarConfig, org.metadatacenter.bridge.CedarDataServices dataServices,
                   DataCiteHttpClient httpClient, DoiAnnotationWriter doiAnnotationWriter) {
    super(cedarConfig);
    this.dataServices = dataServices;
    var dataCiteConfig = cedarConfig.getBridgeConfig().getDataCite();
    String repositoryId = dataCiteConfig.getRepositoryId();
    String password = dataCiteConfig.getPassword();
    this.endpointUrl = dataCiteConfig.getEndpointUrl();
    this.templateId = dataCiteConfig.getTemplateId();
    this.dataciteEnabled = dataCiteConfig.isEnabled();
    this.basicAuth = Base64.getEncoder().encodeToString(
        (repositoryId + ":" + password).getBytes(StandardCharsets.UTF_8));
    this.httpClient = httpClient;
    this.doiAnnotationWriter = doiAnnotationWriter;
  }

  @GET
  @Timed
  @Path("/get-doi-metadata/{id}")
  @Operation(summary = "Get a DOI's metadata from DataCite",
      description = "Fetch what DataCite holds for a DOI, including affiliation detail. The status "
          + "is DataCite's own, so a DOI DataCite does not know comes back as its 404 rather than "
          + "this server's. DataCite integration can be switched off by configuration, and every route here answers 400 when it is.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "The DOI's metadata, as DataCite returned it"),
      @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "DataCite integration is disabled"),
      @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "DataCite holds no such DOI; the body is DataCite's own"),
      @ApiResponse(responseCode = "502", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "DataCite could not be reached"),
      @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Internal server error")
  })
  public Response getDOIMetadata(
      @Parameter(description = "The DOI as a URL. Example: https://doi.org/10.82658/abcd-1234",
          required = true)
      @PathParam(PP_ID) String doiIdUrl) throws CedarException {
    CedarRequestContext c = buildRequestContext();
    c.must(c.user()).be(LoggedIn);

    if (!dataciteEnabled) {
      return CedarResponse
          .badRequest()
          .errorKey(CedarErrorKey.DATACITE_DOI_DISABLED)
          .errorMessage("DataCite DOI integration is disabled")
          .build();
    }

    try {
      //Get the doi from doiName
      URI doiUrl = new URI(doiIdUrl);
      String doi = doiUrl.getPath();

      String endpointUrl = cedarConfig.getBridgeConfig().getDataCite().getEndpointUrl() + doi + DataciteConstants.QUERY_AFFILIATION;
      URI uri = URI.create(endpointUrl);

      HttpRequest.Builder httpRequest = HttpRequest.newBuilder(uri)
          .header(HttpConstants.HTTP_HEADER_AUTHORIZATION, DataciteConstants.BASIC + basicAuth)
          .GET();

      // Send HTTP httpRequest and get response
      HttpResponse<String> httpResponse = httpClient.send(httpRequest);
      int statusCode = httpResponse.statusCode();
      String jsonResponse = httpResponse.body();
      JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);

      // Deserialize DataCite response json file to DataCiteRequest Class
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      DataCiteSchema dataCiteResponse = mapper.readValue(jsonResponse, DataCiteSchema.class);

      return Response.status(statusCode).entity(jsonResource).build();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return upstreamFailure("The DataCite metadata request was interrupted", e);
    } catch (IOException e) {
      return upstreamFailure("The DataCite metadata request failed", e);
    } catch (Exception e) {
      return CedarResponse.internalServerError().exception(e).build();
    }
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
      @ApiResponse(responseCode = "200", description = "The template, the artifact, and the draft or pre-filled metadata"),
      @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "DataCite integration is disabled, or the artifact is not eligible for a DOI"),
      @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Unauthorized"),
      @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "The caller lacks the template read permission"),
      @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "The artifact already has a findable DOI"),
      @ApiResponse(responseCode = "502", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "DataCite could not be reached"),
      @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Internal server error")
  })
  public Response createDOIStart(
      @Parameter(description = "Identifier of the artifact the DOI is for.", required = true)
      @QueryParam(QP_SOURCE_ARTIFACT_ID) String sourceArtifactId) throws CedarException {
    CedarRequestContext c = buildRequestContext();
    String userID = c.getCedarUser().getId();
    c.must(c.user()).be(LoggedIn);

    if (!dataciteEnabled) {
      return CedarResponse
          .badRequest()
          .errorKey(CedarErrorKey.DATACITE_DOI_DISABLED)
          .errorMessage("DataCite DOI integration is disabled")
          .build();
    }

    c.must(c.user()).have(CedarPermission.TEMPLATE_READ);

    Map<String, Object> response = new HashMap<>();

    String dataCiteTemplateIdS = cedarConfig.getBridgeConfig().getDataCite().getTemplateId();
    CedarTemplateId dataCiteTemplateId = CedarTemplateId.build(dataCiteTemplateIdS);
    String url1 = microserviceUrlUtil.getArtifact().getArtifactTypeWithId(CedarResourceType.TEMPLATE, dataCiteTemplateId);
    JsonNode dataCiteTemplateProxyJson = ProxyUtil.proxyGetBodyAsJsonNode(url1, c);

    CedarFQResourceId sourceArtifactResourceId = CedarFQResourceId.build(sourceArtifactId);
    CedarArtifactId sourceArtifactIdTyped = CedarArtifactId.build(sourceArtifactId, sourceArtifactResourceId.getType());
    String url2 = microserviceUrlUtil.getArtifact().getArtifactTypeWithId(sourceArtifactResourceId.getType(), sourceArtifactIdTyped);
    JsonNode sourceArtifactProxyJson = ProxyUtil.proxyGetBodyAsJsonNode(url2, c);

    Response eligibilityError = validateSourceArtifactForDoi(c, sourceArtifactResourceId.getType(),
        sourceArtifactIdTyped, sourceArtifactProxyJson);
    if (eligibilityError != null) {
      return eligibilityError;
    }

    //Check if the source artifact has a DOI
    String doiName = getFindableDoi(sourceArtifactProxyJson);
    if (doiName != null) {
      String hasDoiError = String.format("The %s(%s) already has a DOI: %s", sourceArtifactResourceId.getType().getValue(), sourceArtifactId, doiName);
      return CedarResponse
          .conflict()
          .errorMessage(hasDoiError)
          .errorKey(CedarErrorKey.DOI_ALREADY_EXISTS)
          .parameter("doi", doiName)
          .build();
    }

    //Check if there is an already started DOI metadata instance. If yes, load it as well
    //Use publisher and openView Url as parameters to send query to DataCite
    try {
      Response httpResponse = getDraftDoiMetadata(sourceArtifactId, cedarConfig);
      HashMap<String, Object> entity = (HashMap<String, Object>) httpResponse.getEntity();
      boolean hasDraftDoi = (boolean) entity.get(DataciteConstants.HAS_DRAFT_DOI);
      JsonNode dataNode = (JsonNode) entity.get(DataciteConstants.DRAFT_METADATA);

      if (hasDraftDoi) {
        // if draft DOI is returned, convert the data from dataCite JSON to Cedar Instance JSON-LD, and put it into response
        JsonNode attributesNode = dataNode.get(0).get(DataciteConstants.ATTRIBUTES);
        JsonNode draftDoi = attributesNode.get(DataciteConstants.DOI);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        Attributes existingDoiMetadata = mapper.treeToValue(attributesNode, Attributes.class);

        // Pass the value from dataCiteResponse to cedarDataCiteInstance
        MetadataInstance cedarExistingDoiMetadata = DataCiteMetadataParser.parseDataCiteSchema(existingDoiMetadata, userID, cedarConfig);
        response.put(DataciteConstants.EXISTING_DATACITE_METADATA, cedarExistingDoiMetadata);
        response.put(DataciteConstants.DRAFT_DOI, draftDoi);
      } else {
        // if draft DOI is not available, set the url and resourceType fields
        MetadataInstance defaultInstance = GenerateMetadataInstance.getDefaultInstance(sourceArtifactId, userID, templateId, cedarConfig);
        response.put(DataciteConstants.EXISTING_DATACITE_METADATA, defaultInstance);
        response.put(DataciteConstants.DRAFT_DOI, null);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return upstreamFailure("The DataCite draft lookup was interrupted", e);
    } catch (IOException e) {
      return upstreamFailure("The DataCite draft lookup failed", e);
    }
    response.put(DataciteConstants.SOURCE_ARTIFACT_TYPE, sourceArtifactResourceId.getType().getValue());
    response.put(DataciteConstants.SOURCE_ARTIFACT, sourceArtifactProxyJson);
    response.put(DataciteConstants.DATACITE_TEMPLATE, dataCiteTemplateProxyJson);

    return CedarResponse.ok().entity(response).build();
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
          + "DataCite template first, and a failure is returned rather than half-registered. "
          + "DataCite integration can be switched off by configuration, and every route here answers 400 when it is.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "The DOI, as registered"),
      @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = CedarError.class)),
          description = "DataCite integration is disabled, `state` is neither draft nor publish, the "
              + "artifact is not eligible, or the metadata failed validation"),
      @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Unauthorized"),
      @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "The caller lacks the template read permission"),
      @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "The artifact already has a findable DOI"),
      @ApiResponse(responseCode = "502", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "DataCite could not be reached"),
      @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = CedarError.class)), description = "Internal server error")
  })
  public Response createDOI(
      @Parameter(description = "Identifier of the artifact the DOI is for.", required = true)
      @QueryParam(QP_SOURCE_ARTIFACT_ID) String sourceArtifactId,
      @Parameter(description = "`draft` for a DOI that can still be changed, `publish` for a "
          + "findable and permanent one.", required = true)
      @QueryParam("state") String state,
      JsonNode dataCiteInstance) throws CedarException, IOException, InterruptedException {
    CedarRequestContext c = buildRequestContext();

    c.must(c.user()).be(LoggedIn);

    if (!dataciteEnabled) {
      return CedarResponse
          .badRequest()
          .errorKey(CedarErrorKey.DATACITE_DOI_DISABLED)
          .errorMessage("DataCite DOI integration is disabled")
          .build();
    }

    Response stateError = validateDoiState(state);
    if (stateError != null) {
      return stateError;
    }

    c.must(c.user()).have(CedarPermission.TEMPLATE_READ);

    //Check if the source artifact has a DOI
    CedarFQResourceId sourceArtifactResourceId = CedarFQResourceId.build(sourceArtifactId);
    CedarResourceType sourceArtifactType = sourceArtifactResourceId.getType();
    CedarArtifactId sourceArtifactIdTyped = CedarArtifactId.build(sourceArtifactId, sourceArtifactType);
    String url = microserviceUrlUtil.getArtifact().getArtifactTypeWithId(sourceArtifactType,
        sourceArtifactIdTyped);
    JsonNode sourceArtifactProxyJson = ProxyUtil.proxyGetBodyAsJsonNode(url, c);
    Response eligibilityError = validateSourceArtifactForDoi(c, sourceArtifactType, sourceArtifactIdTyped,
        sourceArtifactProxyJson);
    if (eligibilityError != null) {
      return eligibilityError;
    }
    String findableDoiName = getFindableDoi(sourceArtifactProxyJson);
    if (findableDoiName != null) {
      String hasDoiError = String.format("The %s(%s) already has a DOI: %s", sourceArtifactResourceId.getType().getValue(), sourceArtifactId, findableDoiName);
      return CedarResponse
          .conflict()
          .errorKey(CedarErrorKey.DOI_ALREADY_EXISTS)
          .parameter("doi", findableDoiName)
          .errorMessage(hasDoiError)
          .build();
    }

    Map<String, Object> response = new HashMap<>();

    Pair<Boolean, JsonNode> validationResultPair;
    try {
      validationResultPair = validateCEDARInstance(c, templateId, dataCiteInstance);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return upstreamFailure("CEDAR instance validation was interrupted", e);
    } catch (IOException e) {
      return upstreamFailure("CEDAR instance validation failed", e);
    }
    Response validationError = validationFailure(validationResultPair);
    if (validationError != null) {
      return validationError;
    }

    // Get DOI request json
    String jsonData = "";
    if (dataCiteInstance != null && !dataCiteInstance.isEmpty()) {
      try {
        jsonData = getRequestJson(dataCiteInstance, sourceArtifactId, state);
      } catch (Exception e) {
        return CedarResponse
            .badRequest()
            .exception(e)
            .errorMessage(e.getMessage())
            .errorKey(CedarErrorKey.INVALID_INPUT)
            .build();
      }
    }

    try {
      //Send HTTP GET request to DataCite to recheck the draft DOI
      Response getDraftDoiResponse = getDraftDoiMetadata(sourceArtifactId, cedarConfig);
      //Send PUT request if draft DOI exists, otherwise send POST request
      HashMap<String, Object> entity = (HashMap<String, Object>) getDraftDoiResponse.getEntity();
      boolean hasDraftDoi = (boolean) entity.get(DataciteConstants.HAS_DRAFT_DOI);
      JsonNode dataNode = (JsonNode) entity.get(DataciteConstants.DRAFT_METADATA);
      HttpResponse<String> putOrPostResponse = null;
      if (hasDraftDoi) {
        //Send HTTP PUT request to DataCite and get response
        JsonNode attributesNode = dataNode.get(0).get(DataciteConstants.ATTRIBUTES);
        String draftDoi = attributesNode.get(DataciteConstants.DOI).toString();
        putOrPostResponse = httpDataCitePutCall(draftDoi, basicAuth, jsonData);
      } else {
        //Send HTTP POST request to DataCite and get response
        putOrPostResponse = httpDataCitePostCall(endpointUrl, basicAuth, jsonData);
      }
      int statusCode = putOrPostResponse.statusCode();
      //If the Put or Post response status code is 200 or 201
      String jsonResponse = putOrPostResponse.body();
      try {
        if (statusCode == HttpConstants.CREATED || statusCode == HttpConstants.OK) {
          // Deserialize DataCite response json file to DataCiteRequest Class
          ObjectMapper mapper = new ObjectMapper();
          // DataCiteSchema dataCiteResponse = mapper.readValue(jsonResponse, DataCiteSchema.class);
          JsonNode jsonNode = mapper.readTree(jsonResponse);
          String id = jsonNode.get("data").get("id").asText();
          String doiName = DataciteConstants.DOI_PREFIX + id;
          URI uri = URI.create(doiName);
          response.put(DataciteConstants.DOI_ID, id);
          response.put(DataciteConstants.DOI_NAME, doiName);
          response.put(DataciteConstants.DATACITE_RESPONSE, jsonResponse);

          //If a DOI is minted, add _annotation entry to sourceArtifactProxyJson and then put artifact
          if (DataciteConstants.PUBLISH.equals(state)) {
            // Add doi of _annotation
            // Put the updated source artifact JSON
            String urlResource = microserviceUrlUtil.getResource().getCommandDOIUpdate();
            Response reconciliationError = recordPublishedDoi(urlResource, c, sourceArtifactId, doiName);
            if (reconciliationError != null) {
              return reconciliationError;
            }
          }
          return CedarResponse
              .created(uri)
              .entity(response)
              .build();
        } //If the status code is 422, return what DataCite returns
        else if (statusCode == CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode()) {
          JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);
          JsonNode errorsNode = jsonResource.get("errors");
          StringBuilder errorMessageBuilder = new StringBuilder();
          for (JsonNode errorNode : errorsNode) {
            JsonNode sourceNode = errorNode.get("source");
            if (sourceNode != null && sourceNode.isTextual()) {
              String source = sourceNode.asText();
              errorMessageBuilder.append(source).append(": ");
            }
            JsonNode titleNode = errorNode.get("title");
            if (titleNode != null && titleNode.isTextual()) {
              String title = titleNode.asText();
              errorMessageBuilder.append(title).append("\n");
            }
          }
          return CedarResponse
              .badRequest()
              .errorMessage(errorMessageBuilder.toString().trim())
              .errorKey(CedarErrorKey.INVALID_INPUT)
              .build();
        } else {
          //DOI is not created or updated successfully, return what DataCite returns
          JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);
          return Response
              .status(statusCode)
              .entity(jsonResource)
              .build();
        }
      } catch (Exception e) {
        return CedarResponse
            .internalServerError()
            .errorMessage(e.getMessage())
            .exception(e)
            .build();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return upstreamFailure("The DataCite DOI request was interrupted; publication state is unknown", e);
    } catch (IOException e) {
      return upstreamFailure("The DataCite DOI request failed; publication state is unknown", e);
    }
  }

  Response validateDoiState(String state) {
    if (DataciteConstants.DRAFT.equals(state) || DataciteConstants.PUBLISH.equals(state)) {
      return null;
    }
    return CedarResponse.badRequest()
        .errorKey(CedarErrorKey.INVALID_INPUT)
        .errorMessage("The DOI state must be 'draft' or 'publish'")
        .parameter("state", state)
        .build();
  }

  Response validationFailure(Pair<Boolean, JsonNode> validationResult) {
    if (Boolean.TRUE.equals(validationResult.getLeft())) {
      return null;
    }
    return CedarResponse.badRequest()
        .errorKey(CedarErrorKey.INVALID_INPUT)
        .errorMessage("The DataCite metadata instance is invalid")
        .object("validationResult", validationResult.getRight())
        .build();
  }

  Response recordPublishedDoi(String annotationUrl, CedarRequestContext context, String sourceArtifactId,
                              String doiName) {
    ObjectNode commandContent = JsonMapper.MAPPER.createObjectNode();
    commandContent.put(LinkedData.ID, sourceArtifactId);
    commandContent.put(DataciteConstants.DOI, doiName);

    ClassicHttpResponse annotationResponse = null;
    try {
      annotationResponse = doiAnnotationWriter.post(annotationUrl, context, commandContent.toString());
      int statusCode = annotationResponse.getCode();
      if (Response.Status.Family.familyOf(statusCode) == Response.Status.Family.SUCCESSFUL) {
        return null;
      }
      log.error("DOI {} was minted for {}, but CEDAR annotation write-back returned status {}",
          doiName, sourceArtifactId, statusCode);
      return reconciliationFailure(sourceArtifactId, doiName, statusCode, null);
    } catch (CedarProcessingException e) {
      return reconciliationFailure(sourceArtifactId, doiName, null, e);
    } finally {
      if (annotationResponse != null) {
        EntityUtils.consumeQuietly(annotationResponse.getEntity());
      }
    }
  }

  private Response reconciliationFailure(String sourceArtifactId, String doiName, Integer annotationStatus,
                                         Exception exception) {
    CedarResponse.CedarResponseBuilder response = CedarResponse.badGateway()
        .errorMessage("The DOI was minted at DataCite but could not be recorded in CEDAR; reconciliation is required")
        .parameter("doi", doiName)
        .parameter("sourceArtifactId", sourceArtifactId)
        .parameter("reconciliationRequired", true);
    if (annotationStatus != null) {
      response.parameter("annotationStatus", annotationStatus);
    }
    if (exception != null) {
      response.exception(exception);
    }
    return response.build();
  }

  private Response upstreamFailure(String message, Exception exception) {
    return CedarResponse.badGateway()
        .errorMessage(message)
        .exception(exception)
        .build();
  }

  Response validateSourceArtifactForDoi(CedarRequestContext context, CedarResourceType sourceArtifactType,
                                        CedarArtifactId sourceArtifactId, JsonNode sourceArtifactJson)
      throws CedarException {
    ResourcePermissionServiceSession permissionSession = dataServices.getResourcePermissionServiceSession(context);
    if (!permissionSession.userHasWriteAccessToResource(sourceArtifactId)) {
      return CedarResponse
          .unauthorized()
          .errorKey(CedarErrorKey.NO_WRITE_ACCESS_TO_ARTIFACT)
          .errorMessage("You do not have write access to the artifact")
          .parameter(DataciteConstants.RESOURCE_ID, sourceArtifactId)
          .build();
    }

    FolderServiceSession folderSession = dataServices.getFolderServiceSession(context);
    FolderServerArtifact folderServerResource = folderSession.findArtifactById(sourceArtifactId);
    if (folderServerResource == null) {
      return CedarResponse
          .notFound()
          .errorMessage("The source artifact is not found")
          .id(sourceArtifactId)
          .build();
    }
    if (!(folderServerResource.isOpen() || folderSession.isArtifactOpenImplicitly(sourceArtifactId))) {
      return CedarResponse
          .badRequest()
          .errorMessage("Please make the " + sourceArtifactType.getValue().toLowerCase() + " open to create a DOI")
          .build();
    }

    JsonNode publicationStatus = sourceArtifactJson.get(ModelNodeNames.BIBO_STATUS);
    if (sourceArtifactType == CedarResourceType.TEMPLATE
        && (publicationStatus == null
        || !Objects.equals(publicationStatus.asText(), BiboStatus.PUBLISHED.getValue()))) {
      return CedarResponse
          .badRequest()
          .errorMessage("Please publish the template to create a DOI")
          .build();
    }
    return null;
  }

  /**
   * This function check if CEDAR DataCite Instance is valid
   */
  private Pair<Boolean, JsonNode> validateCEDARInstance(CedarRequestContext c, String templateId,
                                                        JsonNode dataCiteInstance)
      throws InterruptedException, IOException {
    // Get Scheme JSONObject and CEDAR DataCite Instance JSONObject
    JsonNode schemaResponse = getCEDARTemplate(c, templateId);

    ObjectNode validationBody = JsonNodeFactory.instance.objectNode();
    validationBody.put("schema", schemaResponse);
    validationBody.put("instance", dataCiteInstance);

    // Construct API endpoint URL
    String endpointUrl = microserviceUrlUtil.getArtifact().getValidateCommand(ResourceType.INSTANCE.getValue());

    // Set authorization header
    String apiKey = c.getCedarUser().getFirstApiKeyAuthHeader();

    URI uri = URI.create(endpointUrl);
    HttpRequest.Builder request = HttpRequest.newBuilder(uri)
        .header(DataciteConstants.CONTENT_TYPE, DataciteConstants.APPLICATION_JSON)
        .header(HttpConstants.HTTP_HEADER_AUTHORIZATION, apiKey)
        .header(HttpConstants.HTTP_HEADER_ACCEPT, DataciteConstants.APPLICATION_JSON)
        .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(validationBody)));

    // Call CEDAR validation endpoint and get the httpResponse
    HttpResponse<String> httpResponse = httpClient.send(request);

    // Parse the httpResponse body as a JSONObject
    String jsonResponse = httpResponse.body();
    JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);

    // Check httpResponse status code
    int statusCode = httpResponse.statusCode();

    if (statusCode != HttpConstants.OK) {
      throw new IOException("CEDAR instance validation returned HTTP " + statusCode);
    }
    String validates = jsonResource.get("validates").asText();
    if (validates.equals("true")) {
      // The resource is valid, handle it here
      return Pair.of(true, jsonResource);
    } else {
      // The resource is invalid, handle the errors and warnings here
      return Pair.of(false, jsonResource);
    }
  }

  /**
   * This function transfer JSON-LD format to JSON scheme that used to call DataCite API
   *
   * @return DataCite requested JSON schema
   */
  private String getRequestJson(JsonNode metadata, String sourceArtifactId, String state) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    DataCiteSchema dataCiteSchema = new DataCiteSchema();
    try {
      // Deserialize JSON-LD to MetadataInstance Class
      String metadataString = metadata.toString();
      MetadataInstance cedarInstance = mapper.readValue(metadataString, MetadataInstance.class);

      // Pass the value from dataCiteInstance to dataCiteRequest
      CedarInstanceParser.parseCedarInstance(cedarInstance, dataCiteSchema, sourceArtifactId, state, cedarConfig);

      //Serialize DataCiteRequest Class to json
      String requestJsonString = mapper.writeValueAsString(dataCiteSchema);
      return requestJsonString;

    } catch (IOException | DataCiteInstanceValidationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This function send HTTP Post request to DataCite to create a DOI
   *
   * @param endPointUrl URL of API call
   * @param basicAuth   Authentication at heater
   * @param jsonData    DataCite metadata instance JSON
   * @return Http POST Response from DataCite
   */
  private HttpResponse<String> httpDataCitePostCall(String endPointUrl, String basicAuth, String jsonData)
      throws IOException, InterruptedException {
    URI uri = URI.create(endPointUrl);
    HttpRequest.Builder request = HttpRequest.newBuilder(uri)
        .header(DataciteConstants.CONTENT_TYPE, DataciteConstants.APPLICATION_VND_API_JSON)
        .header(HttpConstants.HTTP_HEADER_AUTHORIZATION, DataciteConstants.BASIC + basicAuth)
        .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonData)));
    return httpClient.send(request);
  }

  /**
   * This function send HTTP Put request to DataCite to update DOI's metadata
   *
   * @param draftDoi  String of draft DOI
   * @param basicAuth Authentication at heater
   * @param jsonData  DataCite metadata instance JSON
   * @return Http Put Response from DataCite
   */
  private HttpResponse<String> httpDataCitePutCall(String draftDoi, String basicAuth, String jsonData) throws IOException, InterruptedException {
    String url = endpointUrl + "/" + draftDoi.replace("\"", "");
    URI uri = URI.create(url);
    HttpRequest.Builder request = HttpRequest.newBuilder(uri)
        .header(DataciteConstants.CONTENT_TYPE, DataciteConstants.APPLICATION_VND_API_JSON)
        .header(HttpConstants.HTTP_HEADER_AUTHORIZATION, DataciteConstants.BASIC + basicAuth)
        .PUT(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonData)));
    return httpClient.send(request);
  }

  /**
   * This function send HTTP Get request to DataCite to query the draft DOI
   *
   * @param sourceArtifactId ID of source template or instance for which you want to create the DOI
   * @return CedarResponse which contains draft DOI's metadata and boolean value of if draft DOI exists
   */
  private Response getDraftDoiMetadata(String sourceArtifactId, CedarConfig cedarConfig) throws IOException, InterruptedException {
    Map<String, Object> response = new HashMap<>();
    String openViewUrl = GenerateOpenViewUrl.getOpenViewUrl(sourceArtifactId, cedarConfig);
    String encodedOpenViewUrl = URLEncoder.encode(openViewUrl, StandardCharsets.UTF_8);
    String queryUrl = endpointUrl + DataciteConstants.QUERY_AFFILIATION + DataciteConstants.QUERY_DETAIL + DataciteConstants.QUERY_PUBLISHER + "%20AND%20url:%22" + encodedOpenViewUrl + "%22";
    URI uri = URI.create(queryUrl);
    HttpRequest.Builder httpRequest = HttpRequest.newBuilder(uri)
        .header(HttpConstants.HTTP_HEADER_AUTHORIZATION, DataciteConstants.BASIC + basicAuth)
        .GET();
    HttpResponse<String> httpResponse = httpClient.send(httpRequest);
    if (httpResponse.statusCode() != HttpConstants.OK) {
      throw new IOException("DataCite DOI lookup returned HTTP " + httpResponse.statusCode());
    }
    String jsonResponse = httpResponse.body();
    JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);
    JsonNode dataNode = jsonResource.get("data");
    boolean hasDraftDoi = hasDraftDoi(dataNode);
    response.put(DataciteConstants.DRAFT_METADATA, dataNode);
    response.put(DataciteConstants.HAS_DRAFT_DOI, hasDraftDoi);
    return CedarResponse.ok().entity(response).build();
  }

  /**
   * This function check if there is a draft DOI exists
   */
  private Boolean hasDraftDoi(JsonNode dataNode) {
    return dataNode != null && !dataNode.isEmpty();
  }

  private String getFindableDoi(JsonNode sourceArtifactProxyJson) {
    String doiName = null;
    if (sourceArtifactProxyJson.has(ModelNodeNames.ANNOTATIONS)) {
      JsonNode annotationsNode = sourceArtifactProxyJson.get(ModelNodeNames.ANNOTATIONS);
      if (annotationsNode.has(ModelNodeNames.DATACITE_DOI_URI)) {
        JsonNode doiNameNode = annotationsNode.get(ModelNodeNames.DATACITE_DOI_URI);
        doiName = doiNameNode.get(LinkedData.ID).textValue();
      }
    }
    return doiName;
  }

  /**
   * This function get JSON file of a CEDAR template
   */
  private JsonNode getCEDARTemplate(CedarRequestContext c, String templateId) {
    try {
      CedarTemplateId cedarTemplateId = CedarTemplateId.build(templateId);
      String artifactServerUrl = microserviceUrlUtil.getArtifact().getArtifactTypeWithId(CedarResourceType.TEMPLATE, cedarTemplateId);

      HttpEntity currentTemplateEntity = ProxyUtil.proxyGet(artifactServerUrl, c).getEntity();
      String currentTemplateEntityContent = EntityUtils.toString(currentTemplateEntity, CharEncoding.UTF_8);
      return JsonMapper.MAPPER.readTree(currentTemplateEntityContent);
    } catch (IOException | ParseException | CedarProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @FunctionalInterface
  interface DoiAnnotationWriter {
    ClassicHttpResponse post(String url, CedarRequestContext context, String content)
        throws CedarProcessingException;
  }
}
