package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.*;
import org.metadatacenter.cedar.bridge.resource.*;
import org.metadatacenter.cedar.bridge.resource.CedarProperties.CedarDataCiteInstance;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.DataCiteSchema;
import org.metadatacenter.http.CedarResponseStatus;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadata());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredOnly());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceAllUnderRequiredElements());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndSubject());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndContributor());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndDate());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndLang());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndAlternateIdentifier());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndRelatedIdentifier());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndSizeFormatVersion());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndRights());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndDescription());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndFundingRef());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndGeoLocation());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndRelatedItem());

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
  public void dataCiteInstanceRequiredPlusRandomTest() throws IOException, DataCiteInstanceValidationException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndRandomFields());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndEmptyString());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndAllEmptyFields());

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
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingAllFields());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPrefixFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingPrefix());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPublisherFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingPublisher());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPublicationYearFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingPublicationYear());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingMultipleRequiredFieldsTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingMultipleRequiredFields());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingContributorNameFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingContributorName());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingContributorTypeFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingContributorType());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedIdentifierTypeFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingRelatedIdentifierType());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelationTypeFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingRelationType());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingFunderNameFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingFunderName());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedItemTypeFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingRelatedItemType());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedItemRelationTypeFieldTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingRelatedItemRelationType());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceIncorrectNameTypeInputTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceWrongNameType());
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assert.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceGeoLocationBoxOutOfRangeValueTest() throws IOException{
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceDataOutOfRange());
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
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredOnly());

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
    JsonNode givenMetadataAfterUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadata());

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
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadata());

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
    JsonNode givenMetadataAfterUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadataRichUpdate());

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
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredOnly());

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
    JsonNode givenMetadataAfterUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndRandomFields());;

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
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadata());

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
    JsonNode givenMetadataAfterUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadataRichUpdate());

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
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadata());

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
    JsonNode givenMetadataAfterUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadataAddNewInstance());

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
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadata());

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
    JsonNode givenMetadataAfterUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadataDeleteInstance());

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
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadata());

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
    JsonNode givenMetadataAfterUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadataDeleteAndAddInstance());

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
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadata());

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
    JsonNode givenMetadataAfterUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredAndAlternateIdentifier());

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
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingAllFields());

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
  }

  @Test
  public void draftDoiUpdateWithoutPrefixFieldTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceRequiredOnly());

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
    JsonNode givenMetadataAfterUpdate = objectMapper.valueToTree(GenerateTestFiles.getInstanceMissingPrefix());;

    // Update the draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, DRAFT);
    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
  }

  @Test
  public void compareDraftDoiInstanceWithDataCiteResponseTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance from the file
    JsonNode givenMetadata = objectMapper.valueToTree(GenerateTestFiles.getInstanceRichMetadata());

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
    MetadataInstance cedarDataCiteInstance = DataCiteMetadataParserNew.parseDataCiteSchema(dataCiteResponse.getData().getAttributes());

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
