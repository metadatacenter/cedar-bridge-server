package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FloatFormat {
    @JsonProperty("@value")
    private Float value;

    @JsonProperty("@type")
    private String type;

    public Float getValue() {
        if (value == 0.0) {
            return null;
        }
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
