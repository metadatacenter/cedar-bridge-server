package org.metadatacenter.cedar.bridge.resources;

import org.junit.jupiter.api.Test;
import org.metadatacenter.util.test.OpenApiErrorContract;

import java.io.IOException;
import java.io.InputStream;

class OpenApiErrorContractTest {

  @Test
  void errorResponsesPublishTheCommonSchema() throws IOException {
    try (InputStream input = getClass().getResourceAsStream("/assets/swagger-api/swagger.json")) {
      OpenApiErrorContract.assertDocumented(input,
          "GET /datacite/get-doi-metadata/{id} 404",
          "GET /ext-auth/{authority}/search-by-name 404",
          "GET /ext-auth/{authority}/search-by-name 503",
          "GET /ext-auth/{authority}/{id} 404",
          "GET /ext-auth/{authority}/{id} 503");
    }
  }
}
