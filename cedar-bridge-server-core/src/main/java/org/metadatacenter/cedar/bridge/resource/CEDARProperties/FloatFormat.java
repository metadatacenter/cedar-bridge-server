package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FloatFormat {
    @JsonProperty("@value")
    private Float value;

    public Float getValue() {
        if (value == 0.0) {
            return null;
        }
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
