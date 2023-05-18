package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Number {
    @JsonProperty("number")
    private ValueFormat number;

    @JsonProperty("numberType")
    private ValueFormat numberType;

    public ValueFormat getNumber() {
        return number;
    }

    public void setNumber(ValueFormat number) {
        this.number = number;
    }

    public ValueFormat getNumberType() {
        return numberType;
    }

    public void setNumberType(ValueFormat numberType) {
        this.numberType = numberType;
    }
}
