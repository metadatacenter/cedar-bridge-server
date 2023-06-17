package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedItemElement {
    @JsonProperty("@context")
    private Map<String, String> context;

    @JsonProperty("@id")
    private String id;
    @JsonProperty("Related Item")
    private List<RelatedItem> relatedItems;

    public List<RelatedItem> getRelatedItems() {
        return relatedItems;
    }

    public void setRelatedItems(List<RelatedItem> relatedItems) {
        this.relatedItems = relatedItems;
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
