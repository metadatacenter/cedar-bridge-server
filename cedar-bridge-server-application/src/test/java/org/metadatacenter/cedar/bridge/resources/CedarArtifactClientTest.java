package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.ServerConfig;
import org.metadatacenter.constant.CedarHeaderParameters;
import org.metadatacenter.exception.CedarDependencyUnavailableException;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.id.CedarArtifactId;
import org.metadatacenter.id.CedarTemplateId;
import org.metadatacenter.model.CedarResourceType;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.url.ResourceMicroserviceUrlProvider;
import org.metadatacenter.util.http.CedarError;
import org.metadatacenter.util.json.JsonMapper;
import org.junit.jupiter.api.function.Executable;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

@Tag("integration")
class CedarArtifactClientTest {

  private static final String TEMPLATE_ID = "https://repo.metadatacenter.org/templates/12345678-1234-1234-1234-123456789abc";
  private HttpServer server;
  private CedarArtifactClient client;
  private ResourceMicroserviceUrlProvider urls;
  private CedarRequestContext context;
  private final List<CapturedRequest> requests = new CopyOnWriteArrayList<>();
  private int readStatus = 200;
  private String readBody = "{\"@id\":\"" + TEMPLATE_ID + "\"}";
  private int validationStatus = 200;
  private String validationBody = "{\"validates\":\"true\"}";

