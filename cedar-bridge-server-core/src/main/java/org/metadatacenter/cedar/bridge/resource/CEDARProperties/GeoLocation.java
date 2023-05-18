package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation {
    @JsonProperty("geoLocationPlace")
    private ValueFormat geoLocationPlace;


    @JsonProperty("Geo Location Point")
    private Point geoLocationPoint;

    @JsonProperty("Geo Location Box")
    private GeoLocationBox geoLocationBox;

    @JsonProperty("geoLocationPolygen")
    private List<GeoLocationPolygen> geoLocationPolygenList;

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

    public List<GeoLocationPolygen> getGeoLocationPolygenList() {
        return geoLocationPolygenList;
    }

    public void setGeoLocationPolygenList(List<GeoLocationPolygen> geoLocationPolygenList) {
        this.geoLocationPolygenList = geoLocationPolygenList;
    }
}
