package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteRelatedItem {
    private String relatedItemType;

    private String relationType;

    private String volume;

    private String issue;

    private String firstPage;

    private String lastPage;

    private String PublicationYear;

    private String Publisher;

    private String Edition;

    private DataCiteRelatedItemIdentifier relatedItemIdentifier;

    private List<DataCiteCreator> creators;

    private List<DataCiteTitle> titles;

    private DataCiteNumber number;

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

    public String getPublicationYear() {
        return PublicationYear;
    }

    public void setPublicationYear(String publicationYear) {
        PublicationYear = publicationYear;
    }

    public String getPublisher() {
        return Publisher;
    }

    public void setPublisher(String publisher) {
        Publisher = publisher;
    }

    public String getEdition() {
        return Edition;
    }

    public void setEdition(String edition) {
        Edition = edition;
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

    public DataCiteNumber getNumber() {
        return number;
    }

    public void setNumber(DataCiteNumber number) {
        this.number = number;
    }

    public List<DataCiteRelatedItemContributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<DataCiteRelatedItemContributor> contributors) {
        this.contributors = contributors;
    }
}
