package org.metadatacenter.cedar.bridge.resource.datacite;

import java.util.List;

import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.LiteralField;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.*;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.RelatedItemElement.RelatedItemContributorElement;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.RelatedItemElement.RelatedItemCreatorElement;

public class CheckEmptyList {
  public static boolean emptyCreatorList(List<CreatorElement> creatorList) {
    String name = creatorList.get(0).creatorName().value();
    String nameType = creatorList.get(0).nameType() != null ? creatorList.get(0).nameType().label() : null;
    String familyName = creatorList.get(0).familyName() != null ? creatorList.get(0).familyName().value() : null;
    String givenName = creatorList.get(0).givenName() != null ? creatorList.get(0).givenName().value() : null;
    List<CreatorElement.AffiliationElement> affiliationList = creatorList.get(0).affiliation() != null ? creatorList.get(0).affiliation().affiliationList() : null;
    List<CreatorElement.NameIdentifierElement> nameIdentifierList = creatorList.get(0).nameIdentifier() != null ? creatorList.get(0).nameIdentifier().nameIdentifierList() : null;

    return creatorList.size() == 1
        && (name == null || name.isEmpty())
        && (nameType == null || nameType.isEmpty())
        && (familyName == null || familyName.isEmpty())
        && (givenName == null || givenName.isEmpty())
        && (affiliationList == null || affiliationList.isEmpty() || emptyAffiliationList(affiliationList))
        && (nameIdentifierList == null || nameIdentifierList.isEmpty() || emptyNameIdentifierList(nameIdentifierList));
  }

  public static boolean emptyTitleList(List<?> titleList) {
    String title = null;
    String titleType = null;
    Object obj = titleList.get(0);

    if (obj instanceof MetadataInstance.TitleElement t) {
      title = t.title().value();
      titleType = t.titleType() != null ? t.titleType().label() : null;
    } else if (obj instanceof RelatedItemElement.TitleElement t) {
      title = t.title().value();
      titleType = t.titleType() != null ? t.titleType().label() : null;
    }

    return titleList.size() == 1
        && (title == null || title.isEmpty())
        && (titleType == null || titleType.isEmpty());
  }

  public static boolean emptySubjectList(List<SubjectElement> subjectList) {
    String subjectName = subjectList.get(0).subject() != null ? subjectList.get(0).subject().value() : null;
    String subjectScheme = subjectList.get(0).subjectScheme() != null ? subjectList.get(0).subjectScheme().value() : null;
    String subjectSchemeURI = subjectList.get(0).schemeUri() != null ? subjectList.get(0).schemeUri().id() : null;
    String classificationCode = subjectList.get(0).classificationCode() != null ? subjectList.get(0).classificationCode().value() : null;
    String valueURI = subjectList.get(0).valueUri() != null ? subjectList.get(0).valueUri().id() : null;

    return subjectList.size() == 1
        && (subjectName == null || subjectName.isEmpty())
        && (subjectScheme == null || subjectScheme.isEmpty())
        && (subjectSchemeURI == null || subjectSchemeURI.isEmpty())
        && (classificationCode == null || classificationCode.isEmpty())
        && (valueURI == null || valueURI.isEmpty());
  }

  public static boolean emptyContributorList(List<ContributorElement> contributorList) {
    String contributorName = contributorList.get(0).contributorName() != null ? contributorList.get(0).contributorName().value() : null;
    String nameType = contributorList.get(0).nameType() != null ? contributorList.get(0).nameType().label() : null;
    String givenName = contributorList.get(0).givenName() != null ? contributorList.get(0).givenName().value() : null;
    String familyName = contributorList.get(0).familyName() != null ? contributorList.get(0).familyName().value() : null;
    String contributorType = contributorList.get(0).contributorType() != null ? contributorList.get(0).contributorType().label() : null;
    List<ContributorElement.AffiliationElement> affiliationList = contributorList.get(0).affiliation() != null ? contributorList.get(0).affiliation().affiliationList() : null;
    List<ContributorElement.NameIdentifierElement> nameIdentifierList = contributorList.get(0).nameIdentifier() != null ? contributorList.get(0).nameIdentifier().nameIdentifierList() : null;

    return contributorList.size() == 1
        && (contributorName == null || contributorName.isEmpty())
        && (nameType == null || nameType.isEmpty())
        && (givenName == null || givenName.isEmpty())
        && (familyName == null || familyName.isEmpty())
        && (contributorType == null || contributorType.isEmpty())
        && (affiliationList == null || affiliationList.isEmpty() || emptyAffiliationList(affiliationList))
        && (nameIdentifierList == null || nameIdentifierList.isEmpty() || emptyNameIdentifierList(nameIdentifierList));
  }

