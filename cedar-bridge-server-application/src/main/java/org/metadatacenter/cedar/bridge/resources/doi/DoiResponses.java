package org.metadatacenter.cedar.bridge.resources.doi;

import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.cedar.bridge.resource.datacite.DataciteConstants;
import org.metadatacenter.cedar.bridge.resources.UpstreamRefusal;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.model.CedarResourceType;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.json.JsonMapper;

import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The answers the DOI routes give, each refusal in the shared error body.
 *
 * <p>DataCite explains a refusal in a list of errors of its own. A refusal it makes carries that list
 * in {@code objects.dataCiteErrors}, and the message repeats its titles.
 */
final class DoiResponses {

  static final String DATACITE = "DataCite";
  static final String DATACITE_ERRORS = "dataCiteErrors";
  static final String RECONCILIATION_REQUIRED = "reconciliationRequired";

  private DoiResponses() {
  }

  static Response disabled() {
    return CedarResponse.badRequest()
        .errorKey(CedarErrorKey.DATACITE_DOI_DISABLED)
        .message("DataCite DOI integration is disabled")
        .build();
  }

  static Response invalidState(String state) {
    return CedarResponse.badRequest()
        .errorKey(CedarErrorKey.INVALID_INPUT)
        .message("The DOI state must be 'draft' or 'publish'")
        .parameter("state", state)
        .build();
  }

  static Response invalidMetadata(JsonNode validationReport) {
    return CedarResponse.badRequest()
        .errorKey(CedarErrorKey.INVALID_INPUT)
        .message("The DataCite metadata instance is invalid")
        .object("validationResult", validationReport)
        .build();
  }

  /** The metadata could not be converted into a DataCite request. */
  static Response unconvertible(Exception exception) {
    return CedarResponse.badRequest()
        .exception(exception)
        .message(exception.getMessage())
        .errorKey(CedarErrorKey.INVALID_INPUT)
        .build();
  }

  /**
   * The artifact has a DOI already, which is permanent.
   *
   * @param reconciliationRequired true when CEDAR holds a published DOI for the artifact that the
   *                               artifact does not yet carry, which a publish request records
   */
  static Response alreadyHasDoi(CedarResourceType sourceArtifactType, String sourceArtifactId, String doiName,
                                boolean reconciliationRequired) {
    CedarResponse.CedarResponseBuilder conflict = CedarResponse.conflict()
        .errorKey(CedarErrorKey.DOI_ALREADY_EXISTS)
        .message(String.format("The %s(%s) already has a DOI: %s", sourceArtifactType.getValue(), sourceArtifactId,
            doiName))
        .parameter(DataciteConstants.DOI, doiName);
    if (reconciliationRequired) {
      conflict.parameter(RECONCILIATION_REQUIRED, true)
          .parameter("sourceArtifactId", sourceArtifactId);
    }
    return conflict.build();
  }

  /** The DOI, as DataCite registered it. */
  static Response created(String doi, String dataCiteResponse) {
    String doiName = DataciteConstants.DOI_PREFIX + doi;
    Map<String, Object> entity = new HashMap<>();
    entity.put(DataciteConstants.DOI_ID, doi);
    entity.put(DataciteConstants.DOI_NAME, doiName);
    entity.put(DataciteConstants.DATACITE_RESPONSE, dataCiteResponse);
    return CedarResponse.created(URI.create(doiName)).entity(entity).build();
  }

  /** DataCite could not accept the metadata it was sent. */
  static Response unprocessable(String dataCiteBody) {
    JsonNode errors = dataCiteErrors(dataCiteBody);
    CedarResponse.CedarResponseBuilder refusal = CedarResponse.badRequest()
        .errorKey(CedarErrorKey.INVALID_INPUT)
        .message(errors == null ? "DataCite could not process the metadata" : dataCiteErrorText(errors));
    if (errors != null) {
      refusal.object(DATACITE_ERRORS, errors);
    }
    return refusal.build();
  }

