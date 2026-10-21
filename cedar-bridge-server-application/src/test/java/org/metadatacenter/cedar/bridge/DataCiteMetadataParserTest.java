package org.metadatacenter.cedar.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMetadataParser;
import org.metadatacenter.cedar.bridge.resource.datacite.form.Attributes;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSchema;
import org.metadatacenter.model.validation.CedarValidator;
import org.metadatacenter.model.validation.report.ValidationReport;
import org.metadatacenter.util.json.JsonMapper;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * The instance comes back as the JSON document the form receives, and goes to DataCite form again
 * the way the form's answer does, through the library's reader. That instance must also be valid
 * against the template, or the form would open on an instance its own template refuses.
 */
class DataCiteMetadataParserTest {

  private static final String USER =
      "https://metadatacenter.org/users/11111111-2222-3333-4444-555555555555";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final TemplateSchemaArtifact TEMPLATE = DataCiteFixtures.template();

  /** Every fixture the forward conversion accepts. The ones it refuses are tested there. */
  static Stream<Arguments> instances() {
    return Stream.of(
        fixture("required properties only", "required-only"),
        fixture("rich metadata", "rich-metadata"),
        fixture("every property under the required elements", "all-under-required-elements"),
        fixture("subjects", "required-and-subject"),
        fixture("contributors", "required-and-contributor"),
        fixture("dates", "required-and-date"),
        fixture("language", "required-and-lang"),
        fixture("alternate identifiers", "required-and-alternate-identifier"),
        fixture("related identifiers", "required-and-related-identifier"),
        fixture("sizes, formats and version", "required-and-size-format-version"),
        fixture("rights", "required-and-rights"),
        fixture("descriptions", "required-and-description"),
        fixture("funding references", "required-and-funding-ref"),
        fixture("geolocations", "required-and-geo-location"),
        fixture("related items", "required-and-related-item"),
        fixture("a random selection of properties", "required-and-random-fields"),
        fixture("empty strings", "required-and-empty-string"),
        fixture("every optional property empty", "required-and-all-empty-fields"),
        fixture("rich metadata after an update", "rich-metadata-rich-update"),
        fixture("rich metadata with an instance added", "rich-metadata-add-new-instance"),
        fixture("rich metadata with an instance deleted", "rich-metadata-delete-instance"),
        fixture("rich metadata with an instance deleted and another added",
            "rich-metadata-delete-and-add-instance"),
        fixture("rich metadata, as CEE posts it", "cee-rich-metadata"),
        fixture("rich metadata with an instance added, as CEE posts it", "cee-rich-metadata-add-new-instance"),
        fixture("empty strings, as CEE posts them", "cee-required-and-empty-string"),
        fixture("every optional property empty, as CEE posts it", "cee-required-and-all-empty-fields"));
  }

  private static Arguments fixture(String name, String document) {
    return arguments(name, document);
  }

  private static ObjectNode readBack(String document) throws Exception {
    DataCiteSchema sent = CedarInstanceParserTest.convert(CedarInstanceParserTest.fixture(document), "draft");
    return DataCiteMetadataParser.parseDataCiteSchema(sent.getData().getAttributes(), USER, TEMPLATE,
        CedarInstanceParserTest.config(CedarInstanceParserTest.PREFIX));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("instances")
  void aDoisMetadataSurvivesTheTripBackToCedar(String name, String document) throws Exception {
    DataCiteSchema sent = CedarInstanceParserTest.convert(CedarInstanceParserTest.fixture(document), "draft");

    DataCiteSchema resent = CedarInstanceParserTest.convert(readBack(document), "draft");

    JsonNode expected = MAPPER.valueToTree(sent);
    JsonNode actual = MAPPER.valueToTree(resent);
    assertEquals(expected, actual, () -> "the round trip changed the DataCite document:\n"
        + expected.toPrettyString() + "\nbecame\n" + actual.toPrettyString());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("instances")
  void theInstanceTheFormOpensWithIsValidAgainstTheTemplate(String name, String document) throws Exception {
    ObjectNode instance = readBack(document);

    ValidationReport report =
        new CedarValidator().validateTemplateInstance(instance, DataCiteFixtures.templateDocument());

    assertTrue(report.getErrors().isEmpty(), () -> report.getErrors() + "\n" + instance.toPrettyString());
  }

  /**
   * A term takes its IRI from the template: from the terms a field lists, or, for a field
   * constrained to a branch of DataCite's vocabulary, in that vocabulary's namespace. A term DataCite
   * does not give has none, and a link DataCite holds as an empty string is no link.
   */
  @Test
  void aResumedDraftsTermsCarryTheIrisTheTemplateGivesThem() throws Exception {
    Attributes held = JsonMapper.TOLERANT_MAPPER.readValue("""
        {"titles": [{"title": "Untyped"}, {"title": "Translated", "titleType": "TranslatedTitle"}],
         "publicationYear": 2026,
         "fundingReferences": [{"funderName": "NIH", "funderIdentifierType": "ROR"}],
         "relatedIdentifiers": [{"relatedIdentifier": "10.1/x", "relatedIdentifierType": "DOI",
                                 "relationType": "IsCitedBy", "schemeUri": ""}],
         "relatedItems": [{"relatedItemType": "Book", "relationType": "IsPartOf", "numberType": "Other",
                           "titles": [{"title": "A book"}]}]}
        """, Attributes.class);

    ObjectNode instance = DataCiteMetadataParser.parseDataCiteSchema(held, USER, TEMPLATE,
        CedarInstanceParserTest.config(CedarInstanceParserTest.PREFIX));

    assertEquals(MAPPER.createObjectNode(), instance.at("/title/0/titleType"));
    assertEquals("http://purl.org/datacite/v4.4/TranslatedTitle", instance.at("/title/1/titleType/@id").asText());
    assertEquals("http://purl.obolibrary.org/obo/BE_ROR",
        instance.at("/fundingReference/0/funderIdentifierType/@id").asText());
    assertEquals("http://purl.org/datacite/v4.4/IsCitedBy",
        instance.at("/relatedIdentifier/0/relationType/@id").asText());
    assertEquals(MAPPER.createObjectNode(), instance.at("/relatedIdentifier/0/schemeURI"));
    assertEquals("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C17649",
        instance.at("/relatedItem/0/numberType/@id").asText());
    assertEquals("Other", instance.at("/relatedItem/0/numberType/rdfs:label").asText());
    new JsonArtifactReader().readTemplateInstanceArtifact(instance);
  }
}
