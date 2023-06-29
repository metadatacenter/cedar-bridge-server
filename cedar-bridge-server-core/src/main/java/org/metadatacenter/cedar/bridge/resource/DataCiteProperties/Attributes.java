package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attributes {
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("event")
    private String event;
    @JsonProperty("url")
    private String url;
    @JsonProperty("schemaVersion")
    private String schemaVersion;
    @JsonProperty("creators")
    private List<DataCiteCreator> creators;
    @JsonProperty("titles")
    private List<DataCiteTitle> titles;
    @JsonProperty("publisher")
    private String publisher;
    @JsonProperty("publicationYear")
    private int publicationYear;
    @JsonProperty("subjects")
    private List<DataCiteSubject> subjects;
    @JsonProperty("contributors")
    private List<DataCiteContributor> contributors;
    @JsonProperty("dates")
    private List<DataCiteDate> dates;
    @JsonProperty("alternateIdentifiers")
    private List<DataCiteAlternateIdentifier> alternateIdentifiers;
    @JsonProperty("language")
    private String language;
    @JsonProperty("types")
    private DataCiteType types;
    @JsonProperty("relatedIdentifiers")
    private List<DataCiteRelatedIdentifier> relatedIdentifiers;
    @JsonProperty("sizes")
    private List<String> sizes;
    @JsonProperty("formats")
    private List<String> formats;
    @JsonProperty("version")
    private String version;
    @JsonProperty("rightsList")
    private List<DataCiteRights> rightsList;
    @JsonProperty("descriptions")
    private List<DataCiteDescription> descriptions;
    @JsonProperty("geoLocations")
    private List<DataCiteGeoLocation> geoLocations;
    @JsonProperty("fundingReferences")
    private List<DataCiteFundingReference> fundingReferences;
    @JsonProperty("relatedItems")
    private List<DataCiteRelatedItem> relatedItems;

    public Attributes() {
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
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


    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public List<DataCiteSubject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<DataCiteSubject> subjects) {
        this.subjects = subjects;
    }

    public List<DataCiteContributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<DataCiteContributor> contributors) {
        this.contributors = contributors;
    }

    public List<DataCiteDate> getDates() {
        return dates;
    }

    public void setDates(List<DataCiteDate> dates) {
        this.dates = dates;
    }

    public List<DataCiteAlternateIdentifier> getAlternateIdentifiers() {
        return alternateIdentifiers;
    }

    public void setAlternateIdentifiers(List<DataCiteAlternateIdentifier> alternateIdentifiers) {
        this.alternateIdentifiers = alternateIdentifiers;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public DataCiteType getTypes() {
        return types;
    }

    public void setTypes(DataCiteType types) {
        this.types = types;
    }

    public List<DataCiteRelatedIdentifier> getRelatedIdentifiers() {
        return relatedIdentifiers;
    }

    public void setRelatedIdentifiers(List<DataCiteRelatedIdentifier> relatedIdentifiers) {
        this.relatedIdentifiers = relatedIdentifiers;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<DataCiteRights> getRightsList() {
        return rightsList;
    }

    public void setRightsList(List<DataCiteRights> rightsList) {
        this.rightsList = rightsList;
    }

    public List<DataCiteDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<DataCiteDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public List<DataCiteGeoLocation> getGeoLocations() {
        return geoLocations;
    }

    public void setGeoLocations(List<DataCiteGeoLocation> geoLocations) {
        this.geoLocations = geoLocations;
    }

    public List<DataCiteFundingReference> getFundingReferences() {
        return fundingReferences;
    }

    public void setFundingReferences(List<DataCiteFundingReference> fundingReferences) {
        this.fundingReferences = fundingReferences;
    }

    public List<DataCiteRelatedItem> getRelatedItems() {
        return relatedItems;
    }

    public void setRelatedItems(List<DataCiteRelatedItem> relatedItems) {
        this.relatedItems = relatedItems;
    }
}
