package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CEDARDataCiteInstance {

    @JsonProperty("@context")
    private Context context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("schema:isBasedOn")
    private String schemaIsBasedOn;

    @JsonProperty("schema:name")
    private String schemaName;

    @JsonProperty("schema:description")
    private String schemaDescription;

    @JsonProperty("pav:createdOn")
    private String pavCreatedOn;

    @JsonProperty("pav:createdBy")
    private String pavCreatedBy;

    @JsonProperty("pav:lastUpdatedOn")
    private String pavLastUpdatedOn;

    @JsonProperty("oslc:modifiedBy")
    private String oslcModifiedBy;

    @JsonProperty("prefix")
    private ValueFormat prefix;

    @JsonProperty("creator")
    private List<Creator> creators;

    @JsonProperty("title")
    private List<Title> titles;;

    @JsonProperty("publisher")
    private Publisher publisher;

    @JsonProperty("publicationYear")
    private PublicationYear publicationYear;

    @JsonProperty("resourceType")
    private ValueFormat resourceType;

    @JsonProperty("subject")
    private List<Subject> subjects;

    @JsonProperty("contributor")
    private List<Contributor> contributors;

    @JsonProperty("dateElement")
    private List<Date> dates;

    @JsonProperty("language")
    private IdFormat language;

    @JsonProperty("alternateIdentifierElement")
    private List<AlternateIdentifier> alternateIdentifiers;

    @JsonProperty("relatedIdentifierElement")
    private List<RelatedIdentifier> relatedIdentifiers;

    @JsonProperty("size")
    private List<ValueFormat> sizes;

    @JsonProperty("format")
    private List<ValueFormat> formats;

    @JsonProperty("version")
    private ValueFormat version;

    @JsonProperty("rightsElement")
    private List<Rights> rightsList;

    @JsonProperty("descriptionElement")
    private List<Description> descriptions;

    @JsonProperty("geoLocation")
    private List<GeoLocation> geoLocations;

    @JsonProperty("fundingReference")
    private List<FundingReference> fundingReferences;

    @JsonProperty("relatedItem")
    private List<RelatedItem> relatedItems;

    public CEDARDataCiteInstance() {

    }

    public ValueFormat getPrefix() {
        return prefix;
    }

    public void setPrefix(ValueFormat prefix) {
        this.prefix = prefix;
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

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public PublicationYear getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(PublicationYear publicationYear) {
        this.publicationYear = publicationYear;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public ValueFormat getResourceType() {
        return resourceType;
    }

    public void setResourceType(ValueFormat resourceType) {
        this.resourceType = resourceType;
    }

    public List<Contributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
    }

    public List<Date> getDates() {
        return dates;
    }

    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

    public IdFormat getLanguage() {
        return language;
    }

    public void setLanguage(IdFormat language) {
        this.language = language;
    }

    public List<AlternateIdentifier> getAlternateIdentifiers() {
        return alternateIdentifiers;
    }

    public void setAlternateIdentifiers(List<AlternateIdentifier> alternateIdentifiers) {
        this.alternateIdentifiers = alternateIdentifiers;
    }

    public List<RelatedIdentifier> getRelatedIdentifiers() {
        return relatedIdentifiers;
    }

    public void setRelatedIdentifiers(List<RelatedIdentifier> relatedIdentifiers) {
        this.relatedIdentifiers = relatedIdentifiers;
    }

    public List<ValueFormat> getSizes() {
        return sizes;
    }

    public void setSizes(List<ValueFormat> sizes) {
        this.sizes = sizes;
    }

    public List<ValueFormat> getFormats() {
        return formats;
    }

    public void setFormats(List<ValueFormat> formats) {
        this.formats = formats;
    }

    public ValueFormat getVersion() {
        return version;
    }

    public void setVersion(ValueFormat version) {
        this.version = version;
    }

    public List<Rights> getRightsList() {
        return rightsList;
    }

    public void setRightsList(List<Rights> rightsList) {
        this.rightsList = rightsList;
    }

    public List<GeoLocation> getGeoLocations() {
        return geoLocations;
    }

    public void setGeoLocations(List<GeoLocation> geoLocations) {
        this.geoLocations = geoLocations;
    }

    public List<FundingReference> getFundingReferences() {
        return fundingReferences;
    }

    public void setFundingReferences(List<FundingReference> fundingReferences) {
        this.fundingReferences = fundingReferences;
    }

    public List<RelatedItem> getRelatedItems() {
        return relatedItems;
    }

    public void setRelatedItems(List<RelatedItem> relatedItems) {
        this.relatedItems = relatedItems;
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<Description> descriptions) {
        this.descriptions = descriptions;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchemaIsBasedOn() {
        return schemaIsBasedOn;
    }

    public void setSchemaIsBasedOn(String schemaIsBasedOn) {
        this.schemaIsBasedOn = schemaIsBasedOn;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaDescription() {
        return schemaDescription;
    }

    public void setSchemaDescription(String schemaDescription) {
        this.schemaDescription = schemaDescription;
    }

    public String getPavCreatedOn() {
        return pavCreatedOn;
    }

    public void setPavCreatedOn(String pavCreatedOn) {
        this.pavCreatedOn = pavCreatedOn;
    }

    public String getPavCreatedBy() {
        return pavCreatedBy;
    }

    public void setPavCreatedBy(String pavCreatedBy) {
        this.pavCreatedBy = pavCreatedBy;
    }

    public String getPavLastUpdatedOn() {
        return pavLastUpdatedOn;
    }

    public void setPavLastUpdatedOn(String pavLastUpdatedOn) {
        this.pavLastUpdatedOn = pavLastUpdatedOn;
    }

    public String getOslcModifiedBy() {
        return oslcModifiedBy;
    }

    public void setOslcModifiedBy(String oslcModifiedBy) {
        this.oslcModifiedBy = oslcModifiedBy;
    }
}
