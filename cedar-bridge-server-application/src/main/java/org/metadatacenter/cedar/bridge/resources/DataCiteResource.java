package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.cedar.bridge.resource.CEDARProperties.CEDARDataCiteInstance;
import org.metadatacenter.cedar.bridge.resource.CedarInstanceParser;
import org.metadatacenter.cedar.bridge.resource.DataCiteInstanceValidationException;
import org.metadatacenter.cedar.bridge.resource.DataCiteMetaDataParser;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.DataCiteSchema;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarPermissionException;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.id.CedarArtifactId;
import org.metadatacenter.id.CedarFQResourceId;
import org.metadatacenter.id.CedarTemplateId;
import org.metadatacenter.model.BiboStatus;
import org.metadatacenter.model.CedarResourceType;
import org.metadatacenter.model.folderserver.basic.FolderServerArtifact;
import org.metadatacenter.model.request.ResourceType;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.FolderServiceSession;
import org.metadatacenter.server.ResourcePermissionServiceSession;
import org.metadatacenter.server.security.model.auth.CedarPermission;
import org.metadatacenter.server.service.TemplateInstanceService;
import org.metadatacenter.server.service.TemplateService;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
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
public class DataCiteResource extends CedarMicroserviceResource {

  private static TemplateService<String, JsonNode> templateService;
  private static TemplateInstanceService<String, JsonNode> templateInstanceService;


  public DataCiteResource(CedarConfig cedarConfig, TemplateService<String, JsonNode> templateService,
                          TemplateInstanceService<String, JsonNode> templateInstanceService) {
    super(cedarConfig);
    DataCiteResource.templateService = templateService;
    DataCiteResource.templateInstanceService = templateInstanceService;
  }

