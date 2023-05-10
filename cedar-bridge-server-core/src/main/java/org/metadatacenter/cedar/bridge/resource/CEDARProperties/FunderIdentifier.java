package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FunderIdentifier {
    @JsonProperty("funderIdentifier")
    private ValueFormat funderIdentifier;

    @JsonProperty("funderIdentifierType")
    private ValueFormat funderIdentifierType;

    @JsonProperty("SchemeURI")
    private SchemeURI schemeURI;

    public ValueFormat getFunderIdentifier() {
        return funderIdentifier;
    }

    public void setFunderIdentifier(ValueFormat funderIdentifier) {
        this.funderIdentifier = funderIdentifier;
    }

    public ValueFormat getFunderIdentifierType() {
        return funderIdentifierType;
    }

    public void setFunderIdentifierType(ValueFormat funderIdentifierType) {
        this.funderIdentifierType = funderIdentifierType;
    }

    public SchemeURI getSchemeURI() {
        return schemeURI;
    }

    public void setSchemeURI(SchemeURI schemeURI) {
        this.schemeURI = schemeURI;
    }
}
