package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteDate {
    private String date;

    private String dateType;

    private String dateInformation;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public String getDateInformation() {
        return dateInformation;
    }

    public void setDateInformation(String dateInformation) {
        this.dateInformation = dateInformation;
    }
}
