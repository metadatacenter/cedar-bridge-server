package org.metadatacenter.cedar.bridge.resource.datacite;

import org.metadatacenter.config.CedarConfig;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.AlternateIdentifierElement.AlternateIdentifierField2;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.AlternateIdentifierElement.AlternateIdentifierTypeField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.CreatorElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.DateElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.DescriptionElement.DescriptionField2;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.DescriptionElement.DescriptionTypeField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.FundingReferenceElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.GeoLocationElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.GeoLocationElement.GeoLocationBoxElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.GeoLocationElement.GeoLocationPointElement.PointLatitudeField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.GeoLocationElement.GeoLocationPointElement.PointLongitudeField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.RelatedIdentifierElement.ResourceTypeGeneralField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.RelatedItemElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.RightsElement.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.generateId;

/*
 * It converts the DataCite JSON to CEDAR JSON
 */
public class DataCiteMetadataParser {
  private static final String DATACITE_ID_URL_PREFIX = "http://purl.org/datacite/v4.4/";
  private static final String PERSONAL_ID_URL = "http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Personal";
  private static final String ORGANIZATIONAL_ID_URL = "http://www.w3.org/2006/vcard/ns#Organizational";

  public static MetadataInstance parseDataCiteSchema(Attributes dataCiteAttributes, String userID, CedarConfig cedarConfig) {
    String prefix = cedarConfig.getBridgeConfig().getDataCite().getPrefix();
    var now = Instant.ofEpochSecond(System.currentTimeMillis() / 1000).toString();

    String url = dataCiteAttributes.getUrl();

    //pass creators values
    var creatorList = parseCreatorValue(dataCiteAttributes.getCreators());

    //pass titles values
    var titleList = parseTitleValue(dataCiteAttributes.getTitles());

    //pass publisher value
    String publisher = dataCiteAttributes.getPublisher();

    //pass publicationYear value
    String publicationYear = parsePublicationYearValue(dataCiteAttributes.getPublicationYear());

    String resourceType = dataCiteAttributes.getTypes().getResourceType();

    //pass subject values
    var subjectList = parseSubjectValue(dataCiteAttributes.getSubjects());

    //pass contributor values
    var contributorList = parseContributorValue(dataCiteAttributes.getContributors());

    //pass date values
    var dateList = parseDateValue(dataCiteAttributes.getDates());

    //pass lang value
    String lang = dataCiteAttributes.getLanguage();

    //pass alternateIdentifier value
    var alternateIdentifierList = parseAlternateIdentifierValue(dataCiteAttributes.getAlternateIdentifiers());

    //pass relatedIdentifier value
    var relatedIdentifierList = parseRelatedIdentifierValue(dataCiteAttributes.getRelatedIdentifiers());

    //pass size value
    var sizeList = parseSizeValue(dataCiteAttributes.getSizes());

    //pass format value
    var formatList = parseFormatValue(dataCiteAttributes.getFormats());

    //pass version value
    String version = dataCiteAttributes.getVersion();

    //pass rights value
    var rightsList = parseRightsValue(dataCiteAttributes.getRightsList());

    //pass description value
    var descriptionList = parseDescriptionValue(dataCiteAttributes.getDescriptions());

    //pass geoLocation value
    var geoLocationList = parseGeoLocationValue(dataCiteAttributes.getGeoLocations());

    //pass fundingReference value
    var fundingReferenceList = parseFundingReferenceValue(dataCiteAttributes.getFundingReferences());

    //pass relatedItem value
    var relatedItems = parseRelatedItemValue(dataCiteAttributes.getRelatedItems());

    String dataciteTemplateId = cedarConfig.getBridgeConfig().getDataCite().getTemplateId();

    return new MetadataInstance(
        "TheId",
        "The name",
        "",
        dataciteTemplateId,
        now,
        userID,
        now,
        userID,
        dataciteTemplateId,
        PrefixField.of(prefix),
        UrlField.of(url),
        CreatorElementList.of(creatorList),
        Cedar.MetadataInstance.TitleElementList.of(titleList),
        Cedar.MetadataInstance.PublicationYearField.of(publicationYear),
        Cedar.MetadataInstance.PublisherField.of(publisher),
        ResourceTypeField.of(resourceType),
        SubjectElementList.of(subjectList),
        ContributorElementList.of(contributorList),
        DateElementList.of(dateList),
        LanguageField.of(lang),
        AlternateIdentifierElementList.of(alternateIdentifierList),
        RelatedIdentifierElementList.of(relatedIdentifierList),
        SizeFieldList.of(sizeList),
        FormatFieldList.of(formatList),
        VersionField.of(version),
        RightsElementList.of(rightsList),
        DescriptionElementList.of(descriptionList),
        GeoLocationElementList.of(geoLocationList),
        FundingReferenceElementList.of(fundingReferenceList),
        RelatedItemElementList.of(relatedItems)
    );
  }

