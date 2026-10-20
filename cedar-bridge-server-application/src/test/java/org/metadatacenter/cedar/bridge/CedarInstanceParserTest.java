package org.metadatacenter.cedar.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;
import org.metadatacenter.cedar.bridge.resource.datacite.CedarInstanceParser;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteInstanceValidationException;
import org.metadatacenter.cedar.bridge.resource.datacite.DataciteConstants;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteRelatedIdentifier;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSchema;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.LinkedDataConfig;
import org.metadatacenter.server.jsonld.LinkedDataUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

  static final String PREFIX = "10.82658";
  private static final String OPENVIEW_BASE = "https://openview.metadatacenter.orgx/";
  private static final String REPOSITORY_BASE = "https://repo.metadatacenter.org/";
  private static final String SOURCE_ARTIFACT =
      "https://repo.metadatacenter.org/template-instances/8bc64ab5-df6b-48c8-8c61-6c016245918e";

  static CedarConfig config(String prefix) {
    CedarConfig config = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(config.getBridgeConfig().getDataCite().getPrefix()).thenReturn(prefix);
    when(config.getServers().getOpenview().getUriBase()).thenReturn(OPENVIEW_BASE);
    LinkedDataConfig linkedData = mock(LinkedDataConfig.class);
    when(linkedData.getBase()).thenReturn(REPOSITORY_BASE);
    LinkedDataUtil linkedDataUtil = new LinkedDataUtil(linkedData);
    when(config.getLinkedDataUtil()).thenReturn(linkedDataUtil);
    return config;
  }

  /** The named fixture under this test's prefix. */
  static ObjectNode fixture(String name) {
    return DataCiteFixtures.instance(name, PREFIX);
  }

  /** Converts an instance document as the create-doi route does, reading it into the model first. */
  static DataCiteSchema convert(ObjectNode instance, String state) throws DataCiteInstanceValidationException {
    DataCiteSchema schema = new DataCiteSchema();
    CedarInstanceParser.parseCedarInstance(new JsonArtifactReader().readTemplateInstanceArtifact(instance), schema,
        SOURCE_ARTIFACT, state, config(PREFIX));
    return schema;
  }

  @Test
  void aRequiredOnlyInstanceConverts() throws Exception {
    DataCiteSchema schema = convert(fixture("required-only"), "publish");

    assertNotNull(schema.getData(), "the conversion should populate the payload");
    assertEquals("dois", schema.getData().getType());
    assertEquals(PREFIX, schema.getData().getAttributes().getPrefix());
    assertEquals(DataciteConstants.PUBLISHER, schema.getData().getAttributes().getPublisher());
  }

  /** The landing page DataCite will resolve the DOI to. A wrong one is a dead DOI. */
  @Test
  void theLandingPageUrlPointsAtTheSourceArtifact() throws Exception {
    DataCiteSchema schema = convert(fixture("required-only"), "publish");

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
        convert(fixture("required-only"), "publish")
            .getData().getAttributes().getEvent());
    assertEquals("draft",
        convert(fixture("required-only"), "draft")
            .getData().getAttributes().getEvent());
  }

  @Test
  void theSchemaVersionIsTheDataCiteKernel() throws Exception {
    assertEquals("http://datacite.org/schema/kernel-4",
        convert(fixture("required-only"), "publish")
            .getData().getAttributes().getSchemaVersion());
  }

  /**
   * The instance is a record this service consumes rather than owns, and a later template or CEE can
   * add to it. A child the mapping does not name is read and left alone.
   */
  @Test
  void anInstanceCarryingAFieldTheMappingDoesNotNameStillConverts() throws Exception {
    ObjectNode instance = fixture("required-only");
    instance.putObject("fieldAnotherCedarReleaseAddedLater").put("@value", "and did not tell us");

    DataCiteSchema schema = convert(instance, "publish");

    assertEquals(DataciteConstants.PUBLISHER, schema.getData().getAttributes().getPublisher());
  }

  /**
   * The whole loop: a DOI's metadata opens the form as an instance, CEE loads that instance and
   * emits its own, and the emission must convert to the DataCite document the loop started from.
   */
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"rich-metadata", "rich-metadata-add-new-instance", "required-and-empty-string",
      "required-and-all-empty-fields"})
  void whatCeePostsConvertsToTheDocumentTheFormOpenedFrom(String name) throws Exception {
    JsonNode expected = new ObjectMapper().valueToTree(convert(fixture(name), "publish"));
    JsonNode actual = new ObjectMapper().valueToTree(convert(fixture("cee-" + name), "publish"));

    assertEquals(expected, actual);
  }

  /**
   * The form opened with no draft carries the DOI prefix, landing page, publisher, year and resource
   * type, and DataCite accepts all five. What it still needs from the user is a creator and a title.
   */
  @Test
  void whatCeePostsForTheDefaultFormNeedsOnlyACreatorAndATitle() {
    DataCiteInstanceValidationException refused = assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(fixture("cee-default"), "publish"));

    assertEquals("The following fields are required:\nCreator Name under Creators\nTitle under Titles\n",
        refused.getMessage());
  }

  /** The scheme URI of a related identifier is its link's IRI. It once went to DataCite as "". */
  @Test
  void aRelatedIdentifiersSchemeUriReachesDataCite() throws Exception {
    List<DataCiteRelatedIdentifier> sent = convert(fixture("required-and-related-identifier"), "publish")
        .getData().getAttributes().getRelatedIdentifiers();

    assertEquals("https://github.com/citation-style-language/schema/raw/master/csl-data.json",
        sent.get(0).getSchemeUri());
    assertNull(sent.get(1).getSchemeUri(), "an empty link is no scheme URI");
  }

  /** An entry of a repeated element that holds no value says nothing, however many of them there are. */
  @Test
  void aRepeatedElementOfBlankEntriesIsNotSent() throws Exception {
    ObjectNode instance = fixture("required-and-subject");
    ArrayNode subjects = (ArrayNode) instance.get("subject");
    ObjectNode blank = blanked(subjects.get(0).deepCopy());
    subjects.removeAll().add(blank).add(blank.deepCopy());

    assertNull(convert(instance, "publish").getData().getAttributes().getSubjects());
  }

  /** A blank entry beside filled ones is still sent, so a blank contributor still lacks a name. */
  @Test
  void aBlankEntryBesideFilledOnesIsStillSent() throws Exception {
    ObjectNode instance = fixture("required-and-contributor");
    ArrayNode contributors = (ArrayNode) instance.get("contributor");
    contributors.add(blanked(contributors.get(0).deepCopy()));

    DataCiteInstanceValidationException refused =
        assertThrows(DataCiteInstanceValidationException.class, () -> convert(instance, "publish"));
    assertTrue(refused.getMessage().contains("Contributor Name under Contributors"), refused.getMessage());
  }

  /** The entry with every value taken out, at every depth, keeping its identifier and its context. */
  private static ObjectNode blanked(ObjectNode entry) {
    entry.properties().forEach(child -> {
      if (child.getKey().startsWith("@")) {
        return;
      }
      JsonNode value = child.getValue();
      if (value.isArray()) {
        ((ArrayNode) value).removeAll();
      } else if (value instanceof ObjectNode node && node.has("@context")) {
        blanked(node);
      } else if (value instanceof ObjectNode node) {
        node.remove(List.of("@id", "rdfs:label"));
        if (node.has("@value")) {
          node.putNull("@value");
        }
      }
    });
    return entry;
  }

  @Test
  void richMetadataCarriesItsCreatorsAndTitles() throws Exception {
    DataCiteSchema schema = convert(fixture("rich-metadata"), "publish");

    assertNotNull(schema.getData().getAttributes().getCreators());
    assertTrue(schema.getData().getAttributes().getCreators().size() >= 1, "creators should carry over");
    assertNotNull(schema.getData().getAttributes().getTitles());
    assertTrue(schema.getData().getAttributes().getTitles().size() >= 1, "titles should carry over");
  }

  // The refusals

  @Test
  void anInstanceWithNoPrefixIsRefused() {
    DataCiteInstanceValidationException refused = assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(fixture("missing-prefix"), "publish"));
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
            new JsonArtifactReader().readTemplateInstanceArtifact(fixture("required-only")), schema,
            SOURCE_ARTIFACT, "publish", config("10.99999")));
    assertTrue(refused.getMessage().toLowerCase().contains("prefix"), refused.getMessage());
  }

  @Test
  void anInstanceMissingThePublisherIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(fixture("missing-publisher"), "publish"));
  }

  @Test
  void anInstanceMissingThePublicationYearIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(fixture("missing-publication-year"), "publish"));
  }

  @Test
  void anInstanceMissingSeveralRequiredPropertiesIsRefused() {
    DataCiteInstanceValidationException refused = assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(fixture("missing-multiple-required-fields"), "publish"));
    assertNotNull(refused.getMessage());
  }

  /** Coordinates outside the geographic bounds are stopped here rather than at DataCite. */
  @Test
  void anInstanceWithOutOfRangeCoordinatesIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(fixture("data-out-of-range"), "publish"));
  }

  @Test
  void anInstanceMissingAContributorTypeIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(fixture("missing-contributor-type"), "publish"));
  }

  @Test
  void anInstanceMissingAFunderNameIsRefused() {
    assertThrows(DataCiteInstanceValidationException.class,
        () -> convert(fixture("missing-funder-name"), "publish"));
  }
}
