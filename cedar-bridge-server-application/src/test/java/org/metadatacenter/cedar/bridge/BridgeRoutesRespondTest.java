package org.metadatacenter.cedar.bridge;

import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cedar.bridge.resources.DataCiteResource;
import org.metadatacenter.cedar.bridge.resources.extauth.ExternalAuthorityResource;
import org.metadatacenter.cedar.util.dw.CedarHealthCheckResource;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceIndexResource;
import org.metadatacenter.cedar.util.dw.CedarServerInsightReportResource;
import org.metadatacenter.config.environment.CedarEnvironmentSource;
import org.metadatacenter.util.test.RouteSurface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Route safety net for the bridge server's authenticated surface: probes every endpoint
 * {@link DataCiteResource} declares, unauthenticated, and requires each to answer 401. A 404/405
 * means the route vanished or changed verb; any other status means an endpoint lost its
 * authentication assertion.
 *
 * <p>A resource class the application registers and this test does not name is a surface nobody
 * probes, so the classification below is checked against what the booted application actually
 * registers rather than against what its author remembered. A new resource class fails
 * {@link #everyRegisteredResourceIsClassified()} until someone decides which bucket it belongs in.
 *
 * <p>Untagged on purpose. {@code DataCiteResourceTest} carries {@code @Tag("datacite")} and is
 * excluded by default because it calls the live DataCite sandbox; rejecting an unauthenticated
 * request needs no such call, so this runs in the default suite and keeps the server's authenticated
 * surface covered when the tagged tests are skipped.
 */
public class BridgeRoutesRespondTest {

  static {
    // Must run before the test support boots the server, which reads the port env vars. Ports are
    // assigned by the OS, so they cannot collide with the dev server or another test.
    Map<String, String> environment = new HashMap<>(CedarEnvironmentSource.getAll());
    environment.put("CEDAR_BRIDGE_HTTP_PORT", "0");
    environment.put("CEDAR_BRIDGE_ADMIN_PORT", "0");
    environment.put("CEDAR_BRIDGE_STOP_PORT", "0");
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

  /**
   * Resource classes that require a credential on every route.
   *
   * <p>{@link CedarServerInsightReportResource} and {@link CedarHealthCheckResource} are registered
   * by the shared bootstrap rather than by {@code BridgeServerApplication}, which is why reading the
   * application alone would miss them. Both assert {@code LoggedIn} on every route, so they are
   * probed here like any other.
   */
  private static final List<Class<?>> AUTHENTICATED =
      List.of(DataCiteResource.class, CedarServerInsightReportResource.class,
          CedarHealthCheckResource.class);

  /**
   * Resource classes that answer a caller holding no credential, by decision.
   *
   * <p>{@link ExternalAuthorityResource} resolves no user on either route. That is deliberate: the
   * registries behind it are public and third-party deployments of the embeddable editor reach it
   * without a CEDAR session, which is also why the seven classes it replaced were open.
   *
   * <p>It is listed rather than merely absent because the choice has a price that is easy to miss.
   * Three of the seven authorities reach their registry on credentials the deployment holds, so an
   * anonymous caller spends CEDAR's ORCID, PubMed and RRID quota. The class javadoc carries the
   * detail; the roadmap carries the standing item. A class belongs here only when someone has
   * decided it should answer anonymously, not when nobody has looked.
   */
  private static final List<Class<?>> INTENTIONALLY_ANONYMOUS =
      List.of(ExternalAuthorityResource.class);

  @Test
  public void everyAuthenticatedRouteRejectsAnUnauthenticatedRequest() {
    RouteSurface.assertEveryRouteAnswers(
        "http://localhost:" + SERVER.getLocalPort(),
        RouteSurface.endpoints(AUTHENTICATED),
        401);
  }

  /**
   * Every resource class the application registers is named in exactly one bucket above.
   *
   * <p>The index resource is excluded because it is deliberately outside both gates, matching the
   * monitor and repo surface tests.
   */
  @Test
  public void everyRegisteredResourceIsClassified() {
    ResourceConfig resourceConfig = SERVER.getEnvironment().jersey().getResourceConfig();
    List<Object> registeredComponents = new ArrayList<>();
    registeredComponents.addAll(resourceConfig.getInstances());
    registeredComponents.addAll(resourceConfig.getSingletons());
    registeredComponents.addAll(resourceConfig.getClasses());
    registeredComponents.addAll(resourceConfig.getResources());

    Set<Class<?>> registered =
        RouteSurface.registeredResourceClasses(registeredComponents, "org.metadatacenter").stream()
            .filter(c -> !CedarMicroserviceIndexResource.class.isAssignableFrom(c))
            .collect(Collectors.toCollection(LinkedHashSet::new));

    Set<Class<?>> classified = new LinkedHashSet<>(AUTHENTICATED);
    classified.addAll(INTENTIONALLY_ANONYMOUS);

    Assertions.assertFalse(registered.isEmpty(),
        "No resource classes were found on the booted application, so this test asserts nothing");

    Set<Class<?>> unclassified = new LinkedHashSet<>(registered);
    unclassified.removeAll(classified);
    Assertions.assertTrue(unclassified.isEmpty(),
        "The bridge server registers resource classes this test does not classify, so nothing probes "
            + "their routes. Add each to AUTHENTICATED or INTENTIONALLY_ANONYMOUS: "
            + unclassified.stream().map(Class::getName).sorted().toList());

    Set<Class<?>> stale = new LinkedHashSet<>(classified);
    stale.removeAll(registered);
    Assertions.assertTrue(stale.isEmpty(),
        "This test classifies resource classes the application no longer registers: "
            + stale.stream().map(Class::getName).sorted().toList());
  }

}
