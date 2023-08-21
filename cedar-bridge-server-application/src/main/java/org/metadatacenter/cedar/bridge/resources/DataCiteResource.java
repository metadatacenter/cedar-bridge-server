package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.cedar.bridge.resource.*;
import org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.Attributes;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.DataCiteSchema;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.http.CedarResponseStatus;
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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.constant.CedarQueryParameters.QP_SOURCE_ARTIFACT_ID;
import static org.metadatacenter.rest.assertion.GenericAssertions.LoggedIn;

@Path("/datacite")
@Produces(MediaType.APPLICATION_JSON)
public class DataCiteResource extends CedarMicroserviceResource {

  private static TemplateService<String, JsonNode> templateService;
  private static TemplateInstanceService<String, JsonNode> templateInstanceService;
  private final String repositoryID = cedarConfig.getBridgeConfig().getDataCite().getRepositoryId();
  private final String password = cedarConfig.getBridgeConfig().getDataCite().getPassword();
  private final String endpointUrl = cedarConfig.getBridgeConfig().getDataCite().getEndpointUrl();
  private final String templateId = cedarConfig.getBridgeConfig().getDataCite().getTemplateId();
  private final String basicAuth =
      Base64.getEncoder().encodeToString((repositoryID + ":" + password).getBytes(StandardCharsets.UTF_8));
  private final String queryAffiliation = "?affiliation=true";
  private final String queryPublisher = "&query=publisher:CEDAR";



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

      String endpointUrl = cedarConfig.getBridgeConfig().getDataCite().getEndpointUrl() + doi + queryAffiliation;
      URI uri = URI.create(endpointUrl);


      HttpClient client = HttpClient.newBuilder().build();
      HttpRequest httpRequest = HttpRequest.newBuilder(uri)
          .header("Authorization", "Basic " + basicAuth)
          .GET()
          .build();

