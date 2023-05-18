package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteGeoLocationPoint {
    private Float pointLatitude;

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
