package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CEDARDataCiteInstance {
//    @JsonIgnore
//    @JsonProperty("@context")
//    private String context;
//
//    @JsonIgnore
//    @JsonProperty("@id")
//    private String id;
//
//    @JsonIgnore
//    @JsonProperty("schema:isBasedOn")
//    private String schemaIsBasedOn;
//
//    @JsonIgnore
//    @JsonProperty("schema:name")
//    private String schemaName;
//
//    @JsonIgnore
//    @JsonProperty("schema:description")
//    private String schemaDescription;
//
//    @JsonIgnore
//    @JsonProperty("pav:createdOn")
//    private String pavCreatedOn;
//
//    @JsonIgnore
//    @JsonProperty("pav:createdBy")
//    private String pavCreatedBy;
//
//    @JsonIgnore
//    @JsonProperty("pav:lastUpdatedOn")
//    private String pavLastUpdatedOn;
//
//    @JsonIgnore
//    @JsonProperty("oslc:modifiedBy")
//    private String oslcModifiedBy;

    @JsonProperty("prefix")
    private ValueFormat prefix;

    @JsonProperty("CreatorElement")
    private CreatorElement creatorElement;

    @JsonProperty("TitleElement")
    private TitleElement titleElement;

    @JsonProperty("PublisherElement")
    private PublisherElement publisherElement;

    @JsonProperty("PublicationYearElement")
    private PublicationYearElement publicationYearElement;

    @JsonProperty("SubjectElement")
    private SubjectElement subjectElement;

    @JsonProperty("ResourceTypeElement")
    private ResourceTypeElement resourceTypeElement;

    @JsonProperty("ContributorElement")
    private ContributorElement contributorElement;

    @JsonProperty("DateElement")
    private DateElement dateElement;

    @JsonProperty("Language")
    private IdFormat language;

    @JsonProperty("AlternateIdentifierElement")
    private AlternateIdentifierElement alternateIdentifierElement;

    @JsonProperty("RelatedIdentifierElement")
    private RelatedIdentifierElement relatedIdentifierElement;

    @JsonProperty("SizeElement")
    private SizeElement sizeElement;

    @JsonProperty("FormatElement")
    private FormatElement formatElement;

    @JsonProperty("Version")
    private ValueFormat version;

    @JsonProperty("RightsElement")
    private RightsElement rightsElement;

    @JsonProperty("DescriptionElement")
    private DescriptionElement descriptionElement;

    @JsonProperty("GeoLocationElement")
    private GeoLocationElement geoLocationElement;

    @JsonProperty("FundingReferenceElement")
    private FundingReferenceElement fundingReferenceElement;

    @JsonProperty("RelatedItemElement")
    private RelatedItemElement relatedItemElement;

    public CEDARDataCiteInstance() {

    }

    public ValueFormat getPrefix() {
        return prefix;
    }

    public void setPrefix(ValueFormat prefix) {
        this.prefix = prefix;
    }

    public CreatorElement getCreatorElement() {
        return creatorElement;
    }

    public void setCreatorElement(CreatorElement creatorElement) {
        this.creatorElement = creatorElement;
    }

    public TitleElement getTitleElement() {
        return titleElement;
    }

    public void setTitleElement(TitleElement titleElement) {
        this.titleElement = titleElement;
    }

    public PublisherElement getPublisherElement() {
        return publisherElement;
    }

    public void setPublisherElement(PublisherElement publisherElement) {
        this.publisherElement = publisherElement;
    }

    public PublicationYearElement getPublicationYearElement() {
        return publicationYearElement;
    }

    public void setPublicationYearElement(PublicationYearElement publicationYearElement) {
        this.publicationYearElement = publicationYearElement;
    }

    public SubjectElement getSubjectElement() {
        return subjectElement;
    }

    public void setSubjectElement(SubjectElement subjectElement) {
        this.subjectElement = subjectElement;
    }

    public ResourceTypeElement getResourceTypeElement() {
        return resourceTypeElement;
    }

    public void setResourceTypeElement(ResourceTypeElement resourceTypeElement) {
        this.resourceTypeElement = resourceTypeElement;
    }

    public ContributorElement getContributorElement() {
        return contributorElement;
    }

    public void setContributorElement(ContributorElement contributorElement) {
        this.contributorElement = contributorElement;
    }

    public DateElement getDateElement() {
        return dateElement;
    }

    public void setDateElement(DateElement dateElement) {
        this.dateElement = dateElement;
    }

    public IdFormat getLanguage() {
        return language;
    }

    public void setLanguage(IdFormat language) {
        this.language = language;
    }

    public AlternateIdentifierElement getAlternateIdentifierElement() {
        return alternateIdentifierElement;
    }

    public void setAlternateIdentifierElement(AlternateIdentifierElement alternateIdentifierElement) {
        this.alternateIdentifierElement = alternateIdentifierElement;
    }

    public RelatedIdentifierElement getRelatedIdentifierElement() {
        return relatedIdentifierElement;
    }

    public void setRelatedIdentifierElement(RelatedIdentifierElement relatedIdentifierElement) {
        this.relatedIdentifierElement = relatedIdentifierElement;
    }

    public SizeElement getSizeElement() {
        return sizeElement;
    }

    public void setSizeElement(SizeElement sizeElement) {
        this.sizeElement = sizeElement;
    }

    public FormatElement getFormatElement() {
        return formatElement;
    }

    public void setFormatElement(FormatElement formatElement) {
        this.formatElement = formatElement;
    }

    public ValueFormat getVersion() {
        return version;
    }

    public void setVersion(ValueFormat version) {
        this.version = version;
    }

    public RightsElement getRightsElement() {
        return rightsElement;
    }

    public void setRightsElement(RightsElement rightsElement) {
        this.rightsElement = rightsElement;
    }

    public GeoLocationElement getGeoLocationElement() {
        return geoLocationElement;
    }

    public void setGeoLocationElement(GeoLocationElement geoLocationElement) {
        this.geoLocationElement = geoLocationElement;
    }

    public FundingReferenceElement getFundingReferenceElement() {
        return fundingReferenceElement;
    }

    public void setFundingReferenceElement(FundingReferenceElement fundingReferenceElement) {
        this.fundingReferenceElement = fundingReferenceElement;
    }

    public RelatedItemElement getRelatedItemElement() {
        return relatedItemElement;
    }

    public void setRelatedItemElement(RelatedItemElement relatedItemElement) {
        this.relatedItemElement = relatedItemElement;
    }

    public DescriptionElement getDescriptionElement() {
        return descriptionElement;
    }

    public void setDescriptionElement(DescriptionElement descriptionElement) {
        this.descriptionElement = descriptionElement;
    }
}
