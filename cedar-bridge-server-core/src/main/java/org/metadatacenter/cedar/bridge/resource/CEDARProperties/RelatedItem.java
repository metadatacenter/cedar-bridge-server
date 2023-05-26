package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedItem {
    @JsonProperty("relatedItemType")
    private IdFormat relatedItemType;

    @JsonProperty("relationType")
    private IdFormat relationType;

    @JsonProperty("volume")
    private ValueFormat volume;

    @JsonProperty("issue")
    private ValueFormat issue;

    @JsonProperty("firstPage")
    private ValueFormat firstPage;

    @JsonProperty("lastPage")
    private ValueFormat lastPage;

    @JsonProperty("PublicationYear")
    private PublicationYear publicationYear;

    @JsonProperty("Publisher")
    private ValueFormat publisher;

    @JsonProperty("edition")
    private ValueFormat edition;

    @JsonProperty("Related Item Identifier Element")
    private RelatedIdentifier relatedItemIdentifier;

    @JsonProperty("Creator")
    private List<Creator> creators;

    @JsonProperty("Title")
    private List<Title> titles;

    @JsonProperty("Number")
    private Number number;

    @JsonProperty("Contributor")
    private List<Contributor> contributors;

    public IdFormat getRelatedItemType() {
        return relatedItemType;
    }

    public void setRelatedItemType(IdFormat relatedItemType) {
        this.relatedItemType = relatedItemType;
    }

    public IdFormat getRelationType() {
        return relationType;
    }

    public void setRelationType(IdFormat relationType) {
        this.relationType = relationType;
    }

    public ValueFormat getVolume() {
        return volume;
    }

    public void setVolume(ValueFormat volume) {
        this.volume = volume;
    }

    public ValueFormat getIssue() {
        return issue;
    }

    public void setIssue(ValueFormat issue) {
        this.issue = issue;
    }

    public ValueFormat getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(ValueFormat firstPage) {
        this.firstPage = firstPage;
    }

    public ValueFormat getLastPage() {
        return lastPage;
    }

    public void setLastPage(ValueFormat lastPage) {
        this.lastPage = lastPage;
    }

    public PublicationYear getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(PublicationYear publicationYear) {
        this.publicationYear = publicationYear;
    }

    public ValueFormat getPublisher() {
        return publisher;
    }

    public void setPublisher(ValueFormat publisher) {
        this.publisher = publisher;
    }

    public ValueFormat getEdition() {
        return edition;
    }

    public void setEdition(ValueFormat edition) {
        this.edition = edition;
    }

    public RelatedIdentifier getRelatedItemIdentifier() {
        return relatedItemIdentifier;
    }

    public void setRelatedItemIdentifier(RelatedIdentifier relatedItemIdentifier) {
        this.relatedItemIdentifier = relatedItemIdentifier;
    }

    public List<Creator> getCreators() {
        return creators;
    }

    public void setCreators(List<Creator> creators) {
        this.creators = creators;
    }

    public List<Title> getTitles() {
        return titles;
    }

    public void setTitles(List<Title> titles) {
        this.titles = titles;
    }

    public Number getNumber() {
        return number;
    }

    public void setNumber(Number number) {
        this.number = number;
    }

    public List<Contributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
    }
}
