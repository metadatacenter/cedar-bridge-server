package org.metadatacenter.cedar.bridge.resource.datacite.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteGeoLocationBox {
  @JsonProperty("eastBoundLongitude")
  private Float eastBoundLongitude;
  @JsonProperty("northBoundLatitude")
  private Float northBoundLatitude;
  @JsonProperty("southBoundLatitude")
  private Float southBoundLatitude;
  @JsonProperty("westBoundLongitude")
  private Float westBoundLongitude;

  public Float getEastBoundLongitude() {
    return eastBoundLongitude;
  }

  public void setEastBoundLongitude(Float eastBoundLongitude) {
    this.eastBoundLongitude = eastBoundLongitude;
  }

  public Float getNorthBoundLatitude() {
    return northBoundLatitude;
  }

  public void setNorthBoundLatitude(Float northBoundLatitude) {
    this.northBoundLatitude = northBoundLatitude;
  }

  public Float getSouthBoundLatitude() {
    return southBoundLatitude;
  }

  public void setSouthBoundLatitude(Float southBoundLatitude) {
    this.southBoundLatitude = southBoundLatitude;
  }

  public Float getWestBoundLongitude() {
    return westBoundLongitude;
  }

  public void setWestBoundLongitude(Float westBoundLongitude) {
    this.westBoundLongitude = westBoundLongitude;
  }
}
