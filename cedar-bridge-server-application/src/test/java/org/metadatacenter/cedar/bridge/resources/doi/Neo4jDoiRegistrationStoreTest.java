package org.metadatacenter.cedar.bridge.resources.doi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cedar.bridge.ScratchNeo4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The graph store meets the contract, and keeps each record on a node of its own under a constraint. */
@Tag("integration")
class Neo4jDoiRegistrationStoreTest extends DoiRegistrationStoreContract {

  private final List<Neo4jDoiRegistrationStore> opened = new ArrayList<>();
  private Neo4jDoiRegistrationStore store;

  @BeforeEach
  void emptyGraph() {
    ScratchNeo4j.clear();
    store = open();
  }

  @AfterEach
  void closeStores() {
    opened.forEach(Neo4jDoiRegistrationStore::close);
  }

  private Neo4jDoiRegistrationStore open() {
    Neo4jDoiRegistrationStore opening = new Neo4jDoiRegistrationStore(ScratchNeo4j.driver());
    opened.add(opening);
    return opening;
  }

  @Override
  DoiRegistrationStore store() {
    return store;
  }

  @Override
  DoiRegistrationStore restarted() {
    return open();
  }

  @Test
  void eachRecordIsANodeOfItsOwnUnderAUniquenessConstraint() {
    store.claim(ARTIFACT, DOI, DoiRegistrationState.RESERVED, USER);

    try (Driver driver = ScratchNeo4j.driver(); Session session = driver.session()) {
      assertEquals(1, session.run("MATCH (r:CedarDoiRegistration) RETURN count(r) AS n").single().get("n").asInt());
      assertEquals(USER, session.run("MATCH (r:CedarDoiRegistration) RETURN r.createdBy AS user")
          .single().get("user").asString());
      List<String> constraints = session.run("SHOW CONSTRAINTS YIELD name RETURN name").list(
          row -> row.get("name").asString());
      assertTrue(constraints.contains("cedar_doi_registration_source"), constraints.toString());
    }
  }
}
