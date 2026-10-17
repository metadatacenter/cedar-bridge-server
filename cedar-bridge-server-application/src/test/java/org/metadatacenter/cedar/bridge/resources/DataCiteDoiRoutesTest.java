package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.cedar.bridge.GenerateMetadataInstanceTests;
import org.metadatacenter.cedar.bridge.resource.datacite.Cedar;
import org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.DataCiteConfig;
import org.metadatacenter.config.ServerConfig;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.model.folderserver.basic.FolderServerArtifact;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.FolderServiceSession;
import org.metadatacenter.server.ResourcePermissionServiceSession;
import org.metadatacenter.server.security.model.permission.resource.ResourceCapability;
import org.metadatacenter.server.url.ResourceMicroserviceUrlProvider;
import org.metadatacenter.util.http.CedarError;
import org.metadatacenter.util.json.JsonMapper;
import org.mockito.ArgumentCaptor;

import jakarta.ws.rs.core.Response;
import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Base64;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Each case drives one of the three DOI routes in-process against scripted collaborators.
 *
 * <p>A stubbed client answers for DataCite, a local HTTP server answers for the resource server, and
 * mocked workspace sessions settle eligibility, so no case needs a network or a DataCite account.
 * DataCiteResourceTest exercises the same routes against DataCite itself. It is tagged
 * {@code datacite} and excluded from the default build.
 */
class DataCiteDoiRoutesTest {

  private static final String PREFIX = "10.5072";
  private static final String ENDPOINT = "https://api.test.datacite.org/dois";
  private static final String REPOSITORY_ID = "CEDAR.TEST";
  private static final String REPOSITORY_PASSWORD = "repository-password";
  private static final String BASIC_AUTH = "Basic " + Base64.getEncoder().encodeToString(
      (REPOSITORY_ID + ":" + REPOSITORY_PASSWORD).getBytes(StandardCharsets.UTF_8));
  private static final String OPENVIEW_BASE = "https://openview.metadatacenter.orgx/";
  private static final String USER_ID = "https://metadatacenter.org/users/6124554b-9c83-443c-8207-241d75b82f44";
  private static final String TEMPLATE_ID =
      "https://repo.metadatacenter.org/templates/5b6e0952-8a56-4f97-a35d-7ce784773b57";
  private static final String SOURCE_UUID = "8bc64ab5-df6b-48c8-8c61-6c016245918e";
  private static final String SOURCE_ID = "https://repo.metadatacenter.org/template-instances/" + SOURCE_UUID;
  private static final String OPENVIEW_URL = OPENVIEW_BASE + "template-instances/" + SOURCE_UUID;
  private static final String DRAFT_DOI = PREFIX + "/draft-0001";
  private static final String NEW_DOI = PREFIX + "/new-0001";

  private static final ObjectNode DATACITE_TEMPLATE = JsonMapper.STRICT_MAPPER.createObjectNode()
      .put("@id", TEMPLATE_ID).put("schema:name", "DataCite Template");
  private static final ObjectNode SOURCE_ARTIFACT = JsonMapper.STRICT_MAPPER.createObjectNode()
      .put("@id", SOURCE_ID).put("schema:name", "Source instance");

  private enum DataCiteCall { DRAFT_LOOKUP, METADATA_READ, CREATE, UPDATE }

  private record Reply(int status, String body) {}

  private record SentToDataCite(DataCiteCall call, URI uri, String contentType, String body) {}

  private final Map<DataCiteCall, Reply> dataCiteReplies = new EnumMap<>(DataCiteCall.class);
  private final List<SentToDataCite> sentToDataCite = new CopyOnWriteArrayList<>();
  private final List<String> sentToResource = new CopyOnWriteArrayList<>();
  private String validationReply = "{\"validates\":\"true\"}";

  private HttpServer resourceServer;
  private CedarRequestContext context;
  private ResourcePermissionServiceSession permissionSession;
  private DataCiteResource.DoiAnnotationWriter annotationWriter;
  private DataCiteResource resource;