  @GET
  @Timed
  @Path("/get-doi-metadata/{id}")
  public Response getDOIMetadata(@PathParam(PP_ID) String doiIdUrl) throws CedarException {

    CedarRequestContext c = buildRequestContext();

//    c.must(c.user()).be(LoggedIn);

    try {
      //Get the doi from doiName
      URI doiUrl = new URI(doiIdUrl);
      String doi = doiUrl.getPath();

      String endpointUrl = cedarConfig.getBridgeConfig().getDataCite().getEndpointUrl() + doi + "?affiliation=true";
      URI uri = URI.create(endpointUrl);

      // Create HTTP client
      HttpClient client = HttpClient.newBuilder().build();

      // Create HTTP httpRequest with JSON body and basic authentication
      HttpRequest httpRequest = HttpRequest.newBuilder(uri)
          .GET()
          .build();

      // Send HTTP httpRequest and get response
      HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      int statusCode = httpResponse.statusCode();
      String jsonResponse = httpResponse.body();
      JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);

      // Deserialize DataCite response json file to DataCiteRequest Class
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      DataCiteSchema dataCiteResponse = mapper.readValue(jsonResponse, DataCiteSchema.class);

      String dataCiteResponseString = mapper.writeValueAsString(dataCiteResponse);
      System.out.println("DataCiteResponse converted to Data Cite Schema Json: " + dataCiteResponseString);

      // Pass the value from dataCiteResponse to cedarDataCiteInstance
      CEDARDataCiteInstance cedarDataCiteInstance = new CEDARDataCiteInstance();
      DataCiteMetaDataParser.parseDataCiteSchema(dataCiteResponse, cedarDataCiteInstance);

      //Serialize DataCiteRequest Class to json
      String cedarDataCiteInstanceString = mapper.writeValueAsString(cedarDataCiteInstance);
      System.out.println("Converted Cedar DataCite Instance JSON-LD: " + cedarDataCiteInstanceString);

      return Response.status(statusCode).entity(jsonResource).build();
    } catch (Exception e) {
      return CedarResponse.internalServerError().exception(e).build();
    }
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Path("/create-doi")
  public Response createDOIStart(@QueryParam(QP_SOURCE_ARTIFACT_ID) String sourceArtifactId) throws CedarException {
    CedarRequestContext c = buildRequestContext();
    c.must(c.user()).be(LoggedIn);
    c.must(c.user()).have(CedarPermission.TEMPLATE_READ);

    Map<String, Object> response = new HashMap<>();

    String dataCiteTemplateIdS = cedarConfig.getBridgeConfig().getDataCite().getTemplateId();
    CedarTemplateId dataCiteTemplateId = CedarTemplateId.build(dataCiteTemplateIdS);
    String url1 = microserviceUrlUtil.getArtifact().getArtifactTypeWithId(CedarResourceType.TEMPLATE,
        dataCiteTemplateId);
    JsonNode dataCiteTemplateProxyJson = ProxyUtil.proxyGetBodyAsJsonNode(url1, c);

    CedarFQResourceId sourceArtifactResourceId = CedarFQResourceId.build(sourceArtifactId);
    CedarArtifactId sourceArtifactIdTyped = CedarArtifactId.build(sourceArtifactId, sourceArtifactResourceId.getType());
    String url2 = microserviceUrlUtil.getArtifact().getArtifactTypeWithId(sourceArtifactResourceId.getType(),
        sourceArtifactIdTyped);
    JsonNode sourceArtifactProxyJson = ProxyUtil.proxyGetBodyAsJsonNode(url2, c);

    // check if user has write permission to the source artifact
    ResourcePermissionServiceSession permissionSession = CedarDataServices.getResourcePermissionServiceSession(c);
    boolean hasWriteAccess = permissionSession.userHasWriteAccessToResource(sourceArtifactIdTyped);
    if (!hasWriteAccess) {
      return CedarResponse
          .unauthorized()
          .errorKey(CedarErrorKey.NO_WRITE_ACCESS_TO_ARTIFACT)
          .errorMessage("You do not have write access to the artifact")
          .parameter("resourceId", sourceArtifactIdTyped)
          .build();
    }

    // check if the source artifact is open
    FolderServiceSession folderSession = CedarDataServices.getFolderServiceSession(c);
    FolderServerArtifact folderServerResource = folderSession.findArtifactById(sourceArtifactIdTyped);
    if (folderServerResource == null) {
      return CedarResponse
          .notFound()
          .errorMessage("The source artifact is not found")
          .id(sourceArtifactIdTyped)
          .build();
    } else if (!(folderServerResource.isOpen() || folderSession.isArtifactOpenImplicitly(sourceArtifactIdTyped))){
        return CedarResponse
            .badRequest()
            .errorMessage("Please make the " + sourceArtifactResourceId.getType().getValue().toLowerCase() + " open to create a DOI")
            .build();
      }

    // check if the source artifact is published (version) - if it is a template
    // "bibo:status": "bibo:published"
    // "pav:version": "0.0.1"
    if (sourceArtifactResourceId.getType() == CedarResourceType.TEMPLATE){
      if (!Objects.equals(sourceArtifactProxyJson.get("bibo:status").asText(), BiboStatus.PUBLISHED.getValue())){
        return CedarResponse
            .badRequest()
            .errorMessage("Please make the template publish to create a DOI")
            .build();
      }
    }

    //TODO:
    // later: check if the source artifact is published - if it is an instance
    // later: check if there is an already started DOI metadata instance. If yes, load it as well
    // If there are errors, send error response
    // If there are no errors, send:
    // - the DOI metadata template
    // - the source artifact
    // - later: the already started DOI metadata instance

    response.put("sourceArtifactType", sourceArtifactResourceId.getType().getValue());
    response.put("sourceArtifact", sourceArtifactProxyJson);
    response.put("dataCiteTemplate", dataCiteTemplateProxyJson);
    response.put("existingDataCiteMetadata", null);

    return CedarResponse.ok().entity(response).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Path("/create-doi")
  public Response createDOI(@QueryParam(QP_SOURCE_ARTIFACT_ID) String sourceArtifactId, JsonNode dataCiteInstance) throws CedarException, IOException, InterruptedException {
    CedarRequestContext c = buildRequestContext();

//    c.must(c.user()).be(LoggedIn);

    Map<String, Object> response = new HashMap<>();

    String repositoryID = cedarConfig.getBridgeConfig().getDataCite().getRepositoryId();
    String password = cedarConfig.getBridgeConfig().getDataCite().getPassword();
    String endpointUrl = cedarConfig.getBridgeConfig().getDataCite().getEndpointUrl();
    String templateId = cedarConfig.getBridgeConfig().getDataCite().getTemplateId();

    // Create basic authentication
    String basicAuth =
        Base64.getEncoder().encodeToString((repositoryID + ":" + password).getBytes(StandardCharsets.UTF_8));

    Pair<Boolean, JsonNode> validationResultPair = validateCEDARInstance(c, templateId, dataCiteInstance);
    boolean validates = validationResultPair.getLeft();
    JsonNode validationResult = validationResultPair.getRight();

    //Call CEDAR validation endpoint and continue if return true
    // if (validates){
    if (true) {
      try {
        // Get DOI request json
        String jsonData = "";
        if (dataCiteInstance != null && !dataCiteInstance.isEmpty()) {
          try {
            jsonData = getRequestJson(dataCiteInstance, sourceArtifactId);
          } catch (Exception e){
            return CedarResponse
                .badRequest()
                .errorMessage(e.getMessage())
                .errorKey(CedarErrorKey.INVALID_INPUT)
                .build();
          }
        }
        // Send HTTP request and get response
        HttpResponse<String> httResponse = httpDataCitePostCall(endpointUrl, basicAuth, jsonData);

        int statusCode = httResponse.statusCode();
        if (statusCode == HttpConstants.CREATED) {

          String jsonResponse = httResponse.body();

          // Deserialize DataCite response json file to DataCiteRequest Class
          ObjectMapper mapper = new ObjectMapper();
          DataCiteSchema dataCiteResponse = mapper.readValue(jsonResponse, DataCiteSchema.class);
          String id = dataCiteResponse.getData().getId();
          String doiName = "https://doi.org/" + id;
          URI uri = URI.create(doiName);
          response.put("doiId", id);
          response.put("doiName", doiName);
          response.put("dataCiteResponse", dataCiteResponse);
          return CedarResponse.created(uri).entity(response).build();
        } else {
          String jsonResponse = httResponse.body();
          JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);
          return Response.status(statusCode).entity(jsonResource).build();
        }
      } catch (Exception e) {
        return CedarResponse
            .internalServerError()
            .errorMessage(e.getMessage())
            .exception(e)
            .build();
      }
    } else {
      response.put("request", dataCiteInstance);
//      response.put("validationResult", validationResult);
//      response.put("validationResult", null);
      return CedarResponse
          .badRequest()
          .errorMessage("Validation Error")
          .entity(response)
          .build();
    }
  }

