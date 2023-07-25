package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicationYear {
    @JsonProperty("@value")
    private String value;

    @JsonProperty("@type")
    private String type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if (value == null || value.isEmpty()) {
            return null;
        } else {
            return value.substring(0, Math.min(4, value.length()));
        }
    }
}
