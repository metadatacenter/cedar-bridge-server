package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.util.json.JsonMapper;

import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** What every refusal of the external-authority routes must be: the shared error body. */
final class SharedErrorBody {

  private SharedErrorBody() {
  }

  /** The answer as a CedarError whose status is the one the response carries. */
  static JsonNode of(HttpResponse<String> response, int status) throws Exception {
    assertEquals(status, response.statusCode(), response.body());
    assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"),
        response.headers().map().toString());
    JsonNode error = JsonMapper.STRICT_MAPPER.readTree(response.body());
    assertEquals(status, error.path("statusCode").asInt(), response.body());
    assertTrue(error.path("status").asText().matches("[A-Z][A-Z0-9_]*"), response.body());
    for (String field : List.of("errorKey", "errorReasonKey", "message", "parameters", "objects")) {
      assertTrue(error.has(field), field + " is missing from " + response.body());
    }
    assertTrue(error.get("parameters").isObject() && error.get("objects").isObject(), response.body());
    return error;
  }
}
