package org.metadatacenter.cedar.bridge.resource;

import org.metadatacenter.cedar.bridge.resource.CEDARProperties.*;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.*;

import java.util.ArrayList;
import java.util.List;

public class CEDARInstanceParser {
    public static void parseCEDARInstance(CEDARDataCiteInstance CEDARDataCiteInstance, DataCiteSchema dataCiteSchema) {
        Data data = new Data();
        Attributes attributes = new Attributes();

        // Set type to "dois"
        data.setType("dois");

        //Pass prefix value from CEDAR class to DataCite class
        attributes.setPrefix(CEDARDataCiteInstance.getPrefix().getValue());

        // set event value
        attributes.setEvent("publish");

        // Set url and schemeVersion
        attributes.setUrl("https://schema.datacite.org/meta/kernel-4.0/index.html");
        attributes.setSchemaVersion("http://datacite.org/schema/kernel-4");

        // Pass creator values from CEDAR class to DataCite class
        attributes.setCreators(parseCreatorValue(CEDARDataCiteInstance.getCreatorElement().getCreators()));

        //Pass titles values from CEDAR class to DataCite class
        attributes.setTitles(parseTitleValue(CEDARDataCiteInstance.getTitleElement().getTitles()));

        //Pass publisher values from CEDAR class to DataCite class
        attributes.setPublisher(CEDARDataCiteInstance.getPublisherElement().getPublisher().toString());

        //Pass publisherYear values
        attributes.setPublicationYear(parsePublisherYearValue(CEDARDataCiteInstance));

        //Pass subjects values
        attributes.setSubjects(parseSubjectValue(CEDARDataCiteInstance.getSubjectElement().getSubjects()));

        // Pass resourceType values
        attributes.setTypes(parseTypeValue(CEDARDataCiteInstance.getResourceTypeElement()));

        //Pass contributors values
        attributes.setContributors(parseContributorValue(CEDARDataCiteInstance.getContributorElement().getContributors()));

        //Pass dates values
        attributes.setDates(parseDateValue(CEDARDataCiteInstance.getDateElement().getDates()));

        //Pass Language value
        attributes.setLanguage(CEDARDataCiteInstance.getLanguage().toString());

        //Pass alternateIdentifier values
        attributes.setAlternateIdentifiers(parseAlternateIdentifier(CEDARDataCiteInstance.getAlternateIdentifierElement().getAlternateIdentifiers()));

        //Pass relatedIdentifier values
        attributes.setRelatedIdentifiers(parseRelatedIdentifier(CEDARDataCiteInstance.getRelatedIdentifierElement().getRelatedIdentifiers()));

        //Pass size values
        attributes.setSizes(parseSizeValue(CEDARDataCiteInstance.getSizeElement().getSizes()));

        //Pass format values
        attributes.setFormats(parseFormatValue(CEDARDataCiteInstance.getFormatElement().getFormats()));

        //Pass version value
        attributes.setVersion(CEDARDataCiteInstance.getVersion().toString());

        //Pass rights values
        attributes.setRightsList(parseRightsValue(CEDARDataCiteInstance.getRightsElement().getRightsList()));

        //Pass description values
        attributes.setDescriptions(parseDescriptionValue(CEDARDataCiteInstance.getDescriptionElement().getDescriptions()));

        //Pass geoLocation values
        attributes.setGeoLocations(parseGeoLocationValue(CEDARDataCiteInstance.getGeoLocationElement().getGeoLocations()));

        //Pass fundingReference values
        attributes.setFundingReferences(parseFundingReference(CEDARDataCiteInstance.getFundingReferenceElement().getFundingReferences()));

        //Pass relatedItem values
        attributes.setRelatedItems(parseRelatedItemValue(CEDARDataCiteInstance.getRelatedItemElement().getRelatedItems()));

        data.setAttributes(attributes);
        dataCiteSchema.setData(data);

    }

