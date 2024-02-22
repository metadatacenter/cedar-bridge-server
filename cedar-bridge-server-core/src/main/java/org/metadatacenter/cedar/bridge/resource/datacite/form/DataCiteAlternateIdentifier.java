package org.metadatacenter.cedar.bridge.resource.datacite.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteAlternateIdentifier {
  @JsonProperty("alternateIdentifier")
  private String alternateIdentifier;
  @JsonProperty("alternateIdentifierType")
  private String alternateIdentifierType;

  public String getAlternateIdentifier() {
    return alternateIdentifier;
  }

  public void setAlternateIdentifier(String alternateIdentifier) {
    this.alternateIdentifier = alternateIdentifier;
  }

  public String getAlternateIdentifierType() {
    return alternateIdentifierType;
  }

  public void setAlternateIdentifierType(String alternateIdentifierType) {
    this.alternateIdentifierType = alternateIdentifierType;
  }
}
