package org.metadatacenter.cedar.bridge;

import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cedar.bridge.resources.DataCiteResource;
import org.metadatacenter.config.environment.CedarEnvironmentSource;
import org.metadatacenter.util.test.RouteSurface;

import java.util.HashMap;
import java.util.Map;

/**
 * Route safety net for the bridge server's authenticated surface: probes every endpoint
 * {@link DataCiteResource} declares, unauthenticated, and requires each to answer 401. A 404/405
 * means the route vanished or changed verb; any other status means an endpoint lost its
 * authentication assertion.
 *
 * <p>The other bridge resources proxy public registries (ROR, ORCID, PubMed, RRID, NIH grants,
 * CompTox) and assert no login, so they are deliberately outside this surface.
 *
 * <p>Untagged on purpose. {@code DataCiteResourceTest} carries {@code @Tag("datacite")} and is
 * excluded by default because it calls the live DataCite sandbox; rejecting an unauthenticated
 * request needs no such call, so this runs in the default suite and keeps the server's authenticated
 * surface covered when the tagged tests are skipped.
 */
public class BridgeRoutesRespondTest {

  static {
    // Must run before the test support boots the server, which reads the port env vars. Ports are
    // distinct from the dev server and from every other booting test class.
    Map<String, String> environment = new HashMap<>(CedarEnvironmentSource.getAll());
    environment.put("CEDAR_BRIDGE_HTTP_PORT", "19028");
    environment.put("CEDAR_BRIDGE_ADMIN_PORT", "19128");
    environment.put("CEDAR_BRIDGE_STOP_PORT", "19228");
    CedarEnvironmentSource.setOverride(environment);
  }

  private static final DropwizardTestSupport<BridgeServerConfiguration> SERVER =
      new DropwizardTestSupport<>(BridgeServerApplicationTest.class,
          ResourceHelpers.resourceFilePath("test-config.yml"));

  @BeforeAll
  public static void startServer() throws Exception {
    SERVER.before();
  }

  @AfterAll
  public static void stopServer() {
    SERVER.after();
  }

  @Test
  public void everyAuthenticatedRouteRejectsAnUnauthenticatedRequest() {
    RouteSurface.assertEveryRouteAnswers(
        "http://localhost:" + SERVER.getLocalPort(),
        RouteSurface.endpoints(DataCiteResource.class),
        401);
  }

}
