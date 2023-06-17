package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundingReferenceElement {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("Funding Reference")
    private List<FundingReference> fundingReferences;

    public List<FundingReference> getFundingReferences() {
        return fundingReferences;
    }

    public void setFundingReferences(List<FundingReference> fundingReferences) {
        this.fundingReferences = fundingReferences;
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
