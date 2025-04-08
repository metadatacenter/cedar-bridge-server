package org.metadatacenter.cedar.bridge;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.metadatacenter.cedar.bridge.health.BridgeServerHealthCheck;
import org.metadatacenter.cedar.bridge.resources.*;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceApplication;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.ExternalAuthorityEPACompTox;
import org.metadatacenter.model.ServerName;

public class BridgeServerApplication extends CedarMicroserviceApplication<BridgeServerConfiguration> {

  public static void main(String[] args) throws Exception {
    new BridgeServerApplication().run(args);
  }

  @Override
  protected ServerName getServerName() {
    return ServerName.BRIDGE;
  }

  @Override
  protected void initializeWithBootstrap(Bootstrap<BridgeServerConfiguration> bootstrap, CedarConfig cedarConfig) {
  }

  @Override
  public void initializeApp() {
  }

  @Override
  public void runApp(BridgeServerConfiguration configuration, Environment environment) {

    final IndexResource index = new IndexResource();
    environment.jersey().register(index);

    final DataCiteResource dataCite = new DataCiteResource(cedarConfig);
    environment.jersey().register(dataCite);

    final ExternalAuthorityRORResource extAuthROR = new ExternalAuthorityRORResource(cedarConfig);
    environment.jersey().register(extAuthROR);

    final ExternalAuthorityORCIDResource extAuthORCID = new ExternalAuthorityORCIDResource(cedarConfig);
    environment.jersey().register(extAuthORCID);

    final SubstanceRegistry substanceRegistry = new SubstanceRegistry(cedarConfig);
    environment.lifecycle().manage(substanceRegistry);

    final ExternalAuthorityEPACompToxResource extAuthEPACompTox = new ExternalAuthorityEPACompToxResource(cedarConfig, substanceRegistry);
    environment.jersey().register(extAuthEPACompTox);

    final BridgeServerHealthCheck healthCheck = new BridgeServerHealthCheck();
    environment.healthChecks().register("message", healthCheck);

  }
}
