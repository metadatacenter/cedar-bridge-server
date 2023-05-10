package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.minidev.json.JSONObject;
import org.metadatacenter.cedar.bridge.resource.CEDARInstanceParser;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.DataCiteSchema;
import org.metadatacenter.cedar.bridge.resource.CEDARProperties.CEDARDataCiteInstance;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.service.TemplateInstanceService;
import org.metadatacenter.server.service.TemplateService;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.http.CedarUrlUtil;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
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
  public Response getDOIMetadata(@PathParam(PP_ID) String doiId) throws CedarException {

    CedarRequestContext c = buildRequestContext();

    c.must(c.user()).be(LoggedIn);

    try {
      String encodedId = URLEncoder.encode(doiId, StandardCharsets.UTF_8.toString());
      String endpointUrl = cedarConfig.getBridgeConfig().getDataCite().getEndpointUrl() + "/" + encodedId;
      System.out.println(endpointUrl);
//      String endpointUrl = "https://api.datacite.org/dois/" + encodedId;
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
      return Response.status(statusCode).entity(jsonResource).build();
    } catch (Exception e) {
      return CedarResponse.internalServerError().exception(e).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Path("/create-doi")
  public Response createDOI(JSONObject metadata) throws CedarException, IOException, InterruptedException {
    CedarRequestContext c = buildRequestContext();

    c.must(c.user()).be(LoggedIn);

    Map<String, Object> response = new HashMap<>();

    String repositoryID = cedarConfig.getBridgeConfig().getDataCite().getRepositoryId();
    String password = cedarConfig.getBridgeConfig().getDataCite().getPassword();
    String endpointUrl = cedarConfig.getBridgeConfig().getDataCite().getEndpointUrl();

    // Create basic authentication
    String basicAuth =
        Base64.getEncoder().encodeToString((repositoryID + ":" + password).getBytes(StandardCharsets.UTF_8));

    //Call CEDAR validation endpoint and continue if return true
//    if (validateCEDARInstance()){
    if (true) {
      try {
        // Get DOI request json
        String jsonData = getRequestJson(metadata);

        // Send HTTP request and get response
        HttpResponse<String> httResponse = httpDataCitePostCall(endpointUrl, basicAuth, jsonData);

        int statusCode = httResponse.statusCode();
        if (statusCode == HttpConstants.CREATED) {

          String jsonResponse = httResponse.body();

          // Deserialize DaraCite response json file to DataCiteRequest Class
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
        return CedarResponse.internalServerError().exception(e).build();
      }
    } else {
      response.put("request", metadata);
      response.put("validationResult", null);
      return CedarResponse.badRequest().errorMessage("Validation Error").entity(response).build();
    }
  }

//  /**
//   * This function check if CEDAR DataCite Instance is valid
//   */
//  private static Boolean validateCEDARInstance() throws IOException, InterruptedException {
//    // Get Scheme JSONObject and CEDAR DataCite Instance JSONObject
//    HttpResponse<String> schemaResponse = getCEDARTemplate("51808bbc-04bf-4547-8c57-9ead630ee5fd");
////        System.out.println(schemaResponse.body());
//    JSONObject schema = new JSONObject(schemaResponse.body());
//
////        HttpResponse instanceResponse = getCEDARDataCiteInstance("");
//    File instanceFile = new File("./datacite-integartion/src/main/java/org/metadatacenter/bridge/datacite/jsonFile
//    /instanceExample.json");
//    String instanceJSON = new String(Files.readAllBytes(instanceFile.toPath()));
//    JSONObject instance = new JSONObject(instanceJSON);
//
//    JSONObject validationBody = new JSONObject();
//    validationBody.put("schema", schema);
//    validationBody.put("instance", instance);
//
//    try {
//      // Construct API endpoint URL
//      String endpointUrl = "https://resource.metadatacenter.org/command/validate?resource_type=template";
//
//      // Set authorization header
//      String apiKey = "apiKey e94e265d4c3cd623ca8bde96cfc743074196409e345b164f148333dd403c3401";
////            String basicAuth = Base64.getEncoder().encodeToString(("apiKey " + apiKey).getBytes(StandardCharsets
// .UTF_8));
//
//      URI uri = URI.create(endpointUrl);
//
//      // Set up the HTTP client
//      HttpClient client = HttpClient.newHttpClient();
//
//      // Create HTTP request with JSON body and basic authentication
//      HttpRequest request = HttpRequest.newBuilder(uri)
////                    .header("Content-Type", "application/vnd.api+json")
////                    .header("Authorization", "Basic " + basicAuth)
//              .header("Content-Type", "application/json")
//              .header("Authorization", apiKey)
//              .header("Accept", "application/json")
//              .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(validationBody)))
//              .build();
//
//      // Call CEDAR validation endpoint and get the response
//      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
////            System.out.println(response.body());
//
//      // Check response status code
//      int statusCode = response.statusCode();
//
//      if (statusCode == 200) {
//        // the request was successful
//        System.out.println();
//        System.out.println("Validation POST Request is successful");
//
//        // Parse the response body as a JSONObject
//        JSONObject responseJson = new JSONObject(response.body());
//
//        // Check the "validates" field to determine how to handle the response
//        boolean validates = responseJson.getBoolean("validates");
//        if (validates) {
//          // The resource is valid, handle it here
//          System.out.println("Resource is valid.");
//          return true;
//        } else {
//          // The resource is invalid, handle the errors and warnings here
//          JSONArray errors = responseJson.getJSONArray("errors");
//          JSONArray warnings = responseJson.getJSONArray("warnings");
//
//          // Print the errors and warnings
//          for (int i = 0; i < errors.length(); i++) {
//            System.out.println("Resource is invalid. Errors:" + i);
//            JSONObject error = errors.getJSONObject(i);
//            System.out.println("Message: " + error.getString("message"));
//            System.out.println("Location: " + error.getString("location"));
//            JSONObject additionalInfo = error.getJSONObject("additionalInfo");
//            System.out.println("Schema File: " + additionalInfo.getString("schemaFile"));
//            System.out.println("Schema Pointer: " + additionalInfo.getString("schemaPointer"));
//            System.out.println();
//          }
//
//          for (int i = 0; i < warnings.length(); i++) {
//            System.out.println("Warnings" + i);
//            System.out.println(warnings.get(i));
//          }
//          return false;
//        }
//      } else {
//        // The request failed
//        System.out.println("Validation failed with status code: " + statusCode);
//        return false;
//      }
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    } catch (JSONException e) {
//      throw new RuntimeException(e);
//    }
//  }

  /**
   * This function transfer JSON-LD format to JSON scheme that used to call DataCite API
   *
   * @return DataCite requested JSON scheme
   */
  private static String getRequestJson(JSONObject metadata) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    DataCiteSchema dataCiteSchema = new DataCiteSchema();
    try {
      // Deserialize JSON-LD to CRDARDataCiteInstance Class
      String metadataString = metadata.toString();
      CEDARDataCiteInstance cedarInstance = mapper.readValue(metadataString, CEDARDataCiteInstance.class);

      // Pass the value from dataCiteInstance to dataCiteRequest
      CEDARInstanceParser.parseCEDARInstance(cedarInstance, dataCiteSchema);

      //Serialize DataCiteRequest Class to json
      String requestJsonString = mapper.writeValueAsString(dataCiteSchema);
      return requestJsonString;

    } catch (IOException e) {
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
   * @throws IOException
   * @throws InterruptedException
   */
  private static HttpResponse<String> httpDataCitePostCall(String endPointUrl, String basicAuth, String jsonData) throws IOException, InterruptedException {
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

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
//
//  /**
//   * This function make HTTP Get request
//   * @param endPointUrl URL of API call
//   * @param basicAuth Authentication at heater
//   * @return Http Get Response
//   * @throws IOException
//   * @throws InterruptedException
//   */
//  private static HttpResponse<String> httpGetCall(String endPointUrl, String basicAuth) throws IOException,
//  InterruptedException {
//    try {
//      URI uri = URI.create(endPointUrl);
//
//      // Set up the HTTP client
//      HttpClient client = HttpClient.newHttpClient();
//
//      // Create HTTP request with JSON body and basic authentication
//      HttpRequest request = HttpRequest.newBuilder(uri)
//              .header("Accept", "application/json")
//              .header("Authorization", basicAuth)
//              .GET()
//              .build();
//
//      // Call CEDAR validation endpoint and get the response
//      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//      int statusCode = response.statusCode();
//      if (statusCode == 200){
//        System.out.println("The get request was successful");
//      } else {
//        System.out.println("The get request was failed with status code: " + statusCode);
//      }
//
//      return response;
//
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e);
//    }
//
//  }
//
//  /**
//   * This function get JSON file of a CEDAR template
//   * @param templateID ID of CEDAR template
//   * @return CEDAR template's corresponding JSON file
//   */
//  private static HttpResponse<String> getCEDARTemplate(String templateID) throws IOException, InterruptedException {
//    try {
//      // Construct API endpoint URL
//      String endpointUrl = "https://resource.metadatacenter.org/templates/https%3A%2F%2Frepo.metadatacenter.org%2Ftemplates%2F"
//      + templateID;
//
//      // Set authentication header
//      String apiKey = "apiKey e94e265d4c3cd623ca8bde96cfc743074196409e345b164f148333dd403c3401";
////            String basicAuth = Base64.getEncoder().encodeToString(("apiKey " + apiKey).getBytes(StandardCharsets
// .UTF_8));
//
//      HttpResponse<String> response = httpGetCall(endpointUrl, apiKey);
//
////            //Save response to local JSON file
////            File file = saveJSONFile(response, "CEDARDataCiteTemplateSchema.json");
//
//      // Return JSON file
//      return response;
//
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  private static HttpResponse getCEDARDataCiteInstance(String instanceID){
//    try {
//      String endpointUrl = "https://resource.metadatacenter.org/template-instances/https%3A%2F%2Frepo.metadatacenter.org%2Ftemplate-elements%2F"
//      + instanceID + "?format=jsonld";
//
//      String apiKey = "apiKey e94e265d4c3cd623ca8bde96cfc743074196409e345b164f148333dd403c3401";
//
//      HttpResponse<String> response = httpGetCall(endpointUrl, apiKey);
//
//      return response;
//
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  private static void ErrorHandler(HttpResponse<String> response) {
//    JSONObject responseJson = new JSONObject(response.body());
//    // If value is not an element of controlled value or a mandatory
//    if (response.statusCode() == 422) {
//      JSONArray errors = responseJson.getJSONArray("errors");
//      for (int i = 0; i < errors.length(); i++) {
//        System.out.println("Error:" + i);
//        JSONObject error = errors.getJSONObject(i);
//        System.out.println("Source: " + error.getString("source"));
//        System.out.println("UID: " + error.getString("uid"));
//        System.out.println("Title: " + error.getString("title"));
//        System.out.println();
//      }
//      //TODO: if a sub-mandatory properties is missed
//    }
//  }
}
