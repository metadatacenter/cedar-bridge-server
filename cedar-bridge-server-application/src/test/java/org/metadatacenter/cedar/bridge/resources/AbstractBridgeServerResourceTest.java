package org.metadatacenter.cedar.bridge.resources;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.ClientProperties;
import io.dropwizard.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.metadatacenter.cedar.bridge.BridgeServerApplicationTest;
import org.metadatacenter.cedar.bridge.BridgeServerConfiguration;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.environment.CedarEnvironmentVariableProvider;
import org.metadatacenter.model.SystemComponent;
import org.metadatacenter.util.test.TestUserUtil;
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
  protected static final String DUMMY_SOURCE_ARTIFACT_ID = "https://repo.metadatacenter.org/template-instances/";
  protected static final String PUBLISH = "publish";
  protected static final String DRAFT = "draft";

  static {
    log = LoggerFactory.getLogger("Cedar Bridge Server Test");
  }

  @ClassRule
  public static final DropwizardAppRule<BridgeServerConfiguration> RULE =
    new DropwizardAppRule<>(BridgeServerApplicationTest.class, ResourceHelpers.resourceFilePath("test-config" +
      ".yml"));

  @BeforeClass
  public static void oneTimeSetUpAbstract() {

    SystemComponent systemComponent = SystemComponent.SERVER_BRIDGE;
    Map<String, String> environment = CedarEnvironmentVariableProvider.getFor(systemComponent);
    CedarConfig cedarConfig = CedarConfig.getInstance(environment);

    AbstractBridgeServerResourceTest.cedarConfig = cedarConfig;

    client = new JerseyClientBuilder(RULE.getEnvironment()).build("Bridge server endpoint client");
    client.property(ClientProperties.CONNECT_TIMEOUT, 3000);
    client.property(ClientProperties.READ_TIMEOUT, 30000);

//    authHeader1 = TestUserUtil.getTestUser1AuthHeader(cedarConfig);
//    authHeader2 = TestUserUtil.getTestUser2AuthHeader(cedarConfig);
    authHeaderAdmin = TestUserUtil.getAdminUserAuthHeader(cedarConfig);

    baseUrlGetDoiMetadata = BASE_URL + ":" + RULE.getLocalPort() + "/datacite/get-doi-metadata/";
    baseUrlCreateDoi = BASE_URL + ":" + RULE.getLocalPort() + "/datacite/create-doi";
  }

  @Before
  public void setUpAbstract() {
  }

  @After
  public void tearDownAbstract() {
  }

}
