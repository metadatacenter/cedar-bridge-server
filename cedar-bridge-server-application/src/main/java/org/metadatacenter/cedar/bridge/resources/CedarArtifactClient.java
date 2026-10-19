package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.id.CedarArtifactId;
import org.metadatacenter.id.CedarTemplateId;
import org.metadatacenter.model.CedarResourceType;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.url.ResourceMicroserviceUrlProvider;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** CEDAR document access for DOI workflows. Resource owns every read authorization decision. */
final class CedarArtifactClient {

  private final ResourceMicroserviceUrlProvider resource;

  CedarArtifactClient(ResourceMicroserviceUrlProvider resource) {
    this.resource = resource;
  }

  JsonNode read(CedarResourceType type, CedarArtifactId id, CedarRequestContext context)
      throws CedarProcessingException {
    return jsonResponse(ProxyUtil.proxyGet(resource.getArtifactTypeWithId(type, id), context,
        Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)), "the " + type.getValue() + " " + id.getId());
  }

  Pair<Boolean, JsonNode> validate(String templateId, JsonNode instance, CedarRequestContext context)
      throws CedarProcessingException {
    JsonNode schema = read(CedarResourceType.TEMPLATE, CedarTemplateId.build(templateId), context);
    var body = JsonNodeFactory.instance.objectNode();
    body.set("schema", schema);
    body.set("instance", instance);
    // Forward the actual caller credential and request identity. A user's first stored API key is
    // neither the incoming identity nor a service credential.
    JsonNode report = jsonResponse(ProxyUtil.proxyPost(resource.getValidateCommand("instance"), context,
        body.toString()), "the validation of the DataCite metadata");
    String validates = report.path("validates").asText();
    if (!"true".equals(validates) && !"false".equals(validates)) {
      throw failure(502, "The resource server's validation of the DataCite metadata gave no verdict.");
    }
    return Pair.of("true".equals(validates), report);
  }

  private JsonNode jsonResponse(ClassicHttpResponse response, String what) throws CedarProcessingException {
    try {
      int status = response.getCode();
      if (status != 200) {
        // Never interpret a denial or dependency error body as a document or validation report.
        // Preserve actionable CEDAR rejections; unexpected responses are dependency failures.
        int failureStatus = switch (status) {
          case 400, 401, 403, 404, 429, 503 -> status;
          default -> 502;
        };
        throw failure(failureStatus, "The resource server answered HTTP " + status + " when asked for " + what + ".");
      }
      if (response.getEntity() == null) {
        throw failure(502, "The resource server answered with no body when asked for " + what + ".");
      }
      JsonNode body = JsonMapper.STRICT_MAPPER.readTree(
          EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
      if (body == null || !body.isObject()) {
        throw failure(502, "The resource server answered with something other than a JSON object when asked for "
            + what + ".");
      }
      return body;
    } catch (IOException | ParseException e) {
      throw failure(502, "The resource server's answer could not be read when asked for " + what + ".", e);
    } finally {
      EntityUtils.consumeQuietly(response.getEntity());
    }
  }

  /**
   * A resource answer the DOI workflow cannot use, as an exception the shared error body reports with
   * its message. A bodyless WebApplicationException left the body with its status and no words.
   */
  private static CedarProcessingException failure(int status, String message) {
    return failure(status, message, null);
  }

  private static CedarProcessingException failure(int status, String message, Exception cause) {
    CedarProcessingException failure = new CedarProcessingException(message, cause);
    failure.errorKey(CedarErrorKey.UPSTREAM_SERVER_ERROR);
    failure.getErrorPack().status(CedarResponseStatus.fromStatusCode(status));
    return failure;
  }
}