  private static List<Object> parseAffiliationValue(List<DataCiteAffiliation> dataCiteAffiliations, String element) {
    List<Object> affiliationList = new ArrayList<>();

    if (dataCiteAffiliations != null && !dataCiteAffiliations.isEmpty()) {
      for (DataCiteAffiliation a : dataCiteAffiliations) {
        //initialize corresponding class
        String name = a.getName();
        String affiliationIdentifier = a.getAffiliationIdentifier();
        String affiliationScheme = a.getAffiliationIdentifierScheme();
        String schemeUri = a.getAffiliationSchemeURI();
        String id = generateId();

        if (element.equals("Creator")) {
          CreatorElement.AffiliationElement affiliation = new CreatorElement.AffiliationElement(id,
              CreatorElement.AffiliationElement.NameField.of(name),
              CreatorElement.AffiliationElement.AffiliationIdentifierField.of(affiliationIdentifier),
              CreatorElement.AffiliationElement.AffiliationIdentifierSchemeField.of(affiliationScheme),
              CreatorElement.AffiliationElement.SchemeUriField.of(schemeUri));
          // add affiliation to the list
          affiliationList.add(affiliation);
        } else if (element.equals("Contributor")) {
          ContributorElement.AffiliationElement affiliation = new ContributorElement.AffiliationElement(id,
              ContributorElement.AffiliationElement.NameField.of(name),
              ContributorElement.AffiliationElement.AffiliationIdentifierField.of(affiliationIdentifier),
              ContributorElement.AffiliationElement.AffiliationIdentifierSchemeField.of(affiliationScheme),
              ContributorElement.AffiliationElement.SchemeUriField.of(schemeUri));
          // add affiliation to the list
          affiliationList.add(affiliation);
        }
      }
    }
    return affiliationList;
  }

  private static List<Object> parseNameIdentifierValue(List<DataCiteNameIdentifier> dataCiteNameIdentifiers, String element) {
    List<Object> nameIdentifierList = new ArrayList<>();

    if (dataCiteNameIdentifiers != null && !dataCiteNameIdentifiers.isEmpty()) {
      for (DataCiteNameIdentifier n : dataCiteNameIdentifiers) {
        String name = n.getNameIdentifier();
        String nameIdentifierScheme = n.getNameIdentifierScheme();
        String schemeUri = n.getSchemeUri();
        String id = generateId();

        if (element.equals("Creator")) {
          CreatorElement.NameIdentifierElement nameIdentifier = new CreatorElement.NameIdentifierElement(id,
              NameIdentifierElement.NameField.of(name),
              NameIdentifierElement.NameIdentifierSchemeField.of(nameIdentifierScheme),
              NameIdentifierElement.SchemeUriField.of(schemeUri));
          nameIdentifierList.add(nameIdentifier);
        } else if (element.equals("Contributor")) {
          ContributorElement.NameIdentifierElement nameIdentifier = new ContributorElement.NameIdentifierElement(id,
              ContributorElement.NameIdentifierElement.NameField.of(name),
              ContributorElement.NameIdentifierElement.NameIdentifierSchemeField.of(nameIdentifierScheme),
              ContributorElement.NameIdentifierElement.SchemeUriField.of(schemeUri));
          nameIdentifierList.add(nameIdentifier);
        }
      }
    }
    return nameIdentifierList;
  }