  public static boolean emptyAffiliationList(List<?> affiliationList) {
    String name = null;
    String affiliationIdentifier = null;
    String affiliationIdentifierScheme = null;
    String affiliationIdentifierSchemeURI = null;
    Object obj = affiliationList.get(0);
    if (obj instanceof CreatorElement.AffiliationElement a) {
      name = a.name() != null ? a.name().value() : null;
      affiliationIdentifier = a.affiliationIdentifier() != null ? a.affiliationIdentifier().value() : null;
      affiliationIdentifierScheme = a.affiliationIdentifierScheme() != null ? a.affiliationIdentifierScheme().value() : null;
      affiliationIdentifierSchemeURI = a.schemeUri() != null ? a.schemeUri().id() : null;
    } else if (obj instanceof ContributorElement.AffiliationElement a) {
      name = a.name() != null ? a.name().value() : null;
      affiliationIdentifier = a.affiliationIdentifier() != null ? a.affiliationIdentifier().value() : null;
      affiliationIdentifierScheme = a.affiliationIdentifierScheme() != null ? a.affiliationIdentifierScheme().value() : null;
      affiliationIdentifierSchemeURI = a.schemeUri() != null ? a.schemeUri().id() : null;
    }

    return affiliationList.size() == 1
        && (name == null || name.isEmpty())
        && (affiliationIdentifier == null || affiliationIdentifier.isEmpty())
        && (affiliationIdentifierScheme == null || affiliationIdentifierScheme.isEmpty())
        && (affiliationIdentifierSchemeURI == null || affiliationIdentifierSchemeURI.isEmpty());
  }

  public static boolean emptyNameIdentifierList(List<?> nameIdentifierList) {
    String nameIdentifierName = null;
    String nameIdentifierScheme = null;
    String schemeURI = null;
    Object obj = nameIdentifierList.get(0);
    if (obj instanceof CreatorElement.NameIdentifierElement n) {
      nameIdentifierName = n.name() != null ? n.name().value() : null;
      nameIdentifierScheme = n.nameIdentifierScheme() != null ? n.nameIdentifierScheme().value() : null;
      schemeURI = n.schemeUri() != null ? n.schemeUri().id() : null;
    } else if (obj instanceof ContributorElement.NameIdentifierElement n) {
      nameIdentifierName = n.name() != null ? n.name().value() : null;
      nameIdentifierScheme = n.nameIdentifierScheme() != null ? n.nameIdentifierScheme().value() : null;
      schemeURI = n.schemeUri() != null ? n.schemeUri().id() : null;
    }

    return nameIdentifierList.size() == 1
        && (nameIdentifierName == null || nameIdentifierName.isEmpty())
        && (nameIdentifierScheme == null || nameIdentifierScheme.isEmpty())
        && (schemeURI == null || schemeURI.isEmpty());
  }

  public static boolean emptyDateList(List<DateElement> dateList) {
    String date = dateList.get(0).date() != null ? dateList.get(0).date().value() : null;
    String dateType = dateList.get(0).dateType() != null ? dateList.get(0).dateType().label() : null;
    String dateInformation = dateList.get(0).dateInformation() != null ? dateList.get(0).dateInformation().value() : null;

    return dateList.size() == 1
        && (date == null || date.isEmpty())
        && (dateType == null || dateType.isEmpty())
        && (dateInformation == null || dateInformation.isEmpty());
  }

  public static boolean emptyAlternateIdentifierList(List<AlternateIdentifierElement> alternateIdentifierList) {
    String alternateIdentifier = alternateIdentifierList.get(0).alternateIdentifier() != null ? alternateIdentifierList.get(0).alternateIdentifier().value() : null;
    String alternateIdentifierType = alternateIdentifierList.get(0).alternateIdentifierType() != null ? alternateIdentifierList.get(0).alternateIdentifierType().value() : null;

    return alternateIdentifierList.size() == 1
        && (alternateIdentifier == null || alternateIdentifier.isEmpty())
        && (alternateIdentifierType == null || alternateIdentifierType.isEmpty());
  }

