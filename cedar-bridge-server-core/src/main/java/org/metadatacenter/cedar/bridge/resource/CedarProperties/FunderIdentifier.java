package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FunderIdentifier {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("funderIdentifier")
    private ValueFormat funderIdentifier;

    @JsonProperty("funderIdentifierType")
    private IdFormat funderIdentifierType;

    @JsonProperty("SchemeURI")
    private SchemeUri schemeURI;

    public ValueFormat getFunderIdentifier() {
        return funderIdentifier;
    }

    public void setFunderIdentifier(ValueFormat funderIdentifier) {
        this.funderIdentifier = funderIdentifier;
    }

    public IdFormat getFunderIdentifierType() {
        return funderIdentifierType;
    }

    public void setFunderIdentifierType(IdFormat funderIdentifierType) {
        this.funderIdentifierType = funderIdentifierType;
    }

    public SchemeUri getSchemeURI() {
        return schemeURI;
    }

    public void setSchemeURI(SchemeUri schemeURI) {
        this.schemeURI = schemeURI;
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
