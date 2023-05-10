package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlternateIdentifier {
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
}