    private static List<DataCiteAffiliation> parseAffiliationValue(List<Affiliation> affiliationList){
        List<DataCiteAffiliation> dataCiteAffiliationList = new ArrayList<>();
        for (Affiliation a : affiliationList) {
            DataCiteAffiliation dataCiteAffiliation = new DataCiteAffiliation();
            // Retrieve values from CEDAR class
            String affiliationName = a.getAffiliation().toString();
            String affiliationIdentifier = a.getAffiliationIdentifier().toString();
            String affiliationIdentifierScheme = a.getAffiliationIdentifierScheme().toString();
            String affiliationSchemeURI = a.getAffiliationIdentifierSchemeURI().toString();
            // set values to DataCite class
            dataCiteAffiliation.setAffiliation(affiliationName);
            dataCiteAffiliation.setAffiliationIdentifier(affiliationIdentifier);
            dataCiteAffiliation.setAffiliationIdentifierScheme(affiliationIdentifierScheme);
            dataCiteAffiliation.setAffiliationSchemeURI(affiliationSchemeURI);
            dataCiteAffiliationList.add(dataCiteAffiliation);
        }

        return dataCiteAffiliationList;
    }


    private static List<DataCiteNameIdentifier> parseNameIdentifierValue(List<NameIdentifier> nameIdentifierList) {
        List<DataCiteNameIdentifier> dataCiteNameIdentifierList = new ArrayList<>();
        for (NameIdentifier n : nameIdentifierList) {
            DataCiteNameIdentifier dataCiteNameIdentifier = new DataCiteNameIdentifier();
            // Retrieve values from CEDAR class
            String nameIdentifierName = n.getNameIdentifierName().toString();
            String nameIdentifierScheme = n.getNameIdentifierScheme().toString();
            String nameIdentifierSchemeURI = n.getNameIdentifierSchemeURI().toString();
            // set values to DataCite class
            dataCiteNameIdentifier.setNameIdentifier(nameIdentifierName);
            dataCiteNameIdentifier.setNameIdentifierScheme(nameIdentifierScheme);
            dataCiteNameIdentifier.setSchemeURI(nameIdentifierSchemeURI);
            dataCiteNameIdentifierList.add(dataCiteNameIdentifier);
        }

        return dataCiteNameIdentifierList;
    }


    // Parse CreatorElement values
    private static List<DataCiteCreator> parseCreatorValue(List<Creator> CreatorList) {
        List<DataCiteCreator> dataCiteCreatorList = new ArrayList<>();

        //Loop each creator in creator list to get values
        for (Creator c: CreatorList) {
            DataCiteCreator dataCiteCreator = new DataCiteCreator();
            // Retrieve values from CEDAR class
            String creatorName = c.getCreatorName().toString();
            String nameType = c.getNameType().toString();
            String givenName = c.getGivenName().toString();
            String familyName = c.getFamilyName().toString();
            // set values to DataCite class
            dataCiteCreator.setName(creatorName);
            dataCiteCreator.setNameType(nameType);
            dataCiteCreator.setFamilyName(familyName);
            dataCiteCreator.setGivenName(givenName);

            // Set values to corresponding Affiliation list in dataCiteCreator
            List<Affiliation> affiliationList = c.getAffiliations();
            if (affiliationList != null) {
                dataCiteCreator.setAffiliations(parseAffiliationValue(affiliationList));
            }


            // Set values to corresponding Affiliation list in dataCiteCreator
            List<NameIdentifier> nameIdentifierList = c.getNameIdentifiers();
            if (nameIdentifierList != null) {
                dataCiteCreator.setNameIdentifiers(parseNameIdentifierValue(nameIdentifierList));
            }

            // Add dataCiteCreator to dataCiteCreator list
            dataCiteCreatorList.add(dataCiteCreator);
        }

        return dataCiteCreatorList;
    }


    // Parse TitleElement values
    private static List<DataCiteTitle> parseTitleValue(List<Title> titlesList) {
        List<DataCiteTitle> dataCiteTitles = new ArrayList<>();

        for (Title t : titlesList) {
            DataCiteTitle dataCiteTitle = new DataCiteTitle();
            // Retrieve values from CEDAR class
            String titleName = t.getTitleName().toString();
            String titleType = t.getTitleType().toString();

            // Set values to DataCite class
            dataCiteTitle.setTitle(titleName);
            dataCiteTitle.setTitleType(titleType);

            // titles as subProperty under relatedItem property don't have language
            if (t.getLanguage() != null) {
                String language = t.getLanguage().toString();
                dataCiteTitle.setLang(language);
            }

            //Add dataCiteTitle to the list
            dataCiteTitles.add(dataCiteTitle);
        }

        return dataCiteTitles;
    }

