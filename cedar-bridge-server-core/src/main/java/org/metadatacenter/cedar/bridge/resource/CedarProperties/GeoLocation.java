package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("geoLocationPlace")
    private ValueFormat geoLocationPlace;

    @JsonProperty("geoLocationPoint")
    private Point geoLocationPoint;

    @JsonProperty("geoLocationBox")
    private GeoLocationBox geoLocationBox;

    @JsonProperty("geoLocationPolygon")
    private List<GeoLocationPolygon> geoLocationPolygonList;

    public ValueFormat getGeoLocationPlace() {
        return geoLocationPlace;
    }

    public void setGeoLocationPlace(ValueFormat geoLocationPlace) {
        this.geoLocationPlace = geoLocationPlace;
    }

    public Point getGeoLocationPoint() {
        return geoLocationPoint;
    }

    public void setGeoLocationPoint(Point geoLocationPoint) {
        this.geoLocationPoint = geoLocationPoint;
    }

    public GeoLocationBox getGeoLocationBox() {
        return geoLocationBox;
    }

    public void setGeoLocationBox(GeoLocationBox geoLocationBox) {
        this.geoLocationBox = geoLocationBox;
    }

    public List<GeoLocationPolygon> getGeoLocationPolygonList() {
        return geoLocationPolygonList;
    }

    public void setGeoLocationPolygonList(List<GeoLocationPolygon> geoLocationPolygonList) {
        this.geoLocationPolygonList = geoLocationPolygonList;
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
