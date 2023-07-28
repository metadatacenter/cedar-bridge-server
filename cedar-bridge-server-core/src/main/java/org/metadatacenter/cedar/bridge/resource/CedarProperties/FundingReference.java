package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundingReference {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("funderName")
    private ValueFormat funderName;

    @JsonProperty("awardNumber")
    private ValueFormat awardNumber;

    @JsonProperty("awardURI")
    private SchemeURI awardURI;

    @JsonProperty("awardTitle")
    private ValueFormat awardTitle;

    @JsonProperty("funderIdentifier")
    private ValueFormat funderIdentifier;

    @JsonProperty("funderIdentifierType")
    private IdFormat funderIdentifierType;

    @JsonProperty("schemeURI")
    private SchemeURI schemeURI;

    public ValueFormat getFunderName() {
        return funderName;
    }

    public void setFunderName(ValueFormat funderName) {
        this.funderName = funderName;
    }

    public ValueFormat getAwardTitle() {
        return awardTitle;
    }

    public void setAwardTitle(ValueFormat awardTitle) {
        this.awardTitle = awardTitle;
    }

    public ValueFormat getAwardNumber() {
        return awardNumber;
    }

    public void setAwardNumber(ValueFormat awardNumber) {
        this.awardNumber = awardNumber;
    }

    public SchemeURI getAwardURI() {
        return awardURI;
    }

    public void setAwardURI(SchemeURI awardURI) {
        this.awardURI = awardURI;
    }

    public ValueFormat getFunderIdentifier() {
        return funderIdentifier;
    }

    public void setFunderIdentifier(ValueFormat funderIdentifier) {
        this.funderIdentifier = funderIdentifier;
    }

    public IdFormat getFunderIdentifierType() {
        return funderIdentifierType;
    }

    public void setFunderIdentifierType(IdFormat funderIdentifierType) {
        this.funderIdentifierType = funderIdentifierType;
    }

    public SchemeURI getSchemeURI() {
        return schemeURI;
    }

    public void setSchemeURI(SchemeURI schemeURI) {
        this.schemeURI = schemeURI;
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
