package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Description {
    @JsonProperty("Description")
    private ValueFormat description;
    @JsonProperty("descriptionType")
    private IdFormat descriptionType;

    @JsonProperty("Language")
    private ValueFormat language;

    public ValueFormat getDescription() {
        return description;
    }

    public void setDescription(ValueFormat description) {
        this.description = description;
    }

    public IdFormat getDescriptionType() {
        return descriptionType;
    }

    public void setDescriptionType(IdFormat descriptionType) {
        this.descriptionType = descriptionType;
    }

    public ValueFormat getLanguage() {
        return language;
    }

    public void setLanguage(ValueFormat language) {
        this.language = language;
    }
}
