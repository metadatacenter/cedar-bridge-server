package org.metadatacenter.cedar.bridge.resource.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteGeoLocationPoint {
  @JsonProperty("pointLatitude")
  private Float pointLatitude;
  @JsonProperty("pointLongitude")
  private Float pointLongitude;

  public Float getPointLatitude() {
    return pointLatitude;
  }

  public void setPointLatitude(Float pointLatitude) {
    this.pointLatitude = pointLatitude;
  }

  public Float getPointLongitude() {
    return pointLongitude;
  }

  public void setPointLongitude(Float pointLongitude) {
    this.pointLongitude = pointLongitude;
  }
}
