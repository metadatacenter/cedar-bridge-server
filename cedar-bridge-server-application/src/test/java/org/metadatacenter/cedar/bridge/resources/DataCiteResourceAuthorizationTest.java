package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.id.CedarTemplateId;
import org.metadatacenter.model.BiboStatus;
import org.metadatacenter.model.CedarResourceType;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.folderserver.basic.FolderServerArtifact;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.FolderServiceSession;
import org.metadatacenter.server.ResourcePermissionServiceSession;

import jakarta.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataCiteResourceAuthorizationTest {

  private final CedarRequestContext context = mock(CedarRequestContext.class);
  private final CedarDataServices dataServices = mock(CedarDataServices.class);
  private final ResourcePermissionServiceSession permissionSession = mock(ResourcePermissionServiceSession.class);
  private final FolderServiceSession folderSession = mock(FolderServiceSession.class);
  private final FolderServerArtifact artifact = mock(FolderServerArtifact.class);
  private final CedarTemplateId templateId = CedarTemplateId.build(
      "https://repo.metadatacenter.org/templates/" + UUID.randomUUID());

  private DataCiteResource resource;
  private ObjectNode publishedTemplate;

  @BeforeEach
  void setUp() {
    CedarConfig cedarConfig = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    resource = new DataCiteResource(cedarConfig, dataServices);
    publishedTemplate = JsonNodeFactory.instance.objectNode()
        .put(ModelNodeNames.BIBO_STATUS, BiboStatus.PUBLISHED.getValue());
    when(dataServices.getResourcePermissionServiceSession(context)).thenReturn(permissionSession);
  }

  @Test
  void rejectsAUserWithoutWriteAccessBeforeCheckingArtifactState() throws Exception {
    when(permissionSession.userHasWriteAccessToResource(templateId)).thenReturn(false);

    Response response = resource.validateSourceArtifactForDoi(
        context, CedarResourceType.TEMPLATE, templateId, publishedTemplate);

    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    verifyNoInteractions(folderSession);
  }

  @Test
  void rejectsAnArtifactThatIsNotOpen() throws Exception {
    allowWriteAndReturnArtifact();
    when(artifact.isOpen()).thenReturn(false);
    when(folderSession.isArtifactOpenImplicitly(templateId)).thenReturn(false);

    Response response = resource.validateSourceArtifactForDoi(
        context, CedarResourceType.TEMPLATE, templateId, publishedTemplate);

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  void rejectsAnUnpublishedTemplate() throws Exception {
    allowWriteAndReturnArtifact();
    when(artifact.isOpen()).thenReturn(true);
    ObjectNode draftTemplate = JsonNodeFactory.instance.objectNode()
        .put(ModelNodeNames.BIBO_STATUS, BiboStatus.DRAFT.getValue());

    Response response = resource.validateSourceArtifactForDoi(
        context, CedarResourceType.TEMPLATE, templateId, draftTemplate);

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  void acceptsAWritableOpenPublishedTemplate() throws Exception {
    allowWriteAndReturnArtifact();
    when(artifact.isOpen()).thenReturn(true);

    Response response = resource.validateSourceArtifactForDoi(
        context, CedarResourceType.TEMPLATE, templateId, publishedTemplate);

    assertNull(response);
  }

  private void allowWriteAndReturnArtifact() throws Exception {
    when(permissionSession.userHasWriteAccessToResource(templateId)).thenReturn(true);
    when(dataServices.getFolderServiceSession(context)).thenReturn(folderSession);
    when(folderSession.findArtifactById(templateId)).thenReturn(artifact);
  }
}
