package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The DOI and external-authority routes document every answer they give, each refusal in the shared
 * error body. A refusal DataCite makes carries DataCite's errors, a refusal another registry makes
 * carries the authority's answer, and the document says where. The external-authority routes take no
 * credentials and answer a registry's 401 as a 502, so neither documents a 401.
 */
class OpenApiContractTest {

  @Test
  void doiMetadataReadDocumentsEveryAnswer() throws IOException {
    JsonNode operation = readSpec().at("/paths/~1datacite~1get-doi-metadata~1{id}/get");
    assertAnswers(operation, "200", "#/components/schemas/DataCiteDoiMetadata",
        "400", "401", "404", "406", "429", "500", "502", "503");
    assertTrue(operation.at("/responses/404/description").asText().contains("objects.dataCiteErrors"),
        operation.at("/responses/404").toString());
  }

  @Test
  void doiFormStartDocumentsEveryAnswer() throws IOException {
    JsonNode operation = readSpec().at("/paths/~1datacite~1create-doi/get");
    assertAnswers(operation, "200", "#/components/schemas/DoiFormStart",
        "400", "401", "403", "404", "406", "409", "429", "500", "502", "503");
  }

  @Test
  void doiRegistrationDocumentsEveryAnswer() throws IOException {
    JsonNode operation = readSpec().at("/paths/~1datacite~1create-doi/post");
    assertAnswers(operation, "201", "#/components/schemas/DoiRegistration",
        "400", "401", "403", "404", "406", "409", "415", "429", "500", "502", "503");
    for (String status : Set.of("400", "502")) {
      assertTrue(operation.at("/responses/" + status + "/description").asText().contains("objects.dataCiteErrors"),
          operation.at("/responses/" + status).toString());
    }
  }

  @Test
  void authoritySearchDocumentsEveryAnswer() throws IOException {
    JsonNode operation = readSpec().at("/paths/~1ext-auth~1{authority}~1search-by-name/get");
    assertAnswers(operation, "200", "#/components/schemas/AuthoritySearchResults",
        "400", "404", "406", "500", "502", "503");
    assertTrue(operation.path("description").asText().contains("objects.authorityAnswer"),
        operation.path("description").asText());
    assertSaysWhichRegistryAnswersAreA502(operation);
  }

  @Test
  void authorityLookupDocumentsEveryAnswer() throws IOException {
    JsonNode operation = readSpec().at("/paths/~1ext-auth~1{authority}~1{id}/get");
    assertAnswers(operation, "200", "#/components/schemas/AuthorityDetails",
        "400", "404", "406", "500", "502", "503");
    assertTrue(operation.at("/responses/404/description").asText().contains("objects.authorityAnswer"),
        operation.at("/responses/404").toString());
    assertSaysWhichRegistryAnswersAreA502(operation);
  }

  /** The spec's own description says that DataCite and the external registries share the rule. */
  @Test
  void theDescriptionSaysWhichUpstreamAnswersAreA502() throws IOException {
    String description = readSpec().at("/info/description").asText().replaceAll("\\s+", " ");
    assertTrue(description.contains("DataCite or an external registry failing, refusing this deployment's "
        + "credentials or access, or limiting its requests answers 502"), description);
    assertTrue(description.contains("never answer 401"), description);
  }

  /**
   * A registry's status is kept only for a refusal of the request itself, so no part of the document
   * that describes the external authorities says a registry's status is kept without saying which.
   */
  @Test
  void noAuthorityDescriptionSaysEveryRefusalKeepsTheRegistrysStatus() throws IOException {
    JsonNode spec = readSpec();
    List<String> descriptions = new ArrayList<>();
    for (String path : List.of("/paths/~1ext-auth~1{authority}~1search-by-name", "/paths/~1ext-auth~1{authority}~1{id}",
        "/components/schemas/AuthoritySearchResults", "/components/schemas/AuthorityDetails", "/info")) {
      collectText(spec.at(path), descriptions);
    }
    assertFalse(descriptions.isEmpty());
    for (String description : descriptions) {
      String text = description.replaceAll("\\s+", " ");
      assertEquals(text.split("keeps the registry's status", -1).length,
          text.split("A refusal of the request itself keeps the registry's status", -1).length, text);
      assertFalse(text.contains("the status is the registry's") || text.contains("relayed the same way"), text);
    }
  }

  private static void collectText(JsonNode node, List<String> into) {
    if (node.isTextual()) {
      into.add(node.asText());
    }
    node.elements().forEachRemaining(child -> collectText(child, into));
  }

  private static void assertSaysWhichRegistryAnswersAreA502(JsonNode operation) {
    String description = operation.path("description").asText();
    assertTrue(description.contains("A registry that refuses this deployment's access (401 or 403), limits its "
        + "requests (429) or fails (5xx) is a 502"), description);
    assertTrue(description.contains("never answer 401"), description);
    String badGateway = operation.at("/responses/502/description").asText();
    assertTrue(badGateway.contains("refuses this deployment's access (401 or 403), limits its requests (429), "
        + "fails (5xx)"), badGateway);
    // A registry's own 503 is a 502, so a 503 is never a registry's answer passed on.
    String unavailable = operation.at("/responses/503/description").asText();
    assertFalse(unavailable.contains("upstreamServerError"), unavailable);
  }

  private static void assertAnswers(JsonNode operation, String successStatus, String success, String... refusals) {
    JsonNode responses = operation.path("responses");
    Set<String> documented = new TreeSet<>();
    responses.fieldNames().forEachRemaining(documented::add);
    Set<String> expected = new TreeSet<>(Set.of(refusals));
    expected.add(successStatus);
    assertEquals(expected, documented);
    assertEquals(success, responses.at("/" + successStatus + "/content/application~1json/schema/$ref").asText());
    for (String status : refusals) {
      assertEquals("#/components/schemas/CedarError",
          responses.at("/" + status + "/content/application~1json/schema/$ref").asText(), status);
      assertFalse(responses.path(status).path("description").asText().isBlank(), status);
    }
  }

  private static JsonNode readSpec() throws IOException {
    try (InputStream input = OpenApiContractTest.class.getResourceAsStream("/assets/swagger-api/swagger.json")) {
      assertNotNull(input, "generated OpenAPI document");
      return JsonMapper.STRICT_MAPPER.readTree(input);
    }
  }
}
