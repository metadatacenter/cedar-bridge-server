package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlternateIdentifier {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("alternateIdentifier")
    private ValueFormat alternateIdentifier;

    @JsonProperty("alternateIdentifierType")
    private ValueFormat alternateIdentifierType;

    public ValueFormat getAlternateIdentifier() {
        return alternateIdentifier;
    }

    public void setAlternateIdentifier(ValueFormat alternateIdentifier) {
        this.alternateIdentifier = alternateIdentifier;
    }

    public ValueFormat getAlternateIdentifierType() {
        return alternateIdentifierType;
    }

    public void setAlternateIdentifierType(ValueFormat alternateIdentifierType) {
        this.alternateIdentifierType = alternateIdentifierType;
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
