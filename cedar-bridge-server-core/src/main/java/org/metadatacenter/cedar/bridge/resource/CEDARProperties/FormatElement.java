package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormatElement {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("Format")
    private List<ValueFormat> formats;

    public List<ValueFormat> getFormats() {
        return formats;
    }

    public void setFormats(List<ValueFormat> formats) {
        this.formats = formats;
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
