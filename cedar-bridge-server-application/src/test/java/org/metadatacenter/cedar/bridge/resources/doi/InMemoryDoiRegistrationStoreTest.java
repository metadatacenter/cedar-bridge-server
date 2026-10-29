package org.metadatacenter.cedar.bridge.resources.doi;

import org.junit.jupiter.api.BeforeEach;

/** Holds the in-memory store the route tests use to the contract the graph store meets. */
class InMemoryDoiRegistrationStoreTest extends DoiRegistrationStoreContract {

  private InMemoryDoiRegistrationStore store;

  @BeforeEach
  void newStore() {
    store = new InMemoryDoiRegistrationStore();
  }

  @Override
  DoiRegistrationStore store() {
    return store;
  }

  @Override
  DoiRegistrationStore restarted() {
    return store;
  }
}
