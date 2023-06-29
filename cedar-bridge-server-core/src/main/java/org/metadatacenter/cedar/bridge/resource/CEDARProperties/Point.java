package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Point {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
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
