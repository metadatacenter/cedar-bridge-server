package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cedar.bridge.CompareValues;
import org.metadatacenter.cedar.bridge.resources.doi.DoiRegistration;
import org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.util.http.CedarError;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.metadatacenter.cedar.bridge.resources.FakeDataCite.Call.CREATE;
import static org.metadatacenter.cedar.bridge.resources.FakeDataCite.Call.READ;
import static org.metadatacenter.cedar.bridge.resources.FakeDataCite.Call.UPDATE;
import static org.metadatacenter.cedar.bridge.resources.FakeDataCite.Fault.ANSWER_LOST;
import static org.metadatacenter.cedar.bridge.resources.FakeDataCite.Fault.NEVER_ARRIVED;

/**
 * A request that fails part way, repeated, continues with the DOI the first attempt began, so an
 * artifact never ends up with two DOIs, or with one it does not carry. Each repeat runs on a newly
 * started server, which knows only what the graph recorded.
 */
class DoiMintingRecoveryTest extends AbstractDoiWorkflowTest {

  // Retry after a timeout

  /** DataCite published the DOI and the answer was lost. The retry reads it there and records it on the artifact. */
  @Test
  void aPublishWhoseAnswerWasLostIsCompletedByTheRetry() throws Exception {
    String source = newSource();
    dataCite.fail(CREATE, ANSWER_LOST);

    Response timedOut = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(502, timedOut.getStatus());
    assertEquals("The DataCite DOI request failed; publication state is unknown",
        assertInstanceOf(CedarError.class, timedOut.getEntity()).message);
    String doi = recorded(source).doi();
    assertEquals(DoiRegistrationState.RESERVED, recorded(source).state());
    assertEquals(List.of(doi), dataCite.doisFor(landingPage(source)), "DataCite carried out the registration");
    assertNull(resourceServer.carriedDoi(source));

    int callsBefore = dataCite.calls().size();
    Response retry = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(201, retry.getStatus());
    assertEquals(doi, doiOf(retry));
    assertEquals(List.of(READ), dataCite.calls().subList(callsBefore, dataCite.calls().size()),
        "the retry reads the reserved DOI and registers nothing");
    assertEquals(List.of(doi), dataCite.doisFor(landingPage(source)));
    assertEquals("https://doi.org/" + doi, resourceServer.carriedDoi(source));
    assertEquals(DoiRegistrationState.ATTACHED, recorded(source).state());
  }

  /** The request never reached DataCite. The retry registers the DOI the first attempt reserved. */
  @Test
  void aPublishThatNeverReachedDataCiteRegistersTheReservedDoiOnRetry() throws Exception {
    String source = newSource();
    dataCite.fail(CREATE, NEVER_ARRIVED);

    assertEquals(502, resource().createDOI(source, "publish", metadata("required-only")).getStatus());
    String reserved = recorded(source).doi();
    assertTrue(dataCite.dois().isEmpty());

    Response retry = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(201, retry.getStatus());
    assertEquals(reserved, doiOf(retry));
    assertEquals(List.of(reserved), dataCite.dois());
    assertEquals("findable", dataCite.state(reserved));
    assertEquals("https://doi.org/" + reserved, resourceServer.carriedDoi(source));
  }

  /** A draft save whose answer was lost is resumed: the retry updates the draft the first attempt registered. */
  @Test
  void aDraftSaveWhoseAnswerWasLostIsResumedInPlace() throws Exception {
    String source = newSource();
    dataCite.fail(CREATE, ANSWER_LOST);
    assertEquals(502, resource().createDOI(source, "draft", metadata("required-only")).getStatus());
    String doi = recorded(source).doi();

    Response start = resource().createDOIStart(source);
    assertEquals(TextNode.valueOf(doi), entity(start).get("draftDoi"), "the form opens on the registered draft");

    Response retry = resource().createDOI(source, "draft", metadata("rich-metadata"));

    assertEquals(201, retry.getStatus());
    assertEquals(doi, doiOf(retry));
    assertEquals(List.of(doi), dataCite.dois());
    assertEquals("draft", dataCite.state(doi));
    assertTrue(CompareValues.compareResponseWithGivenMetadata(metadata("rich-metadata"), readBack(doi), source,
        "draft", config), "DataCite holds the metadata of the retry");
    assertEquals(1, dataCite.count(UPDATE));
    assertEquals(DoiRegistrationState.DRAFT, recorded(source).state());
  }

