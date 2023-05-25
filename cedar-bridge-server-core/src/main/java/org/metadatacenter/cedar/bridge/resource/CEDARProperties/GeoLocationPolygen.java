package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocationPolygen {
    @JsonProperty("polygonPoint")
    private Point polygonPoint;

    @JsonProperty("inPolygonPoint")
    private Point inPolygonPoint;

    public Point getPolygonPoint() {
        return polygonPoint;
    }

    public void setPolygonPoint(Point polygonPoint) {
        this.polygonPoint = polygonPoint;
    }

    public Point getInPolygonPoint() {
        return inPolygonPoint;
    }

    public void setInPolygonPoint(Point inPolygonPoint) {
        this.inPolygonPoint = inPolygonPoint;
    }
}
