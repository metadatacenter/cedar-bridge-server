package org.metadatacenter.cedar.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMetadataParser;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSchema;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * The DataCite to CEDAR conversion, which turns an existing DOI's metadata back into an instance
 * the editor shows and the user updates.
 * <p>
 * The live DataCite suite checks it against DataCite's own response and is excluded by default, so
 * a normal build ran almost none of it. Here each fixture instance goes to DataCite form, back to a
 * CEDAR instance, and to DataCite form again, and the two DataCite documents must agree. A property
 * the reverse conversion drops or alters would vanish from a DOI the next time someone updates it,
 * so the comparison covers the whole document rather than chosen properties.
 * <p>
 * The comparison is between DataCite documents, not between the classes that hold a CEDAR instance
 * here, so it stays meaningful if those classes are replaced.
 */
class DataCiteMetadataParserTest {

  private static final String USER =
      "https://metadatacenter.org/users/11111111-2222-3333-4444-555555555555";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @BeforeAll
  static void givePrefixToTheFixtures() throws Exception {
    CedarInstanceParserTest.givePrefixToTheFixtures();
  }

  @AfterAll
  static void restoreTheFixturePrefix() throws Exception {
    CedarInstanceParserTest.restoreTheFixturePrefix();
  }

  /** Every fixture the forward conversion accepts. The ones it refuses are tested there. */
  static Stream<Arguments> instances() {
    return Stream.of(
        fixture("required properties only", GenerateMetadataInstanceTests::getInstanceRequiredOnly),
        fixture("rich metadata", GenerateMetadataInstanceTests::getInstanceRichMetadata),
        fixture("every property under the required elements",
            GenerateMetadataInstanceTests::getInstanceAllUnderRequiredElements),
        fixture("subjects", GenerateMetadataInstanceTests::getInstanceRequiredAndSubject),
        fixture("contributors", GenerateMetadataInstanceTests::getInstanceRequiredAndContributor),
        fixture("dates", GenerateMetadataInstanceTests::getInstanceRequiredAndDate),
        fixture("language", GenerateMetadataInstanceTests::getInstanceRequiredAndLang),
        fixture("alternate identifiers",
            GenerateMetadataInstanceTests::getInstanceRequiredAndAlternateIdentifier),
        fixture("related identifiers",
            GenerateMetadataInstanceTests::getInstanceRequiredAndRelatedIdentifier),
        fixture("sizes, formats and version",
            GenerateMetadataInstanceTests::getInstanceRequiredAndSizeFormatVersion),
        fixture("rights", GenerateMetadataInstanceTests::getInstanceRequiredAndRights),
        fixture("descriptions", GenerateMetadataInstanceTests::getInstanceRequiredAndDescription),
        fixture("funding references", GenerateMetadataInstanceTests::getInstanceRequiredAndFundingRef),
        fixture("geolocations", GenerateMetadataInstanceTests::getInstanceRequiredAndGeoLocation),
        fixture("related items", GenerateMetadataInstanceTests::getInstanceRequiredAndRelatedItem),
        fixture("a random selection of properties",
            GenerateMetadataInstanceTests::getInstanceRequiredAndRandomFields),
        fixture("empty strings", GenerateMetadataInstanceTests::getInstanceRequiredAndEmptyString),
        fixture("every optional property empty",
            GenerateMetadataInstanceTests::getInstanceRequiredAndAllEmptyFields),
        fixture("rich metadata after an update",
            GenerateMetadataInstanceTests::getInstanceRichMetadataRichUpdate),
        fixture("rich metadata with an instance added",
            GenerateMetadataInstanceTests::getInstanceRichMetadataAddNewInstance),
        fixture("rich metadata with an instance deleted",
            GenerateMetadataInstanceTests::getInstanceRichMetadataDeleteInstance),
        fixture("rich metadata with an instance deleted and another added",
            GenerateMetadataInstanceTests::getInstanceRichMetadataDeleteAndAddInstance));
  }

  private static Arguments fixture(String name, Supplier<MetadataInstance> instance) {
    return arguments(name, instance);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("instances")
  void aDoisMetadataSurvivesTheTripBackToCedar(String name, Supplier<MetadataInstance> instance)
      throws Exception {
    DataCiteSchema sent = CedarInstanceParserTest.convert(instance.get(), "draft");

    MetadataInstance readBack = DataCiteMetadataParser.parseDataCiteSchema(
        sent.getData().getAttributes(), USER, CedarInstanceParserTest.config(CedarInstanceParserTest.PREFIX));
    DataCiteSchema resent = CedarInstanceParserTest.convert(readBack, "draft");

    JsonNode expected = MAPPER.valueToTree(sent);
    JsonNode actual = MAPPER.valueToTree(resent);
    assertEquals(expected, actual, () -> "the round trip changed the DataCite document:\n"
        + expected.toPrettyString() + "\nbecame\n" + actual.toPrettyString());
  }
}