  @BeforeEach
  void setUp() throws Exception {
    resourceServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    resourceServer.createContext("/", exchange -> {
      String path = exchange.getRequestURI().getPath();
      String reply;
      int status = 200;
      if (path.equals("/command/validate")) {
        sentToResource.add("validate");
        reply = validationReply;
      } else if (path.equals("/templates/" + TEMPLATE_ID)) {
        sentToResource.add("read template");
        reply = DATACITE_TEMPLATE.toString();
      } else if (path.equals("/template-instances/" + SOURCE_ID)) {
        sentToResource.add("read source");
        reply = SOURCE_ARTIFACT.toString();
      } else {
        sentToResource.add("unexpected " + path);
        status = 404;
        reply = "{}";
      }
      byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(status, bytes.length);
      try (var output = exchange.getResponseBody()) {
        output.write(bytes);
      }
      exchange.close();
    });
    resourceServer.start();

    ServerConfig resourceConfig = mock(ServerConfig.class);
    when(resourceConfig.getBase()).thenReturn("http://127.0.0.1:" + resourceServer.getAddress().getPort() + "/");
    ResourceMicroserviceUrlProvider resourceUrls = new ResourceMicroserviceUrlProvider(resourceConfig);
    CedarConfig config = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(config.getMicroserviceUrlUtil().getResource()).thenReturn(resourceUrls);
    when(config.getLinkedDataUtil().resolveResourceId(anyString())).thenAnswer(call -> call.getArgument(0));
    when(config.getServers().getOpenview().getUriBase()).thenReturn(OPENVIEW_BASE);
    DataCiteConfig dataCite = config.getBridgeConfig().getDataCite();
    when(dataCite.isEnabled()).thenReturn(true);
    when(dataCite.getEndpointUrl()).thenReturn(ENDPOINT);
    when(dataCite.getRepositoryId()).thenReturn(REPOSITORY_ID);
    when(dataCite.getPassword()).thenReturn(REPOSITORY_PASSWORD);
    when(dataCite.getPrefix()).thenReturn(PREFIX);
    when(dataCite.getTemplateId()).thenReturn(TEMPLATE_ID);

    context = mock(CedarRequestContext.class, RETURNS_DEEP_STUBS);
    when(context.getAuthorizationHeader()).thenReturn("Bearer incoming-token");
    when(context.getGlobalRequestIdHeader()).thenReturn("doi-request-identity");
    when(context.getCedarUser().getId()).thenReturn(USER_ID);

    CedarDataServices dataServices = mock(CedarDataServices.class);
    permissionSession = mock(ResourcePermissionServiceSession.class);
    FolderServiceSession folderSession = mock(FolderServiceSession.class);
    FolderServerArtifact folderArtifact = mock(FolderServerArtifact.class);
    when(dataServices.getResourcePermissionServiceSession(context)).thenReturn(permissionSession);
    when(dataServices.getFolderServiceSession(context)).thenReturn(folderSession);
    when(permissionSession.userHasCapability(any(), eq(ResourceCapability.UPDATE_RESOURCE))).thenReturn(true);
    when(folderSession.findArtifactById(any())).thenReturn(folderArtifact);
    when(folderArtifact.isOpen()).thenReturn(true);

    DataCiteHttpClient dataCiteClient = mock(DataCiteHttpClient.class);
    when(dataCiteClient.send(any())).thenAnswer(call -> answerAsDataCite(call.getArgument(0)));

    annotationWriter = mock(DataCiteResource.DoiAnnotationWriter.class);
    when(annotationWriter.post(anyString(), any(), anyString())).thenReturn(new BasicClassicHttpResponse(200));

    resource = new DataCiteResource(config, dataServices, dataCiteClient, annotationWriter) {
      @Override
      protected CedarRequestContext buildRequestContext() {
        return context;
      }
    };
  }

  @AfterEach
  void stopResourceServer() {
    resourceServer.stop(0);
  }

  // Registering a DOI

