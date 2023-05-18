package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Title {
    @JsonIgnore
    @JsonProperty("@context")
    private String context;

    @JsonIgnore
    @JsonProperty("@id")
    private String id;

    @JsonProperty("titleName")
    private ValueFormat titleName;

    @JsonProperty("titleType")
    private IdFormat titleType;

    @JsonProperty("language")
    private ValueFormat language;

    public ValueFormat getTitleName() {
        return titleName;
    }

    public void setTitleName(ValueFormat titleName) {
        this.titleName = titleName;
    }

    public IdFormat getTitleType() {
        return titleType;
    }

    public void setTitleType(IdFormat titleType) {
        this.titleType = titleType;
    }

    public ValueFormat getLanguage() {
        return language;
    }

    public void setLanguage(ValueFormat language) {
        this.language = language;
    }
}