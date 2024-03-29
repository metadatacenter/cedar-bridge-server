package org.metadatacenter.cedar.bridge.resource.datacite.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteNameIdentifier {
  @JsonProperty("nameIdentifier")
  private String nameIdentifier;
  @JsonProperty("nameIdentifierScheme")
  private String nameIdentifierScheme;
  @JsonProperty("schemeUri")
  private String schemeUri;

  public String getNameIdentifier() {
    return nameIdentifier;
  }

  public void setNameIdentifier(String nameIdentifier) {
    this.nameIdentifier = nameIdentifier;
  }

  public String getNameIdentifierScheme() {
    return nameIdentifierScheme;
  }

  public void setNameIdentifierScheme(String nameIdentifierScheme) {
    this.nameIdentifierScheme = nameIdentifierScheme;
  }

  public String getSchemeUri() {
    return schemeUri;
  }

  public void setSchemeUri(String schemeUri) {
    this.schemeUri = schemeUri;
  }
}
