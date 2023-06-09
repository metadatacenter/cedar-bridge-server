package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteRelatedItem {
    @JsonProperty("relatedItemType")
    private String relatedItemType;
    @JsonProperty("relationType")
    private String relationType;
    @JsonProperty("volume")
    private String volume;
    @JsonProperty("issue")
    private String issue;
    @JsonProperty("firstPage")
    private String firstPage;
    @JsonProperty("lastPage")
    private String lastPage;
    @JsonProperty("publicationYear")
    private Integer publicationYear;
    @JsonProperty("publisher")
    private String publisher;
    @JsonProperty("edition")
    private String edition;
    @JsonProperty("relatedItemIdentifier")
    private DataCiteRelatedItemIdentifier relatedItemIdentifier;
    @JsonProperty("creators")
    private List<DataCiteCreator> creators;
    @JsonProperty("titles")
    private List<DataCiteTitle> titles;
    @JsonProperty("number")
    private String number;
    @JsonProperty("numberType")
    private String numberType;
    @JsonProperty("contributors")
    private List<DataCiteRelatedItemContributor> contributors;

    public String getRelatedItemType() {
        return relatedItemType;
    }

    public void setRelatedItemType(String relatedItemType) {
        this.relatedItemType = relatedItemType;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(String firstPage) {
        this.firstPage = firstPage;
    }

    public String getLastPage() {
        return lastPage;
    }

    public void setLastPage(String lastPage) {
        this.lastPage = lastPage;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public DataCiteRelatedItemIdentifier getRelatedItemIdentifier() {
        return relatedItemIdentifier;
    }

    public void setRelatedItemIdentifier(DataCiteRelatedItemIdentifier relatedItemIdentifier) {
        this.relatedItemIdentifier = relatedItemIdentifier;
    }

    public List<DataCiteCreator> getCreators() {
        return creators;
    }

    public void setCreators(List<DataCiteCreator> creators) {
        this.creators = creators;
    }

    public List<DataCiteTitle> getTitles() {
        return titles;
    }

    public void setTitles(List<DataCiteTitle> titles) {
        this.titles = titles;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumberType() {
        return numberType;
    }

    public void setNumberType(String numberType) {
        this.numberType = numberType;
    }

    public List<DataCiteRelatedItemContributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<DataCiteRelatedItemContributor> contributors) {
        this.contributors = contributors;
    }
}
