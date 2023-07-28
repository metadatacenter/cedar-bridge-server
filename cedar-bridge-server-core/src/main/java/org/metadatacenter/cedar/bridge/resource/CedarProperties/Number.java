package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Number {
    @JsonProperty("number")
    private ValueFormat number;

    @JsonProperty("numberType")
    private IdFormat numberType;

    public ValueFormat getNumber() {
        return number;
    }

    public void setNumber(ValueFormat number) {
        this.number = number;
    }

    public IdFormat getNumberType() {
        return numberType;
    }

    public void setNumberType(IdFormat numberType) {
        this.numberType = numberType;
    }
}
