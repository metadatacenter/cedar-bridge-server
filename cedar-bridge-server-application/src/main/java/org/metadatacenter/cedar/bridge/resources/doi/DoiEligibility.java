package org.metadatacenter.cedar.bridge.resources.doi;

import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.cedar.bridge.resource.datacite.DataciteConstants;
import org.metadatacenter.error.CedarErrorKey;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.id.CedarArtifactId;
import org.metadatacenter.model.BiboStatus;
import org.metadatacenter.model.CedarResourceType;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.folderserver.basic.FolderServerArtifact;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.FolderServiceSession;
import org.metadatacenter.server.ResourcePermissionServiceSession;
import org.metadatacenter.server.security.model.permission.resource.ResourceCapability;
import org.metadatacenter.util.http.CedarResponse;

import jakarta.ws.rs.core.Response;
import java.util.Locale;
import java.util.Objects;

/**
 * Whether the caller may give an artifact a DOI: the caller can edit it, it is open, and a template is
 * published. The write permission is checked first, so a caller who cannot edit the artifact learns
 * nothing about its state.
 */
public final class DoiEligibility {

  private final CedarDataServices dataServices;

  public DoiEligibility(CedarDataServices dataServices) {
    this.dataServices = dataServices;
  }

  /** The refusal for an artifact that cannot have a DOI, or null for one that can. */
  public Response refusal(CedarRequestContext context, CedarResourceType sourceArtifactType,
                          CedarArtifactId sourceArtifactId, JsonNode sourceArtifactJson) throws CedarException {
    ResourcePermissionServiceSession permissionSession = dataServices.getResourcePermissionServiceSession(context);
    if (!permissionSession.userHasCapability(sourceArtifactId, ResourceCapability.UPDATE_RESOURCE)) {
      return CedarResponse
          .unauthorized()
          .errorKey(CedarErrorKey.NO_WRITE_ACCESS_TO_ARTIFACT)
          .message("You do not have permission to edit the artifact")
          .parameter(DataciteConstants.RESOURCE_ID, sourceArtifactId)
          .build();
    }

    FolderServiceSession folderSession = dataServices.getFolderServiceSession(context);
    FolderServerArtifact folderServerResource = folderSession.findArtifactById(sourceArtifactId);
    if (folderServerResource == null) {
      return CedarResponse
          .notFound()
          .message("The source artifact is not found")
          .id(sourceArtifactId)
          .build();
    }
    if (!(folderServerResource.isOpen() || folderSession.isArtifactOpenImplicitly(sourceArtifactId))) {
      return CedarResponse
          .badRequest()
          .message("Please make the " + sourceArtifactType.getValue().toLowerCase(Locale.ROOT) + " open to create a DOI")
          .build();
    }

    JsonNode publicationStatus = sourceArtifactJson.get(ModelNodeNames.BIBO_STATUS);
    if (sourceArtifactType == CedarResourceType.TEMPLATE
        && (publicationStatus == null
        || !Objects.equals(publicationStatus.asText(), BiboStatus.PUBLISHED.getValue()))) {
      return CedarResponse
          .badRequest()
          .message("Please publish the template to create a DOI")
          .build();
    }
    return null;
  }
}
