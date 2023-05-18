package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundingReferenceElement {
    @JsonProperty("Funding Reference")
    private List<FundingReference> fundingReferences;

    public List<FundingReference> getFundingReferences() {
        return fundingReferences;
    }

    public void setFundingReferences(List<FundingReference> fundingReferences) {
        this.fundingReferences = fundingReferences;
    }
}
