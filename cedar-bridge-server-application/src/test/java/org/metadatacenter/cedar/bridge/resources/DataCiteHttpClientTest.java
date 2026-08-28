package org.metadatacenter.cedar.bridge.resources;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataCiteHttpClientTest {

  private HttpServer server;

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void reusesConfiguredTimeoutsForSuccessfulRequests() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/ok", exchange -> {
      byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      exchange.getResponseBody().write(body);
      exchange.close();
    });
    server.start();

    DataCiteHttpClient client = new DataCiteHttpClient(Duration.ofMillis(700), Duration.ofMillis(900));
    HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/ok")).GET());

    assertEquals(Duration.ofMillis(700), client.connectTimeout().orElseThrow());
    assertEquals(Duration.ofMillis(900), client.requestTimeout());
    assertEquals(200, response.statusCode());
    assertEquals("ok", response.body());
  }

  @Test
  void abortsARequestThatExceedsItsDeadline() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/slow", exchange -> {
      try {
        Thread.sleep(500);
        exchange.sendResponseHeaders(204, -1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (IOException ignored) {
        // The client is expected to close the exchange when its deadline expires.
      } finally {
        exchange.close();
      }
    });
    server.start();

    DataCiteHttpClient client = new DataCiteHttpClient(Duration.ofMillis(700), Duration.ofMillis(50));

    assertThrows(HttpTimeoutException.class,
        () -> client.send(HttpRequest.newBuilder(uri("/slow")).GET()));
  }

  private URI uri(String path) {
    return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
  }
}
