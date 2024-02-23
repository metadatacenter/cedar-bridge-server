package org.metadatacenter.cedar.bridge.resource.datacite.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteRelatedItemContributor {
  @JsonProperty("name")
  private String name;
  @JsonProperty("nameType")
  private String nameType;
  @JsonProperty("givenName")
  private String givenName;
  @JsonProperty("familyName")
  private String familyName;
  @JsonProperty("contributorType")
  private String contributorType;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNameType() {
    return nameType;
  }

  public void setNameType(String nameType) {
    this.nameType = nameType;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  public String getContributorType() {
    return contributorType;
  }

  public void setContributorType(String contributorType) {
    this.contributorType = contributorType;
  }
}
