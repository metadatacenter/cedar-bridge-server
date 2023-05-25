package org.metadatacenter.cedar.bridge.config;

import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.metadatacenter.cedar.bridge.BridgeServerApplication;
import org.metadatacenter.cedar.bridge.BridgeServerConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;

public class IntegrationTest {
  @ExtendWith(DropwizardExtensionsSupport.class)
  public static final DropwizardAppExtension<BridgeServerConfiguration> dropwizardAppRule = new DropwizardAppExtension<>(BridgeServerApplication.class, "config.yml");

  private String creatDoiUrl = "https://bridge.metadatacenter.orgx/datacite/create-doi";
  private String getMetadataUrl = "https://bridge.metadatacenter.orgx/datacite/{doi}}";

  @Test
  public void testGivenSuccessRequiredOnlyJson() throws FileNotFoundException {
    Client client = dropwizardAppRule.client();

    String filePath = "/Users/ycao77/CEDAR/cedar-bridge-server/cedar-bridge-server-application/src/test/java/org/metadatacenter/cedar/bridge/config/JsonForTest/SuccessRequiredOnly.json";

//    try {
//      FileReader reader = new FileReader(filePath);
//      JsonNode jsonObject = new JSONObject(reader);
//
//      Response response = client.target(creatDoiUrl)
////          String.format(createDoiUrl, dropwizardAppRule.getLocalPort()))
//          .request()
//          .post(Entity.json(jsonObject));
//
//      assertEquals(response.getStatus(), 201);
//
//    } catch (FileNotFoundException e) {
//      throw new RuntimeException(e);
//    }
  }
}
