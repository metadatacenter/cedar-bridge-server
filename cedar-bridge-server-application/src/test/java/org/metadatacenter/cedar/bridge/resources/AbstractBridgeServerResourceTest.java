package org.metadatacenter.cedar.bridge.resources;

import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.glassfish.jersey.client.ClientProperties;
import io.dropwizard.client.JerseyClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.metadatacenter.cedar.bridge.BridgeServerApplicationTest;
import org.metadatacenter.cedar.bridge.BridgeServerConfiguration;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.environment.CedarEnvironmentVariableProvider;
import org.metadatacenter.model.SystemComponent;
import org.metadatacenter.util.test.TestAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.util.Map;

public abstract class AbstractBridgeServerResourceTest
{
  protected static CedarConfig cedarConfig;
  protected static Client client;
  protected static String authHeader1;
  protected static String authHeader2;
  protected static String authHeaderAdmin;

  protected static String baseUrlGetDoiMetadata;
  protected static String baseUrlCreateDoi;
  protected static Logger log;

  protected static final String BASE_URL = "http://localhost";

  protected static final String FILE_BASE_PATH = "TestJsonFiles/";
  protected static final String DISPLAY_AFFILIATION = "?affiliation=true";
  protected static final String DUMMY_SOURCE_ARTIFACT_ID_PREFIX = "https://repo.metadatacenter.org/template-instances/";
  protected static final String DUMMY_USER_ID_PREFIX = "https://metadatacenter.org/users/";
  protected static final String PUBLISH = "publish";
  protected static final String DRAFT = "draft";

  static {
    log = LoggerFactory.getLogger("Cedar Bridge Server Test");
  }

  protected static final DropwizardTestSupport<BridgeServerConfiguration> SERVER =
    new DropwizardTestSupport<>(BridgeServerApplicationTest.class, ResourceHelpers.resourceFilePath("test-config" +
      ".yml"));

  @BeforeAll
  public static void oneTimeSetUpAbstract() throws Exception {

    SERVER.before();

    SystemComponent systemComponent = SystemComponent.SERVER_BRIDGE;
    Map<String, String> environment = CedarEnvironmentVariableProvider.getFor(systemComponent);
    CedarConfig cedarConfig = CedarConfig.getInstance(environment);

    AbstractBridgeServerResourceTest.cedarConfig = cedarConfig;

    client = new JerseyClientBuilder(SERVER.getEnvironment()).build("Bridge server endpoint client");
    client.property(ClientProperties.CONNECT_TIMEOUT, 3000);
    client.property(ClientProperties.READ_TIMEOUT, 30000);

    // Replace the Neo4j-backed user service wired at application startup with an in-memory one,
    // so API-key authentication needs no live Neo4j (and no Keycloak)
    TestAuthUtil.installInMemoryUserService(cedarConfig);

//    authHeader1 = TestAuthUtil.getTestUser1AuthHeader(cedarConfig);
//    authHeader2 = TestAuthUtil.getTestUser2AuthHeader(cedarConfig);
    authHeaderAdmin = TestAuthUtil.getAdminUserAuthHeader(cedarConfig);

    baseUrlGetDoiMetadata = BASE_URL + ":" + SERVER.getLocalPort() + "/datacite/get-doi-metadata/";
    baseUrlCreateDoi = BASE_URL + ":" + SERVER.getLocalPort() + "/datacite/create-doi";
  }

  @AfterAll
  public static void oneTimeTearDownAbstract() {
    SERVER.after();
  }

  @BeforeEach
  public void setUpAbstract() {
  }

  @AfterEach
  public void tearDownAbstract() {
  }

}
