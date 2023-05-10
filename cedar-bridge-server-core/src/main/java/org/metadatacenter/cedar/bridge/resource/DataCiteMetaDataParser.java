package org.metadatacenter.cedar.bridge.resource;

import org.metadatacenter.cedar.bridge.resource.CEDARProperties.*;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.*;

import java.util.ArrayList;
import java.util.List;

public class DataCiteMetaDataParser {
    public static void parseDataCiteScheme(DataCiteScheme dataCiteScheme, CEDARDataCiteInstance cedarInstance) {
        Attributes dataCiteAttributes = dataCiteScheme.getData().getAttributes();
        //pass creators values
        cedarInstance.setCreatorElement(parseCreatorValue(dataCiteAttributes.getCreators()));

        //pass titles values
        cedarInstance.setTitleElement(parseTitleValue(dataCiteAttributes.getTitles()));


    }

    private static List<Affiliation> parseAffiliationValue (List<DataCiteAffiliation> dataCiteAffiliations){
        List<Affiliation> affiliationList = new ArrayList<>();

        for (DataCiteAffiliation a : dataCiteAffiliations) {
            //initialize corresponding class
            Affiliation affiliation = new Affiliation();
            ValueFormat affiliationName = new ValueFormat(), affiliationIdentifier = new ValueFormat(), affiliationIdentifierScheme = new ValueFormat();
            SchemeURI affiliationIdentifierSchemeURI = new SchemeURI();

            //retrieve values from DataCiteScheme and set it to CEDAR class
            affiliationName.setValue(a.getAffiliation());
            affiliationIdentifier.setValue(a.getAffiliationIdentifier());
            affiliationIdentifierScheme.setValue(a.getAffiliationIdentifierScheme());
            affiliationIdentifierSchemeURI.setId(a.getAffiliationSchemeURI());

            //set CEDAR Affiliation class value
            affiliation.setAffiliation(affiliationName);
            affiliation.setAffiliationIdentifier(affiliationIdentifier);
            affiliation.setAffiliationIdentifierScheme(affiliationIdentifierScheme);
            affiliation.setAffiliationIdentifierSchemeURI(affiliationIdentifierSchemeURI);

            // add affiliation to the list
            affiliationList.add(affiliation);
        }
        return affiliationList;
    }

    private static List<NameIdentifier> parseNameIdentifierValue (List<DataCiteNameIdentifier> dataCiteNameIdentifiers){
        List<NameIdentifier> nameIdentifierList = new ArrayList<>();

        for (DataCiteNameIdentifier ni : dataCiteNameIdentifiers) {
            NameIdentifier nameIdentifier = new NameIdentifier();
            ValueFormat name = new ValueFormat(), nameIdentifierScheme = new ValueFormat();
            SchemeURI nameIdentifierSchemeURI = new SchemeURI();

            name.setValue(ni.getNameIdentifier());
            nameIdentifierScheme.setValue(ni.getNameIdentifierScheme());
            nameIdentifierSchemeURI.setId(ni.getSchemeURI());

            nameIdentifier.setNameIdentifierName(name);
            nameIdentifier.setNameIdentifierScheme(nameIdentifierScheme);
            nameIdentifier.setNameIdentifierSchemeURI(nameIdentifierSchemeURI);

            nameIdentifierList.add(nameIdentifier);
        }
        return nameIdentifierList;
    }

    private static CreatorElement parseCreatorValue(List<DataCiteCreator> dataCiteCreators)    {
        CreatorElement creatorElement = new CreatorElement();
        List<Creator> creatorList = new ArrayList<>();

        for (DataCiteCreator c : dataCiteCreators) {
            Creator creator = new Creator();
            // retrieve values from DataCiteScheme
            ValueFormat name = new ValueFormat(), givenName = new ValueFormat(), familyName = new ValueFormat();
            IdFormat nameType = new IdFormat();
            name.setValue(c.getName());
            nameType.setLabel(c.getNameType());
            givenName.setValue(c.getGivenName());
            familyName .setValue(c.getFamilyName());

            //set values to CEDARInstance
            creator.setCreatorName(name);
            creator.setNameType(nameType);
            creator.setGivenName(givenName);
            creator.setFamilyName(familyName);

            //set affiliation list values
            if (c.getAffiliations() != null) {
                creator.setAffiliations(parseAffiliationValue(c.getAffiliations()));
            }

            //set nameIdentifier list values
            if (c.getNameIdentifiers() != null) {
                creator.setNameIdentifiers(parseNameIdentifierValue(c.getNameIdentifiers()));
            }

            creatorList.add(creator);
        }
        // set creatorElement value
        creatorElement.setCreators(creatorList);
        return creatorElement;
    }

    private static TitleElement parseTitleValue(List<DataCiteTitle> dataCiteTitles) {
        TitleElement titleElement = new TitleElement();
        List<Title> titleList = new ArrayList<>();

        for (DataCiteTitle t : dataCiteTitles) {
            Title title = new Title();
            ValueFormat titleName = new ValueFormat(), language = new ValueFormat();
            IdFormat titleType = new IdFormat();

            titleName.setValue(t.getTitle());
            titleType.setLabel(t.getTitleType());
            language.setValue(t.getLang());

            title.setTitleName(titleName);
            title.setTitleType(titleType);
            title.setLanguage(language);

            titleList.add(title);
        }
        titleElement.setTitles(titleList);
        return titleElement;
    }
}
