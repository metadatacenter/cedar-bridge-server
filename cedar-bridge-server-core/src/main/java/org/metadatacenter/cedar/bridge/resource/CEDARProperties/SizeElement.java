package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SizeElement {
    @JsonProperty("Size")
    private List<ValueFormat> sizes;

    public List<ValueFormat> getSizes() {
        return sizes;
    }

    public void setSizes(List<ValueFormat> sizes) {
        this.sizes = sizes;
    }
}
