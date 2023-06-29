package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocationBox {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
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

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
