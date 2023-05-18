package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocationPolygen {
    @JsonProperty("polygonPoint")
    private Point polygenPoint;

    @JsonProperty("inPolygenPoint")
    private Point inPolygenPoint;

    public Point getPolygenPoint() {
        return polygenPoint;
    }

    public void setPolygenPoint(Point polygenPoint) {
        this.polygenPoint = polygenPoint;
    }

    public Point getInPolygenPoint() {
        return inPolygenPoint;
    }

    public void setInPolygenPoint(Point inPolygenPoint) {
        this.inPolygenPoint = inPolygenPoint;
    }
}
