package org.metadatacenter.cedar.bridge.resource;

import org.metadatacenter.cedar.bridge.resource.datacite.*;
import org.metadatacenter.id.CedarFQResourceId;
import org.metadatacenter.model.CedarResourceType;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.metadatacenter.cedar.bridge.resource.Cedar.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.*;
import static org.metadatacenter.cedar.bridge.resource.Cedar.MetadataInstance.RelatedItemElement.*;

public class CedarInstanceParser {
  private static final String PREFIX = "10.82658";
  private static final String PUBLISH = "publish";
  private static final String RESOURCE_TYPE_GENERAL = "Other";
  private static final String DRAFT = "draft";
  private static final String DATACITE_SCHEMA = "http://datacite.org/schema/kernel-4";
  public static void parseCedarInstance(MetadataInstance MetadataInstance, DataCiteSchema dataCiteSchema, String sourceArtifactId, String state) throws DataCiteInstanceValidationException{
    Data data = new Data();
    Attributes attributes = new Attributes();
    HashSet<String> missedProperties = new HashSet<>();

    // Set type to "dois"
    data.setType("dois");

    //Pass prefix value from CEDAR class to DataCite class
    if (MetadataInstance.prefix().value() == null){
      throw new DataCiteInstanceValidationException("The 'Prefix of DOI' is required, please provide your valid prefix");
    } else if (!MetadataInstance.prefix().value().equals(PREFIX)){
      throw new DataCiteInstanceValidationException("The 'Prefix of DOI' is incorrect, please provide your valid prefix");
    } else{
      attributes.setPrefix(MetadataInstance.prefix().value());
    }

    // set event value
    switch (state){
      case PUBLISH:
        attributes.setEvent(PUBLISH);
        break;
      case DRAFT:
        attributes.setEvent(DRAFT);
        break;
    }

    // Set url and schemeVersion
    attributes.setUrl(GenerateOpenViewUrl.getOpenViewUrl(sourceArtifactId));

    attributes.setSchemaVersion(CedarInstanceParser.DATACITE_SCHEMA);

    // Pass creator values from CEDAR class to DataCite class
    List<MetadataInstance.CreatorElement> creatorList = MetadataInstance.creator().creatorList();
    if (!creatorList.isEmpty() && !CheckEmptyList.emptyCreatorList(creatorList)) {
      attributes.setCreators(parseCreatorValue(creatorList, missedProperties));
    } else{
      missedProperties.add("Creator Name under Creators");
      attributes.setCreators(new ArrayList<>());
    }

    //Pass titles values from CEDAR class to DataCite class
    List<MetadataInstance.TitleElement> titleList = MetadataInstance.title().titleList();
    if (!titleList.isEmpty() && !CheckEmptyList.emptyTitleList(titleList)) {
      attributes.setTitles(parseTitleValue(titleList,  missedProperties));
    } else{
      missedProperties.add("Title under Titles");
      attributes.setTitles(new ArrayList<>());
    }

    //Pass publisher values from CEDAR class to DataCite class
    String publisher = MetadataInstance.publisher().value();
    if (publisher != null && !publisher.isEmpty()) {
      attributes.setPublisher(publisher);
    } else{
      missedProperties.add("Publisher");
    }

    //Pass publisherYear values
    String publicationYear = MetadataInstance.publicationYear().value();
    String currentYear = String.valueOf(Year.now().getValue());
    if (publicationYear == null){
      missedProperties.add("Publication Year");
    } else{
      String givenYear = publicationYear.substring(0,4);
      if (!givenYear.equals(currentYear)){
        throw new DataCiteInstanceValidationException("The 'Publication Year' should be the current year: " + currentYear);
      } else{
        attributes.setPublicationYear(parsePublicationYearValue(givenYear));
      }
    }

    //Pass subjects values
    if(MetadataInstance.subject() != null && !MetadataInstance.subject().isEmpty()){
      List<SubjectElement> subjectList = MetadataInstance.subject().subjectList();
      if (!(CheckEmptyList.emptySubjectList(subjectList))) {
        attributes.setSubjects(parseSubjectValue(subjectList));
      }
    }

    // Pass resourceType values
    String resourceType = MetadataInstance.resourceType().value();
    if(resourceType == null){
      missedProperties.add("Resource Type");
    }
    CedarResourceType cedarResourceType = CedarFQResourceId.build(sourceArtifactId).getType();
    attributes.setTypes(parseTypeValue(cedarResourceType.getValue()));

    //Pass contributors values
    if(MetadataInstance.contributor() != null && !MetadataInstance.contributor().isEmpty()){
      List<ContributorElement> contributorList = MetadataInstance.contributor().contributorList();
      if (!CheckEmptyList.emptyContributorList(contributorList)){
        attributes.setContributors(parseContributorValue(contributorList, missedProperties));
      }
    }

    //Pass dates values
    if(MetadataInstance.date() != null && !MetadataInstance.date().isEmpty()){
      List<DateElement> dateList = MetadataInstance.date().dateList();
      if (!CheckEmptyList.emptyDateList(dateList)){
        attributes.setDates(parseDateValue(dateList, missedProperties));
      }
    }

    //Pass Language value
    String lang = MetadataInstance.language() != null ? MetadataInstance.language().value() : null;
    if(lang != null) {
      attributes.setLanguage(lang);
    } else {
      attributes.setLanguage(null);
    }

    //Pass alternateIdentifier values
    if(MetadataInstance.alternateIdentifier() != null && !MetadataInstance.alternateIdentifier().isEmpty()){
      List<AlternateIdentifierElement> alternateIdentifierList = MetadataInstance.alternateIdentifier().alternateIdentifierList();
      if (!CheckEmptyList.emptyAlternateIdentifierList(alternateIdentifierList)){
        attributes.setAlternateIdentifiers(parseAlternateIdentifier(alternateIdentifierList, missedProperties));
      }
    }

    //Pass relatedIdentifier values
    if(MetadataInstance.relatedIdentifier() != null && !MetadataInstance.relatedIdentifier().isEmpty()){
      List<RelatedIdentifierElement> relatedIdentifierList = MetadataInstance.relatedIdentifier().relatedIdentifierList();
      if (!CheckEmptyList.emptyRelatedIdentifierList(relatedIdentifierList)){
        attributes.setRelatedIdentifiers(parseRelatedIdentifier(relatedIdentifierList, missedProperties));
      }
    }


    //Pass size values
    List<SizeField> sizeList = MetadataInstance.size() != null ? MetadataInstance.size().sizeList() : null;
    if (sizeList != null && sizeList.size()>0 && !CheckEmptyList.emptyValueList(sizeList)){
      attributes.setSizes(parseSizeValue(sizeList));
    }

    //Pass format values
    List<FormatField> formatList = MetadataInstance.format() != null ? MetadataInstance.format().formatList() : null;
    if (formatList != null && formatList.size() > 0 && !CheckEmptyList.emptyValueList(formatList)){
      attributes.setFormats(parseFormatValue(formatList));
    }

    //Pass version value
    if (MetadataInstance.version() != null){
      attributes.setVersion(MetadataInstance.version().value());
    } else{
      attributes.setVersion(null);
    }

    //Pass rights values
    if(MetadataInstance.rights() != null && !MetadataInstance.rights().isEmpty()){
      List<RightsElement> rightsList = MetadataInstance.rights().rightsList();
      if (!CheckEmptyList.emptyRightsList(rightsList)){
        attributes.setRightsList(parseRightsValue(rightsList));
      }
    }

    //Pass description values
    if(MetadataInstance.description() != null && !MetadataInstance.description().isEmpty()){
      List<DescriptionElement> descriptionList = MetadataInstance.description().descriptionList();
      if (!CheckEmptyList.emptyDescriptionList(descriptionList)){
        attributes.setDescriptions(parseDescriptionValue(descriptionList, missedProperties));
      }
    }


    //Pass geoLocation values
    if(MetadataInstance.geoLocation() != null && !MetadataInstance.geoLocation().isEmpty()){
      List<GeoLocationElement> geoLocationList = MetadataInstance.geoLocation().geoLocationList();
      if (!CheckEmptyList.emptyGeoLocationList(geoLocationList)){
        attributes.setGeoLocations(parseGeoLocationValue(geoLocationList, missedProperties));
      }
    }

    //Pass fundingReference values
    if(MetadataInstance.fundingReference() != null && !MetadataInstance.fundingReference().isEmpty()){
      List<FundingReferenceElement> fundingReferenceList = MetadataInstance.fundingReference().fundingReferenceList();
      if (!CheckEmptyList.emptyFundingReferenceList(fundingReferenceList)){
        attributes.setFundingReferences(parseFundingReference(fundingReferenceList, missedProperties));
      }
    }

    //Pass relatedItem values
    if(MetadataInstance.relatedItem() != null && !MetadataInstance.relatedItem().isEmpty()){
      List<RelatedItemElement> relatedItemList = MetadataInstance.relatedItem().relatedItemList();
      if (!CheckEmptyList.emptyRelatedItemList(relatedItemList)){
        attributes.setRelatedItems(parseRelatedItemValue(relatedItemList, missedProperties));
      }
    }

    data.setAttributes(attributes);
    dataCiteSchema.setData(data);

//    if (state.equals(PUBLISH) && !missedProperties.isEmpty()){
    if (!missedProperties.isEmpty()){
      StringBuilder errorMessage = new StringBuilder("The following fields are required:\n");
      for (String property : missedProperties) {
        errorMessage.append(property).append("\n");
      }
      throw new DataCiteInstanceValidationException(errorMessage.toString());
    }
  }

