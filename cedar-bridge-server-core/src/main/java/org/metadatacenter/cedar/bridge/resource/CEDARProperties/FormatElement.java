package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormatElement {
    @JsonProperty("Format")
    private List<ValueFormat> formats;

    public List<ValueFormat> getFormats() {
        return formats;
    }

    public void setFormats(List<ValueFormat> formats) {
        this.formats = formats;
    }
}
