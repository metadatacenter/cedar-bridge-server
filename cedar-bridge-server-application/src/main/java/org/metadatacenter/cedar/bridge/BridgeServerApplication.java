package org.metadatacenter.cedar.bridge;

import com.mongodb.MongoClient;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.cedar.bridge.health.BridgeServerHealthCheck;
import org.metadatacenter.cedar.bridge.resources.*;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceApplicationWithMongo;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.MongoConfig;
import org.metadatacenter.model.ServerName;

public class BridgeServerApplication extends CedarMicroserviceApplicationWithMongo<BridgeServerConfiguration> {

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
    MongoConfig artifactServerConfig = cedarConfig.getArtifactServerConfig();
    CedarDataServices.initializeMongoClientFactoryForDocuments(artifactServerConfig.getMongoConnection());

    MongoClient mongoClientForDocuments = CedarDataServices.getMongoClientFactoryForDocuments().getClient();

    initMongoServices(mongoClientForDocuments, artifactServerConfig);
  }

  @Override
  public void runApp(BridgeServerConfiguration configuration, Environment environment) {

    final IndexResource index = new IndexResource();
    environment.jersey().register(index);


    final DataCiteResource elements = new DataCiteResource(cedarConfig, templateService,templateInstanceService);
    environment.jersey().register(elements);
    final BridgeServerHealthCheck healthCheck = new BridgeServerHealthCheck();
    environment.healthChecks().register("message", healthCheck);
  }
}
