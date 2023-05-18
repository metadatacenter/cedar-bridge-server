package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteGeoLocationPolygen {
    private DataCiteGeoLocationPoint polygonPoint;
    private DataCiteGeoLocationPoint inPolygenPoint;

    public DataCiteGeoLocationPoint getPolygonPoint() {
        return polygonPoint;
    }

    public void setPolygonPoint(DataCiteGeoLocationPoint polygonPoint) {
        this.polygonPoint = polygonPoint;
    }

    public DataCiteGeoLocationPoint getInPolygenPoint() {
        return inPolygenPoint;
    }

    public void setInPolygenPoint(DataCiteGeoLocationPoint inPolygenPoint) {
        this.inPolygenPoint = inPolygenPoint;
    }
}
