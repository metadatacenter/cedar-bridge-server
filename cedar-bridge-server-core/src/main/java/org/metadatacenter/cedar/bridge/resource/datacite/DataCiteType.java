package org.metadatacenter.cedar.bridge.resource.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteType {
  @JsonProperty("resourceType")
  private String resourceType;
  @JsonProperty("resourceTypeGeneral")
  private String resourceTypeGeneral;

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceTypeGeneral() {
    return resourceTypeGeneral;
  }

  public void setResourceTypeGeneral(String resourceTypeGeneral) {
    this.resourceTypeGeneral = resourceTypeGeneral;
  }
}
