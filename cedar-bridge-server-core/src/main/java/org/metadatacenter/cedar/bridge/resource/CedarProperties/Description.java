package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Description {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
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