  /** An update DataCite carried out but did not answer is repeated: updating a DOI twice is harmless. */
  @Test
  void aPublishingUpdateWhoseAnswerWasLostIsRepeated() throws Exception {
    String source = newSource();
    String doi = doiOf(resource().createDOI(source, "draft", metadata("required-only")));
    dataCite.fail(UPDATE, ANSWER_LOST);

    assertEquals(502, resource().createDOI(source, "publish", metadata("required-only")).getStatus());
    assertEquals("findable", dataCite.state(doi));
    assertNull(resourceServer.carriedDoi(source));

    Response retry = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(201, retry.getStatus());
    assertEquals(doi, doiOf(retry));
    assertEquals(List.of(doi), dataCite.dois());
    assertEquals("https://doi.org/" + doi, resourceServer.carriedDoi(source));
    assertEquals(DoiRegistrationState.ATTACHED, recorded(source).state());
  }

  // A failed recording on the artifact

  /**
   * DataCite published the DOI and the resource server did not record it. The answer names the DOI and
   * asks for reconciliation; the form start and a draft save report the same and write nothing; and
   * repeating the publish records the DOI without asking DataCite anything.
   */
  @Test
  void aFailedRecordingIsResolvedByRepeatingThePublish() throws Exception {
    String source = newSource();
    resourceServer.failNextCommand(500);

    Response failed = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(502, failed.getStatus());
    CedarError error = assertInstanceOf(CedarError.class, failed.getEntity());
    String doi = recorded(source).doi();
    assertEquals(true, error.parameters.get("reconciliationRequired"));
    assertEquals("https://doi.org/" + doi, error.parameters.get("doi"));
    assertEquals(source, error.parameters.get("sourceArtifactId"));
    assertEquals(500, error.parameters.get("annotationStatus"));
    assertEquals(DoiRegistrationState.PUBLISHED, recorded(source).state());
    assertEquals("findable", dataCite.state(doi));

    int callsBefore = dataCite.calls().size();
    for (Response pending : List.of(resource().createDOIStart(source),
        resource().createDOI(source, "draft", metadata("rich-metadata")))) {
      assertEquals(409, pending.getStatus());
      CedarError conflict = assertInstanceOf(CedarError.class, pending.getEntity());
      assertEquals(CedarErrorKey.DOI_ALREADY_EXISTS.getValue(), conflict.errorKey);
      assertEquals("https://doi.org/" + doi, conflict.parameters.get("doi"));
      assertEquals(true, conflict.parameters.get("reconciliationRequired"));
    }
    assertNull(resourceServer.carriedDoi(source), "neither the form nor a draft save writes the artifact");

    Response resolved = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(201, resolved.getStatus());
    assertEquals(doi, doiOf(resolved));
    assertEquals(callsBefore, dataCite.calls().size(), "resolving the recording asks DataCite nothing");
    assertEquals(List.of(doi), dataCite.dois());
    assertEquals("https://doi.org/" + doi, resourceServer.carriedDoi(source));
    assertEquals(DoiRegistrationState.ATTACHED, recorded(source).state());
  }

  /**
   * An operator may record the DOI on the artifact by hand, through the resource server's DOI command,
   * as the backend runbook's recovery procedure does. The next request finds the artifact carrying the
   * recorded DOI, refuses a second one, and brings the record up to date.
   */
  @Test
  void aDoiRecordedByHandBringsTheRecordUpToDate() throws Exception {
    String source = newSource();
    resourceServer.failNextCommand(503);
    assertEquals(502, resource().createDOI(source, "publish", metadata("required-only")).getStatus());
    String doi = recorded(source).doi();

    resourceServer.record(source, "https://doi.org/" + doi);
    Response repeated = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(409, repeated.getStatus());
    CedarError conflict = assertInstanceOf(CedarError.class, repeated.getEntity());
    assertEquals("https://doi.org/" + doi, conflict.parameters.get("doi"));
    assertFalse(conflict.parameters.containsKey("reconciliationRequired"));
    assertEquals(DoiRegistrationState.ATTACHED, recorded(source).state());
  }

  // Repeated publish

  /** A DOI is permanent, so publishing again answers with the DOI the artifact has and registers nothing. */
  @Test
  void aRepeatedPublishIsRefusedWithTheDoiTheArtifactHas() throws Exception {
    String source = newSource();
    String doi = doiOf(resource().createDOI(source, "publish", metadata("required-only")));
    int callsBefore = dataCite.calls().size();

    for (Response repeated : List.of(resource().createDOI(source, "publish", metadata("rich-metadata")),
        resource().createDOI(source, "draft", metadata("rich-metadata")), resource().createDOIStart(source))) {
      assertEquals(409, repeated.getStatus());
      CedarError conflict = assertInstanceOf(CedarError.class, repeated.getEntity());
      assertEquals(CedarErrorKey.DOI_ALREADY_EXISTS.getValue(), conflict.errorKey);
      assertEquals("https://doi.org/" + doi, conflict.parameters.get("doi"));
      assertFalse(conflict.parameters.containsKey("reconciliationRequired"));
    }
    assertEquals(callsBefore, dataCite.calls().size());
    assertEquals(List.of(doi), dataCite.dois());
    assertEquals("https://doi.org/" + doi, resourceServer.carriedDoi(source));
  }