  /**
   * This function check if CEDAR DataCite Instance is valid
   */
  private Pair<Boolean, JsonNode> validateCEDARInstance(CedarRequestContext c, String templateId,
                                                        JsonNode dataCiteInstance) throws IOException,
      InterruptedException {
    // Get Scheme JSONObject and CEDAR DataCite Instance JSONObject
    JsonNode schemaResponse = getCEDARTemplate(c, templateId);

    ObjectNode validationBody = JsonNodeFactory.instance.objectNode();
    validationBody.put("schema", schemaResponse);
    validationBody.put("instance", dataCiteInstance);

    try {
      // Construct API endpoint URL
      String endpointUrl = microserviceUrlUtil.getArtifact().getValidateCommand(ResourceType.INSTANCE.getValue());

      // Set authorization header
      String apiKey = c.getCedarUser().getFirstApiKeyAuthHeader();

      URI uri = URI.create(endpointUrl);

      // Set up the HTTP client
      HttpClient client = HttpClient.newHttpClient();

      // Create HTTP request with JSON body and basic authentication
      HttpRequest request = HttpRequest.newBuilder(uri)
          .header("Content-Type", "application/json")
//              .header("Authorization", adminUserApiKey)
          .header("Authorization", apiKey)
          .header("Accept", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(validationBody)))
          .build();

      // Call CEDAR validation endpoint and get the httpResponse
      HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

      // Parse the httpResponse body as a JSONObject
      String jsonResponse = httpResponse.body();
      JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);
//      JSONObject responseJson = new JSONObject(httpResponse.body());