  private static List<CreatorElement> parseCreatorValue(List<DataCiteCreator> dataCiteCreators) {
    List<CreatorElement> creatorList = new ArrayList<>();

    if (dataCiteCreators != null && !dataCiteCreators.isEmpty()) {
      for (DataCiteCreator c : dataCiteCreators) {
        String name = c.getName();
        String givenName = c.getGivenName();
        String familyName = c.getFamilyName();
        String nameType = c.getNameType();
        String nameTypeId = null;
        if (nameType != null) {
          if (nameType.equals("Personal")) {
            nameTypeId = PERSONAL_ID_URL;
          } else {
            nameTypeId = ORGANIZATIONAL_ID_URL;
          }
        }

        List<Object> affiliations = parseAffiliationValue(c.getAffiliations(), "Creator");
        List<CreatorElement.AffiliationElement> creatorAffiliations = new ArrayList<>();
        for (Object obj : affiliations) {
          if (obj instanceof CreatorElement.AffiliationElement) {
            creatorAffiliations.add((CreatorElement.AffiliationElement) obj);
          }
        }

        List<Object> nameIdentifiers = parseNameIdentifierValue(c.getNameIdentifiers(), "Creator");
        List<CreatorElement.NameIdentifierElement> creatorNameIdentifiers = new ArrayList<>();
        for (Object obj : nameIdentifiers) {
          if (obj instanceof CreatorElement.NameIdentifierElement) {
            creatorNameIdentifiers.add((CreatorElement.NameIdentifierElement) obj);
          }
        }

        CreatorElement creator = new CreatorElement(generateId(),
            CreatorNameField.of(name),
            NameTypeField.of(nameTypeId, nameType),
            GivenNameField.of(givenName),
            FamilyNameField.of(familyName),
            AffiliationElementList.of(creatorAffiliations),
            NameIdentifierElementList.of(creatorNameIdentifiers));

        creatorList.add(creator);
      }
    }
    return creatorList;
  }

  private static List<Cedar.MetadataInstance.TitleElement> parseTitleValue(List<DataCiteTitle> dataCiteTitles) {
    List<Cedar.MetadataInstance.TitleElement> titleList = new ArrayList<>();

    if (dataCiteTitles != null && !dataCiteTitles.isEmpty()) {
      for (DataCiteTitle t : dataCiteTitles) {
        String titleName = t.getTitle();
        String titleType = t.getTitleType();
        String titleTypeId = DATACITE_ID_URL_PREFIX + titleType;

        Cedar.MetadataInstance.TitleElement title = new Cedar.MetadataInstance.TitleElement(generateId(),
            Cedar.MetadataInstance.TitleElement.TitleField2.of(titleName),
            Cedar.MetadataInstance.TitleElement.TitleTypeField.of(titleTypeId, titleType));

        titleList.add(title);
      }
    }
    return titleList;
  }

