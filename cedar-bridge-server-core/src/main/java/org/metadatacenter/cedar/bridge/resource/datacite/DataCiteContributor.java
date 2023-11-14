package org.metadatacenter.cedar.bridge.resource.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteContributor {
    @JsonProperty("name")
    private String name;
    @JsonProperty("nameType")
    private String nameType;
    @JsonProperty("givenName")
    private String givenName;
    @JsonProperty("familyName")
    private String familyName;
    @JsonProperty("affiliation")
    private List<DataCiteAffiliation> affiliations;
    @JsonProperty("contributorType")
    private String contributorType;
    @JsonProperty("nameIdentifiers")
    private List<DataCiteNameIdentifier> nameIdentifiers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameType() {
        return nameType;
    }

    public void setNameType(String nameType) {
        this.nameType = nameType;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public List<DataCiteAffiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(List<DataCiteAffiliation> affiliations) {
        this.affiliations = affiliations;
    }

    public String getContributorType() {
        return contributorType;
    }

    public void setContributorType(String contributorType) {
        this.contributorType = contributorType;
    }

    public List<DataCiteNameIdentifier> getNameIdentifiers() {
        return nameIdentifiers;
    }

    public void setNameIdentifiers(List<DataCiteNameIdentifier> nameIdentifiers) {
        this.nameIdentifiers = nameIdentifiers;
    }
}
