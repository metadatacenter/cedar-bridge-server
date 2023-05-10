package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteInstancePart2 {
    @JsonProperty("ContributorElement")
    private ContributorElement contributorElement;

    public DataCiteInstancePart2 () {
    }

    public ContributorElement getContributorElement() {
        return contributorElement;
    }

    public void setContributorElement(ContributorElement contributorElement) {
        this.contributorElement = contributorElement;
    }
}
