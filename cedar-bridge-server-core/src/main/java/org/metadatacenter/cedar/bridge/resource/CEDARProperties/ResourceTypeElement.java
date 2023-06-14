package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceTypeElement {
    @JsonProperty("@context")
    private Map<String, String> context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("ResourceType")
    private ValueFormat resourceType;

    @JsonProperty("resourceTypeGeneral")
    private IdFormat resourceTypeGeneral;

    public ValueFormat getResourceType() {
        return resourceType;
    }

    public void setResourceType(ValueFormat resourceType) {
        this.resourceType = resourceType;
    }

    public IdFormat getResourceTypeGeneral() {
        return resourceTypeGeneral;
    }

    public void setResourceTypeGeneral(IdFormat resourceTypeGeneral) {
        this.resourceTypeGeneral = resourceTypeGeneral;
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
