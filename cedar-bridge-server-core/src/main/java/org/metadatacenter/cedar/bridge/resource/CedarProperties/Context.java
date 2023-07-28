package org.metadatacenter.cedar.bridge.resource.CedarProperties;

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
  @JsonProperty("creator")
  private String creator;
  @JsonProperty("title")
  private String title;
  @JsonProperty("publisher")
  private String publisher;
  @JsonProperty("publicationYear")
  private String publicationYear;
//  @JsonProperty("resourceType")
//  private String resourceType;
  @JsonProperty("subject")
  private String subject;
  @JsonProperty("contributor")
  private String contributor;
  @JsonProperty("dateElement")
  private String dateElement;
  @JsonProperty("language")
  private String language;
  @JsonProperty("alternateIdentifierElement")
  private String alternateIdentifierElement;
  @JsonProperty("relatedIdentifierElement")
  private String relatedIdentifierElement;
  @JsonProperty("size")
  private String size;
  @JsonProperty("format")
  private String format;
  @JsonProperty("version")
  private String version;
  @JsonProperty("rightsElement")
  private String rightsElement;
  @JsonProperty("descriptionElement")
  private String descriptionElement;
  @JsonProperty("geoLocation")
  private String geoLocation;
  @JsonProperty("fundingReference")
  private String fundingReference;
  @JsonProperty("relatedItem")
  private String relatedItem;


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

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getPublicationYear() {
    return publicationYear;
  }

  public void setPublicationYear(String publicationYear) {
    this.publicationYear = publicationYear;
  }

//  public String getResourceType() {
//    return resourceType;
//  }
//
//  public void setResourceType(String resourceType) {
//    this.resourceType = resourceType;
//  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getContributor() {
    return contributor;
  }

  public void setContributor(String contributor) {
    this.contributor = contributor;
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

  public String getAlternateIdentifierElement() {
    return alternateIdentifierElement;
  }

  public void setAlternateIdentifierElement(String alternateIdentifierElement) {
    this.alternateIdentifierElement = alternateIdentifierElement;
  }

  public String getRelatedIdentifierElement() {
    return relatedIdentifierElement;
  }

  public void setRelatedIdentifierElement(String relatedIdentifierElement) {
    this.relatedIdentifierElement = relatedIdentifierElement;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
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

  public String getGeoLocation() {
    return geoLocation;
  }

  public void setGeoLocation(String geoLocation) {
    this.geoLocation = geoLocation;
  }

  public String getFundingReference() {
    return fundingReference;
  }

  public void setFundingReference(String fundingReference) {
    this.fundingReference = fundingReference;
  }

  public String getRelatedItem() {
    return relatedItem;
  }

  public void setRelatedItem(String relatedItem) {
    this.relatedItem = relatedItem;
  }
}
