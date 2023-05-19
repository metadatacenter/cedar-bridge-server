package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Creator {
    @JsonIgnore
    @JsonProperty("@context")
    private String context;

    @JsonIgnore
    @JsonProperty("@id")
    private String id;

    @JsonProperty("creatorName")
    private ValueFormat creatorName;

    @JsonProperty("nameType")
    private IdFormat nameType;

    @JsonProperty("givenName")
    private ValueFormat givenName;

    @JsonProperty("familyName")
    private ValueFormat familyName;

    @JsonProperty("affiliationElement")
    private List<Affiliation> affiliations;

    @JsonProperty("nameIdentifier")
    private List<NameIdentifier> nameIdentifiers;

    public ValueFormat getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(ValueFormat creatorName) {
        this.creatorName = creatorName;
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
}