  @Test
  void createsADoiWhenDataCiteHoldsNoDraftForTheArtifact() throws Exception {
    dataCiteReplies.put(DataCiteCall.DRAFT_LOOKUP, new Reply(200, "{\"data\": [], \"meta\": {\"total\": 0}}"));
    String created = registered(NEW_DOI, "draft");
    dataCiteReplies.put(DataCiteCall.CREATE, new Reply(201, created));

    Response response = resource.createDOI(SOURCE_ID, "draft", dataCiteInstance());

    assertEquals(201, response.getStatus());
    assertEquals(URI.create("https://doi.org/" + NEW_DOI), response.getLocation());
    Map<String, Object> entity = entity(response);
    assertEquals(NEW_DOI, entity.get("doiId"));
    assertEquals("https://doi.org/" + NEW_DOI, entity.get("doiName"));
    assertEquals(created, entity.get("dataCiteResponse"));

    assertEquals(List.of(DataCiteCall.DRAFT_LOOKUP, DataCiteCall.CREATE), calls());
    SentToDataCite post = sentToDataCite.get(1);
    assertEquals(URI.create(ENDPOINT), post.uri());
    assertEquals("application/vnd.api+json", post.contentType());
    JsonNode attributes = JsonMapper.STRICT_MAPPER.readTree(post.body()).path("data").path("attributes");
    assertEquals(PREFIX, attributes.path("prefix").asText());
    assertEquals("draft", attributes.path("event").asText());
    assertEquals(OPENVIEW_URL, attributes.path("url").asText());
    assertEquals("John Doe", attributes.path("creators").path(0).path("name").asText());
    assertEquals("Required properties only", attributes.path("titles").path(0).path("title").asText());

    assertEquals(List.of("read source", "read template", "validate"), sentToResource);
    // Only a published DOI is recorded on the artifact, because a draft can still change.
    verifyNoInteractions(annotationWriter);
  }

  /**
   * A second POST for an artifact that already has a draft would register a second DOI and orphan
   * the first, so the draft lookup decides between updating the draft and creating a new DOI.
   */
  @Test
  void updatesTheDraftDataCiteAlreadyHoldsAndRecordsThePublishedDoi() throws Exception {
    dataCiteReplies.put(DataCiteCall.DRAFT_LOOKUP, new Reply(200, draftLookup()));
    String updated = registered(DRAFT_DOI, "findable");
    dataCiteReplies.put(DataCiteCall.UPDATE, new Reply(200, updated));

    Response response = resource.createDOI(SOURCE_ID, "publish", dataCiteInstance());

    assertEquals(201, response.getStatus());
    assertEquals(URI.create("https://doi.org/" + DRAFT_DOI), response.getLocation());
    Map<String, Object> entity = entity(response);
    assertEquals(DRAFT_DOI, entity.get("doiId"));
    assertEquals("https://doi.org/" + DRAFT_DOI, entity.get("doiName"));
    assertEquals(updated, entity.get("dataCiteResponse"));

    assertEquals(List.of(DataCiteCall.DRAFT_LOOKUP, DataCiteCall.UPDATE), calls());
    SentToDataCite put = sentToDataCite.get(1);
    assertEquals(URI.create(ENDPOINT + "/" + DRAFT_DOI), put.uri());
    assertEquals("application/vnd.api+json", put.contentType());
    JsonNode attributes = JsonMapper.STRICT_MAPPER.readTree(put.body()).path("data").path("attributes");
    assertEquals("publish", attributes.path("event").asText());
    assertEquals(OPENVIEW_URL, attributes.path("url").asText());

    ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> command = ArgumentCaptor.forClass(String.class);
    verify(annotationWriter).post(url.capture(), eq(context), command.capture());
    assertEquals("http://127.0.0.1:" + resourceServer.getAddress().getPort() + "/command/annotations/doi",
        url.getValue());
    JsonNode recorded = JsonMapper.STRICT_MAPPER.readTree(command.getValue());
    assertEquals("template-instances/" + SOURCE_UUID, recorded.path("@id").asText());
    assertEquals("https://doi.org/" + DRAFT_DOI, recorded.path("doi").asText());
  }

  /**
   * DataCite refuses a payload with a list of errors, most of which name the property at fault. The
   * user can act on all of them only if every one reaches the message.
   */
  @Test
  void relaysEveryErrorDataCiteGivesForARefusedPayload() throws Exception {
    dataCiteReplies.put(DataCiteCall.DRAFT_LOOKUP, new Reply(200, "{\"data\": []}"));
    dataCiteReplies.put(DataCiteCall.CREATE, new Reply(422, """
        {"errors": [
          {"status": "422", "source": "titles", "title": "Title can't be blank"},
          {"status": "422", "source": "creators", "title": "Creator can't be blank"},
          {"status": "422", "title": "This DOI has already been taken"}
        ]}
        """));

    Response response = resource.createDOI(SOURCE_ID, "publish", dataCiteInstance());

    assertEquals(400, response.getStatus());
    CedarError error = assertInstanceOf(CedarError.class, response.getEntity());
    assertEquals(CedarErrorKey.INVALID_INPUT.getValue(), error.errorKey);
    assertEquals("titles: Title can't be blank\n"
        + "creators: Creator can't be blank\n"
        + "This DOI has already been taken", error.message);
    assertEquals(3, ((JsonNode) error.objects.get("dataCiteErrors")).size());
    assertEquals("titles", ((JsonNode) error.objects.get("dataCiteErrors")).path(0).path("source").asText());
    verifyNoInteractions(annotationWriter);
  }

