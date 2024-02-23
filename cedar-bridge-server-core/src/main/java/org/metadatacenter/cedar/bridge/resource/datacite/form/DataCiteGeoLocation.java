package org.metadatacenter.cedar.bridge.resource.datacite.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteGeoLocation {
  @JsonProperty("geoLocationPlace")
  private String geoLocationPlace;
  @JsonProperty("geoLocationPoint")
  private DataCiteGeoLocationPoint geoLocationPoint;
  @JsonProperty("geoLocationBox")
  private DataCiteGeoLocationBox geoLocationBox;
  @JsonProperty("geoLocationPolygon")
  private List<DataCiteGeoLocationPolygon> geoLocationPolygonList;

  public String getGeoLocationPlace() {
    return geoLocationPlace;
  }

  public void setGeoLocationPlace(String geoLocationPlace) {
    this.geoLocationPlace = geoLocationPlace;
  }

  public DataCiteGeoLocationPoint getGeoLocationPoint() {
    return geoLocationPoint;
  }

  public void setGeoLocationPoint(DataCiteGeoLocationPoint geoLocationPoint) {
    this.geoLocationPoint = geoLocationPoint;
  }

  public DataCiteGeoLocationBox getGeoLocationBox() {
    return geoLocationBox;
  }

  public void setGeoLocationBox(DataCiteGeoLocationBox geoLocationBox) {
    this.geoLocationBox = geoLocationBox;
  }

  public List<DataCiteGeoLocationPolygon> getGeoLocationPolygonList() {
    return geoLocationPolygonList;
  }

  public void setGeoLocationPolygonList(List<DataCiteGeoLocationPolygon> geoLocationPolygonList) {
    this.geoLocationPolygonList = geoLocationPolygonList;
  }
}
