package org.metadatacenter.cedar.bridge;

import com.sun.net.httpserver.HttpServer;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.metadatacenter.config.environment.CedarEnvironmentSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Boots the real application through Dropwizard test support and exercises the wiring no
 * backend is needed for. This catches configuration and startup rot that a config-only test
 * cannot see: the bridge server registers resources that talk to external authorities, and a
 * resource that reaches one of them while being constructed stops the whole server from
 * starting.
 */
@Tag("integration")
public class BridgeServerApplicationSmokeTest {

  /** The paths the booted server asked the CompTox registry for. */
  private static final List<String> COMP_TOX_REQUESTS = new CopyOnWriteArrayList<>();

  /** A CompTox registry on a local port, which holds an empty PFAS list. */
  private static final HttpServer COMP_TOX = startCompTox();

  static {
    // Must run before the test support boots the server, which reads the port env vars.
    // OS-assigned server ports, so the test instance never collides with a running dev server,
    // and distinct from the other booting test class in this module.
    Map<String, String> environment = new HashMap<>(CedarEnvironmentSource.getAll());
    environment.put("CEDAR_BRIDGE_HTTP_PORT", "0");
    environment.put("CEDAR_BRIDGE_ADMIN_PORT", "0");
    environment.put("CEDAR_BRIDGE_STOP_PORT", "0");
    // The server loads its CompTox registry as it starts. Point it at the one stood up here, so it
    // never reaches EPA's API, which CI configures.
    environment.put("CEDAR_COMP_TOX_API_PREFIX", "http://127.0.0.1:" + COMP_TOX.getAddress().getPort() + "/");
    CedarEnvironmentSource.setOverride(environment);
  }

  public static final DropwizardTestSupport<BridgeServerConfiguration> SERVER =
      new DropwizardTestSupport<>(BridgeServerApplicationTest.class,
          ResourceHelpers.resourceFilePath("test-config.yml"));

  private static final HttpClient CLIENT = HttpClient.newHttpClient();

  @BeforeAll
  public static void oneTimeSetUp() throws Exception {
    SERVER.before();
  }

  @AfterAll
  public static void oneTimeTearDown() {
    SERVER.after();
    COMP_TOX.stop(0);
  }

  private static HttpServer startCompTox() {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext("/", exchange -> {
        COMP_TOX_REQUESTS.add(exchange.getRequestURI().getPath());
        byte[] payload = "[]".getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
      });
      server.start();
      return server;
    } catch (IOException e) {
      throw new IllegalStateException("Could not bind the stub CompTox registry", e);
    }
  }

  private HttpResponse<String> get(String path) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://127.0.0.1:" + SERVER.getLocalPort() + path))
        .GET()
        .build();
    return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
  }

  /**
   * The server loads its PFAS registry from the configured CompTox API as it starts, so a suite that
   * boots it must configure one of its own. CI configures EPA's, which every test class booting the
   * server used to reach.
   */
  @Test
  public void theCompToxRegistryIsLoadedFromTheOneTheSuiteStandsUp() throws Exception {
    long deadline = System.currentTimeMillis() + 10_000;
    while (COMP_TOX_REQUESTS.isEmpty() && System.currentTimeMillis() < deadline) {
      Thread.sleep(20);
    }
    Assertions.assertEquals(List.of("/chemical/list/chemicals/search/by-listname/PFASSTRUCTV5"), COMP_TOX_REQUESTS);
  }

  @Test
  public void indexIsServed() throws Exception {
    HttpResponse<String> response = get("/");
    Assertions.assertEquals(200, response.statusCode());
    Assertions.assertTrue(response.body().contains("name"));
  }

}
