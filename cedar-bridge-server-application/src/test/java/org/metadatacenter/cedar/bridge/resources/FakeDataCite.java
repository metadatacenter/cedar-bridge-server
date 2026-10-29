package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DataCite's DOI API on a local port, keeping what it registers, as DataCite's REST API does for the
 * calls the DOI workflow makes. It refuses a request without the repository credential, registers the
 * DOI a request names and refuses one it holds already, updates only a DOI it holds, publishes on the
 * {@code publish} event, answers a read by identifier at once, and lists drafts by landing page.
 *
 * <p>A test can make the next call of one kind fail. {@link Fault#ANSWER_LOST} carries the call out and
 * then answers too late for the client; {@link Fault#NEVER_ARRIVED} answers too late without carrying
 * it out. Either reaches the client as a timeout.
 */
final class FakeDataCite implements AutoCloseable {

  enum Call { LIST, READ, CREATE, UPDATE }

  enum Fault { ANSWER_LOST, NEVER_ARRIVED }

  /** How long a faulted call waits before answering. The client gives up well before. */
  static final long FAULT_DELAY_MILLIS = 4_000;

  private static final Pattern LANDING_PAGE = Pattern.compile("url:\"([^\"]*)\"");

  private final String prefix;
  private final String authorization;
  private final HttpServer server;
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final Map<String, ObjectNode> held = new LinkedHashMap<>();
  private final Map<Call, Deque<Object>> faults = new EnumMap<>(Call.class);
  private final List<Call> calls = new CopyOnWriteArrayList<>();
  private volatile boolean refuseCredentials;

  FakeDataCite(String repositoryId, String password, String prefix) throws IOException {
    this.prefix = prefix;
    this.authorization = "Basic " + Base64.getEncoder().encodeToString(
        (repositoryId + ":" + password).getBytes(StandardCharsets.UTF_8));
    for (Call call : Call.values()) {
      faults.put(call, new ArrayDeque<>());
    }
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.setExecutor(executor);
    server.createContext("/dois", this::handle);
    server.start();
  }

  /** DataCite's collection of DOIs, which the bridge is configured with. */
  String endpoint() {
    return "http://127.0.0.1:" + server.getAddress().getPort() + "/dois";
  }

  /** Makes the next call of this kind fail with a timeout, in the given way. */
  synchronized void fail(Call call, Fault fault) {
    faults.get(call).add(fault);
  }

  /** Makes the next call of this kind answer with this status and a DataCite error body. */
  synchronized void refuse(Call call, int status) {
    faults.get(call).add(status);
  }

  /** Makes every request fail as DataCite fails a request whose repository credential it does not accept. */
  void refuseCredentials() {
    refuseCredentials = true;
  }

  /** Holds a DOI as an earlier registration left it, such as a draft begun before CEDAR kept records. */
  synchronized void hold(String doi, String state, String landingPage, String title) {
    ObjectNode attributes = JsonMapper.STRICT_MAPPER.createObjectNode();
    attributes.putArray("titles").addObject().put("title", title);
    attributes.putArray("creators").addObject().put("name", "Doe, Jane").put("nameType", "Personal");
    attributes.put("publisher", "CEDAR");
    attributes.putObject("types").put("resourceTypeGeneral", "Other").put("resourceType", "Instance");
    attributes.put("url", landingPage);
    held.put(doi, stored(doi, state, attributes));
  }

  /** Drops a DOI, as DataCite drops a draft deleted through Fabrica or by its own cleanup. */
  synchronized void forget(String doi) {
    held.remove(doi);
  }

  /** The DOIs DataCite holds, in the order it registered them. */
  synchronized List<String> dois() {
    return new ArrayList<>(held.keySet());
  }

  /** The DOIs DataCite holds for a landing page. */
  synchronized List<String> doisFor(String landingPage) {
    return held.entrySet().stream().filter(entry -> landingPage.equals(entry.getValue().path("url").asText()))
        .map(Map.Entry::getKey).toList();
  }

  /** The state DataCite holds a DOI in, or null when it holds no such DOI. */
  synchronized String state(String doi) {
    ObjectNode attributes = held.get(doi);
    return attributes == null ? null : attributes.path("state").asText();
  }

  /** The calls DataCite received, in order. */
  List<Call> calls() {
    return List.copyOf(calls);
  }

  /** How many calls of this kind DataCite received. */
  long count(Call call) {
    return calls.stream().filter(call::equals).count();
  }

  @Override
  public void close() {
    server.stop(0);
    executor.shutdownNow();
  }

  private void handle(HttpExchange exchange) throws IOException {
    try {
      byte[] requestBody = exchange.getRequestBody().readAllBytes();
      String method = exchange.getRequestMethod();
      String path = exchange.getRequestURI().getPath();
      String doi = path.length() > "/dois/".length() ? path.substring("/dois/".length()) : null;
      Call call = switch (method) {
        case "POST" -> Call.CREATE;
        case "PUT" -> Call.UPDATE;
        default -> doi == null ? Call.LIST : Call.READ;
      };
      calls.add(call);
      if (refuseCredentials || !authorization.equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
        answer(exchange, 401, errors(401, null, "Bad credentials."));
        return;
      }
      Object fault;
      synchronized (this) {
        fault = faults.get(call).poll();
      }
      if (fault instanceof Integer status) {
        answer(exchange, status, errors(status, null, "Refused"));
        return;
      }
      if (fault == Fault.NEVER_ARRIVED) {
        sleep();
        return;
      }
      Reply reply = carryOut(call, doi, exchange.getRequestURI().getRawQuery(),
          new String(requestBody, StandardCharsets.UTF_8));
      if (fault == Fault.ANSWER_LOST) {
        sleep();
        return;
      }
      answer(exchange, reply.status(), reply.body());
    } finally {
      exchange.close();
    }
  }

  private record Reply(int status, String body) {}

  private synchronized Reply carryOut(Call call, String doi, String query, String body) throws IOException {
    return switch (call) {
      case LIST -> list(query);
      case READ -> held.containsKey(doi) ? new Reply(200, document(held.get(doi)))
          : new Reply(404, errors(404, null, "The resource you are looking for doesn't exist."));
      case CREATE -> create(body);
      case UPDATE -> update(doi, body);
    };
  }

  private Reply list(String query) {
    String decoded = URLDecoder.decode(query, StandardCharsets.UTF_8);
    Matcher landingPage = LANDING_PAGE.matcher(decoded);
    boolean drafts = decoded.contains("state=draft");
    ArrayNode data = JsonMapper.STRICT_MAPPER.createArrayNode();
    for (ObjectNode attributes : held.values()) {
      boolean listed = landingPage.find(0) && landingPage.group(1).equals(attributes.path("url").asText())
          && (!drafts || "draft".equals(attributes.path("state").asText()));
      if (listed) {
        data.add(entry(attributes));
      }
    }
    ObjectNode listing = JsonMapper.STRICT_MAPPER.createObjectNode();
    listing.set("data", data);
    listing.putObject("meta").put("total", data.size());
    return new Reply(200, listing.toString());
  }

  private Reply create(String body) throws IOException {
    ObjectNode attributes = (ObjectNode) JsonMapper.STRICT_MAPPER.readTree(body).path("data").path("attributes").deepCopy();
    String doi = attributes.path("doi").asText("");
    if (!doi.startsWith(prefix + "/")) {
      return new Reply(422, errors(422, "doi", "The DOI is not under this repository's prefix"));
    }
    if (held.containsKey(doi)) {
      return new Reply(422, errors(422, "doi", "This DOI has already been taken"));
    }
    String state = "publish".equals(attributes.path("event").asText()) ? "findable" : "draft";
    ObjectNode stored = stored(doi, state, attributes);
    held.put(doi, stored);
    return new Reply(201, document(stored));
  }

  private Reply update(String doi, String body) throws IOException {
    ObjectNode current = held.get(doi);
    if (current == null) {
      return new Reply(404, errors(404, null, "The resource you are looking for doesn't exist."));
    }
    ObjectNode attributes = (ObjectNode) JsonMapper.STRICT_MAPPER.readTree(body).path("data").path("attributes").deepCopy();
    String state = "publish".equals(attributes.path("event").asText()) ? "findable" : current.path("state").asText();
    ObjectNode stored = stored(doi, state, attributes);
    held.put(doi, stored);
    return new Reply(200, document(stored));
  }

  /** The attributes DataCite keeps for a DOI: those it was sent, its identity and its state, and no event. */
  private ObjectNode stored(String doi, String state, ObjectNode attributes) {
    ObjectNode stored = attributes.deepCopy();
    stored.remove("event");
    stored.put("doi", doi);
    stored.put("prefix", doi.substring(0, doi.indexOf('/')));
    stored.put("suffix", doi.substring(doi.indexOf('/') + 1));
    stored.put("state", state);
    return stored;
  }

  private static ObjectNode entry(ObjectNode attributes) {
    ObjectNode entry = JsonMapper.STRICT_MAPPER.createObjectNode();
    entry.put("id", attributes.path("doi").asText());
    entry.put("type", "dois");
    entry.set("attributes", attributes.deepCopy());
    return entry;
  }

  private static String document(ObjectNode attributes) {
    ObjectNode document = JsonMapper.STRICT_MAPPER.createObjectNode();
    document.set("data", entry(attributes));
    return document.toString();
  }

  private static String errors(int status, String source, String title) {
    ObjectNode error = JsonMapper.STRICT_MAPPER.createObjectNode().put("status", String.valueOf(status));
    if (source != null) {
      error.put("source", source);
    }
    error.put("title", title);
    ObjectNode body = JsonMapper.STRICT_MAPPER.createObjectNode();
    body.putArray("errors").add(error);
    return body.toString();
  }

  private static void answer(HttpExchange exchange, int status, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/vnd.api+json");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
  }

  private static void sleep() {
    try {
      Thread.sleep(FAULT_DELAY_MILLIS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /** The attributes DataCite holds for a DOI, as a test inspects them. */
  synchronized JsonNode attributes(String doi) {
    ObjectNode attributes = held.get(doi);
    return attributes == null ? null : attributes.deepCopy();
  }
}