    private static Integer parsePublisherYearValue (CEDARDataCiteInstance CEDARDataCiteInstance) {
        PublicationYearElement publicationYearElement = CEDARDataCiteInstance.getPublicationYearElement();
        String publisherYear = publicationYearElement.getPublicationYear().toString(); //"2014-01-01"
        int year = Integer.parseInt(publisherYear);
        return year;
    }

    private static List<DataCiteSubject> parseSubjectValue(List<Subject> subjectList) {
        List<DataCiteSubject> dataCiteSubjects = new ArrayList<>();

        for (Subject s : subjectList){
            DataCiteSubject dataCiteSubject = new DataCiteSubject();
            // Retrieve values from CEDAR class
            String subject = s.getSubjectName().toString();
            String schemeUri = s.getSubjectSchemeURI().getId().toString();
            String subjectScheme = s.getSubjectScheme().toString();
            String classificationCode = s.getClassificationCode().toString();
            String valueUri = s.getValueURI().getId().toString();
            // Set values to DataCite class
            dataCiteSubject.setSubject(subject);
            dataCiteSubject.setSubjectScheme(subjectScheme);
            dataCiteSubject.setSchemeUri(schemeUri);
            dataCiteSubject.setClassificationCode(classificationCode);
            dataCiteSubject.setValueUri(valueUri);

            dataCiteSubjects.add(dataCiteSubject);
        }

        return dataCiteSubjects;
    }

    private static DataCiteType parseTypeValue(ResourceTypeElement resourceTypeElement) {
        DataCiteType dataCiteType = new DataCiteType();
        String type = resourceTypeElement.getResourceType().toString();
        String resourceTypeGeneral = resourceTypeElement.getResourceTypeGeneral().toString();

        dataCiteType.setResourceType(type);
        dataCiteType.setResourceTypeGeneral(resourceTypeGeneral);

        return dataCiteType;
    }

    private static List<DataCiteContributor> parseContributorValue(List<Contributor> contributorList){
        List<DataCiteContributor> dataCiteContributors = new ArrayList<>();

        for (Contributor c : contributorList) {
            DataCiteContributor dataCiteContributor = new DataCiteContributor();
            // Retrieve values from CEDAR class
            String name = c.getContributorName().toString();
            String nameType = c.getNameType().toString();
            String givenName = c.getGivenName().toString();
            String familyName = c.getFamilyName().toString();
            String contributorType = c.getContributorType().toString();

            dataCiteContributor.setName(name);
            dataCiteContributor.setNameType(nameType);
            dataCiteContributor.setGivenName(givenName);
            dataCiteContributor.setFamilyName(familyName);
            if (contributorType != "null"){
                dataCiteContributor.setContributorType(contributorType);
            }


            // Set values to corresponding Affiliation list in dataCiteCreator
            List<Affiliation> affiliationList = c.getAffiliations();
            if (affiliationList != null) {
                dataCiteContributor.setAffiliations(parseAffiliationValue(affiliationList));
            }

            // Set values to corresponding Affiliation list in dataCiteCreator
            List<NameIdentifier> nameIdentifierList = c.getNameIdentifiers();
            if (nameIdentifierList != null) {
                dataCiteContributor.setNameIdentifiers(parseNameIdentifierValue(nameIdentifierList));
            }
            // Add dataCiteContributor to the list
            dataCiteContributors.add(dataCiteContributor);
        }

        return dataCiteContributors;
    }

    private static List<DataCiteDate> parseDateValue(List<Date> dateList){
        List<DataCiteDate> dataCiteDates = new ArrayList<>();

        for (Date d : dateList) {
            DataCiteDate dataCiteDate = new DataCiteDate();
            String date = d.getDate().toString();
            String dateType = d.getDateType().toString();
            String dateInformation = d.getDateInformation().toString();
            dataCiteDate.setDate(date);
            dataCiteDate.setDateType(dateType);
            dataCiteDate.setDateInformation(dateInformation);

            dataCiteDates.add(dataCiteDate);
        }

        return dataCiteDates;
    }

    private static List<DataCiteAlternateIdentifier> parseAlternateIdentifier(List<AlternateIdentifier> alternateIdentifierList) {
        List<DataCiteAlternateIdentifier> dataCiteAlternateIdentifiers = new ArrayList<>();

        for (AlternateIdentifier a : alternateIdentifierList) {
            DataCiteAlternateIdentifier dataCiteAlternateIdentifier = new DataCiteAlternateIdentifier();
            String alternateIdentifier = a.getAlternateIdentifier().toString();
            String alternateIdentifierType = a.getAlternateIdentifierType().toString();

            dataCiteAlternateIdentifier.setAlternateIdentifier(alternateIdentifier);
            dataCiteAlternateIdentifier.setAlternateIdentifierType(alternateIdentifierType);

            dataCiteAlternateIdentifiers.add(dataCiteAlternateIdentifier);
        }

        return dataCiteAlternateIdentifiers;
    }

