package org.metadatacenter.cedar.bridge;

import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.metadatacenter.cedar.bridge.resources.*;
import org.metadatacenter.cedar.bridge.resources.extauth.*;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceIndexResource;
import org.metadatacenter.cedar.util.dw.CedarDefaultHealthCheck;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceApplication;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.model.ServerName;

import java.util.List;

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

    final CedarMicroserviceIndexResource index =
        new CedarMicroserviceIndexResource(cedarConfig, getServerName());
    environment.jersey().register(index);

    final DataCiteResource dataCite = new DataCiteResource(cedarConfig);
    environment.jersey().register(dataCite);

    final SubstanceRegistry substanceRegistry = new SubstanceRegistry(cedarConfig);

    environment.lifecycle().manage(new SubstanceRegistryLoader(substanceRegistry));

    // One route for every external authority, in the order CEE's own descriptors list them. An
    // entry answers for one registry and says nothing about routing, parameters, pagination or
    // the envelope, which the resource owns and writes once — so an eighth authority is one class
    // and one line here.
    final ExternalAuthorityResource extAuth = new ExternalAuthorityResource(cedarConfig, List.of(
        new OrcidAuthority(cedarConfig),
        new RorAuthority(cedarConfig),
        new PfasAuthority(substanceRegistry),
        new PubMedAuthority(cedarConfig),
        new RridAuthority(cedarConfig),
        new NihGrantAuthority(),
        new DoiAuthority()));
    environment.jersey().register(extAuth);

    environment.healthChecks().register("comp-tox", new CompToxHealthCheck(substanceRegistry));

    final CedarDefaultHealthCheck healthCheck = new CedarDefaultHealthCheck();
    environment.healthChecks().register("message", healthCheck);

  }
}
