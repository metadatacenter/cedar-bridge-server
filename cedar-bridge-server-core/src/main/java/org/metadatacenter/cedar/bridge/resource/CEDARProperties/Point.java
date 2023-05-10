package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Point {
    @JsonProperty("pointLongitude")
    private FloatFormat pointLongitude;

    @JsonProperty("pointLatitude")
    private FloatFormat pointLatitude;

    public FloatFormat getPointLongitude() {
        return pointLongitude;
    }

    public void setPointLongitude(FloatFormat pointLongitude) {
        this.pointLongitude = pointLongitude;
    }

    public FloatFormat getPointLatitude() {
        return pointLatitude;
    }

    public void setPointLatitude(FloatFormat pointLatitude) {
        this.pointLatitude = pointLatitude;
    }
}
