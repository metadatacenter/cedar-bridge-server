package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedItem {
    @JsonProperty("@context")
    private Map<String, String> context;

    @JsonProperty("@id")
    private String id;

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

    @JsonProperty("relatedIdentifier")
    private ValueFormat relatedIdentifier;

    @JsonProperty("relatedIdentifierType")
    private IdFormat relatedIdentifierType;

    @JsonProperty("relatedMetadataScheme")
    private ValueFormat relatedMetadataScheme;

    @JsonProperty("schemeURI")
    private SchemeURI schemeURi;

    @JsonProperty("schemeType")
    private ValueFormat schemeType;

    @JsonProperty("Creator")
    private List<Creator> creators;

    @JsonProperty("Title")
    private List<Title> titles;

    @JsonProperty("number")
    private ValueFormat number;

    @JsonProperty("numberType")
    private IdFormat numberType;

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

    public ValueFormat getRelatedIdentifier() {
        return relatedIdentifier;
    }

    public void setRelatedIdentifier(ValueFormat relatedIdentifier) {
        this.relatedIdentifier = relatedIdentifier;
    }

    public IdFormat getRelatedIdentifierType() {
        return relatedIdentifierType;
    }

    public void setRelatedIdentifierType(IdFormat relatedIdentifierType) {
        this.relatedIdentifierType = relatedIdentifierType;
    }

    public ValueFormat getRelatedMetadataScheme() {
        return relatedMetadataScheme;
    }

    public void setRelatedMetadataScheme(ValueFormat relatedMetadataScheme) {
        this.relatedMetadataScheme = relatedMetadataScheme;
    }

    public SchemeURI getSchemeURi() {
        return schemeURi;
    }

    public void setSchemeURi(SchemeURI schemeURi) {
        this.schemeURi = schemeURi;
    }

    public ValueFormat getSchemeType() {
        return schemeType;
    }

    public void setSchemeType(ValueFormat schemeType) {
        this.schemeType = schemeType;
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

    public ValueFormat getNumber() {
        return number;
    }

    public void setNumber(ValueFormat number) {
        this.number = number;
    }

    public IdFormat getNumberType() {
        return numberType;
    }

    public void setNumberType(IdFormat numberType) {
        this.numberType = numberType;
    }

    public List<Contributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
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
