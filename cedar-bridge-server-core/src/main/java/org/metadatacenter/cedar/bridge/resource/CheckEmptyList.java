package org.metadatacenter.cedar.bridge.resource;

import org.metadatacenter.cedar.bridge.resource.CEDARProperties.*;

import java.util.List;

public class CheckEmptyList {
  public static boolean emptySubjectList(List<Subject> subjectList){
    return subjectList.size()==1
        && (subjectList.get(0).getSubjectName().toString()==null || subjectList.get(0).getSubjectName().toString().equals("") )
        && (subjectList.get(0).getSubjectScheme().toString()==null || subjectList.get(0).getSubjectScheme().toString().equals(""))
        && (subjectList.get(0).getSubjectSchemeURI().toString()==null || subjectList.get(0).getSubjectSchemeURI().toString().equals(""))
        && (subjectList.get(0).getClassificationCode().toString()==null || subjectList.get(0).getClassificationCode().toString().equals(""))
        && (subjectList.get(0).getValueURI().toString()==null || subjectList.get(0).getValueURI().toString().equals(""));
  }

  public static boolean emptyContributorList(List<Contributor> contributorList){
    return contributorList.size()==1
        && (contributorList.get(0).getContributorName().toString() == null || contributorList.get(0).getContributorName().toString().equals(""))
        && (contributorList.get(0).getNameType().toString() == null || contributorList.get(0).getNameType().toString().equals(""))
        && (contributorList.get(0).getGivenName().toString() == null || contributorList.get(0).getGivenName().toString().equals(""))
        && (contributorList.get(0).getFamilyName().toString() == null || contributorList.get(0).getFamilyName().toString().equals(""))
        && (contributorList.get(0).getContributorType().toString() == null || contributorList.get(0).getContributorType().toString().equals(""))
        && (contributorList.get(0).getAffiliations().isEmpty() || emptyAffiliationList(contributorList.get(0).getAffiliations()))
        && (contributorList.get(0).getNameIdentifiers().isEmpty() || emptyNameIdentifierList(contributorList.get(0).getNameIdentifiers()));
  }

  public static boolean emptyAffiliationList(List<Affiliation> affiliationList) {
    return affiliationList.size() == 1
        && (affiliationList.get(0).getAffiliationIdentifier().toString() == null || affiliationList.get(0).getAffiliationIdentifier().toString().equals(""))
        && (affiliationList.get(0).getAffiliationIdentifierScheme().toString() == null || affiliationList.get(0).getAffiliationIdentifierScheme().toString().equals(""))
        && (affiliationList.get(0).getAffiliationIdentifierSchemeURI().toString() == null || affiliationList.get(0).getAffiliationIdentifierSchemeURI().toString().equals(""));
  }

  public static boolean emptyNameIdentifierList(List<NameIdentifier> nameIdentifierList){
    return nameIdentifierList.size()==1
        && (nameIdentifierList.get(0).getNameIdentifierName().toString() == null || nameIdentifierList.get(0).getNameIdentifierName().toString().equals(""))
        && (nameIdentifierList.get(0).getNameIdentifierScheme().toString() == null || nameIdentifierList.get(0).getNameIdentifierScheme().toString().equals(""))
        && (nameIdentifierList.get(0).getNameIdentifierSchemeURI().toString() == null || nameIdentifierList.get(0).getNameIdentifierSchemeURI().toString().equals(""));
  }

  public static boolean emptyDateList(List<Date> dateList){
    return dateList.size()==1
        && (dateList.get(0).getDate().toString() == null || dateList.get(0).getDate().toString().equals(""))
        && (dateList.get(0).getDateType().toString() == null || dateList.get(0).getDateType().toString().equals(""))
        && (dateList.get(0).getDateInformation().toString() == null || dateList.get(0).getDateInformation().toString().equals(""));
  }

  public static boolean emptyAlternateIdentifierList(List<AlternateIdentifier> alternateIdentifierList){
    return alternateIdentifierList.size()==1
        && (alternateIdentifierList.get(0).getAlternateIdentifier().toString()==null || alternateIdentifierList.get(0).getAlternateIdentifier().toString().equals(""))
        && (alternateIdentifierList.get(0).getAlternateIdentifierType().toString()==null || alternateIdentifierList.get(0).getAlternateIdentifierType().toString().equals(""));
  }