      // Send HTTP httpRequest and get response
      HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      int statusCode = httpResponse.statusCode();
      String jsonResponse = httpResponse.body();
      JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);

      // Deserialize DataCite response json file to DataCiteRequest Class
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      DataCiteSchema dataCiteResponse = mapper.readValue(jsonResponse, DataCiteSchema.class);

      String dataCiteResponseString = mapper.writeValueAsString(dataCiteResponse);
      System.out.println("DataCiteResponse converted to Data Cite Schema Json: " + dataCiteResponseString);

      // Pass the value from dataCiteResponse to MetadataInstance
      MetadataInstance cedarDataCiteInstance = DataCiteMetadataParserNew.parseDataCiteSchema(dataCiteResponse.getData().getAttributes());

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

    // Check if user has write permission to the source artifact
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

    // Check if the source artifact is open
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

    //Check if there is an already started DOI metadata instance. If yes, load it as well
    //Use publisher and openView Url as parameters to send query to DataCite
    try {
      Response httpResponse = getDraftDoiMetadata(sourceArtifactId);
      HashMap<String, Object> entity = (HashMap<String, Object>) httpResponse.getEntity();
      boolean hasDraftDoi = (boolean) entity.get("hasDraftDoi");
      JsonNode dataNode = (JsonNode) entity.get("draftMetadata");

      if (hasDraftDoi){
        // if draft DOI is returned, convert the data from dataCite JSON to Cedar Instance JSON-LD, and put it into response
        JsonNode attributesNode = dataNode.get(0).get("attributes");
        JsonNode draftDoi = attributesNode.get("doi");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Attributes existingDoiMetadata = mapper.treeToValue(attributesNode, Attributes.class);
        String existingDoiMetadataString = mapper.writeValueAsString(existingDoiMetadata);
        System.out.println("existingDoiMetadata converted to Data Cite Schema Json: " + existingDoiMetadataString);

        // Pass the value from dataCiteResponse to cedarDataCiteInstance
        MetadataInstance cedarExistingDoiMetadata = DataCiteMetadataParserNew.parseDataCiteSchema(existingDoiMetadata);
        response.put("existingDataCiteMetadata", cedarExistingDoiMetadata);
        response.put("draftDoi", draftDoi);

        String cedarDataCiteInstanceString = mapper.writeValueAsString(cedarExistingDoiMetadata);
        System.out.println("Converted Cedar DataCite Instance JSON-LD: " + cedarDataCiteInstanceString);
      }
      else{
        // if draft DOI is not available, set the url and resourceType fields
        MetadataInstance defaultInstance = GenerateInstance.getDefaultInstance(sourceArtifactId);
        response.put("existingDataCiteMetadata", defaultInstance);
        response.put("draftDoi", null);
      }
    } catch(IOException | InterruptedException e){
      throw new RuntimeException(e);
    }
    response.put("sourceArtifactType", sourceArtifactResourceId.getType().getValue());
    response.put("sourceArtifact", sourceArtifactProxyJson);
    response.put("dataCiteTemplate", dataCiteTemplateProxyJson);

    return CedarResponse.ok().entity(response).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Path("/create-doi")
  public Response createDOI(@QueryParam(QP_SOURCE_ARTIFACT_ID) String sourceArtifactId, @QueryParam("state") String state, JsonNode dataCiteInstance) throws CedarException, IOException, InterruptedException {
    CedarRequestContext c = buildRequestContext();

//    c.must(c.user()).be(LoggedIn);

    Map<String, Object> response = new HashMap<>();

    Pair<Boolean, JsonNode> validationResultPair = validateCEDARInstance(c, templateId, dataCiteInstance);
    boolean validates = validationResultPair.getLeft();
    JsonNode validationResult = validationResultPair.getRight();

    //Call CEDAR validation endpoint and continue if return true
    // if (validates){
    if (true) {
      // Get DOI request json
      String jsonData = "";
      if (dataCiteInstance != null && !dataCiteInstance.isEmpty()) {
        try {
          jsonData = getRequestJson(dataCiteInstance, sourceArtifactId, state);
        } catch (Exception e) {
          e.printStackTrace();
          String errorMessage = e.getMessage();
          if (errorMessage != null) {
            String[] errorMessageParts = errorMessage.split(":");
            if (errorMessageParts.length > 1) {
              errorMessage = String.join(":", Arrays.copyOfRange(errorMessageParts, 1, errorMessageParts.length)).trim();
            }
          }
          return CedarResponse
              .badRequest()
              .errorMessage(errorMessage)
              .errorKey(CedarErrorKey.INVALID_INPUT)
              .build();
        }
      }
      try {
        //Send HTTP GET request to DataCite to recheck the draft DOI
        Response getDraftDoiResponse = getDraftDoiMetadata(sourceArtifactId);
        //Send PUT request if draft DOI exists, otherwise send POST request
        HashMap<String, Object> entity = (HashMap<String, Object>) getDraftDoiResponse.getEntity();
        boolean hasDraftDoi = (boolean) entity.get("hasDraftDoi");
        JsonNode dataNode = (JsonNode) entity.get("draftMetadata");
        HttpResponse<String> putOrPostResponse = null;
        if (hasDraftDoi) {
          //Send HTTP PUT request to DataCite and get response
          JsonNode attributesNode = dataNode.get(0).get("attributes");
          String draftDoi = attributesNode.get("doi").toString();
          putOrPostResponse = httpDataCitePutCall(draftDoi, basicAuth, jsonData);
        } else {
          //Send HTTP POST request to DataCite and get response
          putOrPostResponse = httpDataCitePostCall(endpointUrl, basicAuth, jsonData);
        }
        int statusCode = putOrPostResponse.statusCode();
        //If the Put or Post response status code is 200 or 201
        String jsonResponse = putOrPostResponse.body();
        try{
          if (statusCode == HttpConstants.CREATED | statusCode == HttpConstants.OK) {
            // Deserialize DataCite response json file to DataCiteRequest Class
            ObjectMapper mapper = new ObjectMapper();
//            DataCiteSchema dataCiteResponse = mapper.readValue(jsonResponse, DataCiteSchema.class);
            JsonNode jsonNode = mapper.readTree(jsonResponse);
            String id = jsonNode.get("data").get("id").asText();
            String doiName = "https://doi.org/" + id;
            System.out.println("doiName: " + doiName);
            URI uri = URI.create(doiName);
            response.put("doiId", id);
            response.put("doiName", doiName);
            response.put("dataCiteResponse", jsonResponse);
            return CedarResponse
                .created(uri)
                .entity(response)
                .build();
          } //If the status code is 422, return what DataCite returns
          else if (statusCode == CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode()) {
            JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);
            JsonNode errorsNode = jsonResource.get("errors");
            StringBuilder errorMessageBuilder = new StringBuilder();
            for(JsonNode errorNode: errorsNode){
              JsonNode sourceNode = errorNode.get("source");
              if (sourceNode != null && sourceNode.isTextual()) {
                String source = sourceNode.asText();
                errorMessageBuilder.append(source).append(": ");
              }
              JsonNode titleNode = errorNode.get("title");
              if (titleNode != null && titleNode.isTextual()) {
                String title = titleNode.asText();
                String[] titleParts = title.split(":");
                if (titleParts.length > 1) {
                  String titleMessage = String.join(":", Arrays.copyOfRange(titleParts, 1, titleParts.length)).trim();
                  errorMessageBuilder.append(titleMessage).append("\n");
                } else{
                  errorMessageBuilder.append(title).append("\n");
                }
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
        } catch (Exception e){
          return CedarResponse
              .internalServerError()
              .errorMessage(e.getMessage())
              .exception(e)
              .build();
        }
      } catch (IOException | InterruptedException e) {
        return CedarResponse
            .badRequest()
            .errorMessage(e.getMessage())
            .build();
      }
    } else {
      // Failed in Validation Check
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
      HttpClient client = HttpClient.newHttpClient();
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
   * @return DataCite requested JSON schema
   */
  private String getRequestJson(JsonNode metadata, String sourceArtifactId, String state) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
//    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    DataCiteSchema dataCiteSchema = new DataCiteSchema();
    try {
      // Deserialize JSON-LD to CedarDataCiteInstance Class
      String metadataString = metadata.toString();
      // Deserialize JSON-LD to MetadataInstance Class
      MetadataInstance cedarInstance = mapper.readValue(metadataString, MetadataInstance.class);

      String cedarInstanceString = mapper.writeValueAsString(cedarInstance);
      System.out.println("Json Converted to CedarDataCite Instance: " + cedarInstanceString);

      // Pass the value from dataCiteInstance to dataCiteRequest
      try {
//        CedarInstanceParser.parseCedarInstance(cedarInstance, dataCiteSchema, sourceArtifactId, state);
        // pass the value using Matthew's code
        CedarInstanceParserNew.parseCedarInstance(cedarInstance, dataCiteSchema, sourceArtifactId, state);
      } catch (DataCiteInstanceValidationException e){
        e.printStackTrace();
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
   * This function send HTTP Post request to DataCite to create a DOI
   *
   * @param endPointUrl URL of API call
   * @param basicAuth   Authentication at heater
   * @param jsonData    DataCite metadata instance JSON
   * @return            Http POST Response from DataCite
   */
  private HttpResponse<String> httpDataCitePostCall(String endPointUrl, String basicAuth, String jsonData){
    try {
      URI uri = URI.create(endPointUrl);
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder(uri)
          .header("Content-Type", "application/vnd.api+json")
          .header("Authorization", "Basic " + basicAuth)
          .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonData)))
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println("DataCite Post Call with status code: " + response.statusCode());
      return response;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * This function send HTTP Put request to DataCite to update DOI's metadata
   *
   * @param draftDoi    String of draft DOI
   * @param basicAuth   Authentication at heater
   * @param jsonData    DataCite metadata instance JSON
   * @return            Http Put Response from DataCite
   */
  private HttpResponse<String> httpDataCitePutCall(String draftDoi, String basicAuth, String jsonData)throws IOException, InterruptedException{
    String url = endpointUrl + "/" + draftDoi.replace("\"", "");;
    URI uri = URI.create(url);
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder(uri)
        .header("Content-Type", "application/vnd.api+json")
        .header("Authorization", "Basic " + basicAuth)
        .PUT(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonData)))
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    System.out.println("DataCite Put Call with status code: " + response.statusCode());
    return response;
  }


  /**
   * This function send HTTP Get request to DataCite to query the draft DOI
   *
   * @param sourceArtifactId    ID of source template or instance for which you want to create the DOI
   * @return                    CedarResponse which contains draft DOI's metadata and boolean value of if draft DOI exists
   */
  private Response getDraftDoiMetadata(String sourceArtifactId) throws IOException, InterruptedException {
    Map<String, Object> response = new HashMap<>();
    String openViewUrl = CheckOpenViewUrl.getOpenViewUrl(sourceArtifactId);
    String encodedOpenViewUrl = URLEncoder.encode(openViewUrl, StandardCharsets.UTF_8);
    String queryUrl = endpointUrl + queryAffiliation + queryPublisher + "%20AND%20url:%22" + encodedOpenViewUrl + "%22";
    URI uri = URI.create(queryUrl);
    HttpClient client = HttpClient.newBuilder().build();
    HttpRequest httpRequest = HttpRequest.newBuilder(uri)
        .header("Authorization", "Basic " + basicAuth)
        .GET()
        .build();
    HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    String jsonResponse = httpResponse.body();
    JsonNode jsonResource = JsonMapper.MAPPER.readTree(jsonResponse);
    JsonNode dataNode = jsonResource.get("data");
    boolean hasDraftDoi = hasDraftDoi(dataNode);
    response.put("draftMetadata", dataNode);
    response.put("hasDraftDoi", hasDraftDoi);
    return CedarResponse.ok().entity(response).build();
  }


  /**
   * This function check if there is a draft DOI exists
   */
  private Boolean hasDraftDoi(JsonNode dataNode) {
    return dataNode != null && !dataNode.isEmpty();
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