      // Check httpResponse status code
      int statusCode = httpResponse.statusCode();

      if (statusCode == HttpConstants.OK) {
        String validates = jsonResource.get("validates").asText();
        if (validates.equals("true")) {
          // The resource is valid, handle it here
          System.out.println("Resource is valid.");
          return Pair.of(true, jsonResource);
        } else {
          // The resource is invalid, handle the errors and warnings here
          return Pair.of(false, jsonResource);
        }
      } else {
        // The request failed
        return Pair.of(false, jsonResource);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This function transfer JSON-LD format to JSON scheme that used to call DataCite API
   *
   * @return DataCite requested JSON scheme
   */
  private static String getRequestJson(JsonNode metadata, String sourceArtifactId) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    DataCiteSchema dataCiteSchema = new DataCiteSchema();
    try {
      // Deserialize JSON-LD to CedarDataCiteInstance Class
      String metadataString = metadata.toString();
      CEDARDataCiteInstance cedarInstance = mapper.readValue(metadataString, CEDARDataCiteInstance.class);

      String cedarInstanceString = mapper.writeValueAsString(cedarInstance);
      System.out.println("Json Converted to DataCite Instance: " + cedarInstanceString);

      // Pass the value from dataCiteInstance to dataCiteRequest
      try {
        CedarInstanceParser.parseCEDARInstance(cedarInstance, dataCiteSchema, sourceArtifactId);
      } catch (DataCiteInstanceValidationException e){
        throw new DataCiteInstanceValidationException(e.getMessage());
      }

      //Serialize DataCiteRequest Class to json
      String requestJsonString = mapper.writeValueAsString(dataCiteSchema);
      System.out.println("Json Sent to DataCite: " + requestJsonString);
      return requestJsonString;

    } catch (IOException | DataCiteInstanceValidationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * This function make HTTP Post request
   *
   * @param endPointUrl URL of API call
   * @param basicAuth   Authentication at heater
   * @param jsonData    JSON used to call API
   * @return Http POST Response
   */
  private static HttpResponse<String> httpDataCitePostCall(String endPointUrl, String basicAuth, String jsonData){
    try {
      URI uri = URI.create(endPointUrl);

      // Set up the HTTP client
      HttpClient client = HttpClient.newHttpClient();

      // Create HTTP request with JSON body and basic authentication
      HttpRequest request = HttpRequest.newBuilder(uri)
          .header("Content-Type", "application/vnd.api+json")
          .header("Authorization", "Basic " + basicAuth)
          .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonData)))
          .build();

      // Call CEDAR validation endpoint and get the response
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      return response;

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * This function get JSON file of a CEDAR template
   */
  private JsonNode getCEDARTemplate(CedarRequestContext c, String templateId) {
    try {
      CedarTemplateId cedarTemplateId = CedarTemplateId.build(templateId);
      String artifactServerUrl = microserviceUrlUtil.getArtifact().getArtifactTypeWithId(CedarResourceType.TEMPLATE,
          (CedarArtifactId) cedarTemplateId);

      HttpEntity currentTemplateEntity = ProxyUtil.proxyGet(artifactServerUrl, c).getEntity();
      String currentTemplateEntityContent = EntityUtils.toString(currentTemplateEntity, CharEncoding.UTF_8);
      JsonNode currentTemplateJsonNode = JsonMapper.MAPPER.readTree(currentTemplateEntityContent);

      int statusCode = ProxyUtil.proxyGet(artifactServerUrl, c).getStatusLine().getStatusCode();
      if (statusCode == HttpConstants.OK) {
        System.out.println("The GET CEDAR Template request was successful");
      } else {
        System.out.println("The GET CEDAR Template request was failed with status code: " + statusCode);
      }
      return currentTemplateJsonNode;
    } catch (IOException | CedarProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