  /**
   * DataCite refusing the registration itself keeps its status, so the caller can act on it, and its
   * errors reach the caller in the shared error body rather than as a body of DataCite's own.
   */
  @Test
  void relaysARefusedRegistrationInTheSharedErrorBody() throws Exception {
    dataCiteReplies.put(DataCiteCall.DRAFT_LOOKUP, new Reply(200, draftLookup()));
    String gone = """
        {"errors": [{"status": "404", "title": "The resource you are looking for doesn't exist."}]}
        """;
    dataCiteReplies.put(DataCiteCall.UPDATE, new Reply(404, gone));

    Response response = resource.createDOI(SOURCE_ID, "publish", dataCiteInstance());

    assertDataCiteRefusal(response, 404, 404, "DataCite refused the request with HTTP 404.\n"
        + "The resource you are looking for doesn't exist.", gone);
    assertEquals(List.of(DataCiteCall.DRAFT_LOOKUP, DataCiteCall.UPDATE), calls());
    verifyNoInteractions(annotationWriter);
  }

  /**
   * A refused repository credential, a rate limit on the deployment's account and DataCite's own
   * failures are nothing the caller can act on, so each is a bad gateway that still names DataCite's
   * status and carries its errors.
   */
  @ParameterizedTest
  @ValueSource(ints = {401, 403, 429, 500, 503})
  void reportsDataCitesOwnFailureToRegisterAsABadGateway(int status) throws Exception {
    dataCiteReplies.put(DataCiteCall.DRAFT_LOOKUP, new Reply(200, "{\"data\": []}"));
    String failed = "{\"errors\": [{\"status\": \"" + status + "\", \"title\": \"Refused\"}]}";
    dataCiteReplies.put(DataCiteCall.CREATE, new Reply(status, failed));

    Response response = resource.createDOI(SOURCE_ID, "publish", dataCiteInstance());

    assertDataCiteRefusal(response, 502, status, failureExplanation(status) + "\nRefused", failed);
    verifyNoInteractions(annotationWriter);
  }

  @Test
  void reportsAnUnreadableDataCiteFailureAsABadGatewayWithoutErrors() throws Exception {
    dataCiteReplies.put(DataCiteCall.DRAFT_LOOKUP, new Reply(200, "{\"data\": []}"));
    dataCiteReplies.put(DataCiteCall.CREATE, new Reply(503, "<html>Service Unavailable</html>"));

    Response response = resource.createDOI(SOURCE_ID, "publish", dataCiteInstance());

    assertDataCiteRefusal(response, 502, 503, "DataCite failed with HTTP 503.", null);
  }

  /**
   * The draft lookup is the bridge's own question rather than the caller's, so DataCite refusing it is a
   * bad gateway whatever DataCite answered. The message is the one the form has always shown, and the
   * body carries DataCite's status and errors.
   */
  @Test
  void reportsARefusedDraftLookupWithDataCitesErrors() throws Exception {
    String refused = "{\"errors\": [{\"status\": \"401\", \"title\": \"Bad credentials\"}]}";
    dataCiteReplies.put(DataCiteCall.DRAFT_LOOKUP, new Reply(401, refused));

    Response response = resource.createDOIStart(SOURCE_ID);

    assertEquals(502, response.getStatus());
    CedarError error = assertInstanceOf(CedarError.class, response.getEntity());
    assertEquals(502, error.statusCode);
    assertNull(error.errorKey);
    assertEquals("The DataCite draft lookup failed", error.message);
    assertEquals(401, error.parameters.get("upstreamStatusCode"));
    assertEquals("DataCite", error.parameters.get("upstreamService"));
    assertEquals(JsonMapper.STRICT_MAPPER.readTree(refused).path("errors"), error.objects.get("dataCiteErrors"));
    assertEquals(List.of(DataCiteCall.DRAFT_LOOKUP), calls());
  }

