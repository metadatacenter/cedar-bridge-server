package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocationPolygon {
    @JsonProperty("polygonPoint")
    private List<Point> polygonPointsList;

    @JsonProperty("inPolygonPoint")
    private Point inPolygonPoint;

    public List<Point> getPolygonPointsList() {
        return polygonPointsList;
    }

    public void setPolygonPointsList(List<Point> polygonPointsList) {
        this.polygonPointsList = polygonPointsList;
    }

    public Point getInPolygonPoint() {
        return inPolygonPoint;
    }

    public void setInPolygonPoint(Point inPolygonPoint) {
        this.inPolygonPoint = inPolygonPoint;
    }
}
