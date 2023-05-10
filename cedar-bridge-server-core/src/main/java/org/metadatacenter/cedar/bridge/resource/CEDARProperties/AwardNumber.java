package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AwardNumber {
    @JsonProperty("awardNumber")
    private ValueFormat awardNumber;

    @JsonProperty("awardURI")
    private IdFormat awardURI;

    public ValueFormat getAwardNumber() {
        return awardNumber;
    }

    public void setAwardNumber(ValueFormat awardNumber) {
        this.awardNumber = awardNumber;
    }

    public IdFormat getAwardURI() {
        return awardURI;
    }

    public void setAwardURI(IdFormat awardURI) {
        this.awardURI = awardURI;
    }
}