  private static String parsePublicationYearValue(Integer givenPublicationYear) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, givenPublicationYear);
    calendar.set(Calendar.MONTH, Calendar.JANUARY);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    return formatter.format(calendar.getTime());
  }

  private static List<SubjectElement> parseSubjectValue(List<DataCiteSubject> dataCiteSubjects) {
    List<SubjectElement> subjectList = new ArrayList<>();

    if (dataCiteSubjects != null && !dataCiteSubjects.isEmpty()) {
      for (DataCiteSubject s : dataCiteSubjects) {
        String subjectName = s.getSubject();
        String subjectScheme = s.getSubjectScheme();
        String classificationCode = s.getClassificationCode();
        String subjectSchemeUri = s.getSchemeUri();
        String valueUri = s.getValueUri();

        SubjectElement subject = new SubjectElement(generateId(),
            SubjectElement.SubjectField2.of(subjectName),
            SubjectElement.SubjectSchemeField.of(subjectScheme),
            SubjectElement.SchemeUriField.of(subjectSchemeUri),
            SubjectElement.ValueUriField.of(valueUri),
            SubjectElement.ClassificationCodeField.of(classificationCode));

        subjectList.add(subject);
      }
    }
    return subjectList;
  }

  private static List<ContributorElement> parseContributorValue(List<DataCiteContributor> dataCiteContributors) {
    List<ContributorElement> contributorList = new ArrayList<>();

    if (dataCiteContributors != null && !dataCiteContributors.isEmpty()) {
      for (DataCiteContributor c : dataCiteContributors) {
        String name = c.getName();
        String nameType = c.getNameType();
        String nameTypeId = null;
        if (nameType != null) {
          if (nameType.equals("Personal")) {
            nameTypeId = PERSONAL_ID_URL;
          } else {
            nameTypeId = ORGANIZATIONAL_ID_URL;
          }
        }
        String givenName = c.getGivenName();
        String familyName = c.getFamilyName();
        String contributorType = c.getContributorType();
        String contributorTypeId = DATACITE_ID_URL_PREFIX + c.getContributorType();
        List<Object> affiliations = parseAffiliationValue(c.getAffiliations(), "Contributor");
        List<ContributorElement.AffiliationElement> contributorAffiliations = new ArrayList<>();
        for (Object obj : affiliations) {
          if (obj instanceof ContributorElement.AffiliationElement) {
            contributorAffiliations.add((ContributorElement.AffiliationElement) obj);
          }
        }

        List<Object> nameIdentifiers = parseNameIdentifierValue(c.getNameIdentifiers(), "Contributor");
        List<ContributorElement.NameIdentifierElement> contributorNameIdentifiers = new ArrayList<>();
        for (Object obj : nameIdentifiers) {
          if (obj instanceof ContributorElement.NameIdentifierElement) {
            contributorNameIdentifiers.add((ContributorElement.NameIdentifierElement) obj);
          }
        }

        ContributorElement contributor = new ContributorElement(generateId(),
            ContributorElement.ContributorNameField.of(name),
            ContributorElement.NameTypeField.of(nameTypeId, nameType),
            ContributorElement.GivenNameField.of(givenName),
            ContributorElement.FamilyNameField.of(familyName),
            ContributorElement.ContributorTypeField.of(contributorTypeId, contributorType),
            ContributorElement.AffiliationElementList.of(contributorAffiliations),
            ContributorElement.NameIdentifierElementList.of(contributorNameIdentifiers));

        contributorList.add(contributor);
      }
    }
    return contributorList;
  }

  private static List<DateElement> parseDateValue(List<DataCiteDate> dataCiteDates) {
    List<DateElement> dateList = new ArrayList<>();

    if (dataCiteDates != null && !dataCiteDates.isEmpty()) {
      for (DataCiteDate d : dataCiteDates) {
        String date = d.getDate();
        String dateType = d.getDateType();
        String dateTypeId = null;
        if (dateType != null) {
          dateTypeId = DATACITE_ID_URL_PREFIX + dateType;
        }
        String dateInformation = d.getDateInformation();

        DateElement cedarDate = new DateElement(generateId(),
            //TODO:debug - add @type
            DateField2.of(date),
            DateTypeField.of(dateTypeId, dateType),
            DateInformationField.of(dateInformation));

        dateList.add(cedarDate);
      }
    }
    return dateList;
  }

  private static List<AlternateIdentifierElement> parseAlternateIdentifierValue(List<DataCiteAlternateIdentifier> dataCiteAlternateIdentifiers) {
    List<AlternateIdentifierElement> alternateIdentifierList = new ArrayList<>();
    if (dataCiteAlternateIdentifiers != null && !dataCiteAlternateIdentifiers.isEmpty()) {
      for (DataCiteAlternateIdentifier a : dataCiteAlternateIdentifiers) {
        String identifier = a.getAlternateIdentifier();
        String identifierType = a.getAlternateIdentifierType();

        AlternateIdentifierElement alternateIdentifier = new AlternateIdentifierElement(generateId(),
            AlternateIdentifierField2.of(identifier),
            AlternateIdentifierTypeField.of(identifierType));

        alternateIdentifierList.add(alternateIdentifier);
      }
    }
    return alternateIdentifierList;
  }

  private static List<RelatedIdentifierElement> parseRelatedIdentifierValue(List<DataCiteRelatedIdentifier> dataCiteRelatedIdentifiers) {
    List<RelatedIdentifierElement> relatedIdentifierList = new ArrayList<>();

    if (dataCiteRelatedIdentifiers != null && !dataCiteRelatedIdentifiers.isEmpty()) {
      for (DataCiteRelatedIdentifier r : dataCiteRelatedIdentifiers) {
        String identifier = r.getRelatedIdentifier();
        String identifierType = r.getRelatedIdentifierType();
        String identifierTypeId = null;
        if (identifierType != null) {
          identifierTypeId = DATACITE_ID_URL_PREFIX + identifierType;
        }
        String relationType = r.getRelationType();
        String relationTypeId = null;
        if (relationType != null) {
          relationTypeId = DATACITE_ID_URL_PREFIX + relationType;
        }
        String relatedMatadataScheme = r.getRelatedMetadataScheme();
        String schemeUri = r.getSchemeUri();
        String schemeType = r.getSchemeType();
        String resourceTypeGeneral = r.getResourceTypeGeneral();
        String resourceTypeGenearlId = null;
        if (resourceTypeGeneral != null) {
          resourceTypeGenearlId = DATACITE_ID_URL_PREFIX + resourceTypeGeneral;
        }

        RelatedIdentifierElement relatedIdentifier = new RelatedIdentifierElement(generateId(),
            RelatedIdentifierElement.RelatedIdentifierField2.of(identifier),
            RelatedIdentifierElement.RelatedIdentifierTypeField.of(identifierTypeId, identifierType),
            RelatedIdentifierElement.RelationTypeField.of(relationTypeId, relationType),
            RelatedIdentifierElement.RelatedMetadataSchemeField.of(relatedMatadataScheme),
            RelatedIdentifierElement.SchemeUriField.of(schemeUri),
            RelatedIdentifierElement.SchemeTypeField.of(schemeType),
            ResourceTypeGeneralField.of(resourceTypeGenearlId, resourceTypeGeneral));

        relatedIdentifierList.add(relatedIdentifier);
      }
    }
    return relatedIdentifierList;
  }

  private static List<SizeField> parseSizeValue(List<String> dataCiteSizes) {
    List<SizeField> sizeList = new ArrayList<>();

    if (dataCiteSizes != null && !dataCiteSizes.isEmpty()) {
      for (String s : dataCiteSizes) {
        SizeField size = new SizeField(s);
        sizeList.add(size);
      }
    }
    return sizeList;
  }

  private static List<FormatField> parseFormatValue(List<String> dataCiteFormats) {
    List<FormatField> formatList = new ArrayList<>();

    if (dataCiteFormats != null && !dataCiteFormats.isEmpty()) {
      for (String f : dataCiteFormats) {
        FormatField format = new FormatField(f);
        formatList.add(format);
      }
    }
    return formatList;
  }

  private static List<RightsElement> parseRightsValue(List<DataCiteRights> dataCiteRights) {
    List<RightsElement> rightsList = new ArrayList<>();

    if (dataCiteRights != null && !dataCiteRights.isEmpty()) {
      for (DataCiteRights r : dataCiteRights) {
        String rightsField = r.getRights();
        String rightsIdentifier = r.getRightsIdentifier();
        String rightsIdentifierScheme = r.getRightsIdentifierScheme();
        String rightsUri = r.getRightsUri();
        String schemeUri = r.getSchemeUri();

        RightsElement rights = new RightsElement(generateId(),
            RightsField2.of(rightsField),
            RightsUriField.of(rightsUri),
            RightsIdentifierField.of(rightsIdentifier),
            RightsIdentifierSchemeField.of(rightsIdentifierScheme),
            RightsElement.SchemeUriField.of(schemeUri));

        rightsList.add(rights);
      }
    }
    return rightsList;
  }

  private static List<DescriptionElement> parseDescriptionValue(List<DataCiteDescription> dataCiteDescriptions) {
    List<DescriptionElement> descriptionList = new ArrayList<>();

    if (dataCiteDescriptions != null && !dataCiteDescriptions.isEmpty()) {
      for (DataCiteDescription d : dataCiteDescriptions) {
        String descriptionField = d.getDescription();
        String descriptionType = d.getDescriptionType();
        String descriptionTypeId = null;
        if (descriptionType != null) {
          descriptionTypeId = DATACITE_ID_URL_PREFIX + descriptionType;
        }

        DescriptionElement description = new DescriptionElement(generateId(),
            DescriptionField2.of(descriptionField),
            DescriptionTypeField.of(descriptionTypeId, descriptionType));

        descriptionList.add(description);
      }
    }
    return descriptionList;
  }

  private static List<GeoLocationElement> parseGeoLocationValue(List<DataCiteGeoLocation> dataCiteGeolocations) {
    List<GeoLocationElement> geoLocationList = new ArrayList<>();

    if (dataCiteGeolocations != null && !dataCiteGeolocations.isEmpty()) {
      for (DataCiteGeoLocation g : dataCiteGeolocations) {
        //set up geoLocationBox
        GeoLocationBoxElement geoLocationBox = GeoLocationBoxElement.of();
        if (g.getGeoLocationBox() != null) {
          String eastBondLongitude = String.valueOf(g.getGeoLocationBox().getEastBoundLongitude());
          String westBondLongitude = String.valueOf(g.getGeoLocationBox().getWestBoundLongitude());
          String northBondLatitude = String.valueOf(g.getGeoLocationBox().getNorthBoundLatitude());
          String southBondLatitude = String.valueOf(g.getGeoLocationBox().getSouthBoundLatitude());
          geoLocationBox = new GeoLocationBoxElement(generateId(),
              //TODO: debug - add @type
              WestBoundLongitudeField.of(westBondLongitude),
              EastBoundLongitudeField.of(eastBondLongitude),
              SouthBoundLatitudeField.of(southBondLatitude),
              NorthBoundLatitudeField.of(northBondLatitude));
        }

        //set up geoLocationPoint
        GeoLocationPointElement geoLocationPoint = GeoLocationPointElement.of();
        if (g.getGeoLocationPoint() != null) {
          String pointLongitude = String.valueOf(g.getGeoLocationPoint().getPointLongitude());
          String pointLatitude = String.valueOf(g.getGeoLocationPoint().getPointLatitude());
          geoLocationPoint = new GeoLocationPointElement(generateId(),
              //TODO: debug - add @type
              PointLongitudeField.of(pointLongitude),
              PointLatitudeField.of(pointLatitude));
        }

        //set up geoLocationPlace
        String geoLocationPlace = g.getGeoLocationPlace();

        GeoLocationElement geoLocation = new GeoLocationElement(generateId(),
            GeoLocationPlaceField.of(geoLocationPlace),
            geoLocationPoint,
            geoLocationBox);

        geoLocationList.add(geoLocation);
      }
    }
    return geoLocationList;
  }

  private static List<FundingReferenceElement> parseFundingReferenceValue(List<DataCiteFundingReference> dataCiteFundingReferences) {
    List<FundingReferenceElement> fundingReferenceList = new ArrayList<>();

    if (dataCiteFundingReferences != null && !dataCiteFundingReferences.isEmpty()) {
      for (DataCiteFundingReference f : dataCiteFundingReferences) {
        String funderName = f.getFunderName();
        String funderIdentifier = f.getFunderIdentifier();
        String funderIdentifierType = f.getFunderIdentifierType();
        String funderIdentifierTypeId = null;
        if (funderIdentifierType != null) {
          funderIdentifierTypeId = switch (funderIdentifierType) {
            case "GRID" -> "https://www.grid.ac/";
            case "ISNI" -> "http://id.loc.gov/ontologies/bibframe/Isni";
            case "ROR" -> "http://purl.obolibrary.org/obo/BE_ROR";
            case "Other" -> "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C17649";
            default -> funderIdentifierTypeId;
          };
        }
        String schemeUri = f.getSchemeUri();
        String awardNumber = f.getAwardNumber();
        String awardUri = f.getAwardUri();
        String awardTitle = f.getAwardTitle();

        FundingReferenceElement fundingReference = new FundingReferenceElement(generateId(),
            FunderNameField.of(funderName),
            FunderIdentifierField.of(funderIdentifier),
            FunderIdentifierTypeField.of(funderIdentifierTypeId, funderIdentifierType),
            FundingReferenceElement.SchemeUriField.of(schemeUri),
            AwardNumberField.of(awardNumber),
            AwardUriField.of(awardUri),
            AwardTitleField.of(awardTitle));

        fundingReferenceList.add(fundingReference);
      }
    }
    return fundingReferenceList;
  }

  private static List<RelatedItemCreatorElement> parseRelatedItemCreatorValue(List<DataCiteCreator> dataCiteCreators) {
    List<RelatedItemCreatorElement> creatorList = new ArrayList<>();

    if (dataCiteCreators != null && !dataCiteCreators.isEmpty()) {
      for (DataCiteCreator c : dataCiteCreators) {
        String name = c.getName();
        String givenName = c.getGivenName();
        String familyName = c.getFamilyName();
        String nameType = c.getNameType();
        String nameTypeId = null;
        if (nameType != null) {
          if (nameType.equals("Personal")) {
            nameTypeId = PERSONAL_ID_URL;
          } else {
            nameTypeId = ORGANIZATIONAL_ID_URL;
          }
        }

        RelatedItemCreatorElement creator = new RelatedItemCreatorElement(generateId(),
            RelatedItemCreatorElement.CreatorNameField.of(name),
            RelatedItemCreatorElement.NameTypeField.of(nameTypeId, nameType),
            RelatedItemCreatorElement.GivenNameField.of(givenName),
            RelatedItemCreatorElement.FamilyNameField.of(familyName));

        creatorList.add(creator);
      }
    }
    return creatorList;
  }

  private static List<RelatedItemContributorElement> parserRelatedItemContributorValue
      (List<DataCiteRelatedItemContributor> dataCiteRelatedItemContributors) {
    List<RelatedItemContributorElement> contributorList = new ArrayList<>();
    if (dataCiteRelatedItemContributors != null && !dataCiteRelatedItemContributors.isEmpty()) {
      for (DataCiteRelatedItemContributor c : dataCiteRelatedItemContributors) {
        String name = c.getName();
        String nameType = c.getNameType();
        String nameTypeId = null;
        if (nameType != null) {
          if (nameType.equals("Personal")) {
            nameTypeId = PERSONAL_ID_URL;
          } else {
            nameTypeId = ORGANIZATIONAL_ID_URL;
          }
        }
        String givenName = c.getGivenName();
        String familyName = c.getFamilyName();
        String contributorType = c.getContributorType();
        String contributorTypeId = DATACITE_ID_URL_PREFIX + c.getContributorType();

        RelatedItemContributorElement contributor = new RelatedItemContributorElement(generateId(),
            RelatedItemContributorElement.ContributorTypeField.of(contributorTypeId, contributorType),
            RelatedItemContributorElement.ContributorNameField.of(name),
            RelatedItemContributorElement.NameTypeField.of(nameTypeId, nameType),
            RelatedItemContributorElement.GivenNameField.of(givenName),
            RelatedItemContributorElement.FamilyNameField.of(familyName));

        contributorList.add(contributor);
      }
    }
    return contributorList;
  }

  private static List<RelatedItemElement.TitleElement> parseRelatedItemTitleValue(List<DataCiteTitle> dataCiteTitles) {
    List<RelatedItemElement.TitleElement> titleList = new ArrayList<>();

    if (dataCiteTitles != null && !dataCiteTitles.isEmpty()) {
      for (DataCiteTitle t : dataCiteTitles) {
        String titleName = t.getTitle();
        String titleType = t.getTitleType();
        String titleTypeId = DATACITE_ID_URL_PREFIX + titleType;

        RelatedItemElement.TitleElement title = new RelatedItemElement.TitleElement(generateId(),
            RelatedItemElement.TitleElement.TitleField2.of(titleName),
            RelatedItemElement.TitleElement.TitleTypeField.of(titleTypeId, titleType));

        titleList.add(title);
      }
    }
    return titleList;
  }

  private static List<RelatedItemElement> parseRelatedItemValue(List<DataCiteRelatedItem> dataCiteRelatedItems) {
    List<RelatedItemElement> relatedItemList = new ArrayList<>();

    if (dataCiteRelatedItems != null && !dataCiteRelatedItems.isEmpty()) {
      for (DataCiteRelatedItem r : dataCiteRelatedItems) {
        String relatedItemType = r.getRelatedItemType();
        String relatedItemTypeId = null;
        if (relatedItemType != null) {
          relatedItemTypeId = DATACITE_ID_URL_PREFIX + relatedItemType;
        }
        String relationType = r.getRelationType();
        String relationTypeId = null;
        if (relationType != null) {
          relationTypeId = DATACITE_ID_URL_PREFIX + relationType;
        }
        String relatedIdentifier = r.getRelatedItemIdentifier().getRelatedItemIdentifier();
        String relatedIdentifierType = r.getRelatedItemIdentifier().getRelatedItemIdentifierType();
        String relatedIdentifierTypeId = null;
        if (relatedIdentifierType != null) {
          relatedIdentifierTypeId = DATACITE_ID_URL_PREFIX + relatedIdentifierType;
        }
        String relatedMetadataScheme = r.getRelatedItemIdentifier().getRelatedMetadataScheme();
        String schemeUri = r.getRelatedItemIdentifier().getSchemeUri();
        String schemeType = r.getRelatedItemIdentifier().getSchemeType();
        String number = r.getNumber();
        String numberType = r.getNumberType();
        String numberTypeId = null;
        if (numberType != null) {
          numberTypeId = switch (numberType) {
            case "Report" -> "http://purl.bioontology.org/ontology/SNOMEDCT/229059009";
            case "Article" -> "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C47902";
            case "Chapter" -> "http://purl.org/ontology/bibo/Chapter";
            case "Others" -> "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C17649";
            default -> numberTypeId;
          };
        }
        String volume = r.getVolume();
        String issue = r.getIssue();
        String firstPage = r.getFirstPage();
        String lastPage = r.getLastPage();
        String publicationYear = null;
        if (r.getPublicationYear() != null) {
          publicationYear = parsePublicationYearValue(r.getPublicationYear());
        }
        String publisher = r.getPublisher();
        String edition = r.getEdition();
        List<RelatedItemElement.TitleElement> titleList = parseRelatedItemTitleValue(r.getTitles());
        List<RelatedItemCreatorElement> creatorList = parseRelatedItemCreatorValue(r.getCreators());
        List<RelatedItemContributorElement> crontributorList = parserRelatedItemContributorValue(r.getContributors());

        RelatedItemElement relatedItem = new RelatedItemElement(generateId(),
            RelatedItemElement.RelatedItemTypeField.of(relatedItemTypeId, relatedItemType),
            RelatedItemElement.RelationTypeField.of(relationTypeId, relationType),
            RelatedItemElement.RelatedIdentifierField.of(relatedIdentifier),
            RelatedItemElement.RelatedIdentifierTypeField.of(relatedIdentifierTypeId, relatedIdentifierType),
            RelatedItemElement.RelatedMetadataSchemeField.of(relatedMetadataScheme),
            RelatedItemElement.SchemeUriField.of(schemeUri),
            RelatedItemElement.SchemeTypeField.of(schemeType),
            RelatedItemElement.TitleElementList.of(titleList),
            RelatedItemCreatorElementList.of(creatorList),
            RelatedItemElement.NumberField.of(number),
            RelatedItemElement.NumberTypeField.of(numberTypeId, numberType),
            RelatedItemElement.VolumeField.of(volume),
            RelatedItemElement.IssueField.of(issue),
            RelatedItemElement.FirstPageField.of(firstPage),
            RelatedItemElement.LastPageField.of(lastPage),
            RelatedItemElement.PublicationYearField.of(publicationYear),
            RelatedItemElement.PublisherField.of(publisher),
            RelatedItemElement.EditionField.of(edition),
            RelatedItemElement.RelatedItemContributorElementList.of(crontributorList));

        relatedItemList.add(relatedItem);
      }
    }
    return relatedItemList;
  }
}