  public static boolean emptyRelatedIdentifierList(List<RelatedIdentifierElement> relatedIdentifierList) {
    String relatedIdentifier = relatedIdentifierList.get(0).relatedIdentifier() != null ? relatedIdentifierList.get(0).relatedIdentifier().value() : null;
    String relatedIdentifierType = relatedIdentifierList.get(0).relatedIdentifierType() != null ? relatedIdentifierList.get(0).relatedIdentifierType().label() : null;
    String relationType = relatedIdentifierList.get(0).relationType() != null ? relatedIdentifierList.get(0).relationType().label() : null;
    String relatedMetadataScheme = relatedIdentifierList.get(0).relatedMetadataScheme() != null ? relatedIdentifierList.get(0).relatedMetadataScheme().value() : null;
    String schemeUri = relatedIdentifierList.get(0).schemeUri() != null ? relatedIdentifierList.get(0).schemeUri().id() : null;
    String schemeType = relatedIdentifierList.get(0).schemeType() != null ? relatedIdentifierList.get(0).schemeType().value() : null;
    String resourceTypeGeneral = relatedIdentifierList.get(0).resourceTypeGeneral() != null ? relatedIdentifierList.get(0).resourceTypeGeneral().label() : null;

    return relatedIdentifierList.size() == 1
        && (relatedIdentifier == null || relatedIdentifier.isEmpty())
        && (relatedIdentifierType == null || relatedIdentifierType.isEmpty())
        && (relationType == null || relationType.isEmpty())
        && (relatedMetadataScheme == null || relatedMetadataScheme.isEmpty())
        && (schemeUri == null || schemeUri.isEmpty())
        && (schemeType == null || schemeType.isEmpty())
        && (resourceTypeGeneral == null || resourceTypeGeneral.isEmpty());
  }

  public static boolean emptyRightsList(List<RightsElement> rightsList) {
    String rights = rightsList.get(0).rights() != null ? rightsList.get(0).rights().value() : null;
    String rightsUri = rightsList.get(0).rightsUri() != null ? rightsList.get(0).rightsUri().label() : null;
    String schemeUri = rightsList.get(0).schemeUri() != null ? rightsList.get(0).schemeUri().id() : null;
    String rightsIdentifier = rightsList.get(0).rightsIdentifier() != null ? rightsList.get(0).rightsIdentifier().value() : null;
    String rightsIdentifierScheme = rightsList.get(0).rightsIdentifierScheme() != null ? rightsList.get(0).rightsIdentifierScheme().value() : null;

    return rightsList.size() == 1
        && (rights == null || rights.isEmpty())
        && (rightsUri == null || rightsUri.isEmpty())
        && (schemeUri == null || schemeUri.isEmpty())
        && (rightsIdentifier == null || rightsIdentifier.isEmpty())
        && (rightsIdentifierScheme == null || rightsIdentifierScheme.isEmpty());
  }

  public static boolean emptyDescriptionList(List<DescriptionElement> descriptionList) {
    String description = descriptionList.get(0).description() != null ? descriptionList.get(0).description().value() : null;
    String descriptionType = descriptionList.get(0).descriptionType() != null ? descriptionList.get(0).descriptionType().label() : null;

    return descriptionList.size() == 1
        && (description == null || description.isEmpty())
        && (descriptionType == null || descriptionType.isEmpty());
  }