  @Test
  void sendsNothingToDataCiteWhenTheMetadataFailsValidation() throws Exception {
    validationReply = "{\"validates\":\"false\",\"errors\":[{\"message\":\"title is required\"}]}";

    Response response = resource.createDOI(SOURCE_ID, "publish", dataCiteInstance());

    assertEquals(400, response.getStatus());
    assertEquals(List.of("read source", "read template", "validate"), sentToResource);
    assertTrue(sentToDataCite.isEmpty(), "invalid metadata must not reach DataCite: " + sentToDataCite);
    verifyNoInteractions(annotationWriter);
  }

  @Test
  void sendsNothingToDataCiteWhenTheCallerCannotEditTheArtifact() throws Exception {
    when(permissionSession.userHasCapability(any(), eq(ResourceCapability.UPDATE_RESOURCE))).thenReturn(false);

    Response response = resource.createDOI(SOURCE_ID, "publish", dataCiteInstance());

    assertEquals(401, response.getStatus());
    assertEquals(List.of("read source"), sentToResource);
    assertTrue(sentToDataCite.isEmpty(), "an ineligible request must not reach DataCite: " + sentToDataCite);
    verifyNoInteractions(annotationWriter);
  }

  // Reading a DOI's metadata

  @Test
  void readsBackWhatDataCiteHoldsForADoi() throws Exception {
    String held = registered(NEW_DOI, "findable");
    dataCiteReplies.put(DataCiteCall.METADATA_READ, new Reply(200, held));

    Response response = resource.getDOIMetadata("https://doi.org/" + NEW_DOI);

    assertEquals(200, response.getStatus());
    assertEquals(JsonMapper.STRICT_MAPPER.readTree(held), response.getEntity());
    assertEquals(List.of(DataCiteCall.METADATA_READ), calls());
    assertEquals(URI.create(ENDPOINT + "/" + NEW_DOI + "?affiliation=true"), sentToDataCite.get(0).uri());
  }

  /**
   * A DOI DataCite does not know is a 404 the caller can act on. The answer is the shared error body,
   * which carries DataCite's errors in objects.dataCiteErrors rather than relaying DataCite's own body.
   */
  @Test
  void answersADoiDataCiteDoesNotKnowInTheSharedErrorBody() throws Exception {
    String unknown = """
        {"errors": [{"status": "404", "title": "The resource you are looking for doesn't exist."}]}
        """;
    dataCiteReplies.put(DataCiteCall.METADATA_READ, new Reply(404, unknown));

    Response response = resource.getDOIMetadata("https://doi.org/" + PREFIX + "/unknown");

    assertDataCiteRefusal(response, 404, 404, "DataCite refused the request with HTTP 404.\n"
        + "The resource you are looking for doesn't exist.", unknown);
  }

  @ParameterizedTest
  @ValueSource(ints = {401, 403, 429, 500, 502})
  void reportsDataCitesOwnFailureToReadADoiAsABadGateway(int status) throws Exception {
    dataCiteReplies.put(DataCiteCall.METADATA_READ, new Reply(status, "{\"errors\": []}"));

    Response response = resource.getDOIMetadata("https://doi.org/" + NEW_DOI);

    assertDataCiteRefusal(response, 502, status, failureExplanation(status), "{\"errors\": []}");
  }

  /** What the message says first about a DataCite status the caller cannot act on. */
  private static String failureExplanation(int status) {
    return switch (status) {
      case 401, 403 -> "DataCite refused the repository credentials this server holds (HTTP " + status
          + "). This is a CEDAR configuration problem rather than one the caller can resolve.";
      case 429 -> "DataCite is limiting the requests of this deployment's repository account (HTTP 429).";
      default -> "DataCite failed with HTTP " + status + ".";
    };
  }

  /**
   * Asserts a DataCite refusal in the shared error body: the status the caller receives, DataCite's own
   * status and name, the message, and DataCite's errors, or their absence when its answer held none.
   */
  private static void assertDataCiteRefusal(Response response, int status, int upstreamStatus, String message,
                                            String upstreamBody) throws Exception {
    assertEquals(status, response.getStatus());
    CedarError error = assertInstanceOf(CedarError.class, response.getEntity());
    assertEquals(status, error.statusCode);
    assertEquals(CedarErrorKey.UPSTREAM_SERVER_ERROR.getValue(), error.errorKey);
    assertEquals(message, error.message);
    assertEquals(upstreamStatus, error.parameters.get("upstreamStatusCode"));
    assertEquals("DataCite", error.parameters.get("upstreamService"));
    if (upstreamBody == null) {
      assertTrue(error.objects.isEmpty(), error.objects.toString());
    } else {
      assertEquals(JsonMapper.STRICT_MAPPER.readTree(upstreamBody).path("errors"), error.objects.get("dataCiteErrors"));
    }
  }

