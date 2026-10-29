package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.metadatacenter.cedar.bridge.CompareValues;
import org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.util.http.CedarError;
import org.metadatacenter.util.json.JsonMapper;

import jakarta.ws.rs.core.Response;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The whole mint and attach contract, offline. Each case registers a DOI through the routes against a
 * DataCite fake that keeps what it registers, reads it back through the metadata route, and holds what
 * DataCite received to the metadata the form posted. A published DOI must also be recorded on the
 * artifact and in the artifact's DOI record.
 *
 * <p>This replaces a suite that called DataCite's test API and could not pass: its source artifacts
 * were identifiers no resource server held, so every positive case stopped at the artifact read. The
 * metadata cases are the same fixtures it used. The repository credential is checked here as DataCite
 * checks it, on every request.
 */
class DataCiteResourceTest extends AbstractDoiWorkflowTest {

  @ParameterizedTest
  @ValueSource(strings = {"rich-metadata", "required-only", "all-under-required-elements", "required-and-subject",
      "required-and-contributor", "required-and-date", "required-and-lang", "required-and-alternate-identifier",
      "required-and-related-identifier", "required-and-size-format-version", "required-and-rights",
      "required-and-description", "required-and-funding-ref", "required-and-geo-location", "required-and-related-item",
      "required-and-random-fields", "required-and-empty-string", "required-and-all-empty-fields"})
  void publishesTheMetadataAndRecordsTheDoiOnTheArtifact(String fixture) throws Exception {
    String source = newSource();
    JsonNode given = metadata(fixture);

    Response response = resource().createDOI(source, "publish", given);

    assertEquals(201, response.getStatus(), () -> String.valueOf(response.getEntity()));
    String doi = doiOf(response);
    assertEquals("https://doi.org/" + doi, response.getLocation().toString());
    assertEquals("https://doi.org/" + doi, entity(response).get("doiName"));
    assertEquals(List.of(doi), dataCite.doisFor(landingPage(source)));
    assertEquals("findable", dataCite.state(doi));
    assertEquals("https://doi.org/" + doi, resourceServer.carriedDoi(source));
    assertEquals(DoiRegistrationState.ATTACHED, recorded(source).state());
    assertTrue(CompareValues.compareResponseWithGivenMetadata(given, readBack(doi), source, "publish", config));
  }

  /** A draft is saved, changed, and saved again in place, and the artifact carries nothing until it is published. */
  @ParameterizedTest
  @CsvSource({
      "required-only, rich-metadata",
      "rich-metadata, rich-metadata-rich-update",
      "rich-metadata, rich-metadata-add-new-instance",
      "rich-metadata, rich-metadata-delete-instance",
      "rich-metadata, rich-metadata-delete-and-add-instance",
      "rich-metadata, required-and-alternate-identifier"})
  void updatesADraftInPlaceAndThenPublishesIt(String first, String second) throws Exception {
    String source = newSource();

    Response saved = resource().createDOI(source, "draft", metadata(first));
    assertEquals(201, saved.getStatus(), () -> String.valueOf(saved.getEntity()));
    String doi = doiOf(saved);
    assertTrue(CompareValues.compareResponseWithGivenMetadata(metadata(first), readBack(doi), source, "draft", config));
    assertEquals("draft", dataCite.state(doi));

    Response updated = resource().createDOI(source, "draft", metadata(second));
    assertEquals(201, updated.getStatus());
    assertEquals(doi, doiOf(updated), "the draft is updated, not duplicated");
    assertTrue(CompareValues.compareResponseWithGivenMetadata(metadata(second), readBack(doi), source, "draft", config));
    assertNull(resourceServer.carriedDoi(source), "a draft is not recorded on the artifact");
    assertEquals(DoiRegistrationState.DRAFT, recorded(source).state());

    Response published = resource().createDOI(source, "publish", metadata(second));
    assertEquals(201, published.getStatus());
    assertEquals(doi, doiOf(published));
    assertEquals("findable", dataCite.state(doi));
    assertEquals(List.of(doi), dataCite.doisFor(landingPage(source)));
    assertEquals("https://doi.org/" + doi, resourceServer.carriedDoi(source));
    assertEquals(DoiRegistrationState.ATTACHED, recorded(source).state());
  }

  /** The form opens on the draft the artifact's record names, converted back from what DataCite holds. */
  @Test
  void theFormOpensOnTheSavedDraft() throws Exception {
    String source = newSource();
    String doi = doiOf(resource().createDOI(source, "draft", metadata("rich-metadata")));

    Response start = resource().createDOIStart(source);

    assertEquals(200, start.getStatus());
    assertEquals(TextNode.valueOf(doi), entity(start).get("draftDoi"));
    JsonNode resumed = assertInstanceOf(JsonNode.class, entity(start).get("existingDataCiteMetadata"));
    assertTrue(CompareValues.compareResponseWithGivenMetadata(resumed, readBack(doi), source, "draft", config),
        "the form opens on what DataCite holds");
    assertEquals(1, dataCite.count(FakeDataCite.Call.LIST),
        "only the first save looked for a draft begun before CEDAR kept records");
  }

