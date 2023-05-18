package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceTypeElement {
    @JsonIgnore
    @JsonProperty("@context")
    private String context;

    @JsonIgnore
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
}