  // Opening the DOI form

  @Test
  void opensTheFormOnTheDraftDataCiteAlreadyHolds() throws Exception {
    dataCiteReplies.put(DataCiteCall.DRAFT_LOOKUP, new Reply(200, draftLookup()));

    Response response = resource.createDOIStart(SOURCE_ID);

    assertEquals(200, response.getStatus());
    Map<String, Object> entity = entity(response);
    assertEquals(TextNode.valueOf(DRAFT_DOI), entity.get("draftDoi"));
    MetadataInstance resumed = assertInstanceOf(MetadataInstance.class, entity.get("existingDataCiteMetadata"));
    assertEquals(PREFIX, resumed.prefix().value());
    assertEquals(OPENVIEW_URL, resumed.url().id());
    assertEquals("Doe, Jane", resumed.creator().creatorList().get(0).creatorName().value());
    assertEquals("Doe", resumed.creator().creatorList().get(0).familyName().value());
    assertEquals("A draft resumed from DataCite", resumed.title().titleList().get(0).title().value());
    assertEquals(Year.now().getValue() + "-01-01", resumed.publicationYear().value());
    assertEquals("Instance", resumed.resourceType().value());
    assertEquals(USER_ID, resumed.pavCreatedBy());
    assertFormContext(entity);

    assertEquals(List.of(DataCiteCall.DRAFT_LOOKUP), calls());
    assertDraftLookupIdentifiesTheArtifact(sentToDataCite.get(0));
  }

  @Test
  void opensTheFormOnADefaultInstanceWhenThereIsNoDraft() throws Exception {
    dataCiteReplies.put(DataCiteCall.DRAFT_LOOKUP, new Reply(200, "{\"data\": [], \"meta\": {\"total\": 0}}"));

    Response response = resource.createDOIStart(SOURCE_ID);

    assertEquals(200, response.getStatus());
    Map<String, Object> entity = entity(response);
    assertTrue(entity.containsKey("draftDoi"));
    assertNull(entity.get("draftDoi"));
    MetadataInstance prefilled = assertInstanceOf(MetadataInstance.class, entity.get("existingDataCiteMetadata"));
    assertEquals(PREFIX, prefilled.prefix().value());
    assertEquals(OPENVIEW_URL, prefilled.url().id());
    assertEquals("CEDAR", prefilled.publisher().value());
    assertEquals("Instance", prefilled.resourceType().value());
    assertEquals(Year.now().getValue() + "-01-01", prefilled.publicationYear().value());
    assertEquals(TEMPLATE_ID, prefilled.isBasedOn());
    assertEquals(USER_ID, prefilled.pavCreatedBy());
    assertNull(prefilled.creator().creatorList().get(0).creatorName().value());
    assertNull(prefilled.title().titleList().get(0).title().value());
    assertFormContext(entity);

    assertEquals(List.of(DataCiteCall.DRAFT_LOOKUP), calls());
    assertDraftLookupIdentifiesTheArtifact(sentToDataCite.get(0));
  }

  private void assertFormContext(Map<String, Object> entity) {
    assertEquals("instance", entity.get("sourceArtifactType"));
    assertEquals(SOURCE_ARTIFACT, entity.get("sourceArtifact"));
    assertEquals(DATACITE_TEMPLATE, entity.get("dataCiteTemplate"));
    assertEquals(List.of("read template", "read source"), sentToResource);
    verifyNoInteractions(annotationWriter);
  }

  /** CEDAR keeps no record of a draft, so the lookup asks DataCite for one by publisher and landing page. */
  private static void assertDraftLookupIdentifiesTheArtifact(SentToDataCite lookup) {
    String query = lookup.uri().getRawQuery();
    assertTrue(lookup.uri().toString().startsWith(ENDPOINT + "?"), lookup.uri().toString());
    assertTrue(query.contains("query=publisher:CEDAR"), query);
    assertTrue(query.contains("url:%22" + URLEncoder.encode(OPENVIEW_URL, StandardCharsets.UTF_8) + "%22"), query);
  }

  // Fixtures

