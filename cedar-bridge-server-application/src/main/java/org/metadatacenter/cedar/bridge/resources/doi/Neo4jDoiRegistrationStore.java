package org.metadatacenter.cedar.bridge.resources.doi;

import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.CedarTestRuntime;
import org.metadatacenter.server.neo4j.Neo4jConfig;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The DOI records, kept in the graph as nodes of their own, one per source artifact.
 *
 * <p>They follow the outboxes the resource server and the workspace library keep there. A record is
 * apart from the artifact's own node, because it has to survive exactly the failures that leave the
 * artifact and its node disagreeing about a DOI, and the bridge server never writes an artifact node.
 *
 * <p>The uniqueness constraint on the source artifact is created on first use rather than when the
 * server starts, so the server starts without the graph, as it did before it kept records. Merging on
 * the constrained property then settles two requests that reserve a DOI for one artifact at once.
 */
public final class Neo4jDoiRegistrationStore implements DoiRegistrationStore, AutoCloseable {

  private static final String LABEL = "CedarDoiRegistration";
  private static final String RECORD = "properties(r) AS record";

  private final Driver driver;
  private volatile boolean schemaReady;

  public Neo4jDoiRegistrationStore(CedarConfig cedarConfig) {
    this(driver(cedarConfig));
  }

  public Neo4jDoiRegistrationStore(Driver driver) {
    this.driver = driver;
  }

  private static Driver driver(CedarConfig cedarConfig) {
    Neo4jConfig neo4j = Neo4jConfig.fromCedarConfig(cedarConfig);
    Config.ConfigBuilder settings = Config.builder();
    CedarTestRuntime.dependencyTimeoutMillis().ifPresent(timeout -> settings
        .withConnectionTimeout(timeout, TimeUnit.MILLISECONDS)
        .withConnectionAcquisitionTimeout(timeout, TimeUnit.MILLISECONDS)
        .withMaxTransactionRetryTime(timeout, TimeUnit.MILLISECONDS));
    return GraphDatabase.driver(neo4j.getUri(), AuthTokens.basic(neo4j.getUserName(), neo4j.getUserPassword()),
        settings.build());
  }

  @Override
  public Optional<DoiRegistration> find(String sourceArtifactId) {
    ensureSchema();
    try (Session session = driver.session()) {
      return session.executeRead(tx -> tx.run("MATCH (r:" + LABEL + " {sourceArtifactId: $id}) RETURN " + RECORD,
          Map.of("id", sourceArtifactId)).list().stream().findFirst().map(Neo4jDoiRegistrationStore::registration));
    }
  }

  @Override
  public DoiRegistration claim(String sourceArtifactId, String doi, DoiRegistrationState state, String userId) {
    ensureSchema();
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("id", sourceArtifactId);
    parameters.put("doi", doi);
    parameters.put("state", state.name());
    parameters.put("user", userId);
    try (Session session = driver.session()) {
      return session.executeWrite(tx -> registration(tx.run("MERGE (r:" + LABEL + " {sourceArtifactId: $id}) "
          + "ON CREATE SET r.doi = $doi, r.state = $state, r.createdBy = $user, "
          + "r.createdAt = timestamp(), r.updatedAt = timestamp() "
          + "RETURN " + RECORD, parameters).single()));
    }
  }

  @Override
  public DoiRegistration advance(String sourceArtifactId, String doi, DoiRegistrationState state,
                                 String dataCiteResponse) {
    ensureSchema();
    List<String> earlier = new ArrayList<>();
    for (DoiRegistrationState candidate : DoiRegistrationState.values()) {
      if (candidate.compareTo(state) < 0) {
        earlier.add(candidate.name());
      }
    }
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("id", sourceArtifactId);
    parameters.put("doi", doi);
    parameters.put("state", state.name());
    parameters.put("earlier", earlier);
    parameters.put("response", dataCiteResponse);
    // The comparison is taken before anything is set: a SET item sees the items before it.
    String query = "MATCH (r:" + LABEL + " {sourceArtifactId: $id, doi: $doi}) "
        + "WITH r, r.state IN $earlier AS forward "
        + "SET r.state = CASE WHEN forward THEN $state ELSE r.state END, "
        + "r.dataCiteResponse = CASE WHEN forward AND $response IS NOT NULL THEN $response ELSE r.dataCiteResponse END, "
        + "r.updatedAt = CASE WHEN forward THEN timestamp() ELSE r.updatedAt END "
        + "RETURN " + RECORD;
    try (Session session = driver.session()) {
      List<Record> records = session.executeWrite(tx -> tx.run(query, parameters).list());
      if (records.isEmpty()) {
        throw new IllegalStateException("No DOI record for " + sourceArtifactId + " holds " + doi);
      }
      return registration(records.get(0));
    }
  }

  @Override
  public DoiRegistration reserveInstead(String sourceArtifactId, String doi, String replacement) {
    ensureSchema();
    Map<String, Object> parameters = Map.of("id", sourceArtifactId, "doi", doi, "replacement", replacement,
        "replaceable", List.of(DoiRegistrationState.RESERVED.name(), DoiRegistrationState.DRAFT.name()),
        "reserved", DoiRegistrationState.RESERVED.name());
    String query = "MATCH (r:" + LABEL + " {sourceArtifactId: $id, doi: $doi}) WHERE r.state IN $replaceable "
        + "SET r.supersededDois = CASE WHEN $doi = $replacement THEN coalesce(r.supersededDois, []) "
        + "ELSE coalesce(r.supersededDois, []) + $doi END, "
        + "r.doi = $replacement, r.state = $reserved, r.updatedAt = timestamp() "
        + "REMOVE r.dataCiteResponse "
        + "RETURN " + RECORD;
    try (Session session = driver.session()) {
      List<Record> records = session.executeWrite(tx -> tx.run(query, parameters).list());
      if (records.isEmpty()) {
        throw new IllegalStateException("No DOI record for " + sourceArtifactId + " holds " + doi
            + " as a reservation or a draft");
      }
      return registration(records.get(0));
    }
  }

  @Override
  public void close() {
    driver.close();
  }

  private void ensureSchema() {
    if (schemaReady) {
      return;
    }
    synchronized (this) {
      if (!schemaReady) {
        try (Session session = driver.session()) {
          session.run("CREATE CONSTRAINT cedar_doi_registration_source IF NOT EXISTS "
              + "FOR (r:" + LABEL + ") REQUIRE r.sourceArtifactId IS UNIQUE").consume();
        }
        schemaReady = true;
      }
    }
  }

  private static DoiRegistration registration(Record record) {
    Map<String, Object> properties = record.get("record").asMap();
    List<String> superseded = new ArrayList<>();
    if (properties.get("supersededDois") instanceof List<?> dois) {
      for (Object entry : dois) {
        superseded.add(String.valueOf(entry));
      }
    }
    return new DoiRegistration((String) properties.get("sourceArtifactId"), (String) properties.get("doi"),
        DoiRegistrationState.valueOf((String) properties.get("state")), (String) properties.get("dataCiteResponse"),
        superseded);
  }
}
