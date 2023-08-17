package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.*;
import org.metadatacenter.cedar.bridge.resource.CedarProperties.CedarDataCiteInstance;
import org.metadatacenter.cedar.bridge.resource.CompareValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.metadatacenter.cedar.bridge.resource.DataCiteInstanceValidationException;
import org.metadatacenter.cedar.bridge.resource.DataCiteMetadataParser;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.DataCiteSchema;
import org.metadatacenter.cedar.bridge.resource.GenerateTestFiles;
import org.metadatacenter.http.CedarResponseStatus;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.metadatacenter.cedar.bridge.resource.Cedar.*;

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
    objectMapper.registerModule(new JavaTimeModule());
  }

  @After public void tearDown()
  {
  }

  @Test
  public void dataCiteInstanceRichDataTest() throws IOException, DataCiteInstanceValidationException {
    // Retrieve the given DataCite instance from the file
//    JsonNode givenMetadata = getFileContentAsJson("SuccessRichData");
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getRichMetadata());

    // Create a DOI using the given DataCite instance
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceSimplyDataTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredOnly");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceAllRequiredDataTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessAllPropertiesUnderRequiredElement");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusSubjectTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusSubject");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusContributorTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusContributor");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusDateTest() throws IOException,DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusDate");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusLangTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusLang");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusAlternateIdentifierTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusAlternateIdentifier");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRelatedIdentifierTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRelatedIdentifier");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusAffiliationTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusAffiliation");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusSizeFormatVersionTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusSizeFormatVersion");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRightsTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRights");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusDescriptionTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusDescription");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusFundingRefTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusFundingRef");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusGeoLocTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusGeoLoc");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRelatedItemTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRelatedItem");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRandomTest_1() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRandom1");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRandomTest_2() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRandom2");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRandomTest_3() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusRandom3");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusEmptyStringTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusEmptyString");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceRequiredPlusAllElementExpandedTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = getFileContentAsJson("SuccessRequiredPlusAllElementExpanded");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH));
  }

  @Test
  public void dataCiteInstanceEmptyJsonTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailEmptyJson");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingAllFieldsTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingAllFields");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPrefixFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingPrefixField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPublisherFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingPublisherField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPublicationYearFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingPublicationYearField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingMultipleRequiredFieldsTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingMultipleRequiredFields");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingContributorNameFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingContributorNameField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingContributorTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingContributorTypeField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedIdentifierTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingRelatedIdentifierTypeField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelationTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingRelationTypeField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingFunderNameFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingFunderNameField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedItemTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingRelatedItemTypeField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedItemRelationTypeFieldTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailMissingRelatedItemRelationTypeField");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceIncorrectNameTypeInputTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailIncorrectNameTypeInput");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceGeoLocationBoxOutOfRangeValueTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailGeoLocationBoxOutOfRangeValue");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceUnsupportedMediaTypeTest() throws IOException{
    Response createDoiResponse = RULE.client().target(baseUrlCreateDoi)
        .request(MediaType.APPLICATION_XML)
        .header("Authorization", authHeaderAdmin)
        .post(Entity.text(""));
    Assert.assertEquals(CedarResponseStatus.HTTP_VERSION_NOT_SUPPORTED.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void garbageDataCiteInstanceJsonTest() throws IOException{
    JsonNode givenMetadata = getFileContentAsJson("FailGarbageJson");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void draftDoiSaveAndUpdateSimpleMetadataTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiSimpleMetadataBeforeUpdate");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiSimpleMetadataAfterUpdate");

    // Update the draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, DRAFT));
  }

  @Test
  public void draftDoiSaveAndUpdateRichMetadataTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiRichMetadataBeforeUpdate");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiRichMetadataAfterUpdate");

    // Update the draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, DRAFT));
  }

  @Test
  public void draftDoiSaveAndPublishSimpleMetadataTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiSimpleMetadataBeforeUpdate");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiSimpleMetadataAfterUpdate");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH));
  }



  @Test
  public void draftDoiSaveAndPublishRichMetadataTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiRichMetadataBeforeUpdate");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiRichMetadataForPublish");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH));
  }

  @Test
  public void draftDoiAddNewInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiRichMetadataBeforeUpdate");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiAddNewInstance");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH));
  }

  @Test
  public void draftDoiDeleteInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiAddNewInstance");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiDeleteInstance");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH));
  }

  @Test
  public void draftDoiDeleteAndAddNewInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiAddNewInstance");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiDeleteAndAddNewInstance");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH));
  }

  @Test
  public void draftDoiDeleteAllInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiAddNewInstance");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiDeleteAllInstance");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH));
  }

  @Test
  public void draftDoiSaveWithEmptyDataCiteInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiEmptyInstance");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
  }

  @Test
  public void draftDoiUpdateWithoutPrefixFieldTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiSimpleMetadataBeforeUpdate");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiUpdateWithoutPrefix");

    // Update the draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
  }

  @Test
  public void draftDoiPublishWithoutContributorNameFieldTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = getFileContentAsJson("DraftDoiRichMetadataBeforeUpdate");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = getFileContentAsJson("DraftDoiPublishWithoutContributorName");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
  }

  @Test
  public void compareDraftDoiInstanceWithDataCiteResponseTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance from the file
    JsonNode givenMetadata = getFileContentAsJson("DraftDoiRichMetadataBeforeUpdate");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadata, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadata = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assert.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadata.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadata.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadata = objectMapper.readTree(getDoiMetadata.readEntity(String.class));
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, returnedMetadata, sourceArtifactId, DRAFT));

    // Convert the DataCite-format metadata to Cedar-format
    DataCiteSchema dataCiteResponse = objectMapper.readValue(returnedMetadata.toString(), DataCiteSchema.class);
    CedarDataCiteInstance cedarDataCiteInstance = new CedarDataCiteInstance();
    DataCiteMetadataParser.parseDataCiteSchema(dataCiteResponse.getData().getAttributes(), cedarDataCiteInstance);

    //Compare the converted Cedar-format metadata with the DataCite response metadata
    JsonNode convertedMetadata = objectMapper.valueToTree(cedarDataCiteInstance);
    Assert.assertTrue(CompareValues.compareResponseWithGivenMetadata(convertedMetadata, returnedMetadata, sourceArtifactId, DRAFT));
  }

  private Response createDoi(JsonNode givenMetadata, String sourceArtifactId, String state) throws IOException {
    Entity<JsonNode> postContent = Entity.entity(givenMetadata, MediaType.APPLICATION_JSON);
    Response createDoiResponse = client.target(baseUrlCreateDoi)
        .queryParam("source_artifact_id", sourceArtifactId)
        .queryParam("state", state)
        .request()
        .header("Authorization", authHeaderAdmin)
        .post(postContent);
    return createDoiResponse;
  }

  private Response getDoiMetadata(Response createDoiResponse) throws IOException {
    JsonNode responseBody = objectMapper.readTree(createDoiResponse.readEntity(String.class));
    String doiName = responseBody.get("doiName").asText();
    String encodedDoiName = URLEncoder.encode(doiName, StandardCharsets.UTF_8);
    String getDoiMetadataUrl = baseUrlGetDoiMetadata + encodedDoiName + DISPLAY_AFFILIATION;
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
        return objectMapper.readTree(new File(DataCiteResourceTest.class.getClassLoader().getResource(filePath).getFile()));
      } catch (IOException e) {
        log.error("Error reading input file:" + filePath);
        throw e;
      }
    }
    return null;
  }
}