  /**
   * Returns the shared required-only fixture under this test's prefix. The fixture otherwise takes its
   * prefix from the environment, which this test does not control.
   */
  private static ObjectNode dataCiteInstance() {
    ObjectNode instance = JsonMapper.STRICT_MAPPER.valueToTree(GenerateMetadataInstanceTests.getInstanceRequiredOnly());
    ((ObjectNode) instance.get(Cedar.FieldNames.prefix)).put("@value", PREFIX);
    return instance;
  }

  /** Returns DataCite's answer to a registration, an update or a read of one DOI. */
  private static String registered(String doi, String state) {
    return """
        {"data": {"id": "%1$s", "type": "dois",
                  "attributes": {"doi": "%1$s", "prefix": "%2$s", "state": "%3$s", "url": "%4$s"}}}
        """.formatted(doi, PREFIX, state, OPENVIEW_URL);
  }

  /** Returns a draft lookup that finds one draft, in the shape DataCite returns. */
  private static String draftLookup() {
    return """
        {"data": [{"id": "%1$s", "type": "dois", "attributes": {
           "doi": "%1$s", "prefix": "%2$s", "suffix": "draft-0001", "state": "draft", "url": "%3$s",
           "creators": [{"name": "Doe, Jane", "nameType": "Personal", "givenName": "Jane",
                         "familyName": "Doe", "affiliation": [], "nameIdentifiers": []}],
           "titles": [{"title": "A draft resumed from DataCite"}],
           "publisher": "CEDAR", "publicationYear": %4$d,
           "types": {"resourceTypeGeneral": "Other", "resourceType": "Instance"},
           "subjects": [], "contributors": [], "dates": [], "sizes": [], "formats": [],
           "rightsList": [], "descriptions": [], "geoLocations": [], "fundingReferences": [],
           "relatedIdentifiers": [], "relatedItems": [], "alternateIdentifiers": []}}],
         "meta": {"total": 1}}
        """.formatted(DRAFT_DOI, PREFIX, OPENVIEW_URL, Year.now().getValue());
  }

  // The DataCite stub

  private HttpResponse<String> answerAsDataCite(HttpRequest.Builder builder) {
    HttpRequest request = builder.build();
    DataCiteCall call = switch (request.method()) {
      case "POST" -> DataCiteCall.CREATE;
      case "PUT" -> DataCiteCall.UPDATE;
      default -> request.uri().getRawQuery().contains("query=")
          ? DataCiteCall.DRAFT_LOOKUP : DataCiteCall.METADATA_READ;
    };
    sentToDataCite.add(new SentToDataCite(call, request.uri(),
        request.headers().firstValue("Content-Type").orElse(null), body(request)));
    assertEquals(BASIC_AUTH, request.headers().firstValue("Authorization").orElse(null),
        "every DataCite request carries the repository credential");
    Reply reply = dataCiteReplies.get(call);
    if (reply == null) {
      throw new AssertionError("DataCite received an unscripted " + call + " " + request.uri());
    }
    return new ScriptedResponse(reply.status(), reply.body(), request);
  }

  private List<DataCiteCall> calls() {
    return sentToDataCite.stream().map(SentToDataCite::call).toList();
  }

  private static String body(HttpRequest request) {
    return request.bodyPublisher().map(publisher -> {
      HttpResponse.BodySubscriber<String> text = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
      publisher.subscribe(new Flow.Subscriber<ByteBuffer>() {
        @Override
        public void onSubscribe(Flow.Subscription subscription) {
          text.onSubscribe(subscription);
        }

        @Override
        public void onNext(ByteBuffer item) {
          text.onNext(List.of(item));
        }

        @Override
        public void onError(Throwable throwable) {
          text.onError(throwable);
        }

        @Override
        public void onComplete() {
          text.onComplete();
        }
      });
      return text.getBody().toCompletableFuture().join();
    }).orElse("");
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> entity(Response response) {
    return (Map<String, Object>) response.getEntity();
  }

  private record ScriptedResponse(int statusCode, String body, HttpRequest request)
      implements HttpResponse<String> {

    @Override
    public Optional<HttpResponse<String>> previousResponse() {
      return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
      return HttpHeaders.of(Map.of(), (name, value) -> true);
    }

    @Override
    public Optional<SSLSession> sslSession() {
      return Optional.empty();
    }

    @Override
    public URI uri() {
      return request.uri();
    }

    @Override
    public HttpClient.Version version() {
      return HttpClient.Version.HTTP_1_1;
    }
  }
}
