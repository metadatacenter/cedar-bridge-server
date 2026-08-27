package org.metadatacenter.cedar.bridge.resources;

import org.metadatacenter.config.DataCiteConfig;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/** Reusable HTTP client for the DataCite workflow, with bounded connection and request times. */
final class DataCiteHttpClient {

  private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 5000;
  private static final int DEFAULT_REQUEST_TIMEOUT_MILLIS = 20000;

  private final HttpClient client;
  private final Duration requestTimeout;

  DataCiteHttpClient(DataCiteConfig config) {
    this(connectTimeout(config), requestTimeout(config));
  }

  DataCiteHttpClient(Duration connectTimeout, Duration requestTimeout) {
    this(HttpClient.newBuilder().connectTimeout(connectTimeout).build(), requestTimeout);
  }

  DataCiteHttpClient(HttpClient client, Duration requestTimeout) {
    this.client = client;
    this.requestTimeout = requestTimeout;
  }

  HttpResponse<String> send(HttpRequest.Builder requestBuilder) throws IOException, InterruptedException {
    HttpRequest request = requestBuilder.timeout(requestTimeout).build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  Optional<Duration> connectTimeout() {
    return client.connectTimeout();
  }

  Duration requestTimeout() {
    return requestTimeout;
  }

  private static Duration connectTimeout(DataCiteConfig config) {
    return positiveDuration(config.getConnectTimeout(), DEFAULT_CONNECT_TIMEOUT_MILLIS);
  }

  private static Duration requestTimeout(DataCiteConfig config) {
    return positiveDuration(config.getRequestTimeout(), DEFAULT_REQUEST_TIMEOUT_MILLIS);
  }

  private static Duration positiveDuration(int configuredMillis, int defaultMillis) {
    return Duration.ofMillis(configuredMillis > 0 ? configuredMillis : defaultMillis);
  }
}