  /**
   * A refusal DataCite gave, in the shared error body. {@link UpstreamRefusal} decides the status, by
   * the rule the external-authority routes follow too. A refusal of the request itself keeps DataCite's
   * status, so the caller can act on it, whether DataCite holds no such DOI or refused what it was asked
   * to register. A refused repository credential is a CEDAR configuration problem, a rate limit applies
   * to the deployment's account rather than to the caller, and a 5xx is DataCite failing, so each of
   * those answers 502, as does any status that is not a refusal at all.
   */
  static Response dataCiteRefusal(int upstreamStatus, String upstreamBody) {
    UpstreamRefusal refusal = new UpstreamRefusal(DATACITE, upstreamStatus);
    String explanation = switch (refusal.kind()) {
      case ACCESS_REFUSED -> "DataCite refused the repository credentials this server holds (HTTP " + upstreamStatus
          + "). This is a CEDAR configuration problem rather than one the caller can resolve.";
      case RATE_LIMITED -> "DataCite is limiting the requests of this deployment's repository account (HTTP 429).";
      case FAILED -> "DataCite failed with HTTP " + upstreamStatus + ".";
      case UNUSABLE -> "DataCite answered HTTP " + upstreamStatus + ", which the DOI workflow cannot use.";
      case REQUEST_REFUSED -> "DataCite refused the request with HTTP " + upstreamStatus + ".";
    };
    JsonNode errors = dataCiteErrors(upstreamBody);
    CedarResponse.CedarResponseBuilder answer =
        refusal.answer(explanation, errors == null ? "" : dataCiteErrorText(errors));
    if (errors != null) {
      answer.object(DATACITE_ERRORS, errors);
    }
    return answer.build();
  }

  /** DataCite could not be reached, did not answer in time, or refused a lookup the workflow made. */
  static Response upstreamFailure(String message, Exception exception) {
    CedarResponse.CedarResponseBuilder failure = CedarResponse.badGateway()
        .message(message)
        .exception(exception);
    if (exception instanceof DataCiteLookupRefused refused) {
      failure.parameter(UpstreamRefusal.STATUS_PARAMETER, refused.status)
          .parameter(UpstreamRefusal.SERVICE_PARAMETER, DATACITE);
      JsonNode errors = dataCiteErrors(refused.body);
      if (errors != null) {
        failure.object(DATACITE_ERRORS, errors);
      }
    }
    return failure.build();
  }

  /**
   * DataCite published the DOI, and the resource server did not record it on the artifact. CEDAR has
   * recorded the DOI as published for the artifact, so repeating the publish request records this DOI
   * and registers nothing at DataCite.
   */
  static Response reconciliationRequired(String sourceArtifactId, String doiName, Integer annotationStatus,
                                         Exception exception) {
    CedarResponse.CedarResponseBuilder response = CedarResponse.badGateway()
        .message("The DOI was published at DataCite but could not be recorded on the artifact. Repeating the "
            + "publish request records the same DOI.")
        .parameter(DataciteConstants.DOI, doiName)
        .parameter("sourceArtifactId", sourceArtifactId)
        .parameter(RECONCILIATION_REQUIRED, true);
    if (annotationStatus != null) {
      response.parameter("annotationStatus", annotationStatus);
    }
    if (exception != null) {
      response.exception(exception);
    }
    return response.build();
  }

  /** The errors DataCite lists in a refusal, or null when its answer holds no such list. */
  static JsonNode dataCiteErrors(String body) {
    if (body == null || body.isBlank()) {
      return null;
    }
    try {
      JsonNode errors = JsonMapper.TOLERANT_MAPPER.readTree(body).path("errors");
      return errors.isArray() ? errors : null;
    } catch (IOException e) {
      return null;
    }
  }

  /** DataCite's errors as one line each, the property at fault first when DataCite names one. */
  static String dataCiteErrorText(JsonNode errorsNode) {
    StringBuilder errorMessageBuilder = new StringBuilder();
    for (JsonNode errorNode : errorsNode) {
      JsonNode sourceNode = errorNode.get("source");
      if (sourceNode != null && sourceNode.isTextual()) {
        errorMessageBuilder.append(sourceNode.asText()).append(": ");
      }
      JsonNode titleNode = errorNode.get("title");
      if (titleNode != null && titleNode.isTextual()) {
        errorMessageBuilder.append(titleNode.asText()).append("\n");
      }
    }
    return errorMessageBuilder.toString().trim();
  }

  /** DataCite refused a lookup the workflow made. The exception keeps DataCite's status and body for the caller's error. */
  static final class DataCiteLookupRefused extends IOException {
    private final int status;
    private final String body;

    DataCiteLookupRefused(int status, String body) {
      super("DataCite DOI lookup returned HTTP " + status);
      this.status = status;
      this.body = body;
    }
  }
}