  private static List<DataCiteAffiliation> parseAffiliationValue(List<?> affiliationList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteAffiliation> dataCiteAffiliationList = new ArrayList<>();
    if (!affiliationList.isEmpty()){
      for (Object obj : affiliationList) {
        DataCiteAffiliation dataCiteAffiliation = new DataCiteAffiliation();
        String name = null;
        String affiliationIdentifier = null;
        String affiliationIdentifierScheme = null;
        String affiliationSchemeURI = null;
        if(obj instanceof CreatorElement.AffiliationElement){
          CreatorElement.AffiliationElement a = (CreatorElement.AffiliationElement) obj;
          name = a.name() != null ? a.name().value() : null;
          affiliationIdentifier = a.affiliationIdentifier() != null ? a.affiliationIdentifier().value():null;
          affiliationIdentifierScheme = a.affiliationIdentifierScheme() != null ? a.affiliationIdentifierScheme().value() : null;
          if (affiliationIdentifierScheme == null || affiliationIdentifierScheme.isEmpty()){
            missedProperties.add("Affiliation Identifier Scheme under Creators");
          }
          affiliationSchemeURI = a.schemeUri() != null ? a.schemeUri().id() : null;
        } else if(obj instanceof ContributorElement.AffiliationElement){
          ContributorElement.AffiliationElement a = (ContributorElement.AffiliationElement) obj;
          name = a.name() != null ? a.name().value() : null;
          affiliationIdentifier = a.affiliationIdentifier() != null ? a.affiliationIdentifier().value():null;
          affiliationIdentifierScheme = a.affiliationIdentifierScheme() != null ? a.affiliationIdentifierScheme().value() : null;
          if (affiliationIdentifierScheme == null || affiliationIdentifierScheme.isEmpty()){
            missedProperties.add("Affiliation Identifier Scheme under Contributors");
          }
          affiliationSchemeURI = a.schemeUri() != null ? a.schemeUri().id() : null;
        }

        // set values to DataCite class
        dataCiteAffiliation.setName(name);
        dataCiteAffiliation.setAffiliationIdentifier(affiliationIdentifier);
        dataCiteAffiliation.setAffiliationIdentifierScheme(affiliationIdentifierScheme);
        dataCiteAffiliation.setAffiliationSchemeURI(affiliationSchemeURI);
        dataCiteAffiliationList.add(dataCiteAffiliation);
      }
    }
    return dataCiteAffiliationList;
  }