  public static boolean emptyGeoLocationList(List<GeoLocationElement> geoLocationList) {
    String geoLocationPlace = geoLocationList.get(0).geoLocationPlace() != null ? geoLocationList.get(0).geoLocationPlace().value() : null;
    String pointLatitude = null;
    String pointLongitude = null;
    String eastBoundLongitude = null;
    String northBoundLatitude = null;
    String southBoundLatitude = null;
    String westBoundLongitude = null;

    if (geoLocationList.get(0).geoLocationPoint() != null) {
      if (geoLocationList.get(0).geoLocationPoint().pointLongitude() != null) {
        pointLongitude = geoLocationList.get(0).geoLocationPoint().pointLongitude().value();
      }
      if (geoLocationList.get(0).geoLocationPoint().pointLatitude() != null) {
        pointLatitude = geoLocationList.get(0).geoLocationPoint().pointLatitude().value();
      }
    }

    if (geoLocationList.get(0).geoLocationBox() != null) {
      if (geoLocationList.get(0).geoLocationBox().eastBoundLongitude() != null) {
        eastBoundLongitude = geoLocationList.get(0).geoLocationBox().eastBoundLongitude().value();
      }
      if (geoLocationList.get(0).geoLocationBox().northBoundLatitude() != null) {
        northBoundLatitude = geoLocationList.get(0).geoLocationBox().northBoundLatitude().value();
      }
      if (geoLocationList.get(0).geoLocationBox().southBoundLatitude() != null) {
        southBoundLatitude = geoLocationList.get(0).geoLocationBox().southBoundLatitude().value();
      }
      if (geoLocationList.get(0).geoLocationBox().westBoundLongitude() != null) {
        westBoundLongitude = geoLocationList.get(0).geoLocationBox().westBoundLongitude().value();
      }
    }

    return geoLocationList.size() == 1
        && (geoLocationPlace == null || geoLocationPlace.isEmpty())
        && (pointLatitude == null || pointLatitude.isEmpty())
        && (pointLongitude == null || pointLongitude.isEmpty())
        && (eastBoundLongitude == null || eastBoundLongitude.isEmpty())
        && (northBoundLatitude == null || northBoundLatitude.isEmpty())
        && (southBoundLatitude == null || southBoundLatitude.isEmpty())
        && (westBoundLongitude == null || westBoundLongitude.isEmpty());
  }

  public static boolean emptyFundingReferenceList(List<FundingReferenceElement> fundingReferenceList) {
    String funderName = fundingReferenceList.get(0).funderName() != null ? fundingReferenceList.get(0).funderName().value() : null;
    String funderIdentifier = fundingReferenceList.get(0).funderIdentifier() != null ? fundingReferenceList.get(0).funderIdentifier().value() : null;
    String funderIdentifierType = fundingReferenceList.get(0).funderIdentifierType() != null ? fundingReferenceList.get(0).funderIdentifierType().label() : null;
    String schemeUri = fundingReferenceList.get(0).schemeUri() != null ? fundingReferenceList.get(0).schemeUri().id() : null;
    String awardUri = fundingReferenceList.get(0).awardUri() != null ? fundingReferenceList.get(0).awardUri().id() : null;
    String awardNumber = fundingReferenceList.get(0).awardNumber() != null ? fundingReferenceList.get(0).awardNumber().value() : null;
    String awardTitle = fundingReferenceList.get(0).awardTitle() != null ? fundingReferenceList.get(0).awardTitle().value() : null;

    return fundingReferenceList.size() == 1
        && (funderName == null || funderName.isEmpty())
        && (funderIdentifier == null || funderIdentifier.isEmpty())
        && (funderIdentifierType == null || funderIdentifierType.isEmpty())
        && (schemeUri == null || schemeUri.isEmpty())
        && (awardUri == null || awardUri.isEmpty())
        && (awardNumber == null || awardNumber.isEmpty())
        && (awardTitle == null || awardTitle.isEmpty());
  }

