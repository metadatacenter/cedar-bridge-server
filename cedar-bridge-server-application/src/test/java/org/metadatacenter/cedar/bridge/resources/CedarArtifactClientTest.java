package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.ServerConfig;
import org.metadatacenter.constant.CedarHeaderParameters;
import org.metadatacenter.exception.CedarDependencyUnavailableException;
import org.metadatacenter.id.CedarArtifactId;
import org.metadatacenter.id.CedarTemplateId;
import org.metadatacenter.model.CedarResourceType;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.url.ResourceMicroserviceUrlProvider;
import org.metadatacenter.util.json.JsonMapper;

import jakarta.ws.rs.WebApplicationException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

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
    WebApplicationException failure = assertThrows(WebApplicationException.class,
        () -> client.read(CedarResourceType.TEMPLATE, CedarTemplateId.build(TEMPLATE_ID), context));
    assertEquals(status, failure.getResponse().getStatus());
    assertEquals(1, requests.size(), "A rejection must not be retried or trigger a fallback");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "not JSON", "null", "[]"})
  void rejectsMalformedSuccessfulReads(String body) {
    readBody = body;
    assertEquals(502, assertThrows(WebApplicationException.class,
        () -> client.read(CedarResourceType.TEMPLATE, CedarTemplateId.build(TEMPLATE_ID), context))
        .getResponse().getStatus());
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
    assertEquals(403, assertThrows(WebApplicationException.class,
        () -> client.validate(TEMPLATE_ID, JsonNodeFactory.instance.objectNode(), context))
        .getResponse().getStatus());
    assertEquals(1, requests.size());
  }

  @Test
  void keepsAnInvalidVerdictAndRejectsAMissingVerdict() throws Exception {
    validationBody = "{\"validates\":\"false\",\"errors\":[\"invalid instance\"]}";
    var result = client.validate(TEMPLATE_ID, JsonNodeFactory.instance.objectNode(), context);
    assertFalse(result.getLeft());
    assertEquals("invalid instance", result.getRight().path("errors").get(0).asText());
    validationBody = "{\"error\":\"missing verdict\"}";
    assertEquals(502, assertThrows(WebApplicationException.class,
        () -> client.validate(TEMPLATE_ID, JsonNodeFactory.instance.objectNode(), context))
        .getResponse().getStatus());
  }

  @Test
  void validationFailureCannotMasqueradeAsASuccessfulVerdict() {
    validationStatus = 500;
    validationBody = "{\"validates\":\"true\"}";
    assertEquals(502, assertThrows(WebApplicationException.class,
        () -> client.validate(TEMPLATE_ID, JsonNodeFactory.instance.objectNode(), context))
        .getResponse().getStatus());
  }

  @Test
  void bothDoiEntryPointsStopAtAResourceDenialBeforeEligibilityOrMinting() throws Exception {
    CedarConfig config = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(config.getMicroserviceUrlUtil().getResource()).thenReturn(urls);
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
    assertEquals(403, assertThrows(WebApplicationException.class,
        () -> resource.createDOIStart(TEMPLATE_ID)).getResponse().getStatus());
    assertEquals(403, assertThrows(WebApplicationException.class,
        () -> resource.createDOI(TEMPLATE_ID, "publish", JsonNodeFactory.instance.objectNode()))
        .getResponse().getStatus());
    verifyNoInteractions(services, datacite, writer);
  }

  private record CapturedRequest(String method, String uri, String authorization, String requestId,
                                 String accept, String body) {}
}
