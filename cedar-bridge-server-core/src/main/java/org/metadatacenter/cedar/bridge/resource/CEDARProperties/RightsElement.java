package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RightsElement {
    @JsonProperty("Rights")
    private List<Rights> rightsList;

    public List<Rights> getRightsList() {
        return rightsList;
    }

    public void setRightsList(List<Rights> rightsList) {
        this.rightsList = rightsList;
    }
}