  /** Two publish requests for one artifact at once both answer with one DOI, which DataCite registers once. */
  @Test
  void simultaneousPublishesRegisterOneDoi() throws Exception {
    String source = newSource();
    DataCiteResource resource = resource();
    // The mocked request context builds its stubs on first use, which is not safe from two threads at
    // once. Opening the form first builds them, and changes nothing a publish then does.
    assertEquals(200, resource.createDOIStart(source).getStatus());
    CountDownLatch go = new CountDownLatch(1);
    Callable<Response> publish = () -> {
      go.await();
      return resource.createDOI(source, "publish", metadata("required-only"));
    };
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      Future<Response> first = pool.submit(publish);
      Future<Response> second = pool.submit(publish);
      go.countDown();
      List<Response> responses = List.of(first.get(), second.get());

      String doi = recorded(source).doi();
      for (Response response : responses) {
        assertEquals(201, response.getStatus(), () -> String.valueOf(response.getEntity()));
        assertEquals(doi, doiOf(response));
      }
      assertEquals(List.of(doi), dataCite.dois());
      assertEquals("https://doi.org/" + doi, resourceServer.carriedDoi(source));
    } finally {
      pool.shutdownNow();
    }
  }

  // Association

  /** A draft begun before CEDAR kept records is found by its landing page once, then recorded and kept. */
  @Test
  void aDraftBegunBeforeRecordsIsAdoptedRatherThanDuplicated() throws Exception {
    String source = newSource();
    String legacy = PREFIX + "/lega-cy23";
    dataCite.hold(legacy, "draft", landingPage(source), "A draft begun before records");

    Response start = resource().createDOIStart(source);
    assertEquals(TextNode.valueOf(legacy), entity(start).get("draftDoi"));
    assertNull(recorded(source), "opening the form records nothing");

    Response published = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(201, published.getStatus());
    assertEquals(legacy, doiOf(published));
    assertEquals(List.of(legacy), dataCite.dois());
    assertEquals("findable", dataCite.state(legacy));
    assertEquals(DoiRegistrationState.ATTACHED, recorded(source).state());
    assertEquals(2, dataCite.count(FakeDataCite.Call.LIST), "the publish looks for the draft once more, to record it");
    assertEquals(409, resource().createDOI(source, "publish", metadata("required-only")).getStatus());
    assertEquals(2, dataCite.count(FakeDataCite.Call.LIST), "a recorded artifact is never looked up by landing page");
  }

  /** A DOI CEDAR chose that turns out to be another artifact's is left alone, and another is reserved. */
  @Test
  void aSuffixThatIsAnotherArtifactsIsReplaced() throws Exception {
    String source = newSource();
    String other = newSource();
    suffixes("taken-0001", "fresh-0001");
    dataCite.hold(PREFIX + "/taken-0001", "findable", landingPage(other), "Another artifact's DOI");

    Response published = resource().createDOI(source, "publish", metadata("required-only"));

    assertEquals(201, published.getStatus());
    assertEquals(PREFIX + "/fresh-0001", doiOf(published));
    assertEquals(List.of(PREFIX + "/taken-0001"), dataCite.doisFor(landingPage(other)), "the other DOI is untouched");
    assertEquals(List.of(PREFIX + "/fresh-0001"), dataCite.doisFor(landingPage(source)));
    DoiRegistration registration = recorded(source);
    assertEquals(DoiRegistrationState.ATTACHED, registration.state());
    assertEquals(List.of(PREFIX + "/taken-0001"), registration.supersededDois());
  }

  /** A draft DataCite has dropped was never findable, so the next save registers it again under the same DOI. */
  @Test
  void aDraftDataCiteDroppedIsRegisteredAgainUnderItsDoi() throws Exception {
    String source = newSource();
    String doi = doiOf(resource().createDOI(source, "draft", metadata("required-only")));
    dataCite.forget(doi);

    Response saved = resource().createDOI(source, "draft", metadata("rich-metadata"));

    assertEquals(201, saved.getStatus());
    assertEquals(doi, doiOf(saved));
    assertEquals(List.of(doi), dataCite.dois());
    assertEquals(DoiRegistrationState.DRAFT, recorded(source).state());
  }
}
