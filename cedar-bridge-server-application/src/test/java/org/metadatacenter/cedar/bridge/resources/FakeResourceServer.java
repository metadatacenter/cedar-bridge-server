package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.metadatacenter.constant.LinkedData;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The resource server routes the DOI workflow calls, on a local port, holding its source artifacts.
 *
 * <p>The DOI command keeps the resource server's rule: it records a DOI on an artifact that carries
 * none, repeats harmlessly for the DOI the artifact carries, and refuses any other with
 * {@code doiCanNotBeAltered}. Validation accepts every instance, so a refusal of metadata comes from
 * converting it or from DataCite. A test can make the next DOI commands fail.
 */
final class FakeResourceServer implements AutoCloseable {

  private final String templateId;
  private final ObjectNode template;
  private final HttpServer server;
  private final Map<String, ObjectNode> sources = new ConcurrentHashMap<>();
  private final Deque<Integer> commandFailures = new ArrayDeque<>();
  private final List<String> requests = new CopyOnWriteArrayList<>();

  FakeResourceServer(String templateId, ObjectNode template) throws IOException {
    this.templateId = templateId;
    this.template = template;
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", this::handle);
    server.start();
  }

  /** The resource server's base URL, which the bridge is configured with. */
  String base() {
    return "http://127.0.0.1:" + server.getAddress().getPort() + "/";
  }

  /** Holds a template instance the DOI is for. */
  void addSource(String id, ObjectNode document) {
    sources.put(id, document.deepCopy());
  }

  /** The DOI the source artifact carries, or null when it carries none. */
  String carriedDoi(String id) {
    JsonNode doi = sources.get(id).path(ModelNodeNames.ANNOTATIONS).path(ModelNodeNames.DATACITE_DOI_URI)
        .get(LinkedData.ID);
    return doi == null ? null : doi.asText();
  }

  /** Records a DOI on the artifact as an operator's DOI command would, outside the workflow. */
  void record(String id, String doi) {
    ObjectNode source = sources.get(id);
    ObjectNode annotations = source.has(ModelNodeNames.ANNOTATIONS)
        ? (ObjectNode) source.get(ModelNodeNames.ANNOTATIONS) : source.putObject(ModelNodeNames.ANNOTATIONS);
    annotations.putObject(ModelNodeNames.DATACITE_DOI_URI).put(LinkedData.ID, doi);
  }

  /** Makes the next DOI command answer with this status, writing nothing. */
  synchronized void failNextCommand(int status) {
    commandFailures.add(status);
  }

  /** The requests received, each as "read template", "read source", "validate" or "record DOI". */
  List<String> requests() {
    return List.copyOf(requests);
  }

  @Override
  public void close() {
    server.stop(0);
  }

  private void handle(HttpExchange exchange) throws IOException {
    try {
      String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      String path = exchange.getRequestURI().getPath();
      if (path.equals("/templates/" + templateId)) {
        requests.add("read template");
        answer(exchange, 200, template.toString());
      } else if (path.startsWith("/template-instances/") && sources.containsKey(path.substring(20))) {
        requests.add("read source");
        answer(exchange, 200, sources.get(path.substring(20)).toString());
      } else if (path.equals("/command/validate")) {
        requests.add("validate");
        answer(exchange, 200, "{\"validates\":\"true\"}");
      } else if (path.equals("/command/annotations/doi")) {
        requests.add("record DOI");
        recordDoi(exchange, JsonMapper.STRICT_MAPPER.readTree(body));
      } else {
        requests.add("unexpected " + path);
        answer(exchange, 404, "{}");
      }
    } finally {
      exchange.close();
    }
  }

  private void recordDoi(HttpExchange exchange, JsonNode command) throws IOException {
    Integer failure;
    synchronized (this) {
      failure = commandFailures.poll();
    }
    if (failure != null) {
      answer(exchange, failure, "{\"message\":\"The DOI could not be recorded\"}");
      return;
    }
    String selector = command.path(LinkedData.ID).asText();
    String doi = command.path("doi").asText();
    String id = sources.keySet().stream().filter(source -> source.endsWith("/" + selector) || source.equals(selector))
        .findFirst().orElse(null);
    if (id == null) {
      answer(exchange, 404, "{\"message\":\"No such artifact\"}");
      return;
    }
    String carried = carriedDoi(id);
    if (carried != null && !carried.equals(doi)) {
      answer(exchange, 400, "{\"errorKey\":\"doiCanNotBeAltered\",\"message\":\"The doi can not be altered\"}");
      return;
    }
    record(id, doi);
    answer(exchange, 200, sources.get(id).toString());
  }

  private static void answer(HttpExchange exchange, int status, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
  }
}
