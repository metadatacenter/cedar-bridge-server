package org.metadatacenter.cedar.bridge.resources.doi;

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
import org.metadatacenter.util.http.CedarError;

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
import static org.mockito.Mockito.when;

/** The checks a registration passes before DataCite is asked, and the write that records a published DOI. */
class DoiPublicationStepsTest {

  private static final String SOURCE_ID = "https://repo.metadatacenter.org/template-instances/source";
  private static final String DOI = "https://doi.org/10.1234/cedar.test";
  private static final String COMMAND = "http://resource/command/annotations/doi";

  private CedarConfig cedarConfig;
  private CedarDataServices dataServices;
  private CedarRequestContext context;
  private DataCiteHttpClient httpClient;

  @BeforeEach
  void setUp() {
    cedarConfig = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(cedarConfig.getMicroserviceUrlUtil().getResource().getCommandDOIUpdate()).thenReturn(COMMAND);
    dataServices = mock(CedarDataServices.class);
    context = mock(CedarRequestContext.class);
    httpClient = new DataCiteHttpClient(mock(HttpClient.class), Duration.ofSeconds(1));
  }

  @Test
  void acceptsOnlyExplicitDraftAndPublishStates() {
    assertNull(DoiMinting.refusalOfState("draft"));
    assertNull(DoiMinting.refusalOfState("publish"));
    assertEquals(400, DoiMinting.refusalOfState(null).getStatus());
    assertEquals(400, DoiMinting.refusalOfState("published").getStatus());
  }

  @Test
  void stopsWhenCedarInstanceValidationFails() {
    JsonNode report = JsonNodeFactory.instance.objectNode().put("validates", false);

    Response response = DoiMinting.refusalOfMetadata(Pair.of(false, report));

    assertEquals(400, response.getStatus());
    assertSame(report, objects(response).get("validationResult"));
  }

  @Test
  void continuesWhenCedarInstanceValidationSucceeds() {
    JsonNode report = JsonNodeFactory.instance.objectNode().put("validates", true);

    assertNull(DoiMinting.refusalOfMetadata(Pair.of(true, report)));
  }

  @Test
  void returnsReconciliationDetailsWhenAnnotationIsRejected() {
    BasicClassicHttpResponse rejected = new BasicClassicHttpResponse(500);
    rejected.setEntity(new StringEntity("write failed", ContentType.TEXT_PLAIN));
    DoiMinting minting = minting((url, requestContext, content) -> rejected);

    Response response = minting.recordOnArtifact(context, SOURCE_ID, DOI);

    assertEquals(502, response.getStatus());
    assertReconciliationDetails(response);
    assertEquals(500, parameters(response).get("annotationStatus"));
  }

  @Test
  void returnsReconciliationDetailsWhenAnnotationTransportFails() {
    DoiMinting minting = minting((url, requestContext, content) -> {
      throw new CedarProcessingException("resource server unavailable");
    });

    Response response = minting.recordOnArtifact(context, SOURCE_ID, DOI);

    assertEquals(502, response.getStatus());
    assertReconciliationDetails(response);
    assertTrue(error(response).errorId != null);
  }

  @Test
  void recordsTheMintedDoiBeforeReportingSuccess() {
    AtomicReference<String> postedUrl = new AtomicReference<>();
    AtomicReference<String> postedContent = new AtomicReference<>();
    DoiMinting minting = minting((url, requestContext, content) -> {
      postedUrl.set(url);
      postedContent.set(content);
      return new BasicClassicHttpResponse(200);
    });

    Response response = minting.recordOnArtifact(context, SOURCE_ID, DOI);

    assertNull(response);
    assertEquals(COMMAND, postedUrl.get());
    assertTrue(postedContent.get().contains(SOURCE_ID));
    assertTrue(postedContent.get().contains(DOI));
  }

  private DoiMinting minting(DoiAnnotationWriter writer) {
    return new DoiMinting(cedarConfig, dataServices, httpClient, writer, new InMemoryDoiRegistrationStore(),
        () -> "unused");
  }

  private void assertReconciliationDetails(Response response) {
    Map<String, Object> parameters = parameters(response);
    assertEquals(DOI, parameters.get("doi"));
    assertEquals(SOURCE_ID, parameters.get("sourceArtifactId"));
    assertEquals(true, parameters.get("reconciliationRequired"));
  }

  private Map<String, Object> parameters(Response response) {
    return error(response).parameters;
  }

  private Map<String, Object> objects(Response response) {
    return error(response).objects;
  }

  private CedarError error(Response response) {
    return (CedarError) response.getEntity();
  }
}
