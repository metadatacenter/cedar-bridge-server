package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteGeoLocationBox {
    private Float eastBoundLongitude;
    private Float northBoundLatitude;
    private Float southBoundLatitude;
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
