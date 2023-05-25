package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteFundingReference {
    @JsonProperty("awardURI")
    private String awardUri;
    @JsonProperty("awardTitle")
    private String awardTitle;
    @JsonProperty("funderName")
    private String funderName;
    @JsonProperty("awardNumber")
    private String awardNumber;
    @JsonProperty("funderIdentifier")
    private String funderIdentifier;
    @JsonProperty("funderIdentifierType")
    private String funderIdentifierType;
    @JsonProperty("schemeUri")
    private String schemeUri;

    public String getAwardUri() {
        return awardUri;
    }

    public void setAwardUri(String awardUri) {
        this.awardUri = awardUri;
    }

    public String getAwardTitle() {
        return awardTitle;
    }

    public void setAwardTitle(String awardTitle) {
        this.awardTitle = awardTitle;
    }

    public String getFunderName() {
        return funderName;
    }

    public void setFunderName(String funderName) {
        this.funderName = funderName;
    }

    public String getAwardNumber() {
        return awardNumber;
    }

    public void setAwardNumber(String awardNumber) {
        this.awardNumber = awardNumber;
    }

    public String getFunderIdentifier() {
        return funderIdentifier;
    }

    public void setFunderIdentifier(String funderIdentifier) {
        this.funderIdentifier = funderIdentifier;
    }

    public String getFunderIdentifierType() {
        return funderIdentifierType;
    }

    public void setFunderIdentifierType(String funderIdentifierType) {
        this.funderIdentifierType = funderIdentifierType;
    }

    public String getSchemeUri() {
        return schemeUri;
    }

    public void setSchemeUri(String schemeUri) {
        this.schemeUri = schemeUri;
    }
}
