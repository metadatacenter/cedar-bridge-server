package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Context {
  @JsonProperty("rdfs")
  private String rdfs;
  @JsonProperty("xsd")
  private String xsd;
  @JsonProperty("pav")
  private String pav;
  @JsonProperty("schema")
  private String schema;
  @JsonProperty("oslc")
  private String oslc;
  @JsonProperty("skos")
  private String skos;
  @JsonProperty("rdfs:label")
  private TypeFormat label;
  @JsonProperty("schema:isBasedOn")
  private TypeFormat isBasedOn;
  @JsonProperty("schema:name")
  private TypeFormat name;
  @JsonProperty("schema:description")
  private TypeFormat description;
  @JsonProperty("pav:derivedFrom")
  private TypeFormat derivedFrom;
  @JsonProperty("pav:createdOn")
  private TypeFormat createdOn;
  @JsonProperty("pav:createdBy")
  private TypeFormat createdBy;
  @JsonProperty("pav:lastUpdatedOn")
  private TypeFormat lastUpdatedOn;
  @JsonProperty("oslc:modifiedBy")
  private TypeFormat modifiedBy;
  @JsonProperty("skos:notation")
  private TypeFormat notation;
  @JsonProperty("prefix")
  private String prefix;
  @JsonProperty("TitleElement")
  private String titleElement;
  @JsonProperty("PublisherElement")
  private String publisherElement;
  @JsonProperty("PublicationYearElement")
  private String publicationYearElement;
  @JsonProperty("SubjectElement")
  private String subjectElement;
  @JsonProperty("DateElement")
  private String dateElement;
  @JsonProperty("Language")
  private String language;
  @JsonProperty("RelatedIdentifierElement")
  private String relatedIdentifierElement;
  @JsonProperty("SizeElement")
  private String sizeElement;
  @JsonProperty("FormatElement")
  private String formatElement;
  @JsonProperty("Version")
  private String version;
  @JsonProperty("RightsElement")
  private String rightsElement;
  @JsonProperty("DescriptionElement")
  private String descriptionElement;
  @JsonProperty("FundingReferenceElement")
  private String fundingReferenceElement;
  @JsonProperty("ResourceTypeElement")
  private String resourceTypeElement;
  @JsonProperty("CreatorElement")
  private String creatorElement;
  @JsonProperty("AlternateIdentifierElement")
  private String alternateIdentifierElement;
  @JsonProperty("ContributorElement")
  private String contributorElement;
  @JsonProperty("RelatedItemElement")
  private String relatedItemElement;
  @JsonProperty("GeoLocationElement")
  private String geoLocationElement;

  public String getRdfs() {
    return rdfs;
  }

  public void setRdfs(String rdfs) {
    this.rdfs = rdfs;
  }

  public String getXsd() {
    return xsd;
  }

  public void setXsd(String xsd) {
    this.xsd = xsd;
  }

  public String getPav() {
    return pav;
  }

  public void setPav(String pav) {
    this.pav = pav;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getOslc() {
    return oslc;
  }

  public void setOslc(String oslc) {
    this.oslc = oslc;
  }

  public String getSkos() {
    return skos;
  }

  public void setSkos(String skos) {
    this.skos = skos;
  }

  public TypeFormat getLabel() {
    return label;
  }

  public void setLabel(TypeFormat label) {
    this.label = label;
  }

  public TypeFormat getIsBasedOn() {
    return isBasedOn;
  }

  public void setIsBasedOn(TypeFormat isBasedOn) {
    this.isBasedOn = isBasedOn;
  }

  public TypeFormat getName() {
    return name;
  }

  public void setName(TypeFormat name) {
    this.name = name;
  }

  public TypeFormat getDescription() {
    return description;
  }

  public void setDescription(TypeFormat description) {
    this.description = description;
  }

  public TypeFormat getDerivedFrom() {
    return derivedFrom;
  }

  public void setDerivedFrom(TypeFormat derivedFrom) {
    this.derivedFrom = derivedFrom;
  }

  public TypeFormat getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(TypeFormat createdOn) {
    this.createdOn = createdOn;
  }

  public TypeFormat getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(TypeFormat createdBy) {
    this.createdBy = createdBy;
  }

  public TypeFormat getLastUpdatedOn() {
    return lastUpdatedOn;
  }

  public void setLastUpdatedOn(TypeFormat lastUpdatedOn) {
    this.lastUpdatedOn = lastUpdatedOn;
  }

  public TypeFormat getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(TypeFormat modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public TypeFormat getNotation() {
    return notation;
  }

  public void setNotation(TypeFormat notation) {
    this.notation = notation;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getTitleElement() {
    return titleElement;
  }

  public void setTitleElement(String titleElement) {
    this.titleElement = titleElement;
  }

  public String getPublisherElement() {
    return publisherElement;
  }

  public void setPublisherElement(String publisherElement) {
    this.publisherElement = publisherElement;
  }

  public String getPublicationYearElement() {
    return publicationYearElement;
  }

  public void setPublicationYearElement(String publicationYearElement) {
    this.publicationYearElement = publicationYearElement;
  }

  public String getSubjectElement() {
    return subjectElement;
  }

  public void setSubjectElement(String subjectElement) {
    this.subjectElement = subjectElement;
  }

  public String getDateElement() {
    return dateElement;
  }

  public void setDateElement(String dateElement) {
    this.dateElement = dateElement;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getRelatedIdentifierElement() {
    return relatedIdentifierElement;
  }

  public void setRelatedIdentifierElement(String relatedIdentifierElement) {
    this.relatedIdentifierElement = relatedIdentifierElement;
  }

  public String getSizeElement() {
    return sizeElement;
  }

  public void setSizeElement(String sizeElement) {
    this.sizeElement = sizeElement;
  }

  public String getFormatElement() {
    return formatElement;
  }

  public void setFormatElement(String formatElement) {
    this.formatElement = formatElement;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getRightsElement() {
    return rightsElement;
  }

  public void setRightsElement(String rightsElement) {
    this.rightsElement = rightsElement;
  }

  public String getDescriptionElement() {
    return descriptionElement;
  }

  public void setDescriptionElement(String descriptionElement) {
    this.descriptionElement = descriptionElement;
  }

  public String getFundingReferenceElement() {
    return fundingReferenceElement;
  }

  public void setFundingReferenceElement(String fundingReferenceElement) {
    this.fundingReferenceElement = fundingReferenceElement;
  }

  public String getResourceTypeElement() {
    return resourceTypeElement;
  }

  public void setResourceTypeElement(String resourceTypeElement) {
    this.resourceTypeElement = resourceTypeElement;
  }

  public String getCreatorElement() {
    return creatorElement;
  }

  public void setCreatorElement(String creatorElement) {
    this.creatorElement = creatorElement;
  }

  public String getAlternateIdentifierElement() {
    return alternateIdentifierElement;
  }

  public void setAlternateIdentifierElement(String alternateIdentifierElement) {
    this.alternateIdentifierElement = alternateIdentifierElement;
  }

  public String getContributorElement() {
    return contributorElement;
  }

  public void setContributorElement(String contributorElement) {
    this.contributorElement = contributorElement;
  }

  public String getRelatedItemElement() {
    return relatedItemElement;
  }

  public void setRelatedItemElement(String relatedItemElement) {
    this.relatedItemElement = relatedItemElement;
  }

  public String getGeoLocationElement() {
    return geoLocationElement;
  }

  public void setGeoLocationElement(String geoLocationElement) {
    this.geoLocationElement = geoLocationElement;
  }
}
