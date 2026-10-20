package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.metadatacenter.cedar.bridge.CompareValues;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteInstanceValidationException;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMetadataParser;
import org.metadatacenter.cedar.bridge.DataCiteFixtures;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSchema;
import org.metadatacenter.http.CedarResponseStatus;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


@Tag("datacite")
public class DataCiteResourceTest extends AbstractBridgeServerResourceTest {
  @RegisterExtension
  public ObjectMapperExtension objectMapperExtension = new ObjectMapperExtension();
  private ObjectMapper objectMapper;

  @BeforeAll
  public static void oneTimeSetUp() {
  }

  @BeforeEach
  public void setUp() {
    objectMapper = objectMapperExtension.getObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void dataCiteInstanceRichDataTest() throws IOException, DataCiteInstanceValidationException {
    // Retrieve the given DataCite instance from the file
    JsonNode givenMetadata = DataCiteFixtures.instance("rich-metadata");

    // Create a DOI using the given DataCite instance
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceSimplyDataTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-only");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceAllRequiredDataTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("all-under-required-elements");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusSubjectTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-subject");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusContributorTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-contributor");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusDateTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-date");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusLangTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-lang");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusAlternateIdentifierTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-alternate-identifier");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRelatedIdentifierTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-related-identifier");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusSizeFormatVersionTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-size-format-version");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRightsTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-rights");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusDescriptionTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-description");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusFundingRefTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-funding-ref");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusGeoLocTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-geo-location");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRelatedItemTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-related-item");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusRandomTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-random-fields");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusEmptyStringTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-empty-string");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceRequiredPlusAllElementExpandedTest() throws IOException, DataCiteInstanceValidationException {
    JsonNode givenMetadata = DataCiteFixtures.instance("required-and-all-empty-fields");

    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), createDoiResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, createDoiResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Response getDoiMetadataResponse = getDoiMetadata(createDoiResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    JsonNode responseMetadata = objectMapper.readTree(getDoiMetadataResponse.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, responseMetadata, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void dataCiteInstanceEmptyJsonTest() throws IOException {
    JsonNode givenMetadata = getFileContentAsJson("FailEmptyJson");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingAllFieldsTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-all-fields");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPrefixFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-prefix");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPublisherFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-publisher");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingPublicationYearFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-publication-year");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingMultipleRequiredFieldsTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-multiple-required-fields");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingContributorNameFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-contributor-name");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingContributorTypeFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-contributor-type");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedIdentifierTypeFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-related-identifier-type");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelationTypeFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-relation-type");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingFunderNameFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-funder-name");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedItemTypeFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-related-item-type");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceMissingRelatedItemRelationTypeFieldTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("missing-related-item-relation-type");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceIncorrectNameTypeInputTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("wrong-name-type");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceGeoLocationBoxOutOfRangeValueTest() throws IOException {
    JsonNode givenMetadata = DataCiteFixtures.instance("data-out-of-range");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void dataCiteInstanceUnsupportedMediaTypeTest() throws IOException {
    Response createDoiResponse = client.target(baseUrlCreateDoi)
        .request(MediaType.APPLICATION_XML)
        .header("Authorization", authHeaderAdmin)
        .post(Entity.text(""));
    Assertions.assertEquals(CedarResponseStatus.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void garbageDataCiteInstanceJsonTest() throws IOException {
    JsonNode givenMetadata = getFileContentAsJson("FailGarbageJson");
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    Response createDoiResponse = createDoi(givenMetadata, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(CedarResponseStatus.BAD_REQUEST.getStatusCode(), createDoiResponse.getStatus());
  }

  @Test
  public void draftDoiSaveAndUpdateSimpleMetadataTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("required-only");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT, cedarConfig));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = DataCiteFixtures.instance("rich-metadata");

    // Update the draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, DRAFT, cedarConfig));
  }

  @Test
  public void draftDoiSaveAndUpdateRichMetadataTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("rich-metadata");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT, cedarConfig));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = DataCiteFixtures.instance("rich-metadata-rich-update");

    // Update the draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, DRAFT, cedarConfig));
  }

  @Test
  public void draftDoiSaveAndPublishSimpleMetadataTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("required-only");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT, cedarConfig));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = DataCiteFixtures.instance("required-and-random-fields");
    ;

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH, cedarConfig));
  }


  @Test
  public void draftDoiSaveAndPublishRichMetadataTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("rich-metadata");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT, cedarConfig));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = DataCiteFixtures.instance("rich-metadata-rich-update");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void draftDoiAddNewInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("rich-metadata");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT, cedarConfig));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = DataCiteFixtures.instance("rich-metadata-add-new-instance");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void draftDoiDeleteInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("rich-metadata");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT, cedarConfig));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = DataCiteFixtures.instance("rich-metadata-delete-instance");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void draftDoiDeleteAndAddNewInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("rich-metadata");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT, cedarConfig));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = DataCiteFixtures.instance("rich-metadata-delete-and-add-instance");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void draftDoiDeleteAllInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("rich-metadata");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT, cedarConfig));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = DataCiteFixtures.instance("required-and-alternate-identifier");

    // Publish the updated draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, PUBLISH);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiAfterUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the updated metadata
    Response getDoiMetadataAfterUpdate = getDoiMetadata(draftDoiAfterUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataAfterUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataAfterUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the updated DataCite instance with the response metadata
    JsonNode returnedMetadataAfterUpdate = objectMapper.readTree(getDoiMetadataAfterUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataAfterUpdate, returnedMetadataAfterUpdate, sourceArtifactId, PUBLISH, cedarConfig));
  }

  @Test
  public void draftDoiSaveWithEmptyDataCiteInstanceTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("missing-all-fields");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
  }

  @Test
  public void draftDoiUpdateWithoutPrefixFieldTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance before update from the file
    JsonNode givenMetadataBeforeUpdate = DataCiteFixtures.instance("required-only");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadataBeforeUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadataBeforeUpdate = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadataBeforeUpdate.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadataBeforeUpdate.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadataBeforeUpdate = objectMapper.readTree(getDoiMetadataBeforeUpdate.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadataBeforeUpdate, returnedMetadataBeforeUpdate, sourceArtifactId, DRAFT, cedarConfig));

    // Retrieve the given DataCite instance after update from the file
    JsonNode givenMetadataAfterUpdate = DataCiteFixtures.instance("missing-prefix");
    ;

    // Update the draft DOI metadata
    Response draftDoiAfterUpdateResponse = createDoi(givenMetadataAfterUpdate, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), draftDoiAfterUpdateResponse.getStatus());
  }

  @Test
  public void compareDraftDoiInstanceWithDataCiteResponseTest() throws IOException, DataCiteInstanceValidationException {
    String sourceArtifactId = DUMMY_SOURCE_ARTIFACT_ID_PREFIX + UUID.randomUUID();
    String userId = DUMMY_USER_ID_PREFIX + UUID.randomUUID();
    // Retrieve the given DataCite instance from the file
    JsonNode givenMetadata = DataCiteFixtures.instance("rich-metadata");

    // Create a draft DOI using the given DataCite instance
    Response draftDoiBeforeUpdateResponse = createDoi(givenMetadata, sourceArtifactId, DRAFT);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), draftDoiBeforeUpdateResponse.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, draftDoiBeforeUpdateResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Retrieve DOI from the response and using the DOI to retrieve the associated metadata
    Response getDoiMetadata = getDoiMetadata(draftDoiBeforeUpdateResponse);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), getDoiMetadata.getStatus());
    Assertions.assertEquals(MediaType.APPLICATION_JSON, getDoiMetadata.getHeaderString(HttpHeaders.CONTENT_TYPE));

    // Compare the given DataCite instance with the response metadata
    JsonNode returnedMetadata = objectMapper.readTree(getDoiMetadata.readEntity(String.class));
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(givenMetadata, returnedMetadata, sourceArtifactId, DRAFT, cedarConfig));

    // Convert the DataCite-format metadata to Cedar-format
    DataCiteSchema dataCiteResponse = objectMapper.readValue(returnedMetadata.toString(), DataCiteSchema.class);
    JsonNode convertedMetadata = DataCiteMetadataParser.parseDataCiteSchema(dataCiteResponse.getData().getAttributes(),
        userId, DataCiteFixtures.template(), cedarConfig);

    //Compare the converted Cedar-format metadata with the DataCite response metadata
    Assertions.assertTrue(CompareValues.compareResponseWithGivenMetadata(convertedMetadata, returnedMetadata, sourceArtifactId, DRAFT, cedarConfig));
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
