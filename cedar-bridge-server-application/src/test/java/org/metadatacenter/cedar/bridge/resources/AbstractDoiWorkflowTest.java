package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.cedar.bridge.DataCiteFixtures;
import org.metadatacenter.cedar.bridge.ScratchNeo4j;
import org.metadatacenter.cedar.bridge.resources.doi.DataCiteHttpClient;
import org.metadatacenter.cedar.bridge.resources.doi.DoiRegistration;
import org.metadatacenter.cedar.bridge.resources.doi.DoiSuffixes;
import org.metadatacenter.cedar.bridge.resources.doi.Neo4jDoiRegistrationStore;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.DataCiteConfig;
import org.metadatacenter.config.ServerConfig;
import org.metadatacenter.model.folderserver.basic.FolderServerArtifact;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.FolderServiceSession;
import org.metadatacenter.server.ResourcePermissionServiceSession;
import org.metadatacenter.server.security.model.permission.resource.ResourceCapability;
import org.metadatacenter.server.url.ResourceMicroserviceUrlProvider;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Drives the DOI routes in-process against a DataCite fake and a resource server fake on local ports,
 * with the DOI records in an in-process graph. The DataCite client, the resource client and the write
 * of a DOI to the artifact are the production ones, so each request crosses HTTP as it does deployed.
 * Mocked workspace sessions settle eligibility, which no case is about.
 *
 * <p>{@link #resource()} returns a new resource over a new store on the same graph each time, so a case
 * can stop one server and continue on another, as a request repeated after a restart does.
 */
@Tag("integration")
abstract class AbstractDoiWorkflowTest {

  static final String PREFIX = "10.5072";
  static final String REPOSITORY_ID = "CEDAR.TEST";
  static final String REPOSITORY_PASSWORD = "repository-password";
  static final String OPENVIEW_BASE = "https://openview.metadatacenter.orgx/";
  static final String USER_ID = "https://metadatacenter.org/users/6124554b-9c83-443c-8207-241d75b82f44";
  static final String TEMPLATE_ID = "https://repo.metadatacenter.org/templates/f2c2d19d-d94c-420e-80bd-77a9d65e5dc7";

  /** The client's deadline for a DataCite answer, well inside the fake's delay for a faulted call. */
  static final Duration DATACITE_TIMEOUT = Duration.ofMillis(1500);

  FakeDataCite dataCite;
  FakeResourceServer resourceServer;
  CedarConfig config;
  CedarRequestContext context;
  private CedarDataServices dataServices;
  private final List<Neo4jDoiRegistrationStore> stores = new ArrayList<>();
  private Supplier<String> suffixes;

  @BeforeEach
  void startFakes() throws Exception {
    ScratchNeo4j.clear();
    dataCite = new FakeDataCite(REPOSITORY_ID, REPOSITORY_PASSWORD, PREFIX);
    resourceServer = new FakeResourceServer(TEMPLATE_ID, DataCiteFixtures.templateDocument());

    ServerConfig resourceConfig = mock(ServerConfig.class);
    when(resourceConfig.getBase()).thenReturn(resourceServer.base());
    ResourceMicroserviceUrlProvider resourceUrls = new ResourceMicroserviceUrlProvider(resourceConfig);
    config = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(config.getMicroserviceUrlUtil().getResource()).thenReturn(resourceUrls);
    when(config.getLinkedDataUtil().resolveResourceId(anyString())).thenAnswer(call -> call.getArgument(0));
    when(config.getServers().getOpenview().getUriBase()).thenReturn(OPENVIEW_BASE);
    DataCiteConfig dataCiteConfig = config.getBridgeConfig().getDataCite();
    when(dataCiteConfig.isEnabled()).thenReturn(true);
    when(dataCiteConfig.getEndpointUrl()).thenReturn(dataCite.endpoint());
    when(dataCiteConfig.getRepositoryId()).thenReturn(REPOSITORY_ID);
    when(dataCiteConfig.getPassword()).thenReturn(REPOSITORY_PASSWORD);
    when(dataCiteConfig.getPrefix()).thenReturn(PREFIX);
    when(dataCiteConfig.getTemplateId()).thenReturn(TEMPLATE_ID);

    context = mock(CedarRequestContext.class, RETURNS_DEEP_STUBS);
    when(context.getAuthorizationHeader()).thenReturn("Bearer incoming-token");
    when(context.getGlobalRequestIdHeader()).thenReturn("doi-request-identity");
    when(context.getCedarUser().getId()).thenReturn(USER_ID);

    dataServices = mock(CedarDataServices.class);
    ResourcePermissionServiceSession permissionSession = mock(ResourcePermissionServiceSession.class);
    FolderServiceSession folderSession = mock(FolderServiceSession.class);
    FolderServerArtifact folderArtifact = mock(FolderServerArtifact.class);
    when(dataServices.getResourcePermissionServiceSession(context)).thenReturn(permissionSession);
    when(dataServices.getFolderServiceSession(context)).thenReturn(folderSession);
    when(permissionSession.userHasCapability(any(), eq(ResourceCapability.UPDATE_RESOURCE))).thenReturn(true);
    when(folderSession.findArtifactById(any())).thenReturn(folderArtifact);
    when(folderArtifact.isOpen()).thenReturn(true);

    suffixes = new DoiSuffixes(new Random(20261028L));
  }

  @AfterEach
  void stopFakes() {
    stores.forEach(Neo4jDoiRegistrationStore::close);
    dataCite.close();
    resourceServer.close();
  }

  /** The DOI routes as a newly started server answers them, over the records the graph holds. */
  DataCiteResource resource() {
    Neo4jDoiRegistrationStore store = new Neo4jDoiRegistrationStore(ScratchNeo4j.driver());
    stores.add(store);
    return new DataCiteResource(config, dataServices, new DataCiteHttpClient(Duration.ofSeconds(2), DATACITE_TIMEOUT),
        ProxyUtil::proxyPost, store, suffixes) {
      @Override
      protected CedarRequestContext buildRequestContext() {
        return context;
      }
    };
  }

  /** Makes the suffixes of new DOIs these, in order. */
  void suffixes(String... scripted) {
    Iterator<String> next = List.of(scripted).iterator();
    suffixes = next::next;
  }

  /** A new template instance the resource server holds, with no DOI, and returns its identifier. */
  String newSource() {
    String id = "https://repo.metadatacenter.org/template-instances/" + UUID.randomUUID();
    resourceServer.addSource(id, JsonMapper.STRICT_MAPPER.createObjectNode()
        .put("@id", id).put("schema:name", "Source instance"));
    return id;
  }

  /** The landing page CEDAR registers for an instance. */
  static String landingPage(String sourceId) {
    return OPENVIEW_BASE + "template-instances/" + sourceId.substring(sourceId.lastIndexOf('/') + 1);
  }

  /** The named DataCite fixture under this test's prefix. */
  static ObjectNode metadata(String name) {
    return DataCiteFixtures.instance(name, PREFIX);
  }

  /** The artifact's DOI record, read as a newly started server reads it. */
  DoiRegistration recorded(String sourceId) {
    try (Neo4jDoiRegistrationStore store = new Neo4jDoiRegistrationStore(ScratchNeo4j.driver())) {
      return store.find(sourceId).orElse(null);
    }
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> entity(Response response) {
    return (Map<String, Object>) response.getEntity();
  }

  /** The DOI a registration answered with, without a resolver prefix. */
  static String doiOf(Response response) {
    return (String) entity(response).get("doiId");
  }

  /** What DataCite holds for the DOI, read through the metadata route. */
  JsonNode readBack(String doi) throws Exception {
    Response read = resource().getDOIMetadata("https://doi.org/" + doi);
    if (read.getStatus() != 200) {
      throw new AssertionError("Reading " + doi + " back answered " + read.getStatus() + ": " + read.getEntity());
    }
    return (JsonNode) read.getEntity();
  }
}