    private static List<DataCiteRelatedIdentifier> parseRelatedIdentifier(List<RelatedIdentifier> relatedIdentifierList){
        List<DataCiteRelatedIdentifier> dataCiteRelatedIdentifiers = new ArrayList<>();

        for (RelatedIdentifier r : relatedIdentifierList) {
            DataCiteRelatedIdentifier dataCiteRelatedIdentifier = new DataCiteRelatedIdentifier();
            String relatedIdentifier = r.getRelatedIdentifier().toString();
            String relatedIdentifierType = r.getRelatedIdentifierType().toString();
            String relationType = r.getRelationType().toString();
            String relatedMetaDataScheme = r.getRelatedMetadataScheme().toString();
            String schemeURI = r.getSchemeURi().toString();
            String schemeType = r.getSchemeType().toString();
            String resourceTypeGeneral = r.getResourceTypeGeneral().toString();

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


    private static List<String> parseSizeValue(List<ValueFormat> sizeList) {
        List<String> dataCiteSizes = new ArrayList<>();

        for (ValueFormat s : sizeList) {
            String dataCiteSize = s.getValue().toString();
            dataCiteSizes.add(dataCiteSize);
        }
        return dataCiteSizes;
    }

    private static List<String> parseFormatValue(List<ValueFormat> formatList){
        List<String> dataCiteFormats = new ArrayList<>();

        for (ValueFormat f : formatList) {
            String dataCiteFormat = f.getValue().toString();
            dataCiteFormats.add(dataCiteFormat);
        }
        return dataCiteFormats;
    }

    private static List<DataCiteRights> parseRightsValue(List<Rights> rightsList){
        List<DataCiteRights> dataCiteRightsList = new ArrayList<>();

        for(Rights r : rightsList){
            DataCiteRights dataCiteRights = new DataCiteRights();
            String rights = r.getRights().toString();
            String rightsURI = r.getRightsURI().toString();
            String schemeURI = r.getSchemeURI().toString();
            String rightsIdentifier = r.getRightsIdentifier().toString();
            String rightsIdentifierScheme = r.getRightsIdentifierScheme().toString();

            dataCiteRights.setRights(rights);
            dataCiteRights.setRightsUri(rightsURI);
            dataCiteRights.setSchemeUri(schemeURI);
            dataCiteRights.setRightsIdentifier(rightsIdentifier);
            dataCiteRights.setRightsIdentifierScheme(rightsIdentifierScheme);

            dataCiteRightsList.add(dataCiteRights);
        }

        return dataCiteRightsList;
    }

    private static List<DataCiteDescription> parseDescriptionValue(List<Description> descriptionList){
        List<DataCiteDescription> dataCiteDescriptions = new ArrayList<>();

        for(Description d : descriptionList) {
            DataCiteDescription dataCiteDescription = new DataCiteDescription();
            String description = d.getDescription().toString();
            String descriptionTYpe = d.getDescriptionType().toString();
            String lang = d.getLanguage().toString();

            dataCiteDescription.setDescription(description);
            dataCiteDescription.setDescriptionType(descriptionTYpe);
            dataCiteDescription.setLang(lang);

            dataCiteDescriptions.add(dataCiteDescription);
        }

        return dataCiteDescriptions;
    }

    private static List<DataCiteGeoLocation> parseGeoLocationValue(List<GeoLocation> geoLocationList){
        List<DataCiteGeoLocation> dataCiteGeoLocations = new ArrayList<>();

        for(GeoLocation g : geoLocationList){
            DataCiteGeoLocation dataCiteGeoLocation = new DataCiteGeoLocation();
            String geoLocationPlace = g.getGeoLocationPlace().toString();
            dataCiteGeoLocation.setGeoLocationPlace(geoLocationPlace);

            // parse value in geoLocationBox
            DataCiteGeoLocationBox dataCiteGeoLocationBox = new DataCiteGeoLocationBox();
            float eastBoundLongitude = g.getGeoLocationBox().getEastBoundLongitude().getValue();
            float westBoundLongitude = g.getGeoLocationBox().getWestBoundLongitude().getValue();
            float southBoundLatitude = g.getGeoLocationBox().getSouthBoundLatitude().getValue();
            float northBoundLatitude = g.getGeoLocationBox().getNorthBoundLatitude().getValue();
            dataCiteGeoLocationBox.setEastBoundLongitude(eastBoundLongitude);
            dataCiteGeoLocationBox.setWestBoundLongitude(westBoundLongitude);
            dataCiteGeoLocationBox.setSouthBoundLatitude(southBoundLatitude);
            dataCiteGeoLocationBox.setNorthBoundLatitude(northBoundLatitude);
            dataCiteGeoLocation.setGeoLocationBox(dataCiteGeoLocationBox);

            //parse value in geoLocationPolygens
            List<GeoLocationPolygen> geoLocationPolygenList = g.getGeoLocationPolygenList();
            List<DataCiteGeoLocationPolygen> dataCiteGeoLocationPolygens = new ArrayList<>();
            for (GeoLocationPolygen glp: geoLocationPolygenList){
                DataCiteGeoLocationPolygen dataCiteGeoLocationPolygen = new DataCiteGeoLocationPolygen();
                float polygonPointLongitude = glp.getPolygenPoint().getPointLongitude().getValue();
                float polygonPointLatitude = glp.getPolygenPoint().getPointLatitude().getValue();
                float inPolygonPointLongitude = glp.getInPolygenPoint().getPointLongitude().getValue();
                float inPolygonPointLatitude = glp.getInPolygenPoint().getPointLatitude().getValue();

                // set polygenPoint attributes
                DataCiteGeoLocationPoint polygenPoint = new DataCiteGeoLocationPoint();
                polygenPoint.setPointLongitude(polygonPointLongitude);
                polygenPoint.setPointLatitude(polygonPointLatitude);
                dataCiteGeoLocationPolygen.setPolygonPoint(polygenPoint);

                // set inPolygenPoint attributes
                DataCiteGeoLocationPoint inPolygenPoint = new DataCiteGeoLocationPoint();
                inPolygenPoint.setPointLongitude(inPolygonPointLongitude);
                inPolygenPoint.setPointLatitude(inPolygonPointLatitude);
                dataCiteGeoLocationPolygen.setInPolygenPoint(inPolygenPoint);

                dataCiteGeoLocationPolygens.add(dataCiteGeoLocationPolygen);
            }
            dataCiteGeoLocation.setGeoLocationPolygenList(dataCiteGeoLocationPolygens);

            //add dataCiteGeoLocation to the list
            dataCiteGeoLocations.add(dataCiteGeoLocation);
        }

        return dataCiteGeoLocations;
    }

    private static List<DataCiteFundingReference> parseFundingReference(List<FundingReference> fundingReferenceList){
        List<DataCiteFundingReference> dataCiteFundingReferences = new ArrayList<>();

        for(FundingReference f : fundingReferenceList) {
            DataCiteFundingReference dataCiteFundingReference = new DataCiteFundingReference();
            String funderName = f.getFunderName().toString();
            String funderIdentifier = f.getFunderIdentifier().getFunderIdentifier().toString();
            String funderIdentifierType = f.getFunderIdentifier().getFunderIdentifierType().toString();
            String funderIdentifierSchemeURI = f.getFunderIdentifier().getSchemeURI().toString();
            String awardNumber = f.getAwardNumber().getAwardNumber().toString();
            String awardURI = f.getAwardNumber().getAwardURI().toString();
            String awardTitle = f.getAwardTitle().toString();

            dataCiteFundingReference.setFunderName(funderName);
            dataCiteFundingReference.setFunderIdentifier(funderIdentifier);
            dataCiteFundingReference.setFunderIdentifierType(funderIdentifierType);
            dataCiteFundingReference.setSchemeUri(funderIdentifierSchemeURI);
            dataCiteFundingReference.setAwardNumber(awardNumber);
            dataCiteFundingReference.setAwardUri(awardURI);
            dataCiteFundingReference.setAwardTitle(awardTitle);

            dataCiteFundingReferences.add(dataCiteFundingReference);
        }

        return dataCiteFundingReferences;
    }

    private static List<DataCiteRelatedItem> parseRelatedItemValue(List<RelatedItem> relatedItemList){
        List<DataCiteRelatedItem> dataCiteRelatedItems = new ArrayList<>();

        for(RelatedItem r: relatedItemList){
            DataCiteRelatedItem dataCiteRelatedItem = new DataCiteRelatedItem();
            String relatedItemType = r.getRelatedItemType().toString();
            String relationType = r.getRelationType().toString();
            String volume = r.getVolume().toString();
            String issue = r.getIssue().toString();
            String firstPage = r.getFirstPage().toString();
            String lastPage = r.getLastPage().toString();
            String publicationYear = r.getPublicationYear().toString();
            String publisher = r.getPublisher().toString();
            String edition = r.getEdition().toString();

            dataCiteRelatedItem.setRelatedItemType(relatedItemType);
            dataCiteRelatedItem.setRelationType(relationType);
            dataCiteRelatedItem.setVolume(volume);
            dataCiteRelatedItem.setIssue(issue);
            dataCiteRelatedItem.setFirstPage(firstPage);
            dataCiteRelatedItem.setLastPage(lastPage);
            dataCiteRelatedItem.setPublicationYear(publicationYear);
            dataCiteRelatedItem.setPublisher(publisher);
            dataCiteRelatedItem.setEdition(edition);

            // parse relatedItemIdentifier values
            DataCiteRelatedItemIdentifier dataCiteRelatedItemIdentifier = new DataCiteRelatedItemIdentifier();
            String relatedIdentifier = r.getRelatedItemIdentifiers().getRelatedIdentifier().toString();
            String relatedItemIdentifierType = r.getRelatedItemIdentifiers().getRelatedIdentifierType().toString();
            String relatedMedaDataScheme = r.getRelatedItemIdentifiers().getRelatedMetadataScheme().toString();
            String schemeUri = r.getRelatedItemIdentifiers().getSchemeURi().toString();
            String schemeType = r.getRelatedItemIdentifiers().getSchemeType().toString();

            dataCiteRelatedItemIdentifier.setRelatedIdentifier(relatedIdentifier);
            dataCiteRelatedItemIdentifier.setRelatedItemIdentifierType(relatedItemIdentifierType);
            dataCiteRelatedItemIdentifier.setRelatedMetadataScheme(relatedMedaDataScheme);
            dataCiteRelatedItemIdentifier.setSchemeUri(schemeUri);
            dataCiteRelatedItemIdentifier.setSchemeType(schemeType);

            dataCiteRelatedItem.setRelatedItemIdentifier(dataCiteRelatedItemIdentifier);

            //parse creators values
            dataCiteRelatedItem.setCreators(parseCreatorValue(r.getCreators()));

            // parse titles values
            dataCiteRelatedItem.setTitles(parseTitleValue(r.getTitles()));

            // parse number values
            DataCiteNumber dataCiteNumber = new DataCiteNumber();
            String number = r.getNumber().getNumber().toString();
            String numberType = r.getNumber().getNumberType().toString();
            dataCiteNumber.setNumber(number);
            dataCiteNumber.setNumberType(numberType);
            dataCiteRelatedItem.setNumber(dataCiteNumber);

            //parse contributors values
            List<Contributor> contributorList = r.getContributors();
            List<DataCiteRelatedItemContributor> dataCiteRelatedItemContributors = new ArrayList<>();
            for (Contributor c : contributorList) {
                DataCiteRelatedItemContributor dataCiteRelatedItemContributor = new DataCiteRelatedItemContributor();
                // Retrieve values from CEDAR class
                String name = c.getContributorName().toString();
                String nameType = c.getNameType().toString();
                String givenName = c.getGivenName().toString();
                String familyName = c.getFamilyName().toString();
                String contributorType = c.getContributorType().toString();

                dataCiteRelatedItemContributor.setName(name);
                dataCiteRelatedItemContributor.setNameType(nameType);
                dataCiteRelatedItemContributor.setGivenName(givenName);
                dataCiteRelatedItemContributor.setFamilyName(familyName);
                dataCiteRelatedItemContributor.setContributorType(contributorType);

                dataCiteRelatedItemContributors.add(dataCiteRelatedItemContributor);
            }
            dataCiteRelatedItem.setContributors(dataCiteRelatedItemContributors);

            dataCiteRelatedItems.add(dataCiteRelatedItem);
        }

        return dataCiteRelatedItems;
    }
}
