package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Date {
    @JsonProperty("date")
    private ValueFormat date;
    @JsonProperty("dateType")
    private IdFormat dateType;
    @JsonProperty("dateInformation")
    private ValueFormat dateInformation;

    public ValueFormat getDate() {
        return date;
    }

    public void setDate(ValueFormat date) {
        this.date = date;
    }

    public IdFormat getDateType() {
        return dateType;
    }

    public void setDateType(IdFormat dateType) {
        this.dateType = dateType;
    }

    public ValueFormat getDateInformation() {
        return dateInformation;
    }

    public void setDateInformation(ValueFormat dateInformation) {
        this.dateInformation = dateInformation;
    }
}
