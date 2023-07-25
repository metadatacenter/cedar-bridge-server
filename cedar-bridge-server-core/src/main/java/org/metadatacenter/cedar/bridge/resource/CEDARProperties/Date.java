package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Date {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("date")
    private ValueAndTypeFormat date;
    @JsonProperty("dateType")
    private IdFormat dateType;
    @JsonProperty("dateInformation")
    private ValueFormat dateInformation;

    public ValueAndTypeFormat getDate() {
        return date;
    }

    public void setDate(ValueAndTypeFormat date) {
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