  public static boolean emptyRelatedIdentifierList(List<RelatedIdentifier> relatedIdentifierList){
    return relatedIdentifierList.size()==1
        && (relatedIdentifierList.get(0).getRelatedIdentifier().toString()==null || relatedIdentifierList.get(0).getRelatedIdentifier().toString().equals(""))
        && (relatedIdentifierList.get(0).getRelatedIdentifierType().toString()==null || relatedIdentifierList.get(0).getRelatedIdentifierType().toString().equals(""))
        && (relatedIdentifierList.get(0).getRelationType().toString()==null || relatedIdentifierList.get(0).getRelationType().toString().equals(""))
        && (relatedIdentifierList.get(0).getRelatedMetadataScheme().toString()==null || relatedIdentifierList.get(0).getRelatedMetadataScheme().toString().equals(""))
        && (relatedIdentifierList.get(0).getSchemeURi().toString()==null || relatedIdentifierList.get(0).getSchemeURi().toString().equals(""))
        && (relatedIdentifierList.get(0).getSchemeType().toString()==null || relatedIdentifierList.get(0).getSchemeType().toString().equals(""))
        && (relatedIdentifierList.get(0).getResourceTypeGeneral().toString()==null || relatedIdentifierList.get(0).getResourceTypeGeneral().toString().equals(""));
  }

  public static boolean emptyRightsList(List<Rights> rightsList){
    return rightsList.size()==1
        && (rightsList.get(0).getRights().toString()==null || rightsList.get(0).getRights().toString().equals(""))
        && (rightsList.get(0).getRightsURI().toString()==null || rightsList.get(0).getRightsURI().toString().equals(""))
        && (rightsList.get(0).getSchemeURI().toString()==null || rightsList.get(0).getSchemeURI().toString().equals(""))
        && (rightsList.get(0).getRightsIdentifier().toString()==null || rightsList.get(0).getRightsIdentifier().toString().equals(""))
        && (rightsList.get(0).getRightsIdentifierScheme().toString()==null || rightsList.get(0).getRightsIdentifierScheme().toString().equals(""));
  }

  public static boolean emptyDescriptionList(List<Description> descriptionList){
    return descriptionList.size()==1
        && (descriptionList.get(0).getDescription().toString()==null || descriptionList.get(0).getDescription().toString().equals(""))
        && (descriptionList.get(0).getDescriptionType().toString()==null || descriptionList.get(0).getDescriptionType().toString().equals(""))
        && (descriptionList.get(0).getLanguage().toString()==null || descriptionList.get(0).getLanguage().toString().equals(""));
  }

  public static boolean emptyGeoLocationList(List<GeoLocation> geoLocationList){
    return geoLocationList.size()==1
        && (geoLocationList.get(0).getGeoLocationPlace().toString()==null || geoLocationList.get(0).getGeoLocationPlace().toString().equals(""))
        && (geoLocationList.get(0).getGeoLocationPoint().getPointLatitude().getValue()==null)
        && (geoLocationList.get(0).getGeoLocationPoint().getPointLongitude().getValue()==null)
        && (geoLocationList.get(0).getGeoLocationBox().getEastBoundLongitude().getValue()==null)
        && (geoLocationList.get(0).getGeoLocationBox().getNorthBoundLatitude().getValue()==null)
        && (geoLocationList.get(0).getGeoLocationBox().getSouthBoundLatitude().getValue()==null)
        && (geoLocationList.get(0).getGeoLocationBox().getWestBoundLongitude().getValue()==null);
  }

  public static boolean emptyFundingReferenceList(List<FundingReference> fundingReferenceList){
    return fundingReferenceList.size()==1
        && (fundingReferenceList.get(0).getFunderName().toString()==null || fundingReferenceList.get(0).getFunderName().toString().equals(""))
        && (fundingReferenceList.get(0).getFunderIdentifier().getFunderIdentifier().toString()==null || fundingReferenceList.get(0).getFunderIdentifier().getFunderIdentifier().toString().equals(""))
        && (fundingReferenceList.get(0).getFunderIdentifier().getFunderIdentifierType().toString()==null || fundingReferenceList.get(0).getFunderIdentifier().getFunderIdentifierType().toString().equals(""))
        && (fundingReferenceList.get(0).getFunderIdentifier().getSchemeURI().toString()==null || fundingReferenceList.get(0).getFunderIdentifier().getSchemeURI().toString().equals(""))
        && (fundingReferenceList.get(0).getAwardNumber().getAwardURI().toString()==null || fundingReferenceList.get(0).getAwardNumber().getAwardURI().toString().equals(""))
        && (fundingReferenceList.get(0).getAwardNumber().getAwardURI().toString()==null || fundingReferenceList.get(0).getAwardNumber().getAwardURI().toString().equals(""))
        && (fundingReferenceList.get(0).getAwardTitle().toString()==null || fundingReferenceList.get(0).getAwardTitle().toString().equals(""));
  }