  private static List<DataCiteNameIdentifier> parseNameIdentifierValue(List<?> nameIdentifierList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException{
    List<DataCiteNameIdentifier> dataCiteNameIdentifierList = new ArrayList<>();
    if (!nameIdentifierList.isEmpty()) {
      for (Object obj : nameIdentifierList) {
        DataCiteNameIdentifier dataCiteNameIdentifier = new DataCiteNameIdentifier();
        String nameIdentifierName = null;
        String nameIdentifierScheme = null;
        String nameIdentifierSchemeUri = null;

        if(obj instanceof CreatorElement.NameIdentifierElement){
          CreatorElement.NameIdentifierElement n = (CreatorElement.NameIdentifierElement) obj;
          nameIdentifierName = n.name() != null ? n.name().value() : null;
          nameIdentifierScheme = n.nameIdentifierScheme() != null ? n.nameIdentifierScheme().value() : null;
          if (nameIdentifierScheme == null || nameIdentifierScheme.isEmpty()){
            missedProperties.add("Name Identifier Scheme under Creators");
          }
          nameIdentifierSchemeUri = n.schemeUri() != null ? n.schemeUri().id() : null;
        } else if(obj instanceof ContributorElement.NameIdentifierElement){
          ContributorElement.NameIdentifierElement n = (ContributorElement.NameIdentifierElement) obj;
          nameIdentifierName = n.name() != null ? n.name().value() : null;
          nameIdentifierScheme = n.nameIdentifierScheme() != null ? n.nameIdentifierScheme().value() : null;
          if (nameIdentifierScheme == null || nameIdentifierScheme.isEmpty()){
            missedProperties.add("Name Identifier Scheme under Contributors");
          }
          nameIdentifierSchemeUri = n.schemeUri() != null ? n.schemeUri().id() : null;
        }

        // set values to DataCite class
        dataCiteNameIdentifier.setNameIdentifier(nameIdentifierName);
        dataCiteNameIdentifier.setNameIdentifierScheme(nameIdentifierScheme);
        dataCiteNameIdentifier.setSchemeUri(nameIdentifierSchemeUri);
        dataCiteNameIdentifierList.add(dataCiteNameIdentifier);
      }
    }
    return dataCiteNameIdentifierList;
  }


  // Parse CreatorElement values
  private static List<DataCiteCreator> parseCreatorValue(List<CreatorElement> creatorList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteCreator> dataCiteCreatorList = new ArrayList<>();

    //Loop each creator in creator list to get values
    for (CreatorElement c: creatorList) {
      DataCiteCreator dataCiteCreator = new DataCiteCreator();
      // Retrieve values from CEDAR class
      String creatorName = c.creatorName().value();
      if (creatorName == null || creatorName.isEmpty()){
        missedProperties.add("Creator Name under Creators");
      }

      String nameType = c.nameType() != null ? c.nameType().label() : null;
      String givenName = c.givenName() != null ? c.givenName().value() : null;
      String familyName = c.familyName() != null ? c.familyName().value() : null;
      // set values to DataCite class
      dataCiteCreator.setName(creatorName);
      dataCiteCreator.setNameType(nameType);
      dataCiteCreator.setFamilyName(familyName);
      dataCiteCreator.setGivenName(givenName);

      // Set values to corresponding Affiliation list in dataCiteCreator
      if(c.affiliation() != null && !c.affiliation().isEmpty()){
        List<CreatorElement.AffiliationElement> affiliationList = c.affiliation().affiliationList();
        if (affiliationList != null && !affiliationList.isEmpty() && !CheckEmptyList.emptyAffiliationList(affiliationList)) {
          dataCiteCreator.setAffiliations(parseAffiliationValue(affiliationList, missedProperties));
        }
      }

      // Set values to corresponding Affiliation list in dataCiteCreator
      if(c.nameIdentifier() != null && !c.nameIdentifier().isEmpty()){
        List<CreatorElement.NameIdentifierElement> nameIdentifierList = c.nameIdentifier().nameIdentifierList();
        if (nameIdentifierList != null && !nameIdentifierList.isEmpty() && !CheckEmptyList.emptyNameIdentifierList(nameIdentifierList)) {
          dataCiteCreator.setNameIdentifiers(parseNameIdentifierValue(nameIdentifierList, missedProperties));
        }
      }

      // Add dataCiteCreator to dataCiteCreator list
      dataCiteCreatorList.add(dataCiteCreator);
    }
    return dataCiteCreatorList;
  }

  // Parse TitleElement values
  private static List<DataCiteTitle> parseTitleValue(List<?> titlesList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteTitle> dataCiteTitles = new ArrayList<>();

    for (Object obj : titlesList) {
      DataCiteTitle dataCiteTitle = new DataCiteTitle();
      String titleName = null;
      String titleType= null;
      if (obj instanceof MetadataInstance.TitleElement) {
        MetadataInstance.TitleElement t = (MetadataInstance.TitleElement) obj;
        titleName = t.title().value();
        if (titleName == null || titleName.isEmpty()) {
          missedProperties.add("Title under Titles");
        }
        titleType = t.titleType() != null ? t.titleType().label() : null;

      } else if (obj instanceof RelatedItemElement.TitleElement) {
        RelatedItemElement.TitleElement t = (RelatedItemElement.TitleElement) obj;
        titleName = t.title().value();
        if (titleName == null || titleName.isEmpty()) {
          missedProperties.add("Title under Related Items");
        }
        titleType = t.titleType() != null ? t.titleType().label() : null;
      }
      dataCiteTitle.setTitle(titleName);
      dataCiteTitle.setTitleType(titleType);

      dataCiteTitles.add(dataCiteTitle);
    }


    return dataCiteTitles;
  }

  private static Integer parsePublicationYearValue(String publicationYear) {
    return Integer.parseInt(publicationYear.substring(0,4));
  }

  private static List<DataCiteSubject> parseSubjectValue(List<SubjectElement> subjectList) {
    List<DataCiteSubject> dataCiteSubjects = new ArrayList<>();

    for (SubjectElement s : subjectList){
      DataCiteSubject dataCiteSubject = new DataCiteSubject();
      // Retrieve values from CEDAR class
      String subjectName = s.subject() !=null ? s.subject().value() : null;
      String schemeUri = s.schemeUri() != null ? s.schemeUri().id() : null;
      String subjectScheme = s.subjectScheme() != null ? s.subjectScheme().value() : null;
      String classificationCode = s.classificationCode() != null ? s.classificationCode().value() : null;
      String valueUri = s.valueUri() != null ? s.valueUri().id() : null;
      // Set values to DataCite class
      dataCiteSubject.setSubject(subjectName);
      dataCiteSubject.setSubjectScheme(subjectScheme);
      dataCiteSubject.setSchemeUri(schemeUri);
      dataCiteSubject.setClassificationCode(classificationCode);
      dataCiteSubject.setValueUri(valueUri);

      dataCiteSubjects.add(dataCiteSubject);
    }

    return dataCiteSubjects;
  }

  private static DataCiteType parseTypeValue(String resourceType) {
    DataCiteType dataCiteType = new DataCiteType();
//        String resourceTypeGeneral = resourceTypeElement.getResourceTypeGeneral().toString();
    dataCiteType.setResourceTypeGeneral(RESOURCE_TYPE_GENERAL);
    dataCiteType.setResourceType(resourceType);
    return dataCiteType;
  }

  private static List<DataCiteContributor> parseContributorValue(List<ContributorElement> contributorList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteContributor> dataCiteContributors = new ArrayList<>();

    for (ContributorElement c : contributorList) {
      DataCiteContributor dataCiteContributor = new DataCiteContributor();
      // Retrieve values from CEDAR class
      String name = c.contributorName()!=null ? c.contributorName().value() : null;
      if (name == null || name.isEmpty()){
        missedProperties.add("Contributor Name under Contributors");
      }
      String nameType = c.nameType() != null ? c.nameType().label() : null;
      String givenName = c.givenName() != null ? c.givenName().value() : null;
      String familyName = c.familyName() != null ? c.familyName().value() : null;
      String contributorType = c.contributorType() != null ? c.contributorType().label() : null;
      if (contributorType == null || contributorType.isEmpty()){
        missedProperties.add("Contributor Type under Contributors");
      } else{
        dataCiteContributor.setContributorType(contributorType);
      }

      dataCiteContributor.setName(name);
      dataCiteContributor.setNameType(nameType);
      dataCiteContributor.setGivenName(givenName);
      dataCiteContributor.setFamilyName(familyName);

      // Set values to corresponding Affiliation list in dataCiteCreator
      if(c.affiliation()!=null && !c.affiliation().isEmpty()){
        List<ContributorElement.AffiliationElement> affiliationList = c.affiliation().affiliationList();
        if (affiliationList != null && !CheckEmptyList.emptyAffiliationList(affiliationList)) {
          dataCiteContributor.setAffiliations(parseAffiliationValue(affiliationList, missedProperties));
        }
      }

      // Set values to corresponding nameIdentifierList list in dataCiteCreator
      if(c.nameIdentifier()!=null && !c.nameIdentifier().isEmpty()){
        List<ContributorElement.NameIdentifierElement> nameIdentifierList = c.nameIdentifier().nameIdentifierList();
        if (nameIdentifierList != null && !CheckEmptyList.emptyNameIdentifierList(nameIdentifierList)) {
          dataCiteContributor.setNameIdentifiers(parseNameIdentifierValue(nameIdentifierList, missedProperties));
        }
      }

      // Add dataCiteContributor to the list
      dataCiteContributors.add(dataCiteContributor);
    }

    return dataCiteContributors;
  }

  private static List<DataCiteDate> parseDateValue(List<DateElement> dateList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteDate> dataCiteDates = new ArrayList<>();

    for (DateElement d : dateList) {
      DataCiteDate dataCiteDate = new DataCiteDate();
      String date = d.date() != null ? d.date().value() : null;
      String dateType = d.dateType() != null ? d.dateType().label() : null;
      if (dateType == null || dateType.isEmpty()){
        missedProperties.add("Date Type under Date");
      }
      String dateInformation = d.dateInformation()!=null ? d.dateInformation().value():null;
      dataCiteDate.setDate(date);
      dataCiteDate.setDateType(dateType);
      dataCiteDate.setDateInformation(dateInformation);

      dataCiteDates.add(dataCiteDate);
    }

    return dataCiteDates;
  }

  private static List<DataCiteAlternateIdentifier> parseAlternateIdentifier(List<AlternateIdentifierElement> alternateIdentifierList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteAlternateIdentifier> dataCiteAlternateIdentifiers = new ArrayList<>();

    for (AlternateIdentifierElement a : alternateIdentifierList) {
      DataCiteAlternateIdentifier dataCiteAlternateIdentifier = new DataCiteAlternateIdentifier();
      String alternateIdentifier = a.alternateIdentifier() != null ? a.alternateIdentifier().value() : null;
      String alternateIdentifierType = a.alternateIdentifierType() != null ? a.alternateIdentifierType().value() : null;
      if (alternateIdentifierType == null || alternateIdentifierType.isEmpty()){
        missedProperties.add("Alternate Identifier Type under Alternate Identifiers");
      }

      dataCiteAlternateIdentifier.setAlternateIdentifier(alternateIdentifier);
      dataCiteAlternateIdentifier.setAlternateIdentifierType(alternateIdentifierType);

      dataCiteAlternateIdentifiers.add(dataCiteAlternateIdentifier);
    }

    return dataCiteAlternateIdentifiers;
  }

  private static List<DataCiteRelatedIdentifier> parseRelatedIdentifier(List<RelatedIdentifierElement> relatedIdentifierList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteRelatedIdentifier> dataCiteRelatedIdentifiers = new ArrayList<>();

    for (RelatedIdentifierElement r : relatedIdentifierList) {
      DataCiteRelatedIdentifier dataCiteRelatedIdentifier = new DataCiteRelatedIdentifier();
      String relatedIdentifier = r.relatedIdentifier() != null ? r.relatedIdentifier().value() : null;
      String relatedIdentifierType = r.relatedIdentifierType() != null ? r.relatedIdentifierType().label() : null;
      if (relatedIdentifierType == null || relatedIdentifierType.isEmpty()){
        missedProperties.add("Related Identifier Type under Related Identifiers");
      }
      String relationType = r.relationType() != null ? r.relationType().label() : null;
      if (relationType == null || relationType.isEmpty()){
        missedProperties.add("Relation Type under Related Identifiers");
      }
      String relatedMetaDataScheme = r.relatedMetadataScheme() != null ? r.relatedMetadataScheme().value() : null;
      String schemeURI = r.schemeUri() != null ? r.schemeUri().label() : null;
      String schemeType = r.schemeType() != null ? r.schemeType().value() : null;
      String resourceTypeGeneral = r.resourceTypeGeneral() != null ? r.resourceTypeGeneral().label() : null;

      dataCiteRelatedIdentifier.setRelatedIdentifier(relatedIdentifier);
      dataCiteRelatedIdentifier.setRelatedIdentifierType(relatedIdentifierType);
      dataCiteRelatedIdentifier.setRelationType(relationType);
      dataCiteRelatedIdentifier.setRelatedMetadataScheme(relatedMetaDataScheme);
      dataCiteRelatedIdentifier.setSchemeUri(schemeURI);
      dataCiteRelatedIdentifier.setSchemeType(schemeType);
      dataCiteRelatedIdentifier.setResourceTypeGeneral(resourceTypeGeneral);

      dataCiteRelatedIdentifiers.add(dataCiteRelatedIdentifier);
    }

    return dataCiteRelatedIdentifiers;
  }


  private static List<String> parseSizeValue(List<SizeField> sizeList) {
    List<String> dataCiteSizes = new ArrayList<>();
    for (SizeField s : sizeList) {
      String dataCiteSize = s.value();
      dataCiteSizes.add(dataCiteSize);
    }
    return dataCiteSizes;
  }

  private static List<String> parseFormatValue(List<FormatField> formatList){
    List<String> dataCiteFormats = new ArrayList<>();
    for (FormatField f : formatList) {
      String dataCiteFormat = f.value();
      dataCiteFormats.add(dataCiteFormat);
    }
    return dataCiteFormats;
  }

  private static List<DataCiteRights> parseRightsValue(List<RightsElement> rightsList){
    List<DataCiteRights> dataCiteRightsList = new ArrayList<>();

    for(RightsElement r : rightsList){
      DataCiteRights dataCiteRights = new DataCiteRights();
      String rights = r.rights() != null ? r.rights().value() : null;
      String rightsURI = r.rightsUri() != null ? r.rightsUri().id() : null;
      String schemeURI = r.schemeUri() != null ? r.schemeUri().id() : null;
      String rightsIdentifier = r.rightsIdentifier() != null ? r.rightsIdentifier().value() : null;
      String rightsIdentifierScheme = r.rightsIdentifierScheme() != null ? r.rightsIdentifierScheme().value() : null;

      dataCiteRights.setRights(rights);
      dataCiteRights.setRightsUri(rightsURI);
      dataCiteRights.setSchemeUri(schemeURI);
      dataCiteRights.setRightsIdentifier(rightsIdentifier);
      dataCiteRights.setRightsIdentifierScheme(rightsIdentifierScheme);

      dataCiteRightsList.add(dataCiteRights);
    }

    return dataCiteRightsList;
  }

  private static List<DataCiteDescription> parseDescriptionValue(List<DescriptionElement> descriptionList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteDescription> dataCiteDescriptions = new ArrayList<>();

    for(DescriptionElement d : descriptionList) {
      DataCiteDescription dataCiteDescription = new DataCiteDescription();
      String description = d.description() != null ? d.description().value() : null;
      String descriptionType = d.descriptionType() != null ? d.descriptionType().label() : null;
      if (descriptionType == null || descriptionType.isEmpty()){
        missedProperties.add("Description Type under Descriptions");
      }
      dataCiteDescription.setDescription(description);
      dataCiteDescription.setDescriptionType(descriptionType);

      dataCiteDescriptions.add(dataCiteDescription);
    }

    return dataCiteDescriptions;
  }

  private static List<DataCiteGeoLocation> parseGeoLocationValue(List<GeoLocationElement> geoLocationList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteGeoLocation> dataCiteGeoLocations = new ArrayList<>();

    for(GeoLocationElement g : geoLocationList){
      DataCiteGeoLocation dataCiteGeoLocation = new DataCiteGeoLocation();
      ArrayList<String> outOfBoundLongitude = new ArrayList<>();
      ArrayList<String> outOfBoundLatitude = new ArrayList<>();
      // parse geoLocationPlace
      String geoLocationPlace = g.geoLocationPlace() != null ? g.geoLocationPlace().value() : null;
      dataCiteGeoLocation.setGeoLocationPlace(geoLocationPlace);
      // parse geoLocationPoint
      DataCiteGeoLocationPoint point = new DataCiteGeoLocationPoint();
      String pointLongitude = null,
          pointLatitude = null;
      if(g.geoLocationPoint()!= null){
        pointLongitude = g.geoLocationPoint().pointLongitude() != null ? g.geoLocationPoint().pointLongitude().value() : null;
        pointLatitude = g.geoLocationPoint().pointLatitude() != null ? g.geoLocationPoint().pointLatitude().value() : null;
      }
      if (pointLongitude != null || pointLatitude != null){
        if(CheckValueRange.longitudeOutOfBound(pointLongitude)){
          outOfBoundLongitude.add("Point Longitude");
        } else if (pointLongitude == null) {
          missedProperties.add("Point Longitude under Geographic Locations");
        } else{
          point.setPointLongitude(Float.valueOf(pointLongitude));
        }
        if(CheckValueRange.latitudeOutOfBound(pointLatitude)){
          outOfBoundLatitude.add("Point Latitude");
        } else if (pointLatitude == null) {
          missedProperties.add("Point Latitude under Geographic Locations");
        } else{
          point.setPointLatitude(Float.valueOf(pointLatitude));
        }
        dataCiteGeoLocation.setGeoLocationPoint(point);
      }

      // parse value in geoLocationBox
      DataCiteGeoLocationBox dataCiteGeoLocationBox = new DataCiteGeoLocationBox();
      String eastBoundLongitude = null,
          westBoundLongitude = null,
          southBoundLatitude = null,
          northBoundLatitude = null;
      if(g.geoLocationBox() != null){
        eastBoundLongitude = g.geoLocationBox().eastBoundLongitude() != null ? g.geoLocationBox().eastBoundLongitude().value() : null;
        westBoundLongitude = g.geoLocationBox().westBoundLongitude() != null ? g.geoLocationBox().westBoundLongitude().value() : null;
        southBoundLatitude = g.geoLocationBox().southBoundLatitude() != null ? g.geoLocationBox().southBoundLatitude().value() : null;
        northBoundLatitude = g.geoLocationBox().northBoundLatitude() != null ? g.geoLocationBox().northBoundLatitude().value() : null;
      }
      if (eastBoundLongitude != null || westBoundLongitude != null || southBoundLatitude != null || northBoundLatitude != null){
        if(CheckValueRange.longitudeOutOfBound(eastBoundLongitude)){
          outOfBoundLongitude.add("East Bound Longitude");
        } else if (eastBoundLongitude == null) {
          missedProperties.add("East Bound Longitude under Geographic Locations");
        } else{
          dataCiteGeoLocationBox.setEastBoundLongitude(Float.valueOf(eastBoundLongitude));
        }
        if(CheckValueRange.longitudeOutOfBound(westBoundLongitude)){
          outOfBoundLongitude.add("West Bound Longitude");
        } else if (westBoundLongitude == null) {
          missedProperties.add("West Bound Longitude under Geographic Locations");
        } else{
          dataCiteGeoLocationBox.setWestBoundLongitude(Float.valueOf(westBoundLongitude));
        }
        if(CheckValueRange.latitudeOutOfBound(southBoundLatitude)){
          outOfBoundLatitude.add("South Bound Latitude");
        } else if (southBoundLatitude == null) {
          missedProperties.add("South Bound Latitude under Geographic Locations");
        } else{
          dataCiteGeoLocationBox.setSouthBoundLatitude(Float.valueOf(southBoundLatitude));
        }
        if(CheckValueRange.latitudeOutOfBound(northBoundLatitude)){
          outOfBoundLatitude.add("North Bound Latitude");
        } else if (northBoundLatitude == null) {
          missedProperties.add("North Bound Latitude under Geographic Locations");
        } else{
          dataCiteGeoLocationBox.setNorthBoundLatitude(Float.valueOf(northBoundLatitude));
        }
        dataCiteGeoLocation.setGeoLocationBox(dataCiteGeoLocationBox);
      }

      if(!outOfBoundLatitude.isEmpty() || !outOfBoundLongitude.isEmpty()){
        String errorMessage = "";
        if(!outOfBoundLongitude.isEmpty()){
          errorMessage = String.join(", ", outOfBoundLongitude) + " should be in the range of [-180, 180].\n";
        }
        if(!outOfBoundLatitude.isEmpty()){
          errorMessage += " " + String.join(", ", outOfBoundLatitude) + " should be in the range of [-90, 90].";
        }
        throw new DataCiteInstanceValidationException(errorMessage);
      }

//            //parse value in geoLocationPolygons
//            List<GeoLocationPolygon> geoLocationPolygonList = g.getGeoLocationPolygonList();
//            List<DataCiteGeoLocationPolygon> dataCiteGeoLocationPolygons = new ArrayList<>();
//            if (geoLocationPolygonList != null || !geoLocationPolygonList.isEmpty()){
//                for (GeoLocationPolygon glp: geoLocationPolygonList){
//                    DataCiteGeoLocationPolygon dataCiteGeoLocationPolygon = new DataCiteGeoLocationPolygon();
//                    List<DataCiteGeoLocationPoint> dataCiteGeoLocationPointList = new ArrayList<>();
//
//                    // parse value in polygonPointsList
//                    List<Point> polygonPointlist = glp.getPolygonPointsList();
//                    if (polygonPointlist != null || !polygonPointlist.isEmpty()){
//                        for (Point p : polygonPointlist){
//                            DataCiteGeoLocationPoint dataCiteGeoLocationPoint = new DataCiteGeoLocationPoint();
//                            Float polygonPointLongitude = p.getPointLongitude().getValue();
//                            Float polygonPointLatitude = p.getPointLatitude().getValue();
//                            if (polygonPointLongitude != null) {
//                                dataCiteGeoLocationPoint.setPointLongitude(polygonPointLongitude);
//                            }
//                            if (polygonPointLatitude != null) {
//                                dataCiteGeoLocationPoint.setPointLatitude(polygonPointLatitude);
//                            }
//                            dataCiteGeoLocationPointList.add(dataCiteGeoLocationPoint);
//                        }
//                        // set polygonPoint attributes
//                        dataCiteGeoLocationPolygon.setPolygonPointsList(dataCiteGeoLocationPointList);
//                    }
//
//                    //parse value of inPolygonPoint
//                    if (glp.getInPolygonPoint() != null){
//                        Float inPolygonPointLongitude = glp.getInPolygonPoint().getPointLongitude().getValue();
//                        Float inPolygonPointLatitude = glp.getInPolygonPoint().getPointLatitude().getValue();
//
//                        // set inPolygonPoint attributes
//                        DataCiteGeoLocationPoint inPolygonPoint = new DataCiteGeoLocationPoint();
//                        if (inPolygonPointLongitude != null) {
//                            inPolygonPoint.setPointLongitude(inPolygonPointLongitude);
//                        }
//                        if (inPolygonPointLatitude != null) {
//                            inPolygonPoint.setPointLatitude(inPolygonPointLatitude);
//                        }
//                        dataCiteGeoLocationPolygon.setInPolygonPoint(inPolygonPoint);
//                    }
//
//                    dataCiteGeoLocationPolygons.add(dataCiteGeoLocationPolygon);
//            }
//                dataCiteGeoLocation.setGeoLocationPolygonList(dataCiteGeoLocationPolygons);
//            }

      //add dataCiteGeoLocation to the list
      dataCiteGeoLocations.add(dataCiteGeoLocation);
    }

    return dataCiteGeoLocations;
  }

  private static List<DataCiteFundingReference> parseFundingReference(List<FundingReferenceElement> fundingReferenceList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteFundingReference> dataCiteFundingReferences = new ArrayList<>();

    for(FundingReferenceElement f : fundingReferenceList) {
      DataCiteFundingReference dataCiteFundingReference = new DataCiteFundingReference();
      String funderName = f.funderName() != null ? f.funderName().value() : null;
      if (funderName == null || funderName.isEmpty()){
        missedProperties.add("Funder Name under Funding Reference");
      }
      String funderIdentifier = f.funderIdentifier() != null ? f.funderIdentifier().value() : null;
      String funderIdentifierType = f.funderIdentifierType() != null ? f.funderIdentifierType().label() : null;
      String funderIdentifierSchemeURI = f.schemeUri() != null ? f.schemeUri().id() : null;
      String awardNumber = f.awardNumber() != null ? f.awardNumber().value() : null;
      String awardUri = f.awardUri() != null ? f.awardUri().id() : null;
      String awardTitle = f.awardTitle() != null ? f.awardTitle().value() : null;

      dataCiteFundingReference.setFunderName(funderName);
      dataCiteFundingReference.setFunderIdentifier(funderIdentifier);
      dataCiteFundingReference.setFunderIdentifierType(funderIdentifierType);
      dataCiteFundingReference.setSchemeUri(funderIdentifierSchemeURI);
      dataCiteFundingReference.setAwardNumber(awardNumber);
      dataCiteFundingReference.setAwardUri(awardUri);
      dataCiteFundingReference.setAwardTitle(awardTitle);

      dataCiteFundingReferences.add(dataCiteFundingReference);
    }

    return dataCiteFundingReferences;
  }

  private static List<DataCiteRelatedItem> parseRelatedItemValue(List<RelatedItemElement> relatedItemList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException {
    List<DataCiteRelatedItem> dataCiteRelatedItems = new ArrayList<>();

    for(RelatedItemElement r: relatedItemList){
      DataCiteRelatedItem dataCiteRelatedItem = new DataCiteRelatedItem();
      String relatedItemType = r.relatedItemType() != null ? r.relatedItemType().label() : null;
      if (relatedItemType == null || relatedItemType.isEmpty()){
        missedProperties.add("Related Item Type under Related Items");
      }
      String relationType = r.relationType() != null ? r.relationType().label() : null;
      if (relationType == null || relationType.isEmpty()){
        missedProperties.add("Relation Type under Related Items");
      }
      String volume = r.volume() != null ? r.volume().value() : null;
      String issue = r.issue() != null ? r.issue().value() : null;
      String firstPage = r.firstPage() != null ? r.firstPage().value() : null;
      String lastPage = r.lastPage() != null ? r.lastPage().value() : null;
      String publicationYear = r.publicationYear() != null ? r.publicationYear().value() : null;
      String publisher = r.publisher() != null ? r.publisher().value() : null;
      String edition = r.edition() != null ? r.edition().value() : null;
      String number = r.number() != null ? r.number().value() : null;
      String numberType = r.numberType() != null ? r.numberType().label() : null;

      dataCiteRelatedItem.setRelatedItemType(relatedItemType);
      dataCiteRelatedItem.setRelationType(relationType);
      dataCiteRelatedItem.setVolume(volume);
      dataCiteRelatedItem.setIssue(issue);
      dataCiteRelatedItem.setFirstPage(firstPage);
      dataCiteRelatedItem.setLastPage(lastPage);
      if (publicationYear != null) {
        dataCiteRelatedItem.setPublicationYear(parsePublicationYearValue(publicationYear));
      } else{
        dataCiteRelatedItem.setPublicationYear(null);
      }
      dataCiteRelatedItem.setPublisher(publisher);
      dataCiteRelatedItem.setEdition(edition);
      dataCiteRelatedItem.setNumber(number);
      dataCiteRelatedItem.setNumberType(numberType);

      // parse relatedItemIdentifier values
      DataCiteRelatedItemIdentifier dataCiteRelatedItemIdentifier = new DataCiteRelatedItemIdentifier();
      String relatedIdentifier = r.relatedIdentifier() != null ? r.relatedIdentifier().value() : null;
      String relatedIdentifierType = r.relatedIdentifierType() != null ? r.relatedIdentifierType().label() : null;
      String relatedMedaDataScheme = r.relatedMetadataScheme() != null ? r.relatedMetadataScheme().value() : null;
      String schemeUri = r.schemeUri() != null ? r.schemeUri().id() : null;
      String schemeType = r.schemeType() != null ? r.schemeType().value() : null;

      dataCiteRelatedItemIdentifier.setRelatedItemIdentifier(relatedIdentifier);
      dataCiteRelatedItemIdentifier.setRelatedItemIdentifierType(relatedIdentifierType);
      dataCiteRelatedItemIdentifier.setRelatedMetadataScheme(relatedMedaDataScheme);
      dataCiteRelatedItemIdentifier.setSchemeUri(schemeUri);
      dataCiteRelatedItemIdentifier.setSchemeType(schemeType);

      dataCiteRelatedItem.setRelatedItemIdentifier(dataCiteRelatedItemIdentifier);

      //parse creators values
      if (r.relatedItemCreator() != null && r.relatedItemCreator().relatedItemCreatorList() != null && !r.relatedItemCreator().relatedItemCreatorList().isEmpty() && !CheckEmptyList.emptyRelatedItemCreatorList(r.relatedItemCreator().relatedItemCreatorList())){
        dataCiteRelatedItem.setCreators(parseRelatedItemCreators(r.relatedItemCreator().relatedItemCreatorList(), missedProperties));
      }

      // parse titles values
      if (r.title() != null && r.title().titleList() != null && !r.title().titleList().isEmpty() && !CheckEmptyList.emptyTitleList(r.title().titleList())){
        dataCiteRelatedItem.setTitles(parseTitleValue(r.title().titleList(), missedProperties));
      } else {
        missedProperties.add("Title under Related Items");
      }

      //parse contributors values
      if(r.relatedItemContributor() != null && r.relatedItemContributor().relatedItemContributorList() != null){
        List<RelatedItemContributorElement> contributorList = r.relatedItemContributor().relatedItemContributorList();
        if (!contributorList.isEmpty() && !CheckEmptyList.emptyRelatedItemContributorList(contributorList)){
          dataCiteRelatedItem.setContributors(parseRelatedItemContributors(contributorList, missedProperties));
        }
      }

      dataCiteRelatedItems.add(dataCiteRelatedItem);
    }

    return dataCiteRelatedItems;
  }

  private static List<DataCiteCreator> parseRelatedItemCreators(List<RelatedItemCreatorElement> relatedItemCreatorList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException{
    List<DataCiteCreator> dataCiteCreatorList = new ArrayList<>();

    //Loop each creator in creator list to get values
    for (RelatedItemCreatorElement c: relatedItemCreatorList) {
      DataCiteCreator dataCiteCreator = new DataCiteCreator();
      // Retrieve values from CEDAR class
      String creatorName = c.creatorName().value();
      if (creatorName == null || creatorName.isEmpty()){
        missedProperties.add("Creator Name under Related Items");
      }

      String nameType = c.nameType() != null ? c.nameType().label() : null;
      String givenName = c.givenName() != null ? c.givenName().value() : null;
      String familyName = c.familyName() != null ? c.familyName().value() : null;
      // set values to DataCite class
      dataCiteCreator.setName(creatorName);
      dataCiteCreator.setNameType(nameType);
      dataCiteCreator.setFamilyName(familyName);
      dataCiteCreator.setGivenName(givenName);

      // Add dataCiteCreator to dataCiteCreator list
      dataCiteCreatorList.add(dataCiteCreator);
    }
    return dataCiteCreatorList;
  }

  private static List<DataCiteRelatedItemContributor> parseRelatedItemContributors(List<RelatedItemContributorElement> relatedItemContributorList, HashSet<String> missedProperties) throws DataCiteInstanceValidationException{
    List<DataCiteRelatedItemContributor> dataCiteRelatedItemContributors = new ArrayList<>();
    for (RelatedItemContributorElement c : relatedItemContributorList) {
      DataCiteRelatedItemContributor dataCiteRelatedItemContributor = new DataCiteRelatedItemContributor();
      // Retrieve values from CEDAR class
      String name = c.contributorName() != null ? c.contributorName().value() : null;
      if (name == null || name.isEmpty()) {
        missedProperties.add("Contributor Name under Related Items");
      }
      String nameType = c.nameType() != null ? c.nameType().label() : null;
      String givenName = c.givenName() != null ? c.givenName().value() : null;
      String familyName = c.familyName() != null ? c.familyName().value() : null;
      String contributorType = c.contributorType() != null ? c.contributorType().label() : null;
      if (contributorType == null || contributorType.isEmpty()) {
        missedProperties.add("Contributor Type under Related Items");
      }

      dataCiteRelatedItemContributor.setName(name);
      dataCiteRelatedItemContributor.setNameType(nameType);
      dataCiteRelatedItemContributor.setGivenName(givenName);
      dataCiteRelatedItemContributor.setFamilyName(familyName);
      dataCiteRelatedItemContributor.setContributorType(contributorType);

      dataCiteRelatedItemContributors.add(dataCiteRelatedItemContributor);
    }

      return dataCiteRelatedItemContributors;
  }
}