  @BeforeEach
  void startResource() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      requests.add(new CapturedRequest(exchange.getRequestMethod(), exchange.getRequestURI().toString(),
          exchange.getRequestHeaders().getFirst("Authorization"),
          exchange.getRequestHeaders().getFirst(CedarHeaderParameters.GLOBAL_REQUEST_ID_KEY),
          exchange.getRequestHeaders().getFirst("Accept"), body));
      boolean validation = exchange.getRequestURI().getPath().equals("/command/validate");
      byte[] reply = (validation ? validationBody : readBody).getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(validation ? validationStatus : readStatus, reply.length);
      try (var output = exchange.getResponseBody()) {
        output.write(reply);
      }
      exchange.close();
    });
    server.start();
    ServerConfig config = mock(ServerConfig.class);
    when(config.getBase()).thenReturn("http://127.0.0.1:" + server.getAddress().getPort() + "/");
    urls = new ResourceMicroserviceUrlProvider(config);
    client = new CedarArtifactClient(urls);
    context = mock(CedarRequestContext.class, RETURNS_DEEP_STUBS);
    when(context.getAuthorizationHeader()).thenReturn("Bearer incoming-token");
    when(context.getGlobalRequestIdHeader()).thenReturn("doi-request-identity");
  }

  @AfterEach
  void stopResource() {
    server.stop(0);
  }

  @Test
  void readsAllFourKindsThroughResourceWithTheIncomingIdentity() throws Exception {
    for (var type : List.of(CedarResourceType.TEMPLATE, CedarResourceType.ELEMENT,
        CedarResourceType.FIELD, CedarResourceType.INSTANCE)) {
      String id = "https://repo.metadatacenter.org/" + type.getPrefix() + "/12345678-1234-1234-1234-123456789abc";
      assertEquals(TEMPLATE_ID, client.read(type, CedarArtifactId.build(id, type), context).path("@id").asText());
      CapturedRequest request = requests.get(requests.size() - 1);
      assertTrue(request.uri().startsWith("/" + type.getPrefix() + "/https%3A%2F%2F"), request.uri());
      assertEquals("GET", request.method());
      assertEquals("application/json", request.accept());
      assertEquals("Bearer incoming-token", request.authorization());
      assertEquals("doi-request-identity", request.requestId());
    }
    verify(context, never()).getCedarUser();
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 401, 403, 404, 429, 503})
  void preservesReadRejectionsWithoutParsingTheirBodies(int status) {
    readStatus = status;
    readBody = "this is deliberately not JSON";
    assertFailure(status, "The resource server answered HTTP " + status + " when asked for the template "
        + TEMPLATE_ID + ".", () -> client.read(CedarResourceType.TEMPLATE, CedarTemplateId.build(TEMPLATE_ID), context));
    assertEquals(1, requests.size(), "A rejection must not be retried or trigger a fallback");
  }

  @ParameterizedTest
  @ValueSource(ints = {409, 500, 501})
  void reportsAnUnusableReadStatusAsABadGatewayNamingIt(int status) {
    readStatus = status;
    assertFailure(502, "The resource server answered HTTP " + status + " when asked for the template "
        + TEMPLATE_ID + ".", () -> client.read(CedarResourceType.TEMPLATE, CedarTemplateId.build(TEMPLATE_ID), context));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "not JSON", "null", "[]"})
  void rejectsMalformedSuccessfulReads(String body) {
    readBody = body;
    String expected = body.equals("not JSON")
        ? "The resource server's answer could not be read when asked for the template " + TEMPLATE_ID + "."
        : "The resource server answered with something other than a JSON object when asked for the template "
            + TEMPLATE_ID + ".";
    assertFailure(502, expected,
        () -> client.read(CedarResourceType.TEMPLATE, CedarTemplateId.build(TEMPLATE_ID), context));
  }

  @Test
  void resourceOutageFailsWithoutAnArtifactFallback() {
    server.stop(0);
    assertThrows(CedarDependencyUnavailableException.class,
        () -> client.read(CedarResourceType.TEMPLATE, CedarTemplateId.build(TEMPLATE_ID), context));
  }

  @Test
  void validatesThroughResourceUsingTheOriginalBearerCredential() throws Exception {
    var instance = JsonNodeFactory.instance.objectNode().put("name", "metadata");
    assertTrue(client.validate(TEMPLATE_ID, instance, context).getLeft());
    assertEquals(2, requests.size());
    CapturedRequest request = requests.get(1);
    assertEquals("POST", request.method());
    assertEquals("/command/validate?resource_type=instance", request.uri());
    assertEquals("Bearer incoming-token", request.authorization());
    assertEquals("doi-request-identity", request.requestId());
    var posted = JsonMapper.STRICT_MAPPER.readTree(request.body());
    assertEquals(TEMPLATE_ID, posted.path("schema").path("@id").asText());
    assertEquals(instance, posted.path("instance"));
    verify(context, never()).getCedarUser();
  }

  @Test
  void stopsBeforeValidationWhenTemplateReadIsDenied() {
    readStatus = 403;
    assertFailure(403, "The resource server answered HTTP 403 when asked for the template " + TEMPLATE_ID + ".",
        () -> client.validate(TEMPLATE_ID, JsonNodeFactory.instance.objectNode(), context));
    assertEquals(1, requests.size());
  }

  @Test
  void keepsAnInvalidVerdictAndRejectsAMissingVerdict() throws Exception {
    validationBody = "{\"validates\":\"false\",\"errors\":[\"invalid instance\"]}";
    var result = client.validate(TEMPLATE_ID, JsonNodeFactory.instance.objectNode(), context);
    assertFalse(result.getLeft());
    assertEquals("invalid instance", result.getRight().path("errors").get(0).asText());
    validationBody = "{\"error\":\"missing verdict\"}";
    assertFailure(502, "The resource server's validation of the DataCite metadata gave no verdict.",
        () -> client.validate(TEMPLATE_ID, JsonNodeFactory.instance.objectNode(), context));
  }

  @Test
  void validationFailureCannotMasqueradeAsASuccessfulVerdict() {
    validationStatus = 500;
    validationBody = "{\"validates\":\"true\"}";
    assertFailure(502, "The resource server answered HTTP 500 when asked for the validation of the DataCite "
        + "metadata.", () -> client.validate(TEMPLATE_ID, JsonNodeFactory.instance.objectNode(), context));
  }

  @Test
  void bothDoiEntryPointsStopAtAResourceDenialBeforeEligibilityOrMinting() throws Exception {
    CedarConfig config = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(config.getMicroserviceUrlUtil().getResource()).thenReturn(urls);
    when(config.getLinkedDataUtil().resolveResourceId(TEMPLATE_ID)).thenReturn(TEMPLATE_ID);
    when(config.getBridgeConfig().getDataCite().isEnabled()).thenReturn(true);
    when(config.getBridgeConfig().getDataCite().getTemplateId()).thenReturn(TEMPLATE_ID);
    CedarDataServices services = mock(CedarDataServices.class);
    DataCiteHttpClient datacite = mock(DataCiteHttpClient.class);
    DataCiteResource.DoiAnnotationWriter writer = mock(DataCiteResource.DoiAnnotationWriter.class);
    DataCiteResource resource = new DataCiteResource(config, services, datacite, writer) {
      @Override
      protected CedarRequestContext buildRequestContext() {
        return context;
      }
    };
    readStatus = 403;
    readBody = "{\"error\":\"forbidden\"}";
    String denied = "The resource server answered HTTP 403 when asked for the template " + TEMPLATE_ID + ".";
    assertFailure(403, denied, () -> resource.createDOIStart(TEMPLATE_ID));
    assertFailure(403, denied, () -> resource.createDOI(TEMPLATE_ID, "publish", JsonNodeFactory.instance.objectNode()));
    verifyNoInteractions(services, datacite, writer);
  }

  /**
   * A resource answer the workflow cannot use is refused with an exception the shared error body reports.
   * The body the exception mapper builds from it keeps both the status and the message, where a bodyless
   * WebApplicationException left the body with its status alone.
   */
  private static void assertFailure(int status, String message, Executable call) {
    CedarProcessingException failure = assertThrows(CedarProcessingException.class, call);
    CedarError body = CedarError.from(failure.getErrorPack(), null);
    assertEquals(status, body.statusCode);
    assertEquals(message, body.message);
    assertEquals("upstreamServerError", body.errorKey);
    assertTrue(failure.getErrorPack().hasResolvedStatus());
  }

  private record CapturedRequest(String method, String uri, String authorization, String requestId,
                                 String accept, String body) {}
}
