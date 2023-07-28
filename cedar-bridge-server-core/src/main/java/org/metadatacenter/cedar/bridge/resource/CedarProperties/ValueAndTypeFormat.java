package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueAndTypeFormat {
  @JsonProperty("@value")
  private String value;

  @JsonProperty("@type")
  private String type;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {

    return value == null ? null : value;
  }
}
