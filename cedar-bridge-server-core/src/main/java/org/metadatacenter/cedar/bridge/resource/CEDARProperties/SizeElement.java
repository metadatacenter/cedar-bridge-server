package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SizeElement {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("Size")
    private List<ValueFormat> sizes;

    public List<ValueFormat> getSizes() {
        return sizes;
    }

    public void setSizes(List<ValueFormat> sizes) {
        this.sizes = sizes;
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
