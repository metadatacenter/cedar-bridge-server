package org.metadatacenter.cedar.bridge.resources;

import org.metadatacenter.cedar.bridge.resource.CompareValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

  @BeforeClass public static void oneTimeSetUp()
  {
  }

  @Before public void setUp()
  {
  }

  @After public void tearDown()
  {
  }

  @Test
  public void getDoiMetadataSuccessRichDataTest() {
    String givenMetadataFilePath = "/Users/ycao77/CEDAR/cedar-bridge-server/cedar-bridge-server-application/src/test/java/org/metadatacenter/cedar/bridge/resources/TestJsonFiles/SuccessRichData.json";
    ObjectMapper objectMapper = new ObjectMapper();
    try{
      JsonNode givenMetadata = objectMapper.readTree(new File(givenMetadataFilePath));
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void getDoiMetadataSuccessSimplyDataTest() {
    String givenMetadataFilePath = "/Users/ycao77/CEDAR/cedar-bridge-server/cedar-bridge-server-application/src/test/java/org/metadatacenter/cedar/bridge/resources/TestJsonFiles/SuccessRequiredOnly.json";
    ObjectMapper objectMapper = new ObjectMapper();
    try{
      JsonNode givenMetadata = objectMapper.readTree(new File(givenMetadataFilePath));
      Response createDoiResponse = createDoi(givenMetadata);
      Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
      Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

      Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
      Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
      Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

      JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
      Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void getDoiMetadataSuccessAllRequiredDataTest() {
    String givenMetadataFilePath = "/Users/ycao77/CEDAR/cedar-bridge-server/cedar-bridge-server-application/src/test/java/org/metadatacenter/cedar/bridge/resources/TestJsonFiles/SuccessAllPropertiesUnderRequiredElement.json";
    ObjectMapper objectMapper = new ObjectMapper();
    try{
      JsonNode givenMetadata = objectMapper.readTree(new File(givenMetadataFilePath));
      Response createDoiResponse = createDoi(givenMetadata);
      Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
      Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

      Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
      Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
      Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

      JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
      Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
    ObjectMapper objectMapper = new ObjectMapper();
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

}