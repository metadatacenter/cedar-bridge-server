package org.metadatacenter.cedar.bridge.resources;

import org.junit.*;
import org.metadatacenter.cedar.bridge.resource.CompareValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.json.JsonMapper;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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
  public void dataCiteInstanceRichDataTest() throws IOException{
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
  public void dataCiteInstanceSimplyDataTest() throws IOException {
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
  public void dataCiteInstanceAllRequiredDataTest() throws IOException{
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

  @Test
  public void dataCiteInstanceRequiredPlusSubjectTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusSubject");
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
  public void dataCiteInstanceRequiredPlusContributorTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusContributor");
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
  public void dataCiteInstanceRequiredPlusDateTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusDate");
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
  public void dataCiteInstanceRequiredPlusLangTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusLang");
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
  public void dataCiteInstanceRequiredPlusAlternateIdentifierTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusAlternateIdentifier");
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
  public void dataCiteInstanceRequiredPlusRelatedIdentifierTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRelatedIdentifier");
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
  public void dataCiteInstanceRequiredPlusAffiliationTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusAffiliation");
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
  public void dataCiteInstanceRequiredPlusSizeFormatVersionTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusSizeFormatVersion");
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
  public void dataCiteInstanceRequiredPlusRightsTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRights");
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
  public void dataCiteInstanceRequiredPlusDescriptionTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusDescription");
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
  public void dataCiteInstanceRequiredPlusFundingRefTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusFundingRef");
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
  public void dataCiteInstanceRequiredPlusGeoLocTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusGeoLoc");
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
  public void dataCiteInstanceRequiredPlusRelatedItemTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRelatedItem");
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
  public void dataCiteInstanceRequiredPlusRandomTest_1() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRandom1");
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
  public void dataCiteInstanceRequiredPlusRandomTest_2() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRandom2");
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
  public void dataCiteInstanceRequiredPlusRandomTest_3() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRandom3");
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
  public void dataCiteInstanceRequiredPlusEmptyStringTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusEmptyString");
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
  public void dataCiteInstanceEmptyJsonTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailEmptyJson");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingAllFieldsTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingAllFields");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPrefixFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingPrefixField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPublisherFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingPublisherField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPublicationYearFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingPublicationYearField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingResourceTypeGeneralFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingResourceTypeGeneralField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingMultipleRequiredFieldsTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingMultipleRequiredFields");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingContributorNameFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingContributorNameField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingContributorTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingContributorTypeField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedIdentifierTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingRelatedIdentifierTypeField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelationTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingRelationTypeField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingFunderNameFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingFunderNameField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedItemTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingRelatedItemTypeField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedItemRelationTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingRelatedItemRelationTypeField");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingFieldOfGeoLocationPointTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingFieldOfGeoLocationPoint");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingFieldOfGeoLocationBoxTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingFieldOfGeoLocationBox");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceIncorrectNameTypeInputTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailIncorrectNameTypeInput");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceGeoLocationBoxOutOfRangeValueTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailGeoLocationBoxOutOfRangeValue");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.UNPROCESSABLE_ENTITY.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceUnsupportedMediaTypeTest() throws IOException{
    File contentFile = new File(FILE_BASE_PATH + "FailUnsupportedMediaType.xml");
    byte[] contentBytes = Files.readAllBytes(contentFile.toPath());

    Response createDoiResponse = RULE.client().target(baseUrlCreateDoi)
        .request(MediaType.APPLICATION_XML)
        .header("Authorization", authHeaderAdmin)
        .post(Entity.entity(contentBytes, MediaType.APPLICATION_XML));

    Assert.assertEquals(CedarResponseStatus.HTTP_VERSION_NOT_SUPPORTED.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void garbageDataCiteInstanceJsonTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailGarbageJson");
    Response createDoiResponse = createDoi(givenMetadata);
    Assert.assertEquals(CedarResponseStatus.INTERNAL_SERVER_ERROR.getStatusCode(), createDoiResponse.getStatus());
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
    String getDoiMetadataUrl = baseUrlGetDoiMetadata + encodedDoiName + displayAffiliation;
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