package org.metadatacenter.cedar.bridge.resource.datacite;

import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.id.CedarFQResourceId;
import org.metadatacenter.model.CedarResourceType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GenerateOpenViewUrl {
  public static String getOpenViewUrl(String sourceArtifactId, CedarConfig cedarConfig) {
    CedarResourceType cedarResourceType = CedarFQResourceId.build(sourceArtifactId).getType();
    String encodedSourceArtifactId = URLEncoder.encode(sourceArtifactId, StandardCharsets.UTF_8);
    String uriBase = cedarConfig.getServers().getOpenview().getUriBase();
    if (cedarResourceType == CedarResourceType.TEMPLATE) {
      return uriBase + "templates/" + encodedSourceArtifactId;
    } else {
      return uriBase + "template-instances/" + encodedSourceArtifactId;
    }
  }
}
