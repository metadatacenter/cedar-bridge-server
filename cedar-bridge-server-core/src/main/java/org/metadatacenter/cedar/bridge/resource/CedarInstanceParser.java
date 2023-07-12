package org.metadatacenter.cedar.bridge.resource;

import org.metadatacenter.cedar.bridge.resource.CEDARProperties.*;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.*;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.id.CedarArtifactId;
import org.metadatacenter.id.CedarFQResourceId;
import org.metadatacenter.model.CedarResourceType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class CedarInstanceParser {
    public static void parseCEDARInstance(CEDARDataCiteInstance cedarDataCiteInstance, DataCiteSchema dataCiteSchema, String sourceArtifactId) throws DataCiteInstanceValidationException{
        Data data = new Data();
        Attributes attributes = new Attributes();

        // Set type to "dois"
        data.setType("dois");

        //Pass prefix value from CEDAR class to DataCite class
        if (cedarDataCiteInstance.getPrefix() == null){
            throw new DataCiteInstanceValidationException("The 'Prefix of DOI' is required, please provide your valid prefix");
        } else if (!cedarDataCiteInstance.getPrefix().getValue().equals("10.82658")){
            throw new DataCiteInstanceValidationException("The 'Prefix of DOI' is incorrect, please provide your valid prefix");
        } else{
            attributes.setPrefix(cedarDataCiteInstance.getPrefix().getValue());
        }

        // set event value
        attributes.setEvent("publish");

        // Set url and schemeVersion
        // https://openview.metadatacenter.org/templates/  or  https://openview.metadatacenter.org/template-instances/
        CedarResourceType cedarResourceType = CedarFQResourceId.build(sourceArtifactId).getType();
        String encodedSourceArtifactId = URLEncoder.encode(sourceArtifactId, StandardCharsets.UTF_8);
        switch (cedarResourceType){
            case TEMPLATE:
                attributes.setUrl("https://openview.metadatacenter.org/templates/" + encodedSourceArtifactId);
            case INSTANCE:
                attributes.setUrl("https://openview.metadatacenter.org/template-instances/" + encodedSourceArtifactId);
        }

        attributes.setSchemaVersion("http://datacite.org/schema/kernel-4");

        // Pass creator values from CEDAR class to DataCite class
        List<Creator> creatorList = cedarDataCiteInstance.getCreators();
        if (!creatorList.isEmpty()) {
            attributes.setCreators(parseCreatorValue(creatorList, "Creators"));
        } else{
            throw new DataCiteInstanceValidationException("The 'Creators' is required, please provide 'Creators' information");
        }

        //Pass titles values from CEDAR class to DataCite class
        List<Title> titlesList = cedarDataCiteInstance.getTitles();
        if (!titlesList.isEmpty()) {
            attributes.setTitles(parseTitleValue(titlesList, "Titles"));
        } else{
            throw new DataCiteInstanceValidationException("The 'Titles' is required, please provide 'Titles' information");
        }

        //Pass publisher values from CEDAR class to DataCite class
        String publisher = cedarDataCiteInstance.getPublisher().getValue();
        if (publisher != null && !publisher.isEmpty()) {
            attributes.setPublisher(publisher);
        } else{
            throw new DataCiteInstanceValidationException("The 'Publisher' is required, please provide 'Publisher' information");
        }

        //Pass publisherYear values
        String publicationYear = cedarDataCiteInstance.getPublicationYear().toString();
        String currentYear = String.valueOf(Year.now().getValue());
        if (publicationYear == null){
            throw new DataCiteInstanceValidationException("The 'Publication Year' is required, please provide current year: " + currentYear);
        } else if (!publicationYear.equals(currentYear)){
            throw new DataCiteInstanceValidationException("The 'Publication Year' should be the current year: " + currentYear);
        } else{
            attributes.setPublicationYear(parsePublicationYearValue(publicationYear));
        }

        //Pass subjects values
        List<Subject> subjectList = cedarDataCiteInstance.getSubjects();
        if (!(CheckEmptyList.emptySubjectList(subjectList))) {
            attributes.setSubjects(parseSubjectValue(subjectList));
        }

        // Pass resourceType values
        attributes.setTypes(parseTypeValue(cedarResourceType.getValue()));

        //Pass contributors values
        List<Contributor> contributorList = cedarDataCiteInstance.getContributors();
        if (!CheckEmptyList.emptyContributorList(contributorList)){
            attributes.setContributors(parseContributorValue(contributorList));
        }

        //Pass dates values
        List<Date> dateList = cedarDataCiteInstance.getDates();
        if (!CheckEmptyList.emptyDateList(dateList)){
            attributes.setDates(parseDateValue(dateList));
        }

        //Pass Language value
        attributes.setLanguage(cedarDataCiteInstance.getLanguage().toString());

        //Pass alternateIdentifier values
        List<AlternateIdentifier> alternateIdentifierList = cedarDataCiteInstance.getAlternateIdentifiers();
        if (!CheckEmptyList.emptyAlternateIdentifierList(alternateIdentifierList)){
            attributes.setAlternateIdentifiers(parseAlternateIdentifier(alternateIdentifierList));
        }

        //Pass relatedIdentifier values
        List<RelatedIdentifier> relatedIdentifierList = cedarDataCiteInstance.getRelatedIdentifiers();
        if (!CheckEmptyList.emptyRelatedIdentifierList(relatedIdentifierList)){
            attributes.setRelatedIdentifiers(parseRelatedIdentifier(relatedIdentifierList));
        }

        //Pass size values
        List<ValueFormat> sizeList = cedarDataCiteInstance.getSizes();
        if (sizeList.size()>0 && sizeList.get(0).getValue()!=null){
            attributes.setSizes(parseSizeValue(sizeList));
        }

        //Pass format values
        List<ValueFormat> formatList = cedarDataCiteInstance.getFormats();
        if (formatList.size()>0 && formatList.get(0).getValue()!= null){
            attributes.setFormats(parseFormatValue(formatList));
        }

        //Pass version value
        attributes.setVersion(cedarDataCiteInstance.getVersion().toString());

        //Pass rights values
        List<Rights> rightsList = cedarDataCiteInstance.getRightsList();
        if (!CheckEmptyList.emptyRightsList(rightsList)){
            attributes.setRightsList(parseRightsValue(rightsList));
        }

        //Pass description values
        List<Description> descriptionList = cedarDataCiteInstance.getDescriptions();
        if (!CheckEmptyList.emptyDescriptionList(descriptionList)){
            attributes.setDescriptions(parseDescriptionValue(descriptionList));
        }

        //Pass geoLocation values
        List<GeoLocation> geoLocationList = cedarDataCiteInstance.getGeoLocations();
        if (!CheckEmptyList.emptyGeoLocationList(geoLocationList)){
            attributes.setGeoLocations(parseGeoLocationValue(geoLocationList));
        }

        //Pass fundingReference values
        List<FundingReference> fundingReferenceList = cedarDataCiteInstance.getFundingReferences();
        if (!CheckEmptyList.emptyFundingReferenceList(fundingReferenceList)){
            attributes.setFundingReferences(parseFundingReference(fundingReferenceList));
        }

        //Pass relatedItem values
        List<RelatedItem> relatedItemList = cedarDataCiteInstance.getRelatedItems();
        if (!CheckEmptyList.emptyRelatedItemList(relatedItemList)){
            attributes.setRelatedItems(parseRelatedItemValue(relatedItemList));
        }

        data.setAttributes(attributes);
        dataCiteSchema.setData(data);
    }

    private static List<DataCiteAffiliation> parseAffiliationValue(List<Affiliation> affiliationList, String element) throws DataCiteInstanceValidationException {
        List<DataCiteAffiliation> dataCiteAffiliationList = new ArrayList<>();
        if (!affiliationList.isEmpty()){
            for (Affiliation a : affiliationList) {
                DataCiteAffiliation dataCiteAffiliation = new DataCiteAffiliation();
                // Retrieve values from CEDAR class
                String affiliationIdentifier = a.getAffiliationIdentifier().toString();
                String affiliationIdentifierScheme = a.getAffiliationIdentifierScheme().toString();
                if (affiliationIdentifierScheme == null || affiliationIdentifierScheme.equals("")){
                    throw new DataCiteInstanceValidationException("If the 'Affiliation Identifier' of '" + element + "' is used, 'Affiliation Identifier Scheme' is mandatory");
                }
                String affiliationSchemeURI = a.getAffiliationIdentifierSchemeURI().toString();
                // set values to DataCite class
                dataCiteAffiliation.setAffiliationIdentifier(affiliationIdentifier);
                dataCiteAffiliation.setAffiliationIdentifierScheme(affiliationIdentifierScheme);
                dataCiteAffiliation.setAffiliationSchemeURI(affiliationSchemeURI);
                dataCiteAffiliationList.add(dataCiteAffiliation);
            }
        }
        return dataCiteAffiliationList;
    }


    private static List<DataCiteNameIdentifier> parseNameIdentifierValue(List<NameIdentifier> nameIdentifierList, String element) throws DataCiteInstanceValidationException{
        List<DataCiteNameIdentifier> dataCiteNameIdentifierList = new ArrayList<>();
        if (!nameIdentifierList.isEmpty()) {
            for (NameIdentifier n : nameIdentifierList) {
                DataCiteNameIdentifier dataCiteNameIdentifier = new DataCiteNameIdentifier();
                // Retrieve values from CEDAR class
                String nameIdentifierName = n.getNameIdentifierName().toString();
                String nameIdentifierScheme = n.getNameIdentifierScheme().toString();
                if (nameIdentifierScheme == null || nameIdentifierScheme.equals("")){
                    throw new DataCiteInstanceValidationException("If the 'Name Identifier' of '" + element + "' is used, 'Name Identifier Scheme' is mandatory");
                }
                String nameIdentifierSchemeUri = n.getNameIdentifierSchemeURI().toString();
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
    private static List<DataCiteCreator> parseCreatorValue(List<Creator> creatorList, String element) throws DataCiteInstanceValidationException {
        List<DataCiteCreator> dataCiteCreatorList = new ArrayList<>();

        //Loop each creator in creator list to get values
        for (Creator c: creatorList) {
            DataCiteCreator dataCiteCreator = new DataCiteCreator();
            // Retrieve values from CEDAR class
            String creatorName = c.getCreatorName().toString();
            if (creatorName == null || creatorName.equals("")){
                switch (element){
                    case "Creators":throw new DataCiteInstanceValidationException("The 'Creator Name' property of 'Creators' is required, please provide the information");
                    case "RelatedItems": throw new DataCiteInstanceValidationException("If 'Creators' of 'Related Items' is used, 'Creator Name' is mandatory");
                }

            }
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
            if (affiliationList != null && !affiliationList.isEmpty() && !CheckEmptyList.emptyAffiliationList(affiliationList)) {
                dataCiteCreator.setAffiliations(parseAffiliationValue(affiliationList, "Creators"));
            }

            // Set values to corresponding Affiliation list in dataCiteCreator
            List<NameIdentifier> nameIdentifierList = c.getNameIdentifiers();
            if (nameIdentifierList != null && !nameIdentifierList.isEmpty() && !CheckEmptyList.emptyNameIdentifierList(nameIdentifierList)) {
                dataCiteCreator.setNameIdentifiers(parseNameIdentifierValue(nameIdentifierList, "Creators"));
            }

            // Add dataCiteCreator to dataCiteCreator list
            dataCiteCreatorList.add(dataCiteCreator);
        }
        return dataCiteCreatorList;
    }

    // Parse TitleElement values
    private static List<DataCiteTitle> parseTitleValue(List<Title> titlesList, String element) throws DataCiteInstanceValidationException {
        List<DataCiteTitle> dataCiteTitles = new ArrayList<>();

        for (Title t : titlesList) {
            DataCiteTitle dataCiteTitle = new DataCiteTitle();
            // Retrieve values from CEDAR class
            String titleName = t.getTitleName().toString();
            if (titleName == null || titleName.equals("")){
                switch (element){
                    case "Titles": throw new DataCiteInstanceValidationException("The 'Title' property of 'Titles' is required, please provide the information");
                    case "RelatedItems": throw new DataCiteInstanceValidationException("If 'Titles' of 'Related Items' is used, 'Title' property is mandatory");
                }

            }
            String titleType = t.getTitleType().toString();

            // Set values to DataCite class
            dataCiteTitle.setTitle(titleName);
            dataCiteTitle.setTitleType(titleType);

            //Add dataCiteTitle to the list
            dataCiteTitles.add(dataCiteTitle);
        }

        return dataCiteTitles;
    }

    private static Integer parsePublicationYearValue(String publicationYear) {
        int year = Integer.parseInt(publicationYear);
        return year;
    }

    private static List<DataCiteSubject> parseSubjectValue(List<Subject> subjectList) {
        List<DataCiteSubject> dataCiteSubjects = new ArrayList<>();

        for (Subject s : subjectList){
            DataCiteSubject dataCiteSubject = new DataCiteSubject();
            // Retrieve values from CEDAR class
            String subject = s.getSubjectName().toString();
            String schemeUri = s.getSubjectSchemeURI().toString();
            String subjectScheme = s.getSubjectScheme().toString();
            String classificationCode = s.getClassificationCode().toString();
            String valueUri = s.getValueURI().toString();
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

    private static DataCiteType parseTypeValue(String resourceType) {
        DataCiteType dataCiteType = new DataCiteType();
//        String resourceTypeGeneral = resourceTypeElement.getResourceTypeGeneral().toString();
        String resourceTypeGeneral = "Other";
        dataCiteType.setResourceTypeGeneral(resourceTypeGeneral);
        dataCiteType.setResourceType(resourceType);
        return dataCiteType;
    }

    private static List<DataCiteContributor> parseContributorValue(List<Contributor> contributorList) throws DataCiteInstanceValidationException {
        List<DataCiteContributor> dataCiteContributors = new ArrayList<>();

        for (Contributor c : contributorList) {
            DataCiteContributor dataCiteContributor = new DataCiteContributor();
            // Retrieve values from CEDAR class
            String name = c.getContributorName().toString();
            if (name == null || name.equals("")){
                throw new DataCiteInstanceValidationException("If 'Contributors' is used, then 'Contributor Name' is mandatory");
            }
            String nameType = c.getNameType().toString();
            String givenName = c.getGivenName().toString();
            String familyName = c.getFamilyName().toString();
            String contributorType = c.getContributorType().toString();
            if (contributorType == null || contributorType.equals("")){
                throw new DataCiteInstanceValidationException("If 'Contributors' is used, then 'Contributor Type' is mandatory");
            }

            dataCiteContributor.setName(name);
            dataCiteContributor.setNameType(nameType);
            dataCiteContributor.setGivenName(givenName);
            dataCiteContributor.setFamilyName(familyName);
            if (contributorType != "null"){
                dataCiteContributor.setContributorType(contributorType);
            }

            // Set values to corresponding Affiliation list in dataCiteCreator
            List<Affiliation> affiliationList = c.getAffiliations();
            if (affiliationList != null && !CheckEmptyList.emptyAffiliationList(affiliationList)) {
                dataCiteContributor.setAffiliations(parseAffiliationValue(affiliationList, "Contributors"));
            }

            // Set values to corresponding nameIdentifierList list in dataCiteCreator
            List<NameIdentifier> nameIdentifierList = c.getNameIdentifiers();
            if (nameIdentifierList != null && !CheckEmptyList.emptyNameIdentifierList(nameIdentifierList)) {
                dataCiteContributor.setNameIdentifiers(parseNameIdentifierValue(nameIdentifierList, "Contributors"));
            }
            // Add dataCiteContributor to the list
            dataCiteContributors.add(dataCiteContributor);
        }

        return dataCiteContributors;
    }

    private static List<DataCiteDate> parseDateValue(List<Date> dateList) throws DataCiteInstanceValidationException {
        List<DataCiteDate> dataCiteDates = new ArrayList<>();

        for (Date d : dateList) {
            DataCiteDate dataCiteDate = new DataCiteDate();
            String date = d.getDate().toString();
            String dateType = d.getDateType().toString();
            if (dateType == null || dateType.equals("")){
                throw new DataCiteInstanceValidationException("If 'Dates' is used, 'Date Type' is mandatory.");
            }
            String dateInformation = d.getDateInformation().toString();
            dataCiteDate.setDate(date);
            dataCiteDate.setDateType(dateType);
            dataCiteDate.setDateInformation(dateInformation);

            dataCiteDates.add(dataCiteDate);
        }

        return dataCiteDates;
    }

    private static List<DataCiteAlternateIdentifier> parseAlternateIdentifier(List<AlternateIdentifier> alternateIdentifierList) throws DataCiteInstanceValidationException {
        List<DataCiteAlternateIdentifier> dataCiteAlternateIdentifiers = new ArrayList<>();

        for (AlternateIdentifier a : alternateIdentifierList) {
            DataCiteAlternateIdentifier dataCiteAlternateIdentifier = new DataCiteAlternateIdentifier();
            String alternateIdentifier = a.getAlternateIdentifier().toString();
            String alternateIdentifierType = a.getAlternateIdentifierType().toString();
            if (alternateIdentifierType == null || alternateIdentifierType.equals("")){
                throw new DataCiteInstanceValidationException("If 'Alternate Identifiers' is used, 'Alternate Identifier Type' is mandatory.");
            }

            dataCiteAlternateIdentifier.setAlternateIdentifier(alternateIdentifier);
            dataCiteAlternateIdentifier.setAlternateIdentifierType(alternateIdentifierType);

            dataCiteAlternateIdentifiers.add(dataCiteAlternateIdentifier);
        }

        return dataCiteAlternateIdentifiers;
    }

    private static List<DataCiteRelatedIdentifier> parseRelatedIdentifier(List<RelatedIdentifier> relatedIdentifierList) throws DataCiteInstanceValidationException {
        List<DataCiteRelatedIdentifier> dataCiteRelatedIdentifiers = new ArrayList<>();

        for (RelatedIdentifier r : relatedIdentifierList) {
            DataCiteRelatedIdentifier dataCiteRelatedIdentifier = new DataCiteRelatedIdentifier();
            String relatedIdentifier = r.getRelatedIdentifier().toString();
            String relatedIdentifierType = r.getRelatedIdentifierType().toString();
            if (relatedIdentifierType == null || relatedIdentifierType.equals("")){
                throw new DataCiteInstanceValidationException("If 'Related Identifiers' is used, 'Related Identifier Type' is mandatory.");
            }
            String relationType = r.getRelationType().toString();
            if (relationType == null || relationType.equals("")){
                throw new DataCiteInstanceValidationException("If 'Related Identifiers' is used, 'Relation Type' is mandatory.");
            }
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
            String dataCiteSize = s.getValue();
            dataCiteSizes.add(dataCiteSize);
        }
        return dataCiteSizes;
    }

    private static List<String> parseFormatValue(List<ValueFormat> formatList){
        List<String> dataCiteFormats = new ArrayList<>();
        for (ValueFormat f : formatList) {
            String dataCiteFormat = f.getValue();
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

    private static List<DataCiteDescription> parseDescriptionValue(List<Description> descriptionList) throws DataCiteInstanceValidationException {
        List<DataCiteDescription> dataCiteDescriptions = new ArrayList<>();

        for(Description d : descriptionList) {
            DataCiteDescription dataCiteDescription = new DataCiteDescription();
            String description = d.getDescription().toString();
            String descriptionType = d.getDescriptionType().toString();
            if (descriptionType == null || descriptionType.equals("")){
                throw new DataCiteInstanceValidationException("If 'Descriptions' is used, 'Description Type' is mandatory.");
            }
            dataCiteDescription.setDescription(description);
            dataCiteDescription.setDescriptionType(descriptionType);

            dataCiteDescriptions.add(dataCiteDescription);
        }

        return dataCiteDescriptions;
    }

    private static List<DataCiteGeoLocation> parseGeoLocationValue(List<GeoLocation> geoLocationList){
        List<DataCiteGeoLocation> dataCiteGeoLocations = new ArrayList<>();

        for(GeoLocation g : geoLocationList){
            DataCiteGeoLocation dataCiteGeoLocation = new DataCiteGeoLocation();
            // parse geoLocationPlace
            String geoLocationPlace = g.getGeoLocationPlace().toString();
            dataCiteGeoLocation.setGeoLocationPlace(geoLocationPlace);
            // parse geoLocationPoint
            DataCiteGeoLocationPoint point = new DataCiteGeoLocationPoint();
            Float pointLongitude = g.getGeoLocationPoint().getPointLongitude().getValue();
            Float pointLatitude = g.getGeoLocationPoint().getPointLatitude().getValue();
            if (pointLongitude != null || pointLatitude != null){
                point.setPointLongitude(pointLongitude);
                point.setPointLatitude(pointLatitude);
                dataCiteGeoLocation.setGeoLocationPoint(point);
            }

            // parse value in geoLocationBox
            DataCiteGeoLocationBox dataCiteGeoLocationBox = new DataCiteGeoLocationBox();
            Float eastBoundLongitude = g.getGeoLocationBox().getEastBoundLongitude().getValue();
            Float westBoundLongitude = g.getGeoLocationBox().getWestBoundLongitude().getValue();
            Float southBoundLatitude = g.getGeoLocationBox().getSouthBoundLatitude().getValue();
            Float northBoundLatitude = g.getGeoLocationBox().getNorthBoundLatitude().getValue();

            if (eastBoundLongitude != null || westBoundLongitude != null || southBoundLatitude != null || northBoundLatitude != null){
                dataCiteGeoLocationBox.setEastBoundLongitude(eastBoundLongitude);
                dataCiteGeoLocationBox.setWestBoundLongitude(westBoundLongitude);
                dataCiteGeoLocationBox.setSouthBoundLatitude(southBoundLatitude);
                dataCiteGeoLocationBox.setNorthBoundLatitude(northBoundLatitude);
                dataCiteGeoLocation.setGeoLocationBox(dataCiteGeoLocationBox);
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

    private static List<DataCiteFundingReference> parseFundingReference(List<FundingReference> fundingReferenceList) throws DataCiteInstanceValidationException {
        List<DataCiteFundingReference> dataCiteFundingReferences = new ArrayList<>();

        for(FundingReference f : fundingReferenceList) {
            DataCiteFundingReference dataCiteFundingReference = new DataCiteFundingReference();
            String funderName = f.getFunderName().toString();
            if (funderName == null || funderName.equals("")){
                throw new DataCiteInstanceValidationException("If 'Funding References' is used, 'Funder Name' is mandatory.");
            }
            String funderIdentifier = f.getFunderIdentifier().toString();
            String funderIdentifierType = f.getFunderIdentifierType().toString();
            String funderIdentifierSchemeURI = f.getSchemeURI().toString();
            String awardNumber = f.getAwardNumber().toString();
            String awardUri = f.getAwardURI().toString();
            String awardTitle = f.getAwardTitle().toString();

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

    private static List<DataCiteRelatedItem> parseRelatedItemValue(List<RelatedItem> relatedItemList) throws DataCiteInstanceValidationException {
        List<DataCiteRelatedItem> dataCiteRelatedItems = new ArrayList<>();

        for(RelatedItem r: relatedItemList){
            DataCiteRelatedItem dataCiteRelatedItem = new DataCiteRelatedItem();
            String relatedItemType = r.getRelatedItemType().toString();
            if (relatedItemType == null || relatedItemType.equals("")){
                throw new DataCiteInstanceValidationException("If 'Related Items' is used, 'Related Item Type' is mandatory");
            }
            String relationType = r.getRelationType().toString();
            if (relationType == null || relationType.equals("")){
                throw new DataCiteInstanceValidationException("If 'Related Items' is used, 'Relation Type' is mandatory");
            }
            String volume = r.getVolume().toString();
            String issue = r.getIssue().toString();
            String firstPage = r.getFirstPage().toString();
            String lastPage = r.getLastPage().toString();
            String publicationYear = r.getPublicationYear().toString();
            String publisher = r.getPublisher().toString();
            String edition = r.getEdition().toString();
            String number = r.getNumber().toString();
            String numberType = r.getNumberType().toString();

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
            String relatedIdentifier = r.getRelatedIdentifier().toString();
            String relatedItemIdentifierType = r.getRelatedIdentifierType().toString();
            String relatedMedaDataScheme = r.getRelatedMetadataScheme().toString();
            String schemeUri = r.getSchemeURi().toString();
            String schemeType = r.getSchemeType().toString();

            dataCiteRelatedItemIdentifier.setRelatedItemIdentifier(relatedIdentifier);
            dataCiteRelatedItemIdentifier.setRelatedItemIdentifierType(relatedItemIdentifierType);
            dataCiteRelatedItemIdentifier.setRelatedMetadataScheme(relatedMedaDataScheme);
            dataCiteRelatedItemIdentifier.setSchemeUri(schemeUri);
            dataCiteRelatedItemIdentifier.setSchemeType(schemeType);

            dataCiteRelatedItem.setRelatedItemIdentifier(dataCiteRelatedItemIdentifier);

            //parse creators values
            if (r.getCreators() != null && !r.getCreators().isEmpty() && !CheckEmptyList.emptyRelatedItemCreatorList(r.getCreators())){
                dataCiteRelatedItem.setCreators(parseCreatorValue(r.getCreators(), "RelatedItems"));
            }

            // parse titles values
            if (r.getTitles() != null && !r.getTitles().isEmpty() && !CheckEmptyList.emptyRelatedItemTitleList(r.getTitles())){
                dataCiteRelatedItem.setTitles(parseTitleValue(r.getTitles(), "RelatedItems"));
            } else {
                throw new DataCiteInstanceValidationException("If 'Related Items' is used, 'Title' is mandatory.");
            }

            //parse contributors values
            List<Contributor> contributorList = r.getContributors();
            if (contributorList != null && !contributorList.isEmpty() && !CheckEmptyList.emptyRelatedItemContributorList(r.getContributors())){
                List<DataCiteRelatedItemContributor> dataCiteRelatedItemContributors = new ArrayList<>();
                for (Contributor c : contributorList) {
                    DataCiteRelatedItemContributor dataCiteRelatedItemContributor = new DataCiteRelatedItemContributor();
                    // Retrieve values from CEDAR class
                    String name = c.getContributorName().toString();
                    if (name == null || name.equals("")){
                        throw new DataCiteInstanceValidationException("If 'Contributors' of 'Related Items' is used, 'Contributor Name' is mandatory");
                    }
                    String nameType = c.getNameType().toString();
                    String givenName = c.getGivenName().toString();
                    String familyName = c.getFamilyName().toString();
                    String contributorType = c.getContributorType().toString();
                    if (contributorType == null || contributorType.equals("")){
                        throw new DataCiteInstanceValidationException("If 'Contributors' of 'Related Items' is used, 'Contributor Type' is mandatory");
                    }

                    dataCiteRelatedItemContributor.setName(name);
                    dataCiteRelatedItemContributor.setNameType(nameType);
                    dataCiteRelatedItemContributor.setGivenName(givenName);
                    dataCiteRelatedItemContributor.setFamilyName(familyName);
                    dataCiteRelatedItemContributor.setContributorType(contributorType);

                    dataCiteRelatedItemContributors.add(dataCiteRelatedItemContributor);
                }
                dataCiteRelatedItem.setContributors(dataCiteRelatedItemContributors);
            }

            dataCiteRelatedItems.add(dataCiteRelatedItem);
        }

        return dataCiteRelatedItems;
    }
}
