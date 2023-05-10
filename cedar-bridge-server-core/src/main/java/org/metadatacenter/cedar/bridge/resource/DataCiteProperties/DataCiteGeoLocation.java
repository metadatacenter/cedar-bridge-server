package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteGeoLocation {
    private String geoLocationPlace;

    private DataCiteGeoLocationPoint geoLocationPoint;

    private DataCiteGeoLocationBox geoLocationBox;

    private List<DataCiteGeoLocationPolygen> geoLocationPolygenList;

    public String getGeoLocationPlace() {
        return geoLocationPlace;
    }

    public void setGeoLocationPlace(String geoLocationPlace) {
        this.geoLocationPlace = geoLocationPlace;
    }

    public DataCiteGeoLocationPoint getGeoLocationPoint() {
        return geoLocationPoint;
    }

    public void setGeoLocationPoint(DataCiteGeoLocationPoint geoLocationPoint) {
        this.geoLocationPoint = geoLocationPoint;
    }

    public DataCiteGeoLocationBox getGeoLocationBox() {
        return geoLocationBox;
    }

    public void setGeoLocationBox(DataCiteGeoLocationBox geoLocationBox) {
        this.geoLocationBox = geoLocationBox;
    }

    public List<DataCiteGeoLocationPolygen> getGeoLocationPolygenList() {
        return geoLocationPolygenList;
    }

    public void setGeoLocationPolygenList(List<DataCiteGeoLocationPolygen> geoLocationPolygenList) {
        this.geoLocationPolygenList = geoLocationPolygenList;
    }
}
