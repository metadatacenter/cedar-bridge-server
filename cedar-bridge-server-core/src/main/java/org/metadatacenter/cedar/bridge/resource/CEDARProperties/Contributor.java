package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Contributor {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;

    @JsonProperty("contributorName")
    private ValueFormat contributorName;

    @JsonProperty("nameType")
    private IdFormat nameType;

    @JsonProperty("givenName")
    private ValueFormat givenName;

    @JsonProperty("familyName")
    private ValueFormat familyName;

    @JsonProperty("Affiliation")
    private List<Affiliation> affiliations;

    @JsonProperty("nameIdentifier")
    private List<NameIdentifier> nameIdentifiers;

    @JsonProperty("contributorType")
    private IdFormat contributorType;

    public ValueFormat getContributorName() {
        return contributorName;
    }

    public void setContributorName(ValueFormat contributorName) {
        this.contributorName = contributorName;
    }

    public IdFormat getNameType() {
        return nameType;
    }

    public void setNameType(IdFormat nameType) {
        this.nameType = nameType;
    }

    public ValueFormat getGivenName() {
        return givenName;
    }

    public void setGivenName(ValueFormat givenName) {
        this.givenName = givenName;
    }

    public ValueFormat getFamilyName() {
        return familyName;
    }

    public void setFamilyName(ValueFormat familyName) {
        this.familyName = familyName;
    }

    public List<Affiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(List<Affiliation> affiliations) {
        this.affiliations = affiliations;
    }

    public List<NameIdentifier> getNameIdentifiers() {
        return nameIdentifiers;
    }

    public void setNameIdentifiers(List<NameIdentifier> nameIdentifiers) {
        this.nameIdentifiers = nameIdentifiers;
    }

    public IdFormat getContributorType() {
        return contributorType;
    }

    public void setContributorType(IdFormat contributorType) {
        this.contributorType = contributorType;
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

