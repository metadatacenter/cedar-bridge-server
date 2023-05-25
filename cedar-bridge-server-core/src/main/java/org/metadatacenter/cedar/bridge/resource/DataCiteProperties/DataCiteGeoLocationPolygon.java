package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteGeoLocationPolygon {
    @JsonProperty("polygonPoint")
    private DataCiteGeoLocationPoint polygonPoint;
    @JsonProperty("inPolygonPoint")
    private DataCiteGeoLocationPoint inPolygonPoint;

    public DataCiteGeoLocationPoint getPolygonPoint() {
        return polygonPoint;
    }

    public void setPolygonPoint(DataCiteGeoLocationPoint polygonPoint) {
        this.polygonPoint = polygonPoint;
    }

    public DataCiteGeoLocationPoint getInPolygonPoint() {
        return inPolygonPoint;
    }

    public void setInPolygonPoint(DataCiteGeoLocationPoint inPolygonPoint) {
        this.inPolygonPoint = inPolygonPoint;
    }
}
