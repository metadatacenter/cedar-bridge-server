package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundingReference {
    @JsonProperty("funderName")
    private ValueFormat funderName;

    @JsonProperty("Award Number")
    private AwardNumber awardNumber;

    @JsonProperty("awardTitle")
    private ValueFormat awardTitle;

    @JsonProperty("Funder Identifier")
    private FunderIdentifier funderIdentifier;

    public ValueFormat getFunderName() {
        return funderName;
    }

    public void setFunderName(ValueFormat funderName) {
        this.funderName = funderName;
    }

    public AwardNumber getAwardNumber() {
        return awardNumber;
    }

    public void setAwardNumber(AwardNumber awardNumber) {
        this.awardNumber = awardNumber;
    }

    public ValueFormat getAwardTitle() {
        return awardTitle;
    }

    public void setAwardTitle(ValueFormat awardTitle) {
        this.awardTitle = awardTitle;
    }

    public FunderIdentifier getFunderIdentifier() {
        return funderIdentifier;
    }

    public void setFunderIdentifier(FunderIdentifier funderIdentifier) {
        this.funderIdentifier = funderIdentifier;
    }
}
