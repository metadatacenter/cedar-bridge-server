package org.metadatacenter.cedar.bridge;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance;
import org.metadatacenter.cedar.bridge.resource.datacite.CedarInstanceParser;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteInstanceValidationException;
import org.metadatacenter.cedar.bridge.resource.datacite.DataciteConstants;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSchema;
import org.metadatacenter.config.CedarConfig;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The CEDAR instance to DataCite conversion - the step that decides what a DOI actually says.
 * <p>
 * Nothing exercised it in a normal build: the only test that reached it goes through the live
 * DataCite API and is excluded by default. These run the conversion directly, so the mapping and
 * its refusals are checked without a network.
 * <p>
 * The refusals matter as much as the mapping. DataCite rejects an incomplete payload with an error
 * that surfaces to the user long after the fact, so the parser is meant to stop first, with a
 * message that says which property is missing.
 */
class CedarInstanceParserTest {

  private static final String PREFIX = "10.82658";
  private static final String OPENVIEW_BASE = "https://openview.metadatacenter.orgx/";
  private static final String SOURCE_ARTIFACT =
      "https://repo.metadatacenter.org/template-instances/8bc64ab5-df6b-48c8-8c61-6c016245918e";

  /**
   * The fixture builder leaves its PREFIX static unset, so every instance it produces would be
   * refused for a missing prefix before reaching anything else. Setting it once here makes the
   * fixtures usable for the mapping cases; the missing-prefix case builds its own instance instead.
   */
  @BeforeAll
  static void givePrefixToTheFixtures() throws Exception {
    Field prefix = GenerateMetadataInstanceTests.class.getDeclaredField("PREFIX");
    prefix.setAccessible(true);
    prefix.set(null, PREFIX);
  }

  private static CedarConfig config(String prefix) {
    CedarConfig config = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(config.getBridgeConfig().getDataCite().getPrefix()).thenReturn(prefix);
    when(config.getServers().getOpenview().getUriBase()).thenReturn(OPENVIEW_BASE);
    return config;
  }

  private static DataCiteSchema convert(MetadataInstance instance, String state)
      throws DataCiteInstanceValidationException {
    DataCiteSchema schema = new DataCiteSchema();
    CedarInstanceParser.parseCedarInstance(instance, schema, SOURCE_ARTIFACT, state, config(PREFIX));
    return schema;
  }

  @Test
  void aRequiredOnlyInstanceConverts() throws Exception {
    DataCiteSchema schema = convert(GenerateMetadataInstanceTests.getInstanceRequiredOnly(), "publish");

    assertNotNull(schema.getData(), "the conversion should populate the payload");
    assertEquals("dois", schema.getData().getType());
    assertEquals(PREFIX, schema.getData().getAttributes().getPrefix());
    assertEquals(DataciteConstants.PUBLISHER, schema.getData().getAttributes().getPublisher());
  }

  /** The landing page DataCite will resolve the DOI to. A wrong one is a dead DOI. */
  @Test
  void theLandingPageUrlPointsAtTheSourceArtifact() throws Exception {
    DataCiteSchema schema = convert(GenerateMetadataInstanceTests.getInstanceRequiredOnly(), "publish");

    String url = schema.getData().getAttributes().getUrl();
    assertNotNull(url, "a DOI without a landing page is useless");
    assertTrue(url.startsWith(OPENVIEW_BASE), "should resolve through OpenView: " + url);
    assertTrue(url.contains("8bc64ab5-df6b-48c8-8c61-6c016245918e"),
        "should carry the source artifact id: " + url);
  }

  /**
   * Draft and publish are the same conversion with a different event, and the event is what decides
   * whether the DOI becomes public. Swapping them is not recoverable: a registered DOI cannot be
   * withdrawn.
   */
  @Test
  void theStateBecomesTheDataCiteEvent() throws Exception {
    assertEquals("publish",
        convert(GenerateMetadataInstanceTests.getInstanceRequiredOnly(), "publish")
            .getData().getAttributes().getEvent());
    assertEquals("draft",
        convert(GenerateMetadataInstanceTests.getInstanceRequiredOnly(), "draft")
            .getData().getAttributes().getEvent());
  }

  @Test
  void theSchemaVersionIsTheDataCiteKernel() throws Exception {
    assertEquals("http://datacite.org/schema/kernel-4",
        convert(GenerateMetadataInstanceTests.getInstanceRequiredOnly(), "publish")
            .getData().getAttributes().getSchemaVersion());
  }

  @Test
  void richMetadataCarriesItsCreatorsAndTitles() throws Exception {
    DataCiteSchema schema = convert(GenerateMetadataInstanceTests.getInstanceRichMetadata(), "publish");

    assertNotNull(schema.getData().getAttributes().getCreators());
    assertTrue(schema.getData().getAttributes().getCreators().size() >= 1, "creators should carry over");
    assertNotNull(schema.getData().getAttributes().getTitles());
    assertTrue(schema.getData().getAttributes().getTitles().size() >= 1, "titles should carry over");
  }

  // The refusals

  @Test
  void anInstanceWithNoPrefixIsRefused() {
    DataCiteInstanceValidationException refused = assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(GenerateMetadataInstanceTests.getInstanceMissingPrefix(), "publish"));
    assertTrue(refused.getMessage().toLowerCase().contains("prefix"),
        "the message should name the property: " + refused.getMessage());
  }

  /**
   * A prefix belonging to someone else must not be accepted: DOIs are minted under CEDAR's own
   * prefix, and registering under another is not ours to do.
   */
  @Test
  void anInstanceWithSomeoneElsesPrefixIsRefused() {
    DataCiteSchema schema = new DataCiteSchema();
    DataCiteInstanceValidationException refused = assertThrows(DataCiteInstanceValidationException.class,
        () -> CedarInstanceParser.parseCedarInstance(
            GenerateMetadataInstanceTests.getInstanceRequiredOnly(), schema, SOURCE_ARTIFACT,
            "publish", config("10.99999")));
    assertTrue(refused.getMessage().toLowerCase().contains("prefix"), refused.getMessage());
  }

  @Test
  void anInstanceMissingThePublisherIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(GenerateMetadataInstanceTests.getInstanceMissingPublisher(), "publish"));
  }

  @Test
  void anInstanceMissingThePublicationYearIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(GenerateMetadataInstanceTests.getInstanceMissingPublicationYear(), "publish"));
  }

  @Test
  void anInstanceMissingSeveralRequiredPropertiesIsRefused() {
    DataCiteInstanceValidationException refused = assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(GenerateMetadataInstanceTests.getInstanceMissingMultipleRequiredFields(), "publish"));
    assertNotNull(refused.getMessage());
  }

  /** Coordinates outside the geographic bounds are stopped here rather than at DataCite. */
  @Test
  void anInstanceWithOutOfRangeCoordinatesIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(GenerateMetadataInstanceTests.getInstanceDataOutOfRange(), "publish"));
  }

  @Test
  void anInstanceMissingAContributorTypeIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(GenerateMetadataInstanceTests.getInstanceMissingContributorType(), "publish"));
  }

  @Test
  void anInstanceMissingAFunderNameIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(GenerateMetadataInstanceTests.getInstanceMissingFunderName(), "publish"));
  }
}