  /** Metadata the conversion refuses reaches neither DataCite nor the record. */
  @ParameterizedTest
  @ValueSource(strings = {"missing-all-fields", "missing-prefix", "missing-publisher", "missing-publication-year",
      "missing-multiple-required-fields", "missing-contributor-type", "missing-funder-name", "data-out-of-range"})
  void refusesMetadataThatCannotBeRegistered(String fixture) throws Exception {
    String source = newSource();

    Response response = resource().createDOI(source, "publish", metadata(fixture));

    assertEquals(400, response.getStatus());
    assertEquals(CedarErrorKey.INVALID_INPUT.getValue(), assertInstanceOf(CedarError.class, response.getEntity()).errorKey);
    assertTrue(dataCite.calls().isEmpty(), dataCite.calls().toString());
    assertNull(recorded(source));
    assertNull(resourceServer.carriedDoi(source));
  }

  @Test
  void refusesADocumentThatIsNotAnInstance() throws Exception {
    String source = newSource();

    Response empty = resource().createDOI(source, "publish", metadata("missing-all-fields").removeAll());
    Response garbage = resource().createDOI(source, "draft",
        JsonMapper.STRICT_MAPPER.readTree("{\"menu\": {\"id\": \"file\", \"value\": \"File\"}}"));

    assertEquals(400, empty.getStatus());
    assertEquals(400, garbage.getStatus());
    assertTrue(dataCite.calls().isEmpty(), dataCite.calls().toString());
    assertNull(recorded(source));
  }

  /** DataCite's own refusal of the metadata reaches the caller in full, and nothing is recorded on the artifact. */
  @Test
  void relaysDataCitesRefusalOfTheMetadata() throws Exception {
    String source = newSource();
    dataCite.refuse(FakeDataCite.Call.CREATE, 422);

    Response response = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(400, response.getStatus());
    CedarError error = assertInstanceOf(CedarError.class, response.getEntity());
    assertEquals(CedarErrorKey.INVALID_INPUT.getValue(), error.errorKey);
    assertEquals("Refused", error.message);
    assertTrue(dataCite.dois().isEmpty());
    assertNull(resourceServer.carriedDoi(source));
  }

  /**
   * Every request carries the repository credential the server is configured with. DataCite refusing
   * it is a CEDAR configuration problem, which every route reports as a bad gateway, and it leaves
   * nothing registered and nothing recorded.
   */
  @Test
  void everyRequestCarriesTheRepositoryCredentialAndARefusalIsABadGateway() throws Exception {
    String source = newSource();
    Response saved = resource().createDOI(source, "draft", metadata("required-only"));
    assertEquals(201, saved.getStatus(), "the fake answers only a request carrying the configured credential");
    String doi = doiOf(saved);

    dataCite.refuseCredentials();
    String other = newSource();
    List<Response> responses = List.of(resource().createDOIStart(other),
        resource().createDOI(other, "publish", metadata("required-only")),
        resource().createDOI(source, "publish", metadata("required-only")),
        resource().getDOIMetadata("https://doi.org/" + doi));

    for (Response response : responses) {
      assertEquals(502, response.getStatus());
      CedarError error = assertInstanceOf(CedarError.class, response.getEntity());
      assertEquals(401, error.parameters.get("upstreamStatusCode"), error.message);
      assertEquals("DataCite", error.parameters.get("upstreamService"));
    }
    assertTrue(assertInstanceOf(CedarError.class, responses.get(2).getEntity()).message
        .startsWith("DataCite refused the repository credentials this server holds"));
    assertEquals(List.of(doi), dataCite.dois());
    assertEquals("draft", dataCite.state(doi));
    assertNull(recorded(other));
    assertNull(resourceServer.carriedDoi(source));
  }

  /** A DOI DataCite does not hold is a not found the caller can act on, with DataCite's errors. */
  @Test
  void aDoiDataCiteDoesNotHoldIsANotFound() throws Exception {
    Response response = resource().getDOIMetadata("https://doi.org/" + PREFIX + "/zzzz-zz00");

    assertEquals(404, response.getStatus());
    CedarError error = assertInstanceOf(CedarError.class, response.getEntity());
    assertEquals(CedarErrorKey.UPSTREAM_SERVER_ERROR.getValue(), error.errorKey);
    assertInstanceOf(JsonNode.class, error.objects.get("dataCiteErrors"));
  }
}