  public static boolean emptyRelatedItemList(List<RelatedItem> relatedItemList){
    return relatedItemList.size()==1
        && (relatedItemList.get(0).getRelatedItemType().toString()==null || relatedItemList.get(0).getRelatedItemType().toString().equals(""))
        && (relatedItemList.get(0).getRelationType().toString()==null || relatedItemList.get(0).getRelationType().toString().equals(""))
        && (relatedItemList.get(0).getRelatedItemIdentifier().getRelatedIdentifier().toString()==null || relatedItemList.get(0).getRelatedItemIdentifier().getRelatedIdentifier().toString().equals(""))
        && (relatedItemList.get(0).getRelatedItemIdentifier().getRelatedIdentifierType().toString()==null || relatedItemList.get(0).getRelatedItemIdentifier().getRelatedIdentifierType().toString().equals(""))
        && (relatedItemList.get(0).getRelatedItemIdentifier().getRelatedMetadataScheme().toString()==null || relatedItemList.get(0).getRelatedItemIdentifier().getRelatedMetadataScheme().toString().equals(""))
        && (relatedItemList.get(0).getRelatedItemIdentifier().getSchemeURi().toString()==null || relatedItemList.get(0).getRelatedItemIdentifier().getSchemeURi().toString().equals(""))
        && (relatedItemList.get(0).getRelatedItemIdentifier().getSchemeType().toString()==null || relatedItemList.get(0).getRelatedItemIdentifier().getSchemeType().toString().equals(""))
        && (relatedItemList.get(0).getNumber().toString()==null || relatedItemList.get(0).getNumber().toString().equals(""))
        && (relatedItemList.get(0).getNumberType().toString()==null || relatedItemList.get(0).getNumberType().toString().equals(""))
        && (relatedItemList.get(0).getVolume().toString()==null || relatedItemList.get(0).getVolume().toString().equals(""))
        && (relatedItemList.get(0).getIssue().toString()==null || relatedItemList.get(0).getIssue().toString().equals(""))
        && (relatedItemList.get(0).getFirstPage().toString()==null || relatedItemList.get(0).getFirstPage().toString().equals(""))
        && (relatedItemList.get(0).getLastPage().toString()==null || relatedItemList.get(0).getLastPage().toString().equals(""))
        && (relatedItemList.get(0).getPublicationYear().toString()==null || relatedItemList.get(0).getPublicationYear().toString().equals(""))
        && (relatedItemList.get(0).getPublisher().toString()==null || relatedItemList.get(0).getPublisher().toString().equals(""))
        && (relatedItemList.get(0).getEdition().toString()==null || relatedItemList.get(0).getEdition().toString().equals(""))
        && (emptyRelatedItemCreatorList(relatedItemList.get(0).getCreators()) || relatedItemList.get(0).getCreators().isEmpty())
        && (emptyRelatedItemTitleList(relatedItemList.get(0).getTitles()) || relatedItemList.get(0).getTitles().isEmpty())
        && (emptyRelatedItemContributorList(relatedItemList.get(0).getContributors()) || relatedItemList.get(0).getContributors().isEmpty());
  }

  public static boolean emptyRelatedItemTitleList(List<Title> titleList){
    return titleList.size()==1
        && (titleList.get(0).getTitleName().toString()==null || titleList.get(0).getTitleName().toString().equals(""))
        && (titleList.get(0).getTitleType().toString()==null || titleList.get(0).getTitleType().toString().equals(""));
  }

  public static boolean emptyRelatedItemCreatorList(List<Creator> creatorList){
    return creatorList.size()==1
        && (creatorList.get(0).getCreatorName().toString()==null || creatorList.get(0).getCreatorName().toString().equals(""))
        && (creatorList.get(0).getNameType().toString()==null || creatorList.get(0).getNameType().toString().equals(""))
        && (creatorList.get(0).getFamilyName().toString()==null || creatorList.get(0).getFamilyName().toString().equals(""))
        && (creatorList.get(0).getGivenName().toString()==null || creatorList.get(0).getGivenName().toString().equals(""));
  }

  public static boolean emptyRelatedItemContributorList(List<Contributor> contributorList){
    return contributorList.size()==1
        && (contributorList.get(0).getContributorName().toString() == null || contributorList.get(0).getContributorName().toString().equals(""))
        && (contributorList.get(0).getNameType().toString() == null || contributorList.get(0).getNameType().toString().equals(""))
        && (contributorList.get(0).getGivenName().toString() == null || contributorList.get(0).getGivenName().toString().equals(""))
        && (contributorList.get(0).getFamilyName().toString() == null || contributorList.get(0).getFamilyName().toString().equals(""))
        && (contributorList.get(0).getContributorType().toString() == null || contributorList.get(0).getContributorType().toString().equals(""));
  }
}
