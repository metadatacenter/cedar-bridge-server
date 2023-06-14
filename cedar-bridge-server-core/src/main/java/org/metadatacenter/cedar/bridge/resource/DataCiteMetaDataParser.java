package org.metadatacenter.cedar.bridge.resource;

import org.metadatacenter.cedar.bridge.resource.CEDARProperties.*;
import org.metadatacenter.cedar.bridge.resource.CEDARProperties.Date;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class DataCiteMetaDataParser {
    private static String DATACITE_ID_URL = "http://purl.org/datacite/v4.4/";
    private static String PERSOANL_ID_URL = "http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Persoanl";
    private static String ORGANIZATIONAL_ID_URL = "http://www.w3.org/2006/vcard/ns#Organizational";
    private static String LANG_ID_URL = "https://www.omg.org/spec/LCC/Languages/LaISO639-1-LanguageCodes/az";
    private static String Date_Type = "xsd:date";

    public static void parseDataCiteSchema(DataCiteSchema dataCiteSchema, CEDARDataCiteInstance cedarInstance) {
        Attributes dataCiteAttributes = dataCiteSchema.getData().getAttributes();
        //pass creators values
        cedarInstance.setCreatorElement(parseCreatorValue(dataCiteAttributes.getCreators()));

        //pass titles values
        cedarInstance.setTitleElement(parseTitleValue(dataCiteAttributes.getTitles()));

        //pass publisher value
        cedarInstance.setPublisherElement(parsePublisherValue(dataCiteAttributes.getPublisher()));

        //pass publicationYear value
        cedarInstance.setPublicationYearElement(parsePublicationYearValue(dataCiteAttributes.getPublicationYear()));

        //pass resourceType value
        cedarInstance.setResourceTypeElement(parseResourceTypeValue(dataCiteAttributes.getTypes()));

        //pass subject values
        cedarInstance.setSubjectElement(parseSubjectValue(dataCiteAttributes.getSubjects()));

        //pass contributor values
        cedarInstance.setContributorElement(parseContributorValue(dataCiteAttributes.getContributors()));

        //pass date values
        cedarInstance.setDateElement(parseDateValue(dataCiteAttributes.getDates()));

        //pass lang value
        cedarInstance.setLanguage(parseLangValue(dataCiteAttributes.getLanguage()));

        //pass alternteIdentifier value
        cedarInstance.setAlternateIdentifierElement(parseAlternateIdentifierValue(dataCiteAttributes.getAlternateIdentifiers()));

        //pass relatedIdentifier value
        cedarInstance.setRelatedIdentifierElement(parseRelatedIdentifierValue(dataCiteAttributes.getRelatedIdentifiers()));

    }

    private static List<Affiliation> parseAffiliationValue (List<DataCiteAffiliation> dataCiteAffiliations, String element){
        List<Affiliation> affiliationList = new ArrayList<>();

        for (DataCiteAffiliation a : dataCiteAffiliations) {
            //initialize corresponding class
            Affiliation affiliation = new Affiliation();
            ValueFormat affiliationIdentifier = new ValueFormat(), affiliationIdentifierScheme = new ValueFormat();
            SchemeURI affiliationIdentifierSchemeURI = new SchemeURI();

            //retrieve values from DataCiteScheme and set it to CEDAR class
            affiliationIdentifier.setValue(a.getAffiliationIdentifier());
            affiliationIdentifierScheme.setValue(a.getAffiliationIdentifierScheme());
            affiliationIdentifierSchemeURI.setId(a.getAffiliationSchemeURI());

            //set CEDAR Affiliation class value
            affiliation.setAffiliationIdentifier(affiliationIdentifier);
            affiliation.setAffiliationIdentifierScheme(affiliationIdentifierScheme);
            affiliation.setAffiliationIdentifierSchemeURI(affiliationIdentifierSchemeURI);

            //set @context and @id
            Map<String, String> context = new HashMap<>();
            context.put("affiliationIdentifier", "https://schema.metadatacenter.org/properties/1c405b8f-a36a-4b35-a7e5-52ad7828f5a4");
            context.put("affiliationIdentifierScheme", "https://schema.metadatacenter.org/properties/ac3137bc-0bd2-4e50-ad4e-6592bf71e681");
            context.put("affiliationIdentifierSchemeURI", "https://schema.metadatacenter.org/properties/3b1e62d4-f1b6-4874-b36e-d7740a7f7b05");
            affiliation.setContext(context);
            switch (element){
                case "Creator":
                    affiliation.setId("https://repo.metadatacenter.org/template-element-instances/bdceb6db-7f06-40a5-a1d9-42add10411ef");
                    break;
                case "Contributor":
                    affiliation.setId("https://repo.metadatacenter.org/template-element-instances/7ede653f-c1a8-4ae6-9156-e9a6447f13c2");
                    break;
            }

            // add affiliation to the list
            affiliationList.add(affiliation);
        }
        return affiliationList;
    }

    private static List<NameIdentifier> parseNameIdentifierValue (List<DataCiteNameIdentifier> dataCiteNameIdentifiers, String element){
        List<NameIdentifier> nameIdentifierList = new ArrayList<>();

        for (DataCiteNameIdentifier ni : dataCiteNameIdentifiers) {
            NameIdentifier nameIdentifier = new NameIdentifier();
            ValueFormat name = new ValueFormat(), nameIdentifierScheme = new ValueFormat();
            SchemeURI nameIdentifierSchemeURI = new SchemeURI();

            name.setValue(ni.getNameIdentifier());
            nameIdentifierScheme.setValue(ni.getNameIdentifierScheme());
            nameIdentifierSchemeURI.setId(ni.getSchemeUri());

            nameIdentifier.setNameIdentifierName(name);
            nameIdentifier.setNameIdentifierScheme(nameIdentifierScheme);
            nameIdentifier.setNameIdentifierSchemeURI(nameIdentifierSchemeURI);

            //set @context and @id
            Map<String, String> context = new HashMap<>();
            context.put("nameIdentifier", "https://schema.metadatacenter.org/properties/55e405b4-08e3-429e-ac3e-a86778a0f56c");
            context.put("nameIdentifierScheme", "https://schema.metadatacenter.org/properties/ba910380-8046-4298-94bb-27a7f76d54a4");
            context.put("nameIdentifierSchemeURI", "https://schema.metadatacenter.org/properties/be196b75-6ccf-4791-8532-25e6aa6d8bc5");
            nameIdentifier.setContext(context);
            switch (element){
                case "Creator":
                    nameIdentifier.setId("https://repo.metadatacenter.org/template-element-instances/6cf20b14-a7a4-48d3-8424-5f6a104c218f");
                    break;
                case "Contributor":
                    nameIdentifier.setId("https://repo.metadatacenter.org/template-element-instances/f5c1fc89-1a0d-4657-962a-b71c809b8f62");
                    break;
            }

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
            String dataCiteNameType = c.getNameType();
            nameType.setLabel(dataCiteNameType);
            if (dataCiteNameType.equals("Personal")){
                nameType.setId(PERSOANL_ID_URL);
            } else{
                nameType.setId(ORGANIZATIONAL_ID_URL);
            }
            givenName.setValue(c.getGivenName());
            familyName .setValue(c.getFamilyName());

            //set values to CEDARInstance
            creator.setCreatorName(name);
            creator.setNameType(nameType);
            creator.setGivenName(givenName);
            creator.setFamilyName(familyName);

            //set affiliation list values
            if (c.getAffiliations() != null && !c.getAffiliations().isEmpty()) {
                creator.setAffiliations(parseAffiliationValue(c.getAffiliations(), "Creator"));
            }

            //set nameIdentifier list values
            if (c.getNameIdentifiers() != null && !c.getNameIdentifiers().isEmpty()) {
                creator.setNameIdentifiers(parseNameIdentifierValue(c.getNameIdentifiers(), "Creator"));
            }

            //set @context and @id
            Map<String, String> context = new HashMap<>();
            context.put("Affiliation", "https://schema.metadatacenter.org/properties/49e75d91-60ca-40c5-837a-959c7e6891d3");
            context.put("creatorName", "https://schema.metadatacenter.org/properties/8acde937-9aac-4830-901d-18827533d9ee");
            context.put("familyName", "https://schema.metadatacenter.org/properties/8a8c322b-7f15-4a7a-b687-ad969e71defe");
            context.put("givenName", "https://schema.metadatacenter.org/properties/6f29b11a-2c3e-4ebd-92f5-8b6ca863c484");
            context.put("nameIdentifier", "https://schema.metadatacenter.org/properties/bc333322-2f4d-4b63-9acf-7c4a8ceddebb");
            context.put("nameType", "https://schema.metadatacenter.org/properties/c5257a64-69a4-4262-93ff-b7624fd00900");
            creator.setContext(context);
            creator.setId("https://repo.metadatacenter.org/template-element-instances/1d5fbeec-9b22-460c-8cbb-31698f3d949b");

            creatorList.add(creator);
        }
        // set creatorElement value
        creatorElement.setCreators(creatorList);
        Map<String, String> context = new HashMap<>();
        context.put("creator", "https://schema.metadatacenter.org/properties/cf32ea42-9cc4-4ccc-bf0d-1d62d1aae7ac");
        creatorElement.setContext(context);
        creatorElement.setId("https://repo.metadatacenter.org/template-element-instances/e0eae987-86c3-42a9-9906-594b0f926880");

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
            titleType.setId(DATACITE_ID_URL + t.getTitleType());
            language.setValue(t.getLang());

            title.setTitleName(titleName);
            title.setTitleType(titleType);
            title.setLanguage(language);

            //set @context and @id
            Map<String, String> context = new HashMap<>();
            context.put("language", "https://schema.metadatacenter.org/properties/6c972ff2-1f4d-45bb-9f60-7573a999548a");
            context.put("titleName", "https://schema.metadatacenter.org/properties/24c6ed0a-277b-4b1e-8278-b2b1c64a8a27");
            context.put("titleType", "https://schema.metadatacenter.org/properties/9b5eb75f-3355-4ddb-903d-c716f4816b12");
            title.setContext(context);
            title.setId("https://repo.metadatacenter.org/template-element-instances/7d7dc2d3-54b5-43fe-b263-8d725ddf4a18");

            titleList.add(title);
        }
        titleElement.setTitles(titleList);
        Map<String, String> context = new HashMap<>();
        context.put("Title", "https://schema.metadatacenter.org/properties/b0c0b0e9-e5d0-48ec-8716-e7c8d8dbd7a5");
        titleElement.setContext(context);
        titleElement.setId("https://repo.metadatacenter.org/template-element-instances/30fcc8f6-f9c8-41b4-944b-25b659b45496");

        return titleElement;
    }

    private static PublisherElement parsePublisherValue(String givenPublisher){
        PublisherElement publisherElement = new PublisherElement();
        Publisher publisher = new Publisher();
        publisher.setValue(givenPublisher);
        publisherElement.setPublisher(publisher);
        Map<String, String> context = new HashMap<>();
        context.put("Publisher", "https://schema.metadatacenter.org/properties/8e4aa4d3-c0bf-42bf-8f26-03745416058e");
        publisherElement.setContext(context);
        publisherElement.setId("https://repo.metadatacenter.org/template-element-instances/81723cca-83d6-4e83-8eed-a85cff55cb29");
        return publisherElement;
    }

    private static PublicationYearElement parsePublicationYearValue(Integer givenPublicationYear){
        PublicationYearElement publicationYearElement = new PublicationYearElement();
        PublicationYear publicationYear = new PublicationYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, givenPublicationYear);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        publicationYear.setValue(formatter.format(calendar.getTime()));
        publicationYear.setType("xsd:date");
        publicationYearElement.setPublicationYear(publicationYear);
        Map<String, String> context = new HashMap<>();
        context.put("PublicationYear", "https://schema.metadatacenter.org/properties/8d3c881c-46ae-47e4-aaa7-69f7b39a5ee8");
        publicationYearElement.setContext(context);
        publicationYearElement.setId("https://repo.metadatacenter.org/template-element-instances/36fae4d7-631e-4b6c-891b-3c8cd74d56b2");
        return publicationYearElement;
    }

    private static ResourceTypeElement parseResourceTypeValue(DataCiteType dataCiteType){
        ResourceTypeElement resourceTypeElement = new ResourceTypeElement();
        ValueFormat resourceType = new ValueFormat();
        IdFormat resourceTypeGeneral = new IdFormat();
        resourceType.setValue(dataCiteType.getResourceType());
        resourceTypeGeneral.setLabel(dataCiteType.getResourceTypeGeneral());
        resourceTypeGeneral.setId(DATACITE_ID_URL + dataCiteType.getResourceTypeGeneral());
        resourceTypeElement.setResourceType(resourceType);
        resourceTypeElement.setResourceTypeGeneral(resourceTypeGeneral);
        Map<String, String> context = new HashMap<>();
        context.put("ResourceType", "https://schema.metadatacenter.org/properties/467fc1e2-fb85-42fb-a93e-8c379b59748b");
        context.put("resourceTypeGeneral", "https://schema.metadatacenter.org/properties/5b34d6ae-0f7f-4535-b7a1-b41dea86d37b");
        resourceTypeElement.setContext(context);
        resourceTypeElement.setId("https://repo.metadatacenter.org/template-element-instances/9590d7d8-2174-4ee0-aeda-1c9385d476f6");
        return resourceTypeElement;
    }

    private static SubjectElement parseSubjectValue(List<DataCiteSubject> dataCiteSubjects){
        SubjectElement subjectElement = new SubjectElement();
        List<Subject> subjectList = new ArrayList<>();

        for (DataCiteSubject s : dataCiteSubjects) {
            Subject subject = new Subject();
            ValueFormat subjectName = new ValueFormat(), subjectScheme = new ValueFormat(), classificationCode = new ValueFormat();
            SchemeURI subjectSchemeUri = new SchemeURI(), valueUri = new SchemeURI();

            subjectName.setValue(s.getSubject());
            subjectScheme.setValue(s.getSubjectScheme());
            classificationCode.setValue(s.getClassificationCode());
            subjectSchemeUri.setId(s.getSchemeUri());
            valueUri.setId(s.getValueUri());

            subject.setSubjectName(subjectName);
            subject.setSubjectScheme(subjectScheme);
            subject.setClassificationCode(classificationCode);
            subject.setSubjectSchemeURI(subjectSchemeUri);
            subject.setValueURI(valueUri);

            //set @context and @id
            Map<String, String> context = new HashMap<>();
            context.put("classificationCode", "https://schema.metadatacenter.org/properties/f9115bda-17ee-4073-b66b-886a256bc944");
            context.put("subjectName", "https://schema.metadatacenter.org/properties/bbedfb8d-c952-4a79-a01d-f9547f6b729d");
            context.put("subjectScheme", "https://schema.metadatacenter.org/properties/c029122f-b9b4-4176-b235-d3fff6f5167b");
            context.put("subjectSchemeURI", "https://schema.metadatacenter.org/properties/06f649bb-b0dd-4c6d-8379-c7a693566b9e");
            context.put("valueURI", "https://schema.metadatacenter.org/properties/7420f8aa-4a00-418f-92cb-8874c0c770cd");
            subject.setContext(context);
            subject.setId("https://repo.metadatacenter.org/template-element-instances/2528befa-3f1d-424c-b740-1719fb6e4dd3");

            subjectList.add(subject);
        }
        subjectElement.setSubjects(subjectList);
        Map<String, String> context = new HashMap<>();
        context.put("Subject", "https://schema.metadatacenter.org/properties/58ed2553-c4cc-41fb-9206-19b0185c6416");
        subjectElement.setContext(context);
        subjectElement.setId("https://repo.metadatacenter.org/template-element-instances/1dff3904-8146-4aba-a094-4afaa094ab21");

        return subjectElement;
    }

    private static ContributorElement parseContributorValue(List<DataCiteContributor> dataCiteContributors){
        ContributorElement contributorElement = new ContributorElement();
        List<Contributor> contributorList = new ArrayList<>();

        for (DataCiteContributor c : dataCiteContributors) {
            Contributor contributor = new Contributor();
            // retrieve values from DataCiteScheme
            ValueFormat name = new ValueFormat(), givenName = new ValueFormat(), familyName = new ValueFormat();
            IdFormat nameType = new IdFormat(), contributorType = new IdFormat();
            name.setValue(c.getName());
            String dataCiteNameType = c.getNameType();
            nameType.setLabel(dataCiteNameType);
            if (dataCiteNameType.equals("Personal")){
                nameType.setId(PERSOANL_ID_URL);
            } else{
                nameType.setId(ORGANIZATIONAL_ID_URL);
            }
            givenName.setValue(c.getGivenName());
            familyName .setValue(c.getFamilyName());
            contributorType.setLabel(c.getContributorType());
            contributorType.setId(DATACITE_ID_URL + c.getContributorType());

            //set values to CEDARInstance
            contributor.setContributorName(name);
            contributor.setNameType(nameType);
            contributor.setGivenName(givenName);
            contributor.setFamilyName(familyName);
            contributor.setContributorType(contributorType);

            //set affiliation list values
            if (c.getAffiliations() != null && !c.getAffiliations().isEmpty()) {
                contributor.setAffiliations(parseAffiliationValue(c.getAffiliations(), "Contributor"));
            }

            //set nameIdentifier list values
            if (c.getNameIdentifiers() != null && !c.getAffiliations().isEmpty()) {
                contributor.setNameIdentifiers(parseNameIdentifierValue(c.getNameIdentifiers(), "Contributor"));
            }

            //set @context and @id
            Map<String, String> context = new HashMap<>();
            context.put("Affiliation", "https://schema.metadatacenter.org/properties/b49f8dd3-5598-4917-99bb-ada4809142b4");
            context.put("contributorName", "https://schema.metadatacenter.org/properties/f9284b7a-1775-4a1f-b7f7-026198b9aa64");
            context.put("contributorType", "https://schema.metadatacenter.org/properties/ad2f2387-2231-4023-982d-ed3db7c31ac0");
            context.put("familyName", "https://schema.metadatacenter.org/properties/a431cb7f-8675-46dd-b53c-d8461b433f8b");
            context.put("givenName", "https://schema.metadatacenter.org/properties/fd548b4a-0670-4660-9c77-b4210526672b");
            context.put("nameIdentifier", "https://schema.metadatacenter.org/properties/c57237a3-f60d-4818-9a24-7b4ff70a6fa7");
            context.put("nameType", "https://schema.metadatacenter.org/properties/f3554aad-6be5-468a-b665-568a192cb9d1");
            contributor.setContext(context);
            contributor.setId("https://repo.metadatacenter.org/template-element-instances/90ead7cc-c9b4-4ba5-b186-8058f083e34f");

            contributorList.add(contributor);
        }
        // set creatorElement value
        contributorElement.setContributors(contributorList);
        Map<String, String> context = new HashMap<>();
        context.put("Contributor", "https://schema.metadatacenter.org/properties/d1652b2f-bf8a-416c-b878-683f82832525");
        contributorElement.setContext(context);
        contributorElement.setId("https://repo.metadatacenter.org/template-element-instances/b215d0d8-70f9-41fa-898c-78f2e366d7c1");

        return contributorElement;
    }

    private static DateElement parseDateValue(List<DataCiteDate> dataCiteDates){
        DateElement dateElement = new DateElement();
        List<Date> dateList = new ArrayList<>();

        for (DataCiteDate d : dataCiteDates) {
            Date cedarDate = new Date();
            ValueFormat date = new ValueFormat(), dateInformation = new ValueFormat();
            IdFormat dateType = new IdFormat();

            date.setValue(d.getDate());
            date.setType(Date_Type);
            dateInformation.setValue(d.getDateInformation());
            dateType.setLabel(d.getDateType());
            dateType.setId(DATACITE_ID_URL + d.getDateType());

            cedarDate.setDate(date);
            cedarDate.setDateInformation(dateInformation);
            cedarDate.setDateType(dateType);

            //set @context and @id
            Map<String, String> context = new HashMap<>();
            context.put("date", "https://schema.metadatacenter.org/properties/85eb2413-48da-4dd9-904b-a828cd5d2b4d");
            context.put("dateInformation", "https://schema.metadatacenter.org/properties/ba5937f5-4b85-4770-be1c-5c62df8bb6c7");
            context.put("dateType", "https://schema.metadatacenter.org/properties/77e6d4be-3c5f-4076-8517-46ad4759d315");
            cedarDate.setContext(context);
            cedarDate.setId("https://repo.metadatacenter.org/template-element-instances/27a0219d-c6aa-4d83-8d0b-465f3cbb1349");

            dateList.add(cedarDate);
        }
        dateElement.setDates(dateList);
        Map<String, String> context = new HashMap<>();
        context.put("Date", "https://schema.metadatacenter.org/properties/4084c760-30fa-43f8-8441-498f73314100");
        dateElement.setContext(context);
        dateElement.setId("https://repo.metadatacenter.org/template-element-instances/e2e824c3-36d0-4f2a-9c0b-903bf7c9e760");

        return dateElement;
    }

    private static IdFormat parseLangValue(String dateCitelang){
        IdFormat lang = new IdFormat();
        lang.setId(LANG_ID_URL);
        lang.setLabel(dateCitelang);
        return lang;
    }

    private static AlternateIdentifierElement parseAlternateIdentifierValue(List<DataCiteAlternateIdentifier> dataCiteAlternateIdentifiers){
        AlternateIdentifierElement alternateIdentifierElement = new AlternateIdentifierElement();
        List<AlternateIdentifier> alternateIdentifierList = new ArrayList<>();

        for (DataCiteAlternateIdentifier a : dataCiteAlternateIdentifiers) {
            AlternateIdentifier alternateIdentifier = new AlternateIdentifier();
            ValueFormat identifier = new ValueFormat(), identifierType = new ValueFormat();

            identifier.setValue(a.getAlternateIdentifier());
            identifierType.setValue(a.getAlternateIdentifierType());

            alternateIdentifier.setAlternateIdentifier(identifier);
            alternateIdentifier.setAlternateIdentifierType(identifierType);

            //set @context and @id
            Map<String, String> context = new HashMap<>();
            context.put("alternateIdentifier", "https://schema.metadatacenter.org/properties/da9e58cf-9dcc-4ef9-9cf9-f4bc58936f8b");
            context.put("alternateIdentifierType", "https://schema.metadatacenter.org/properties/47a48fd6-97a2-4198-8c06-b9b96608db7e");
            alternateIdentifier.setContext(context);
            alternateIdentifier.setId("https://repo.metadatacenter.org/template-element-instances/27a0219d-c6aa-4d83-8d0b-465f3cbb1349");

            alternateIdentifierList.add(alternateIdentifier);
        }
        alternateIdentifierElement.setAlternateIdentifiers(alternateIdentifierList);
        Map<String, String> context = new HashMap<>();
        context.put("Alternate Identifier", "https://schema.metadatacenter.org/properties/a034fbed-8aea-41c3-80b6-250921ea2bca");
        alternateIdentifierElement.setContext(context);
        alternateIdentifierElement.setId("https://repo.metadatacenter.org/template-element-instances/810aaaf0-16bf-4392-a81d-722d94605a24");

        return alternateIdentifierElement;
    }

    private static RelatedIdentifierElement parseRelatedIdentifierValue(List<DataCiteRelatedIdentifier> dataCiteRelatedIdentifiers){
        RelatedIdentifierElement relatedIdentifierElement = new RelatedIdentifierElement();
        List<RelatedIdentifier> relatedIdentifierList = new ArrayList<>();

        for (DataCiteRelatedIdentifier r : dataCiteRelatedIdentifiers) {
            RelatedIdentifier relatedIdentifier = new RelatedIdentifier();
            ValueFormat identifier = new ValueFormat(), relatedMetadataScheme = new ValueFormat(), schemeType = new ValueFormat();
            IdFormat identifierType = new IdFormat(), relationType = new IdFormat(), resourceTypeGeneral = new IdFormat();
            SchemeURI schemeUri = new SchemeURI();

            identifier.setValue(r.getRelatedIdentifier());
            identifierType.setLabel(r.getRelatedIdentifierType());
            identifierType.setId(DATACITE_ID_URL + r.getRelatedIdentifierType());
            relatedMetadataScheme.setValue(r.getRelatedMetadataScheme());
            relationType.setLabel(r.getRelationType());
            relationType.setId(DATACITE_ID_URL + r.getRelationType());
            resourceTypeGeneral.setLabel(r.getResourceTypeGeneral());
            resourceTypeGeneral.setId(DATACITE_ID_URL + r.getResourceTypeGeneral());
            schemeType.setValue(r.getSchemeType());
            schemeUri.setId(r.getSchemeUri());

            relatedIdentifier.setRelatedIdentifier(identifier);
            relatedIdentifier.setRelatedIdentifierType(identifierType);
            relatedIdentifier.setRelatedMetadataScheme(relatedMetadataScheme);
            relatedIdentifier.setRelationType(relationType);
            relatedIdentifier.setResourceTypeGeneral(resourceTypeGeneral);
            relatedIdentifier.setSchemeType(schemeType);
            relatedIdentifier.setSchemeURi(schemeUri);

            //set @context and @id
            Map<String, String> context = new HashMap<>();
            context.put("RelatedIdentifier", "https://schema.metadatacenter.org/properties/da9e58cf-9dcc-4ef9-9cf9-f4bc58936f8b");
            context.put("relatedIdentifierType", "https://schema.metadatacenter.org/properties/47a48fd6-97a2-4198-8c06-b9b96608db7e");
            context.put("relatedMetadataScheme", "https://schema.metadatacenter.org/properties/da9e58cf-9dcc-4ef9-9cf9-f4bc58936f8b");
            context.put("relationType", "https://schema.metadatacenter.org/properties/47a48fd6-97a2-4198-8c06-b9b96608db7e");
            context.put("resourceTypeGeneral", "https://schema.metadatacenter.org/properties/da9e58cf-9dcc-4ef9-9cf9-f4bc58936f8b");
            context.put("schemeType", "https://schema.metadatacenter.org/properties/47a48fd6-97a2-4198-8c06-b9b96608db7e");
            context.put("schemeURI", "https://schema.metadatacenter.org/properties/47a48fd6-97a2-4198-8c06-b9b96608db7e");
            relatedIdentifier.setContext(context);
            relatedIdentifier.setId("https://repo.metadatacenter.org/template-element-instances/fe3e5e43-7bd3-43e3-96ee-fa10f2c04ac6");

            relatedIdentifierList.add(relatedIdentifier);
        }
        relatedIdentifierElement.setRelatedIdentifiers(relatedIdentifierList);
        Map<String, String> context = new HashMap<>();
        context.put("Related Identifier", "https://schema.metadatacenter.org/properties/c689fe26-eb9c-4240-9b35-32f16fff78ad");
        relatedIdentifierElement.setContext(context);
        relatedIdentifierElement.setId("https://repo.metadatacenter.org/template-element-instances/49f1f43b-4e27-4bda-af8f-6a999ef8956d");

        return relatedIdentifierElement;
    }


}
