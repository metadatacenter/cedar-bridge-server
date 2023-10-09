package org.metadatacenter.cedar.bridge.resource;

import org.metadatacenter.id.CedarFQResourceId;
import org.metadatacenter.model.CedarResourceType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GenerateOpenViewUrl {
  public static String getOpenViewUrl(String sourceArtifactId){
    CedarResourceType cedarResourceType = CedarFQResourceId.build(sourceArtifactId).getType();
    String encodedSourceArtifactId = URLEncoder.encode(sourceArtifactId, StandardCharsets.UTF_8);
    if (cedarResourceType == CedarResourceType.TEMPLATE){
      return "https://openview.metadatacenter.org/templates/" + encodedSourceArtifactId;
    } else {
      return "https://openview.metadatacenter.org/template-instances/" + encodedSourceArtifactId;
    }
  }
}
