package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocationBox {
    @JsonProperty("westBoundLongitude")
    private FloatFormat westBoundLongitude;

    @JsonProperty("eastBoundLongitude")
    private FloatFormat eastBoundLongitude;

    @JsonProperty("southBoundLatitude")
    private FloatFormat southBoundLatitude;

    @JsonProperty("northBoundLatitude")
    private FloatFormat northBoundLatitude;

    public FloatFormat getWestBoundLongitude() {
        return westBoundLongitude;
    }

    public void setWestBoundLongitude(FloatFormat westBoundLongitude) {
        this.westBoundLongitude = westBoundLongitude;
    }

    public FloatFormat getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    public void setEastBoundLongitude(FloatFormat eastBoundLongitude) {
        this.eastBoundLongitude = eastBoundLongitude;
    }

    public FloatFormat getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    public void setSouthBoundLatitude(FloatFormat southBoundLatitude) {
        this.southBoundLatitude = southBoundLatitude;
    }

    public FloatFormat getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    public void setNorthBoundLatitude(FloatFormat northBoundLatitude) {
        this.northBoundLatitude = northBoundLatitude;
    }
}
