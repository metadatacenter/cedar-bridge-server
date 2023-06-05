package org.metadatacenter.cedar.bridge.resources;

import org.junit.*;
import org.metadatacenter.cedar.bridge.resource.CompareValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.metadatacenter.util.json.JsonMapper;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DataCiteResourceTest extends AbstractBridgeServerResourceTest
{
  @Rule
  public ObjectMapperRule objectMapperRule = new ObjectMapperRule();
  private ObjectMapper objectMapper;

  @BeforeClass public static void oneTimeSetUp()
  {
  }

  @Before public void setUp()
  {
    objectMapper = objectMapperRule.getObjectMapper();
  }

  @After public void tearDown()
  {
  }

  @Test
  public void getDoiMetadataSuccessRichDataTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRichData");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

//      Map<String, Object> summary = createDoiResponse.readEntity(new GenericType<>(){});
//      System.out.println(JsonMapper.MAPPER.valueToTree(summary));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata));
  }

  @Test
  public void getDoiMetadataSuccessSimplyDataTest() throws IOException {
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredOnly");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata));
  }

  @Test
  public void getDoiMetadataSuccessAllRequiredDataTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessAllPropertiesUnderRequiredElement");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata));
  }

  private Response createDoi(JsonNode givenMetadata) throws IOException {
    Entity postContent = Entity.entity(givenMetadata, MediaType.APPLICATION_JSON);
    Response createDoiResponse = client.target(baseUrlCreateDoi)
        .request()
        .header("Authorization", authHeaderAdmin)
        .post(postContent);
    return createDoiResponse;
  }

  private Response getDoiMetadata(Response createDoiResponse) throws IOException {
    JsonNode responseBody = objectMapper.readTree(createDoiResponse.readEntity(String.class));
    String doiName = responseBody.get("doiName").asText();
    String encodedDoiName = URLEncoder.encode(doiName, StandardCharsets.UTF_8);
    String getDoiMetadataUrl = baseUrlGetDoiMetadata + encodedDoiName;
    Response getDoiMetadataResponse = client.target(getDoiMetadataUrl)
        .request()
        .header("Authorization", authHeaderAdmin)
        .get();
    return getDoiMetadataResponse;
  }

  protected JsonNode getFileContentAsJson(String jsonFileName) throws IOException {
    if (jsonFileName != null) {
      String filePath = FILE_BASE_PATH + jsonFileName + ".json";
      try {
        return objectMapper.readTree(new File(filePath));
      } catch (IOException e) {
        throw e;
      }
    }
    return null;
  }
}