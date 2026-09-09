package org.metadatacenter.cedar.bridge;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance;
import org.metadatacenter.cedar.bridge.resource.datacite.form.Attributes;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSchema;
import org.metadatacenter.util.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Where this service's tolerance for other people's payloads comes from.
 *
 * <p>DataCite and the EPA own the shapes read here and add fields to them without asking. That
 * used to be survivable because every model carried {@code @JsonIgnoreProperties(ignoreUnknown =
 * true)}, which is a property of the class rather than of the read: a new model, or one someone
 * forgot to annotate, was strict by accident. The reads name the tolerant mapper now, so these
 * tests pin the policy to the read and would fail if a read went back to a strict mapper.
 */
class DataCiteJsonToleranceTest {

  private static final String RESPONSE_WITH_A_NEW_FIELD = """
      {"data": {"id": "10.82658/abc", "type": "dois",
                "attributes": {"doi": "10.82658/abc", "publicationYear": 2026,
                               "fieldDataCiteAddedLater": "and did not tell us"}}}
      """;

  @Test
  void aDataCiteResponseCarryingANewFieldStillReads() throws Exception {
    DataCiteSchema schema =
        JsonMapper.TOLERANT_MAPPER.readValue(RESPONSE_WITH_A_NEW_FIELD, DataCiteSchema.class);

    assertNotNull(schema.getData(), "the known part of the response must survive the unknown part");
  }

  @Test
  void theSameResponseIsRefusedByTheStrictPolicy() {
    assertThrows(UnrecognizedPropertyException.class,
        () -> JsonMapper.STRICT_MAPPER.readValue(RESPONSE_WITH_A_NEW_FIELD, DataCiteSchema.class),
        "the tolerance must come from the mapper, or this test proves nothing");
  }

  @Test
  void dataCiteAttributesReadFromATreeAreTolerantToo() throws Exception {
    JsonNode attributes = JsonMapper.MAPPER.readTree(RESPONSE_WITH_A_NEW_FIELD)
        .get("data").get("attributes");

    Attributes read = JsonMapper.TOLERANT_MAPPER.treeToValue(attributes, Attributes.class);

    assertEquals(2026, read.getPublicationYear(),
        "a known field must survive beside two DataCite does not share with this model");
  }

  @Test
  void anEpaSubstanceCarryingANewFieldStillReads() throws Exception {
    String detail = """
        [{"dtxsid": "DTXSID7020182", "preferredName": "Bisphenol A",
          "fieldTheEpaAddedLater": "and did not tell us"}]
        """;

    List<org.metadatacenter.cedar.bridge.resources.Substance> substances =
        JsonMapper.TOLERANT_MAPPER.readValue(
            detail, new TypeReference<List<org.metadatacenter.cedar.bridge.resources.Substance>>() {});

    assertEquals(1, substances.size());
    assertEquals("DTXSID7020182", substances.get(0).getDtxsid());
  }

  @Test
  void aStoredCedarInstanceCarryingANewFieldStillReads() throws Exception {
    String instance = """
        {"@id": "https://repo.metadatacenter.org/template-instances/abc",
         "fieldAnotherCedarReleaseAddedLater": {"@value": "and did not tell us"}}
        """;

    MetadataInstance read =
        JsonMapper.TOLERANT_MAPPER.readValue(instance, MetadataInstance.class);

    assertNotNull(read, "a stored record this service consumes must survive a newer producer");
  }
}
