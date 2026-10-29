package org.metadatacenter.cedar.bridge;

import org.neo4j.configuration.GraphDatabaseInternalSettings;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

/**
 * One in-process Neo4j, shared within a test JVM by the classes that keep DOI records in it. Booting a
 * harness takes several seconds, so one boot per fork replaces one per class. The harness starts on
 * first use, runs without authentication, and closes when the JVM exits.
 *
 * <p>Each test calls {@link #clear()} first, because a class finds the graph as the previous test left it.
 */
public final class ScratchNeo4j {

  private static Neo4j neo4j;

  private ScratchNeo4j() {
  }

  /** A new driver for the shared graph. The caller closes it. */
  public static synchronized Driver driver() {
    return GraphDatabase.driver(harness().boltURI(), AuthTokens.none());
  }

  /** Deletes every node, and leaves the constraints in place. */
  public static synchronized void clear() {
    harness().defaultDatabaseService().executeTransactionally("MATCH (n) DETACH DELETE n");
  }

  private static Neo4j harness() {
    if (neo4j == null) {
      // A zero quiet period closes the harness in under a second rather than about six, and changes
      // nothing a client can observe, since every connection is closed before the period starts.
      Neo4j started = Neo4jBuilders.newInProcessBuilder().withDisabledServer()
          .withConfig(GraphDatabaseInternalSettings.netty_server_shutdown_quiet_period, 0).build();
      Runtime.getRuntime().addShutdownHook(new Thread(started::close, "scratch-neo4j-close"));
      neo4j = started;
    }
    return neo4j;
  }
}
