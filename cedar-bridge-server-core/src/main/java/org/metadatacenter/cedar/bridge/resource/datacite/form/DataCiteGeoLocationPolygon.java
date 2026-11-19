package org.metadatacenter.cedar.bridge.resource.datacite.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataCiteGeoLocationPolygon {
  @JsonProperty("polygonPoint")
  private List<DataCiteGeoLocationPoint> polygonPointsList;
  @JsonProperty("inPolygonPoint")
  private DataCiteGeoLocationPoint inPolygonPoint;

  public List<DataCiteGeoLocationPoint> getPolygonPointsList() {
    return polygonPointsList;
  }

  public void setPolygonPointsList(List<DataCiteGeoLocationPoint> polygonPointsList) {
    this.polygonPointsList = polygonPointsList;
  }

  public DataCiteGeoLocationPoint getInPolygonPoint() {
    return inPolygonPoint;
  }

  public void setInPolygonPoint(DataCiteGeoLocationPoint inPolygonPoint) {
    this.inPolygonPoint = inPolygonPoint;
  }
}
