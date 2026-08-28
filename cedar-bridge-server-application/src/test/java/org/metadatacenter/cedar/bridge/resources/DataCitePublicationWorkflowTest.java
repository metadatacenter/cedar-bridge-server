package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.rest.context.CedarRequestContext;

import jakarta.ws.rs.core.Response;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class DataCitePublicationWorkflowTest {

  private static final String SOURCE_ID = "https://repo.metadatacenter.org/template-instances/source";
  private static final String DOI = "https://doi.org/10.1234/cedar.test";

  private CedarConfig cedarConfig;
  private CedarDataServices dataServices;
  private CedarRequestContext context;
  private DataCiteHttpClient httpClient;

  @BeforeEach
  void setUp() {
    cedarConfig = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    dataServices = mock(CedarDataServices.class);
    context = mock(CedarRequestContext.class);
    httpClient = new DataCiteHttpClient(mock(HttpClient.class), Duration.ofSeconds(1));
  }

  @Test
  void acceptsOnlyExplicitDraftAndPublishStates() {
    DataCiteResource resource = resource(successfulWriter());

    assertNull(resource.validateDoiState("draft"));
    assertNull(resource.validateDoiState("publish"));
    assertEquals(400, resource.validateDoiState(null).getStatus());
    assertEquals(400, resource.validateDoiState("published").getStatus());
  }

  @Test
  void stopsWhenCedarInstanceValidationFails() {
    DataCiteResource resource = resource(successfulWriter());
    JsonNode report = JsonNodeFactory.instance.objectNode().put("validates", false);

    Response response = resource.validationFailure(Pair.of(false, report));

    assertEquals(400, response.getStatus());
    assertSame(report, objects(response).get("validationResult"));
  }

  @Test
  void continuesWhenCedarInstanceValidationSucceeds() {
    DataCiteResource resource = resource(successfulWriter());
    JsonNode report = JsonNodeFactory.instance.objectNode().put("validates", true);

    assertNull(resource.validationFailure(Pair.of(true, report)));
  }

  @Test
  void returnsReconciliationDetailsWhenAnnotationIsRejected() {
    BasicClassicHttpResponse rejected = new BasicClassicHttpResponse(500);
    rejected.setEntity(new StringEntity("write failed", ContentType.TEXT_PLAIN));
    DataCiteResource resource = resource((url, requestContext, content) -> rejected);

    Response response = resource.recordPublishedDoi("http://resource/command/annotations/doi",
        context, SOURCE_ID, DOI);

    assertEquals(502, response.getStatus());
    assertReconciliationDetails(response);
    assertEquals(500, parameters(response).get("annotationStatus"));
  }

  @Test
  void returnsReconciliationDetailsWhenAnnotationTransportFails() {
    DataCiteResource resource = resource((url, requestContext, content) -> {
      throw new CedarProcessingException("resource server unavailable");
    });

    Response response = resource.recordPublishedDoi("http://resource/command/annotations/doi",
        context, SOURCE_ID, DOI);

    assertEquals(502, response.getStatus());
    assertReconciliationDetails(response);
    assertTrue(entity(response).containsKey("errorId"));
  }

  @Test
  void recordsTheMintedDoiBeforeReportingSuccess() {
    AtomicReference<String> postedContent = new AtomicReference<>();
    DataCiteResource resource = resource((url, requestContext, content) -> {
      postedContent.set(content);
      return new BasicClassicHttpResponse(200);
    });

    Response response = resource.recordPublishedDoi("http://resource/command/annotations/doi",
        context, SOURCE_ID, DOI);

    assertNull(response);
    assertTrue(postedContent.get().contains(SOURCE_ID));
    assertTrue(postedContent.get().contains(DOI));
  }

  private DataCiteResource resource(DataCiteResource.DoiAnnotationWriter writer) {
    return new DataCiteResource(cedarConfig, dataServices, httpClient, writer);
  }

  private DataCiteResource.DoiAnnotationWriter successfulWriter() {
    return (url, requestContext, content) -> new BasicClassicHttpResponse(200);
  }

  private void assertReconciliationDetails(Response response) {
    Map<String, Object> parameters = parameters(response);
    assertEquals(DOI, parameters.get("doi"));
    assertEquals(SOURCE_ID, parameters.get("sourceArtifactId"));
    assertEquals(true, parameters.get("reconciliationRequired"));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parameters(Response response) {
    return (Map<String, Object>) entity(response).get("parameters");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> objects(Response response) {
    return (Map<String, Object>) entity(response).get("objects");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> entity(Response response) {
    return (Map<String, Object>) response.getEntity();
  }
}
