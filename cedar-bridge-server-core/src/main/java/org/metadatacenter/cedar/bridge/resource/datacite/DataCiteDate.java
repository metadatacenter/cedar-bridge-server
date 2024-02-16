package org.metadatacenter.cedar.bridge.resource.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteDate {
  @JsonProperty("date")
  private String date;
  @JsonProperty("dateType")
  private String dateType;
  @JsonProperty("dateInformation")
  private String dateInformation;

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getDateType() {
    return dateType;
  }

  public void setDateType(String dateType) {
    this.dateType = dateType;
  }

  public String getDateInformation() {
    return dateInformation;
  }

  public void setDateInformation(String dateInformation) {
    this.dateInformation = dateInformation;
  }
}