  public static boolean emptyRelatedItemList(List<RelatedItemElement> relatedItemList) {
    String relatedItemType = relatedItemList.get(0).relatedItemType() != null ? relatedItemList.get(0).relatedItemType().label() : null;
    String relationType = relatedItemList.get(0).relationType() != null ? relatedItemList.get(0).relationType().label() : null;
    String relatedIdentifier = relatedItemList.get(0).relatedIdentifier() != null ? relatedItemList.get(0).relatedIdentifier().value() : null;
    String relatedIdentifierType = relatedItemList.get(0).relatedIdentifierType() != null ? relatedItemList.get(0).relatedIdentifierType().label() : null;
    String relatedMetadataSchema = relatedItemList.get(0).relatedMetadataScheme() != null ? relatedItemList.get(0).relatedMetadataScheme().value() : null;
    String schemeUri = relatedItemList.get(0).schemeUri() != null ? relatedItemList.get(0).schemeUri().label() : null;
    String schemeType = relatedItemList.get(0).schemeType() != null ? relatedItemList.get(0).schemeType().value() : null;
    String number = relatedItemList.get(0).number() != null ? relatedItemList.get(0).number().value() : null;
    String numberType = relatedItemList.get(0).numberType() != null ? relatedItemList.get(0).numberType().label() : null;
    String volume = relatedItemList.get(0).volume() != null ? relatedItemList.get(0).volume().value() : null;
    String issue = relatedItemList.get(0).issue() != null ? relatedItemList.get(0).issue().value() : null;
    String firstPage = relatedItemList.get(0).firstPage() != null ? relatedItemList.get(0).firstPage().value() : null;
    String lastPage = relatedItemList.get(0).lastPage() != null ? relatedItemList.get(0).lastPage().value() : null;
    String publicationYear = relatedItemList.get(0).publicationYear() != null ? relatedItemList.get(0).publicationYear().value() : null;
    String publisher = relatedItemList.get(0).publisher() != null ? relatedItemList.get(0).publisher().value() : null;
    String edition = relatedItemList.get(0).edition() != null ? relatedItemList.get(0).edition().value() : null;

    return relatedItemList.size() == 1
        && (relatedItemType == null || relatedItemType.isEmpty())
        && (relationType == null || relationType.isEmpty())
        && (relatedIdentifier == null || relatedIdentifier.isEmpty())
        && (relatedIdentifierType == null || relatedIdentifierType.isEmpty())
        && (relatedMetadataSchema == null || relatedMetadataSchema.isEmpty())
        && (schemeUri == null || schemeUri.isEmpty())
        && (schemeType == null || schemeType.isEmpty())
        && (number == null || number.isEmpty())
        && (numberType == null || numberType.isEmpty())
        && (volume == null || volume.isEmpty())
        && (issue == null || issue.isEmpty())
        && (firstPage == null || firstPage.isEmpty())
        && (lastPage == null || lastPage.isEmpty())
        && (publicationYear == null || publicationYear.isEmpty())
        && (publisher == null || publisher.isEmpty())
        && (edition == null || edition.isEmpty())
        && (relatedItemList.get(0).title() == null ||
        relatedItemList.get(0).title().titleList() == null ||
        relatedItemList.get(0).title().titleList().isEmpty() ||
        emptyTitleList(relatedItemList.get(0).title().titleList()))
        && (relatedItemList.get(0).relatedItemCreator() == null ||
        relatedItemList.get(0).relatedItemCreator().relatedItemCreatorList() == null ||
        relatedItemList.get(0).relatedItemCreator().relatedItemCreatorList().isEmpty() ||
        emptyRelatedItemCreatorList(relatedItemList.get(0).relatedItemCreator().relatedItemCreatorList()))
        && (relatedItemList.get(0).relatedItemContributor() == null ||
        relatedItemList.get(0).relatedItemContributor().relatedItemContributorList() == null ||
        relatedItemList.get(0).relatedItemContributor().relatedItemContributorList().isEmpty() ||
        emptyRelatedItemContributorList(relatedItemList.get(0).relatedItemContributor().relatedItemContributorList()));
  }

  public static boolean emptyRelatedItemCreatorList(List<RelatedItemCreatorElement> creatorList) {
    String name = creatorList.get(0).creatorName().value();
    String nameType = creatorList.get(0).nameType() != null ? creatorList.get(0).nameType().label() : null;
    String familyName = creatorList.get(0).familyName() != null ? creatorList.get(0).familyName().value() : null;
    String givenName = creatorList.get(0).givenName() != null ? creatorList.get(0).givenName().value() : null;

    return creatorList.size() == 1
        && (name == null || name.isEmpty())
        && (nameType == null || nameType.isEmpty())
        && (familyName == null || familyName.isEmpty())
        && (givenName == null || givenName.isEmpty());
  }

  public static boolean emptyRelatedItemContributorList(List<RelatedItemContributorElement> contributorList) {
    String contributorName = contributorList.get(0).contributorName() != null ? contributorList.get(0).contributorName().value() : null;
    String nameType = contributorList.get(0).nameType() != null ? contributorList.get(0).nameType().label() : null;
    String givenName = contributorList.get(0).givenName() != null ? contributorList.get(0).givenName().value() : null;
    String familyName = contributorList.get(0).familyName() != null ? contributorList.get(0).familyName().value() : null;
    String contributorType = contributorList.get(0).contributorType() != null ? contributorList.get(0).contributorType().label() : null;

    return contributorList.size() == 1
        && (contributorName == null || contributorName.isEmpty())
        && (nameType == null || nameType.isEmpty())
        && (givenName == null || givenName.isEmpty())
        && (familyName == null || familyName.isEmpty())
        && (contributorType == null || contributorType.isEmpty());
  }

  public static <T extends LiteralField> boolean emptyValueList(List<T> valueList) {
    boolean empty = true;
    for (LiteralField v : valueList) {
      if (v.value() != null && !v.value().isEmpty()) {
        empty = false;
      }
    }
    return empty;
  }


}
