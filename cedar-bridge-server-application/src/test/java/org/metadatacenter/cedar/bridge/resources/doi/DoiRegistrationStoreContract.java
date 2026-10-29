package org.metadatacenter.cedar.bridge.resources.doi;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState.ATTACHED;
import static org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState.DRAFT;
import static org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState.PUBLISHED;
import static org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState.RESERVED;

/**
 * What every DOI store does. The workflow relies on each of these to keep one DOI per artifact through
 * retries, races and restarts, so each store runs them all.
 */
abstract class DoiRegistrationStoreContract {

  static final String ARTIFACT = "https://repo.metadatacenter.org/template-instances/7e1c3b0a-0d47-4b4e-9d43-2f5c6f0b8a11";
  static final String DOI = "10.5072/aqdn-5e14";
  static final String OTHER_DOI = "10.5072/3w3z-sa82";
  static final String USER = "https://metadatacenter.org/users/6124554b-9c83-443c-8207-241d75b82f44";

  /** A store holding no records. */
  abstract DoiRegistrationStore store();

  /** The same records as {@link #store()}, as a server started again would find them. */
  abstract DoiRegistrationStore restarted();

  @Test
  void anArtifactWithoutARecordHasNone() {
    assertTrue(store().find(ARTIFACT).isEmpty());
  }

  @Test
  void theFirstClaimHoldsAndALaterOneReceivesIt() {
    DoiRegistrationStore store = store();

    DoiRegistration first = store.claim(ARTIFACT, DOI, RESERVED, USER);
    DoiRegistration second = store.claim(ARTIFACT, OTHER_DOI, DRAFT, USER);

    assertEquals(new DoiRegistration(ARTIFACT, DOI, RESERVED, null, List.of()), first);
    assertEquals(first, second, "a request that loses the race continues with the DOI recorded first");
    assertEquals(first, store.find(ARTIFACT).orElseThrow());
  }

  @Test
  void claimsMadeAtOnceLeaveOneRecord() throws Exception {
    DoiRegistrationStore store = store();
    ExecutorService pool = Executors.newFixedThreadPool(8);
    try {
      List<Callable<DoiRegistration>> claims = new java.util.ArrayList<>();
      for (int i = 0; i < 8; i++) {
        String doi = "10.5072/race-" + i;
        claims.add(() -> store.claim(ARTIFACT, doi, RESERVED, USER));
      }
      List<Future<DoiRegistration>> results = pool.invokeAll(claims);
      String held = store.find(ARTIFACT).orElseThrow().doi();
      for (Future<DoiRegistration> result : results) {
        assertEquals(held, result.get().doi(), "every request continues with one DOI");
      }
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void aRecordMovesForwardAndKeepsThePublishingAnswer() {
    DoiRegistrationStore store = store();
    store.claim(ARTIFACT, DOI, RESERVED, USER);

    assertEquals(DRAFT, store.advance(ARTIFACT, DOI, DRAFT, null).state());
    DoiRegistration published = store.advance(ARTIFACT, DOI, PUBLISHED, "{\"data\":{}}");
    assertEquals(PUBLISHED, published.state());
    assertEquals("{\"data\":{}}", published.dataCiteResponse());
    DoiRegistration attached = store.advance(ARTIFACT, DOI, ATTACHED, null);
    assertEquals(ATTACHED, attached.state());
    assertEquals("{\"data\":{}}", attached.dataCiteResponse(), "the answer outlives the move that recorded it");
  }

  @Test
  void aLateOrRepeatedMoveNeverTakesARecordBack() {
    DoiRegistrationStore store = store();
    store.claim(ARTIFACT, DOI, RESERVED, USER);
    store.advance(ARTIFACT, DOI, PUBLISHED, "published");

    DoiRegistration late = store.advance(ARTIFACT, DOI, DRAFT, null);
    DoiRegistration repeated = store.advance(ARTIFACT, DOI, PUBLISHED, "repeated");

    assertEquals(PUBLISHED, late.state());
    assertEquals(PUBLISHED, repeated.state());
    assertEquals("published", repeated.dataCiteResponse(), "a repeated move keeps the first answer");
  }

  @Test
  void aMoveOfADoiTheRecordDoesNotHoldIsRefused() {
    DoiRegistrationStore store = store();
    assertThrows(IllegalStateException.class, () -> store.advance(ARTIFACT, DOI, DRAFT, null));
    store.claim(ARTIFACT, DOI, RESERVED, USER);
    assertThrows(IllegalStateException.class, () -> store.advance(ARTIFACT, OTHER_DOI, DRAFT, null));
    assertEquals(RESERVED, store.find(ARTIFACT).orElseThrow().state());
  }

  @Test
  void aReservationOrADraftCanBeReplaced() {
    DoiRegistrationStore store = store();
    store.claim(ARTIFACT, DOI, RESERVED, USER);
    store.advance(ARTIFACT, DOI, DRAFT, null);

    DoiRegistration lost = store.reserveInstead(ARTIFACT, DOI, DOI);
    assertEquals(new DoiRegistration(ARTIFACT, DOI, RESERVED, null, List.of()), lost,
        "a draft DataCite has lost is reserved again under the same DOI");

    DoiRegistration replaced = store.reserveInstead(ARTIFACT, DOI, OTHER_DOI);
    assertEquals(new DoiRegistration(ARTIFACT, OTHER_DOI, RESERVED, null, List.of(DOI)), replaced);
    assertEquals(replaced, store.find(ARTIFACT).orElseThrow());
  }

  @Test
  void aPublishedDoiIsNeverReplaced() {
    DoiRegistrationStore store = store();
    store.claim(ARTIFACT, DOI, RESERVED, USER);
    store.advance(ARTIFACT, DOI, PUBLISHED, "published");

    assertThrows(IllegalStateException.class, () -> store.reserveInstead(ARTIFACT, DOI, OTHER_DOI));
    store.advance(ARTIFACT, DOI, ATTACHED, null);
    assertThrows(IllegalStateException.class, () -> store.reserveInstead(ARTIFACT, DOI, DOI));
    assertEquals(DOI, store.find(ARTIFACT).orElseThrow().doi());
  }

  @Test
  void aRecordOutlivesTheServerThatWroteIt() {
    DoiRegistrationStore store = store();
    store.claim(ARTIFACT, DOI, RESERVED, USER);
    store.advance(ARTIFACT, DOI, PUBLISHED, "published");

    DoiRegistration found = restarted().find(ARTIFACT).orElseThrow();

    assertEquals(new DoiRegistration(ARTIFACT, DOI, PUBLISHED, "published", List.of()), found);
    assertNull(restarted().find(ARTIFACT + "-other").orElse(null));
  }
}
