package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteCreator {
    @JsonProperty("name")
    private String name;
    @JsonProperty("nameType")
    private String nameType;
    @JsonProperty("givenName")
    private String givenName;
    @JsonProperty("familyName")
    private String familyName;
    @JsonProperty("affiliations")
    private List<DataCiteAffiliation> dataCiteAffiliations;
    @JsonProperty("nameIdentifiers")
    private List<DataCiteNameIdentifier> dataCiteNameIdentifiers;

    public DataCiteCreator() {
    }

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
        return dataCiteAffiliations;
    }

    public void setAffiliations(List<DataCiteAffiliation> dataCiteAffiliations) {
        this.dataCiteAffiliations = dataCiteAffiliations;
    }

    public List<DataCiteNameIdentifier> getNameIdentifiers() {
        return dataCiteNameIdentifiers;
    }

    public void setNameIdentifiers(List<DataCiteNameIdentifier> dataCiteNameIdentifiers) {
        this.dataCiteNameIdentifiers = dataCiteNameIdentifiers;
    }


}
