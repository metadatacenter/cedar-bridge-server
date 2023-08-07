package org.metadatacenter.cedar.bridge.resource;

import org.metadatacenter.cedar.bridge.resource.CedarProperties.*;
import org.metadatacenter.cedar.bridge.resource.CedarProperties.Date;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class DataCiteMetadataParser {
    private static final String DATACITE_ID_URL = "http://purl.org/datacite/v4.4/";
    private static final String PERSOANL_ID_URL = "http://www.semanticweb.org/ambrish/ontologies/2020/10/untitled-ontology-24#Persoanl";
    private static final String ORGANIZATIONAL_ID_URL = "http://www.w3.org/2006/vcard/ns#Organizational";
    private static final String LANG_ID_URL = "https://www.omg.org/spec/LCC/Languages/LaISO639-1-LanguageCodes/";
    private static final String DATE_TYPE = "xsd:date";
    private static final String DECIMAL_TYPE = "xsd:decimal";

    public static void parseDataCiteSchema(Attributes dataCiteAttributes, CedarDataCiteInstance cedarInstance) {
//        Attributes dataCiteAttributes = dataCiteSchema.getData().getAttributes();
        //pass prefix value
        cedarInstance.setPrefix(parsePrefixValue(dataCiteAttributes.getPrefix()));

        //pass creators values
        cedarInstance.setCreators(parseCreatorValue(dataCiteAttributes.getCreators()));

        //pass titles values
        cedarInstance.setTitles(parseTitleValue(dataCiteAttributes.getTitles()));

        //pass publisher value
        cedarInstance.setPublisher(parsePublisherValue(dataCiteAttributes.getPublisher()));

        //pass publicationYear value
        cedarInstance.setPublicationYear(parsePublicationYearValue(dataCiteAttributes.getPublicationYear()));

        //pass resourceType value
//        cedarInstance.setResourceType(parseResourceTypeValue(dataCiteAttributes.getTypes()));

        //pass subject values
        cedarInstance.setSubjects(parseSubjectValue(dataCiteAttributes.getSubjects()));

        //pass contributor values
        cedarInstance.setContributors(parseContributorValue(dataCiteAttributes.getContributors()));

        //pass date values
        cedarInstance.setDates(parseDateValue(dataCiteAttributes.getDates()));

        //pass lang value
        cedarInstance.setLanguage(parseLangValue(dataCiteAttributes.getLanguage()));

        //pass alternateIdentifier value
        cedarInstance.setAlternateIdentifiers(parseAlternateIdentifierValue(dataCiteAttributes.getAlternateIdentifiers()));

        //pass relatedIdentifier value
        cedarInstance.setRelatedIdentifiers(parseRelatedIdentifierValue(dataCiteAttributes.getRelatedIdentifiers()));

        //pass size value
        cedarInstance.setSizes(parseSizeValue(dataCiteAttributes.getSizes()));

        //pass format value
        cedarInstance.setFormats(parseFormatValue(dataCiteAttributes.getFormats()));

        //pass version value
        cedarInstance.setVersion(parseVersionValue(dataCiteAttributes.getVersion()));

        //pass rights value
        cedarInstance.setRightsList(parseRightsValue(dataCiteAttributes.getRightsList()));

        //pass description value
        cedarInstance.setDescriptions(parseDescriptionValue(dataCiteAttributes.getDescriptions()));

        //pass geoLocation value
        cedarInstance.setGeoLocations(parseGeoLocationValue(dataCiteAttributes.getGeoLocations()));

        //pass fundingReference value
        cedarInstance.setFundingReferences(parseFundingReferenceValue(dataCiteAttributes.getFundingReferences()));

        //pass relatedItem value
        cedarInstance.setRelatedItems(parseRelatedItemValue(dataCiteAttributes.getRelatedItems()));

        cedarInstance.setContext(setContext());

        //TODO: update template id with the lastest one
        cedarInstance.setSchemaIsBasedOn("https://repo.metadatacenter.orgx/templates/a08cb3aa-f8fa-40fd-8d27-bcc3b4368436");
        cedarInstance.setSchemaName("DataCite V4.4 without OpenViewUrl field metadata");
        cedarInstance.setSchemaDescription("");
        cedarInstance.setPavCreatedOn("2023-07-24T15:46:11-07:00");
        cedarInstance.setPavCreatedBy("https://metadatacenter.org/users/0e97ec85-77a9-434c-8549-33f9eae22608");
        cedarInstance.setPavLastUpdatedOn("2023-07-24T21:51:40-07:00");
        cedarInstance.setOslcModifiedBy("https://metadatacenter.org/users/0e97ec85-77a9-434c-8549-33f9eae22608");
        cedarInstance.setId("https://repo.metadatacenter.orgx/template-instances/54797d84-cd30-4287-9751-d5cf81775db2");
    }

    private static ValueFormat parsePrefixValue(String dataCitePrefix){
        ValueFormat prefix = new ValueFormat();
//        prefix.setValue(dataCitePrefix);
        prefix.setValue("10.82658");
        return prefix;
    }

    private static List<Affiliation> parseAffiliationValue (List<DataCiteAffiliation> dataCiteAffiliations, String element){
        List<Affiliation> affiliationList = new ArrayList<>();

        if (dataCiteAffiliations != null && !dataCiteAffiliations.isEmpty()){
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
        }
        return affiliationList;
    }

    private static List<NameIdentifier> parseNameIdentifierValue (List<DataCiteNameIdentifier> dataCiteNameIdentifiers, String element){
        List<NameIdentifier> nameIdentifierList = new ArrayList<>();

        if (dataCiteNameIdentifiers != null && !dataCiteNameIdentifiers.isEmpty()){
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
        }
        return nameIdentifierList;
    }

    private static List<Creator> parseCreatorValue(List<DataCiteCreator> dataCiteCreators)    {
//        CreatorElement creatorElement = new CreatorElement();
        List<Creator> creatorList = new ArrayList<>();

        if (dataCiteCreators != null && !dataCiteCreators.isEmpty()){
            for (DataCiteCreator c : dataCiteCreators) {
                Creator creator = new Creator();
                // retrieve values from DataCiteScheme
                ValueFormat name = new ValueFormat(), givenName = new ValueFormat(), familyName = new ValueFormat();
                IdFormat nameType = new IdFormat();
                name.setValue(c.getName());
                String dataCiteNameType = c.getNameType();
                if (dataCiteNameType != null) {
                    nameType.setLabel(dataCiteNameType);
                    if (dataCiteNameType.equals("Personal")){
                        nameType.setId(PERSOANL_ID_URL);
                    } else{
                        nameType.setId(ORGANIZATIONAL_ID_URL);
                    }
                }
                givenName.setValue(c.getGivenName());
                familyName .setValue(c.getFamilyName());

                //set values to CEDARInstance
                creator.setCreatorName(name);
                creator.setNameType(nameType);
                creator.setGivenName(givenName);
                creator.setFamilyName(familyName);

                //set affiliation list values
                creator.setAffiliations(parseAffiliationValue(c.getAffiliations(), "Creator"));

                //set nameIdentifier list values
                creator.setNameIdentifiers(parseNameIdentifierValue(c.getNameIdentifiers(), "Creator"));

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
        }
        return creatorList;
    }

    private static List<Title> parseTitleValue(List<DataCiteTitle> dataCiteTitles) {
        List<Title> titleList = new ArrayList<>();

        if (dataCiteTitles != null && !dataCiteTitles.isEmpty()){
            for (DataCiteTitle t : dataCiteTitles) {
                Title title = new Title();
                ValueFormat titleName = new ValueFormat();
                IdFormat titleType = new IdFormat();

                titleName.setValue(t.getTitle());
                if (t.getTitleType()!=null){
                    titleType.setLabel(t.getTitleType());
                    titleType.setId(DATACITE_ID_URL + t.getTitleType());
                }

                title.setTitleName(titleName);
                title.setTitleType(titleType);

                //set @context and @id
                Map<String, String> context = new HashMap<>();
                context.put("titleName", "https://schema.metadatacenter.org/properties/24c6ed0a-277b-4b1e-8278-b2b1c64a8a27");
                context.put("titleType", "https://schema.metadatacenter.org/properties/9b5eb75f-3355-4ddb-903d-c716f4816b12");
                title.setContext(context);
                title.setId("https://repo.metadatacenter.org/template-element-instances/7d7dc2d3-54b5-43fe-b263-8d725ddf4a18");

                titleList.add(title);
            }
        }
        return titleList;
    }

    private static Publisher parsePublisherValue(String givenPublisher){
        Publisher publisher = new Publisher();
        publisher.setValue(givenPublisher);
        return publisher;
    }

    private static PublicationYear parsePublicationYearValue(Integer givenPublicationYear){
        PublicationYear publicationYear = new PublicationYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, givenPublicationYear);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        publicationYear.setValue(formatter.format(calendar.getTime()));
        publicationYear.setType("xsd:date");

        return publicationYear;
    }

    private static ValueFormat parseResourceTypeValue(DataCiteType dataCiteType){
        ValueFormat resourceType = new ValueFormat();
        resourceType.setValue(dataCiteType.getResourceType());
        return resourceType;
    }

    private static List<Subject> parseSubjectValue(List<DataCiteSubject> dataCiteSubjects){
        List<Subject> subjectList = new ArrayList<>();

        if (dataCiteSubjects!=null && !dataCiteSubjects.isEmpty()){
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
        }
        return subjectList;
    }

    private static List<Contributor> parseContributorValue(List<DataCiteContributor> dataCiteContributors){
        List<Contributor> contributorList = new ArrayList<>();

        if (dataCiteContributors!=null && !dataCiteContributors.isEmpty()){
            for (DataCiteContributor c : dataCiteContributors) {
                Contributor contributor = new Contributor();
                // retrieve values from DataCiteScheme
                ValueFormat name = new ValueFormat(), givenName = new ValueFormat(), familyName = new ValueFormat();
                IdFormat nameType = new IdFormat(), contributorType = new IdFormat();
                name.setValue(c.getName());
                String dataCiteNameType = c.getNameType();
                if (dataCiteNameType != null) {
                    nameType.setLabel(dataCiteNameType);
                    if (dataCiteNameType.equals("Personal")){
                        nameType.setId(PERSOANL_ID_URL);
                    } else{
                        nameType.setId(ORGANIZATIONAL_ID_URL);
                    }
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
                contributor.setAffiliations(parseAffiliationValue(c.getAffiliations(), "Contributor"));

                //set nameIdentifier list values
                contributor.setNameIdentifiers(parseNameIdentifierValue(c.getNameIdentifiers(), "Contributor"));

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
        }
        return contributorList;
    }

    private static List<Date> parseDateValue(List<DataCiteDate> dataCiteDates){
        List<Date> dateList = new ArrayList<>();

        if (dataCiteDates!=null && !dataCiteDates.isEmpty()){
            for (DataCiteDate d : dataCiteDates) {
                Date cedarDate = new Date();
                ValueAndTypeFormat date = new ValueAndTypeFormat();
                ValueFormat dateInformation = new ValueFormat();
                IdFormat dateType = new IdFormat();

                date.setValue(d.getDate());
                date.setType(DATE_TYPE);
                dateInformation.setValue(d.getDateInformation());
                if(d.getDateType()!=null){
                    dateType.setLabel(d.getDateType());
                    dateType.setId(DATACITE_ID_URL + d.getDateType());
                }
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
        }
        return dateList;
    }

    private static IdFormat parseLangValue(String dataCiteLang){
        IdFormat lang = new IdFormat();
        if (dataCiteLang!= null){
            lang.setId(LANG_ID_URL + dataCiteLang);
            lang.setLabel(dataCiteLang);
        }
        return lang;
    }

    private static List<AlternateIdentifier> parseAlternateIdentifierValue(List<DataCiteAlternateIdentifier> dataCiteAlternateIdentifiers){
        List<AlternateIdentifier> alternateIdentifierList = new ArrayList<>();
        if (dataCiteAlternateIdentifiers != null && !dataCiteAlternateIdentifiers.isEmpty()){
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
        }
        return alternateIdentifierList;
    }

    private static List<RelatedIdentifier> parseRelatedIdentifierValue(List<DataCiteRelatedIdentifier> dataCiteRelatedIdentifiers){
        List<RelatedIdentifier> relatedIdentifierList = new ArrayList<>();

        if (dataCiteRelatedIdentifiers!=null && !dataCiteRelatedIdentifiers.isEmpty()){
            for (DataCiteRelatedIdentifier r : dataCiteRelatedIdentifiers) {
                RelatedIdentifier relatedIdentifier = new RelatedIdentifier();
                ValueFormat identifier = new ValueFormat(), relatedMetadataScheme = new ValueFormat(), schemeType = new ValueFormat();
                IdFormat identifierType = new IdFormat(), relationType = new IdFormat(), resourceTypeGeneral = new IdFormat();
                SchemeURI schemeUri = new SchemeURI();

                identifier.setValue(r.getRelatedIdentifier());
                if(r.getRelatedIdentifierType()!=null){
                    identifierType.setLabel(r.getRelatedIdentifierType());
                    identifierType.setId(DATACITE_ID_URL + r.getRelatedIdentifierType());
                }
                relatedMetadataScheme.setValue(r.getRelatedMetadataScheme());
                if(r.getRelationType()!=null){
                    relationType.setLabel(r.getRelationType());
                    relationType.setId(DATACITE_ID_URL + r.getRelationType());
                }
                if(r.getResourceTypeGeneral()!=null){
                    resourceTypeGeneral.setLabel(r.getResourceTypeGeneral());
                    resourceTypeGeneral.setId(DATACITE_ID_URL + r.getResourceTypeGeneral());
                }
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
                context.put("RelatedIdentifier", "https://schema.metadatacenter.org/properties/ab1fcf96-2a7c-4418-b698-9da8d42edf64");
                context.put("relatedIdentifierType", "https://schema.metadatacenter.org/properties/886dba05-0110-4768-82c8-f0ceee4944bb");
                context.put("relatedMetadataScheme", "https://schema.metadatacenter.org/properties/3eb9d332-a035-46ae-8559-834028a50980");
                context.put("relationType", "https://schema.metadatacenter.org/properties/24b03c68-f713-44e1-bb63-f39e7a006d61");
                context.put("resourceTypeGeneral", "https://schema.metadatacenter.org/properties/e7e7821a-f1cc-4243-b817-638276fa33fb");
                context.put("schemeType", "https://schema.metadatacenter.org/properties/9abe3743-9af2-4520-b85a-79b9292c3c39");
                context.put("schemeURI", "https://schema.metadatacenter.org/properties/a985716f-3b3d-4b32-8267-3badd7353c29");
                relatedIdentifier.setContext(context);
                relatedIdentifier.setId("https://repo.metadatacenter.org/template-element-instances/fe3e5e43-7bd3-43e3-96ee-fa10f2c04ac6");

                relatedIdentifierList.add(relatedIdentifier);
            }
        }
        return relatedIdentifierList;
    }

    private static List<ValueFormat> parseSizeValue(List<String> dataCiteSizes){
        List<ValueFormat> sizeList = new ArrayList<>();

        if (dataCiteSizes!= null && !dataCiteSizes.isEmpty()){
            for(String s : dataCiteSizes){
                ValueFormat size = new ValueFormat();
                size.setValue(s);
                sizeList.add(size);
            }
        }
        return sizeList;
    }

    private static List<ValueFormat> parseFormatValue(List<String> dataCiteFormats){
        List<ValueFormat> formatList = new ArrayList<>();

        if (dataCiteFormats!=null && !dataCiteFormats.isEmpty()){
            for(String f : dataCiteFormats){
                ValueFormat format = new ValueFormat();
                format.setValue(f);
                formatList.add(format);
            }
        }
        return formatList;
    }

    private static ValueFormat parseVersionValue(String dataCiteVersion){
        ValueFormat version = new ValueFormat();
        version.setValue(dataCiteVersion);
        return version;
    }

    private static List<Rights> parseRightsValue(List<DataCiteRights> dataCiteRights){
        List<Rights> rightsList = new ArrayList<>();

        if (dataCiteRights!= null && !dataCiteRights.isEmpty()){
            for (DataCiteRights r : dataCiteRights) {
                Rights rights = new Rights();
                ValueFormat rightsField = new ValueFormat(), rightsIdentifier = new ValueFormat(), rightsIdentifierScheme = new ValueFormat();
                SchemeURI rightsUri = new SchemeURI(), schemeUri = new SchemeURI();

                rightsField.setValue(r.getRights());
                rightsIdentifier.setValue(r.getRightsIdentifier());
                rightsIdentifierScheme.setValue(r.getRightsIdentifierScheme());
                rightsUri.setId(r.getRightsUri());
                schemeUri.setId(r.getSchemeUri());

                rights.setRights(rightsField);
                rights.setRightsIdentifier(rightsIdentifier);
                rights.setRightsIdentifierScheme(rightsIdentifierScheme);
                rights.setRightsURI(rightsUri);
                rights.setSchemeURI(schemeUri);

                //set @context and @id
                Map<String, String> context = new HashMap<>();
                context.put("Rights", "https://schema.metadatacenter.org/properties/135e5f01-f29a-43fa-b598-7a3c8f3bcaff");
                context.put("rightsIdentifier", "https://schema.metadatacenter.org/properties/d4acc73e-f030-4902-9ec8-3cf166040679");
                context.put("rightsIdentifierScheme", "https://schema.metadatacenter.org/properties/f1195818-b92e-4916-a5c8-f326b0a546f0");
                context.put("rightsURI", "https://schema.metadatacenter.org/properties/3e8de9e1-2649-42e2-95c5-717247d0060e");
                context.put("schemeURI", "https://schema.metadatacenter.org/properties/9b429a92-5dc3-4ff3-995c-24deaabc9c15");
                rights.setContext(context);
                rights.setId("https://repo.metadatacenter.org/template-element-instances/7ab5d0d7-0b96-465a-af15-e8dd1cc3431b");

                rightsList.add(rights);
            }
        }
        return rightsList;
    }

    private static List<Description> parseDescriptionValue(List<DataCiteDescription> dataCiteDescriptions){
        List<Description> descriptionList = new ArrayList<>();

        if (dataCiteDescriptions!= null && !dataCiteDescriptions.isEmpty()){
            for (DataCiteDescription d : dataCiteDescriptions){
                Description description = new Description();

                ValueFormat descriptionField = new ValueFormat();
                IdFormat descriptionType = new IdFormat();

                descriptionField.setValue(d.getDescription());
                if (d.getDescriptionType()!=null){
                    descriptionType.setLabel(d.getDescriptionType());
                    descriptionType.setId(DATACITE_ID_URL + d.getDescriptionType());
                }

                description.setDescription(descriptionField);
                description.setDescriptionType(descriptionType);

                //set @context and @id
                Map<String, String> context = new HashMap<>();
                context.put("Description", "https://schema.metadatacenter.org/properties/aec05c4d-06c4-43c1-bfcb-7c341c81fab7");
                context.put("descriptionType", "https://schema.metadatacenter.org/properties/ba34a97c-9400-4e79-9581-b9218651231d");
                description.setContext(context);
                description.setId("https://repo.metadatacenter.org/template-element-instances/4a5f07af-9d5b-49b2-a452-065ad88efc65");

                descriptionList.add(description);
            }
        }
        return descriptionList;
    }

    private static List<GeoLocation> parseGeoLocationValue(List<DataCiteGeoLocation> dataCiteGeolocations){
        List<GeoLocation> geoLocationList = new ArrayList<>();

        if(dataCiteGeolocations!= null && !dataCiteGeolocations.isEmpty()){
            for (DataCiteGeoLocation g : dataCiteGeolocations){
                GeoLocation geoLocation = new GeoLocation();

                //set up geoLocationBox
                if (g.getGeoLocationBox()!=null){
                    GeoLocationBox geoLocationBox = new GeoLocationBox();
                    FloatFormat eastBondLongitude = new FloatFormat();
                    FloatFormat westBondLongitude = new FloatFormat();
                    FloatFormat northBondLatitude = new FloatFormat();
                    FloatFormat southBondLatitude = new FloatFormat();
                    eastBondLongitude.setValue(g.getGeoLocationBox().getEastBoundLongitude());
                    eastBondLongitude.setType(DECIMAL_TYPE);
                    westBondLongitude.setValue(g.getGeoLocationBox().getWestBoundLongitude());
                    westBondLongitude.setType(DECIMAL_TYPE);
                    northBondLatitude.setValue(g.getGeoLocationBox().getNorthBoundLatitude());
                    northBondLatitude.setType(DECIMAL_TYPE);
                    southBondLatitude.setValue(g.getGeoLocationBox().getSouthBoundLatitude());
                    southBondLatitude.setType(DECIMAL_TYPE);
                    geoLocationBox.setEastBoundLongitude(eastBondLongitude);
                    geoLocationBox.setWestBoundLongitude(westBondLongitude);
                    geoLocationBox.setNorthBoundLatitude(northBondLatitude);
                    geoLocationBox.setSouthBoundLatitude(southBondLatitude);
                    Map<String, String> geoLocationBoxContext = new HashMap<>();
                    geoLocationBoxContext.put("eastBoundLongitude", "https://schema.metadatacenter.org/properties/9cee2101-38ad-4ae9-922c-55a89e049741");
                    geoLocationBoxContext.put("westBoundLongitude", "https://schema.metadatacenter.org/properties/dd27b92a-04d1-435c-bab1-b6acdc1df9eb");
                    geoLocationBoxContext.put("northBoundLatitude", "https://schema.metadatacenter.org/properties/5f41ba88-3fb0-4871-88ef-23d0a7f1326e");
                    geoLocationBoxContext.put("southBoundLatitude", "https://schema.metadatacenter.org/properties/a4171ee9-162e-4e32-89dd-cad62d60f4d9");
                    geoLocationBox.setContext(geoLocationBoxContext);
                    geoLocationBox.setId("https://repo.metadatacenter.org/template-element-instances/c210ff7d-9ebc-4029-a852-33ed411c5883");

                    geoLocation.setGeoLocationBox(geoLocationBox);
                }

                //set up geoLocationPoint
                if (g.getGeoLocationPoint()!=null){
                    Point geoLocationPoint = new Point();
                    FloatFormat pointLongitude = new FloatFormat();
                    FloatFormat pointLatitude = new FloatFormat();
                    pointLongitude.setValue(g.getGeoLocationPoint().getPointLongitude());
                    pointLongitude.setType(DECIMAL_TYPE);
                    pointLatitude.setValue(g.getGeoLocationPoint().getPointLatitude());
                    pointLatitude.setType(DECIMAL_TYPE);
                    geoLocationPoint.setPointLatitude(pointLatitude);
                    geoLocationPoint.setPointLongitude(pointLongitude);
                    Map<String, String> pointContext = new HashMap<>();
                    pointContext.put("pointLatitude", "https://schema.metadatacenter.org/properties/9f74832e-0eb9-4b3b-bb73-8c9e4f4fa52a");
                    pointContext.put("pointLongitude", "https://schema.metadatacenter.org/properties/89e06703-c7b7-420e-b24a-48969fb58ccb");
                    geoLocationPoint.setContext(pointContext);
                    geoLocationPoint.setId("https://repo.metadatacenter.org/template-element-instances/bdc1c54b-c4b9-48f2-945e-dc7dd1f37916");

                    geoLocation.setGeoLocationPoint(geoLocationPoint);
                }

                //set up geoLocationPlace
                ValueFormat geoLocationPlace = new ValueFormat();
                geoLocationPlace.setValue(g.getGeoLocationPlace());
                geoLocation.setGeoLocationPlace(geoLocationPlace);

                Map<String, String> geoLocationContext = new HashMap<>();
                geoLocationContext.put("Geo Location Box", "https://schema.metadatacenter.org/properties/5fa30be5-6669-4565-83e6-3d9a819542cf");
                geoLocationContext.put("Geo Location Point", "https://schema.metadatacenter.org/properties/41febfbd-fa3c-40ce-b94e-30ba4009a950");
                geoLocationContext.put("geoLocationPlace", "https://schema.metadatacenter.org/properties/37cbffe7-b904-472d-bd36-887b003177ab");
                geoLocation.setContext(geoLocationContext);
                geoLocation.setId("https://repo.metadatacenter.org/template-element-instances/a2197127-a8f6-4381-aa97-accf909cd67c");

                geoLocationList.add(geoLocation);
            }
        }
        return geoLocationList;
    }

    private static List<FundingReference> parseFundingReferenceValue(List<DataCiteFundingReference> dataCiteFundingReferences){
        List<FundingReference> fundingReferenceList = new ArrayList<>();

        if(dataCiteFundingReferences!=null && !dataCiteFundingReferences.isEmpty()){
            for (DataCiteFundingReference f : dataCiteFundingReferences){
                FundingReference fundingReference = new FundingReference();
                ValueFormat awardTitle = new ValueFormat(),
                    funderName = new ValueFormat(),
                    awardNumberField = new ValueFormat(),
                    funderIdentifierField = new ValueFormat();
                SchemeURI awardUri = new SchemeURI(),
                    funderSchemeUri = new SchemeURI();
                IdFormat funderIdentifierType= new IdFormat();

                awardTitle.setValue(f.getAwardTitle());
                funderName.setValue(f.getFunderName());
                awardNumberField.setValue(f.getAwardNumber());
                funderIdentifierField.setValue(f.getFunderIdentifier());
                awardUri.setId(f.getAwardUri());
                funderSchemeUri.setId(f.getSchemeUri());

                String funderIdentifierTypeValue = f.getFunderIdentifierType();
                if (funderIdentifierTypeValue != null){
                    funderIdentifierType.setLabel(funderIdentifierTypeValue);
                    switch (funderIdentifierTypeValue) {
                        case "GRID":
                            funderIdentifierType.setId("https://www.grid.ac/");
                            break;
                        case "ISNI":
                            funderIdentifierType.setId("http://id.loc.gov/ontologies/bibframe/Isni");
                            break;
                        case "ROR":
                            funderIdentifierType.setId("http://purl.obolibrary.org/obo/BE_ROR");
                            break;
                        case "Other": funderIdentifierType.setId("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C17649");
                            break;
                    }
                }

                fundingReference.setAwardTitle(awardTitle);
                fundingReference.setFunderName(funderName);
                fundingReference.setAwardNumber(awardNumberField);
                fundingReference.setFunderIdentifier(funderIdentifierField);
                fundingReference.setAwardURI(awardUri);
                fundingReference.setSchemeURI(funderSchemeUri);
                fundingReference.setFunderIdentifierType(funderIdentifierType);

                Map<String, String> funderContext = new HashMap<>();
                funderContext.put("awardTitle", "https://schema.metadatacenter.org/properties/9afc27b4-407a-4079-98c5-e7669f012d69");
                funderContext.put("funderName", "https://schema.metadatacenter.org/properties/18666fcd-6f8e-4886-90f7-153d1193985c");
                funderContext.put("funderIdentifier", "https://schema.metadatacenter.org/properties/67927c8d-4572-4ba5-b5da-0c1d89f77c7c");
                funderContext.put("funderIdentifierType", "https://schema.metadatacenter.org/properties/a666abd0-8f05-43d7-9aea-dab007d4454a");
                funderContext.put("SchemeURI", "https://schema.metadatacenter.org/properties/9146dbf5-8e4e-4dd4-ab0d-d573509fc60d");
                funderContext.put("awardNumber", "https://schema.metadatacenter.org/properties/3aa01f27-0495-43e9-9bf4-b89c8ce8ea32");
                funderContext.put("awardURI", "https://schema.metadatacenter.org/properties/30a02941-ea11-433d-b145-c1694997e152");

                fundingReference.setContext(funderContext);
                fundingReference.setId("https://repo.metadatacenter.org/template-element-instances/b4b9390b-dfa0-4c69-b87e-3b89478f54c7");

                fundingReferenceList.add(fundingReference);
            }
        }
        return fundingReferenceList;
    }

    private static List<Creator> parseRelatedItemCreatorValue(List<DataCiteCreator> dataCiteCreators){
        List<Creator> creatorList = new ArrayList<>();

        if(dataCiteCreators!= null && !dataCiteCreators.isEmpty()){
            for (DataCiteCreator c : dataCiteCreators) {
                Creator creator = new Creator();
                // retrieve values from DataCiteScheme
                ValueFormat name = new ValueFormat(), givenName = new ValueFormat(), familyName = new ValueFormat();
                IdFormat nameType = new IdFormat();
                name.setValue(c.getName());
                String dataCiteNameType = c.getNameType();
                if (dataCiteNameType != null) {
                    nameType.setLabel(dataCiteNameType);
                    if (dataCiteNameType.equals("Personal")){
                        nameType.setId(PERSOANL_ID_URL);
                    } else{
                        nameType.setId(ORGANIZATIONAL_ID_URL);
                    }
                }
                givenName.setValue(c.getGivenName());
                familyName .setValue(c.getFamilyName());

                //set values to CEDARInstance
                creator.setCreatorName(name);
                creator.setNameType(nameType);
                creator.setGivenName(givenName);
                creator.setFamilyName(familyName);

                //set @context and @id
                Map<String, String> context = new HashMap<>();
                context.put("creatorName", "https://schema.metadatacenter.org/properties/bfe3ce47-a7dc-43d6-85d7-74aad2cf2193");
                context.put("familyName", "https://schema.metadatacenter.org/properties/9c23ca96-f662-4651-8f71-8a964a6070a0");
                context.put("givenName", "https://schema.metadatacenter.org/properties/7ed14f4e-e78e-4ccc-a5a7-986ad2ad5d87");
                context.put("nameType", "https://schema.metadatacenter.org/properties/7797c67e-e0fa-4b84-a8d8-1d4e4af1528d");
                creator.setContext(context);
                creator.setId("https://repo.metadatacenter.org/template-element-instances/1d5fbeec-9b22-460c-8cbb-31698f3d949b");

                creatorList.add(creator);
            }
        }
        return creatorList;
    }

    private static List<Contributor> parserRelatedItemContributorValue(List<DataCiteRelatedItemContributor> dataCiteRelatedItemContributors){
        List<Contributor> contributorList = new ArrayList<>();
        if(dataCiteRelatedItemContributors!=null && !dataCiteRelatedItemContributors.isEmpty()){
            for (DataCiteRelatedItemContributor c : dataCiteRelatedItemContributors) {
                Contributor contributor = new Contributor();
                // retrieve values from DataCiteScheme
                ValueFormat name = new ValueFormat(), givenName = new ValueFormat(), familyName = new ValueFormat();
                IdFormat nameType = new IdFormat(), contributorType = new IdFormat();
                name.setValue(c.getName());
                String dataCiteNameType = c.getNameType();
                if (dataCiteNameType != null) {
                    nameType.setLabel(dataCiteNameType);
                    if (dataCiteNameType.equals("Personal")){
                        nameType.setId(PERSOANL_ID_URL);
                    } else{
                        nameType.setId(ORGANIZATIONAL_ID_URL);
                    }
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

                //set @context and @id
                Map<String, String> context = new HashMap<>();
                context.put("contributorName", "https://schema.metadatacenter.org/properties/7fedc824-9cf4-49c9-8c99-2cc49b291074");
                context.put("contributorType", "https://schema.metadatacenter.org/properties/b44ecdc0-b566-4125-bb14-16323bfe7040");
                context.put("familyName", "https://schema.metadatacenter.org/properties/e26a346a-b2f4-4f0d-a7ff-1ed02badc3be");
                context.put("givenName", "https://schema.metadatacenter.org/properties/e8598641-9631-414d-9179-350eae3895b0");
                context.put("nameType", "https://schema.metadatacenter.org/properties/acbdae1a-e966-4e79-b6fb-1dfb549ba5b0");
                contributor.setContext(context);
                contributor.setId("https://repo.metadatacenter.org/template-element-instances/5cde85a4-97e0-47ba-a9df-96cadf2e7212");

                contributorList.add(contributor);
            }
        }
        return contributorList;
    }

    private static List<Title> parseRelatedItemTitleValue(List<DataCiteTitle> dataCiteTitles){
        List<Title> titleList = new ArrayList<>();

        if(dataCiteTitles!=null && !dataCiteTitles.isEmpty()){
            for (DataCiteTitle t : dataCiteTitles) {
                Title title = new Title();
                ValueFormat titleName = new ValueFormat();
                IdFormat titleType = new IdFormat();

                titleName.setValue(t.getTitle());
                if(t.getTitleType()!=null){
                    titleType.setLabel(t.getTitleType());
                    titleType.setId(DATACITE_ID_URL + t.getTitleType());
                }
                title.setTitleName(titleName);
                title.setTitleType(titleType);

                //set @context and @id
                Map<String, String> context = new HashMap<>();
                context.put("titleName", "https://schema.metadatacenter.org/properties/e6b197ea-6b46-4067-bd71-db0cf6f26c48");
                context.put("titleType", "https://schema.metadatacenter.org/properties/cffe1475-7c26-4911-b04d-f84cc4df5e34");
                title.setContext(context);
                title.setId("https://repo.metadatacenter.org/template-element-instances/ca17d8d1-089a-4ebd-82c4-2cd44cd518e4");

                titleList.add(title);
            }
        }
        return titleList;
    }

    private static RelatedIdentifier parseRelatedItemIdentifier(DataCiteRelatedItemIdentifier dataCiteRelatedItemIdentifier){
        RelatedIdentifier relatedIdentifier = new RelatedIdentifier();
        ValueFormat identifier = new ValueFormat(),
            relatedMetadataScheme = new ValueFormat(),
            schemeType = new ValueFormat();
        IdFormat identifierType = new IdFormat();
        SchemeURI schemeUri = new SchemeURI();

        if(dataCiteRelatedItemIdentifier != null){
            identifier.setValue(dataCiteRelatedItemIdentifier.getRelatedItemIdentifier());
            identifierType.setLabel(dataCiteRelatedItemIdentifier.getRelatedItemIdentifierType());
            identifierType.setId(DATACITE_ID_URL + dataCiteRelatedItemIdentifier.getRelatedItemIdentifierType());
            relatedMetadataScheme.setValue(dataCiteRelatedItemIdentifier.getRelatedMetadataScheme());
            schemeType.setValue(dataCiteRelatedItemIdentifier.getSchemeType());
            schemeUri.setId(dataCiteRelatedItemIdentifier.getSchemeUri());
        }

        relatedIdentifier.setRelatedIdentifier(identifier);
        relatedIdentifier.setRelatedIdentifierType(identifierType);
        relatedIdentifier.setRelatedMetadataScheme(relatedMetadataScheme);
        relatedIdentifier.setSchemeType(schemeType);
        relatedIdentifier.setSchemeURi(schemeUri);

        //set @context and @id
        Map<String, String> context = new HashMap<>();
        context.put("RelatedIdentifier", "https://schema.metadatacenter.org/properties/0e738973-5012-4ce4-9b0f-ba81d9aea079");
        context.put("relatedIdentifierType", "https://schema.metadatacenter.org/properties/9f3e7515-40c9-4299-ab4c-02c0b90342fd");
        context.put("relatedMetadataScheme", "https://schema.metadatacenter.org/properties/01cf1698-424c-4f2e-a869-544e8e46a04a");
        context.put("schemeType", "https://schema.metadatacenter.org/properties/f9d42802-6c45-40d6-bd6f-ef2168e028d4");
        context.put("schemeURI", "https://schema.metadatacenter.org/properties/c0ab6d2c-043d-436b-be7c-cbc8694a6e03");
        relatedIdentifier.setContext(context);
        relatedIdentifier.setId("https://repo.metadatacenter.org/template-element-instances/c0191f61-ec09-4102-8fa2-3e77e1d8304a");
        return relatedIdentifier;
    }

    private static List<RelatedItem> parseRelatedItemValue(List<DataCiteRelatedItem> dataCiteRelatedItems){
        List<RelatedItem> relatedItemList = new ArrayList<>();

        if(dataCiteRelatedItems!=null && !dataCiteRelatedItems.isEmpty()){
            for (DataCiteRelatedItem r : dataCiteRelatedItems){
                RelatedItem relatedItem = new RelatedItem();

                IdFormat relatedItemType = new IdFormat(),
                    relationType = new IdFormat(),
                    numberType = new IdFormat(),
                    identifierType = new IdFormat();
                ValueFormat volume = new ValueFormat(),
                    issue = new ValueFormat(),
                    firstPage = new ValueFormat(),
                    lastPage = new ValueFormat(),
                    publisher = new ValueFormat(),
                    edition = new ValueFormat(),
                    number = new ValueFormat(),
                    identifier = new ValueFormat(),
                    relatedMetadataScheme = new ValueFormat(),
                    schemeType = new ValueFormat();
                SchemeURI schemeUri = new SchemeURI();

                Integer givenYear = r.getPublicationYear();
                PublicationYear publicationYear = new PublicationYear();
                if (givenYear != null){
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, givenYear);
                    calendar.set(Calendar.MONTH, Calendar.JANUARY);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    publicationYear.setValue(formatter.format(calendar.getTime()));
                    publicationYear.setType("xsd:date");
                }

                if(r.getRelatedItemType()!=null){
                    relatedItemType.setLabel(r.getRelatedItemType());
                    relatedItemType.setId(DATACITE_ID_URL + r.getRelatedItemType());
                }
                if(r.getRelationType()!=null){
                    relationType.setLabel(r.getRelationType());
                    relationType.setId(DATACITE_ID_URL + r.getRelationType());
                }
                if(r.getRelatedItemIdentifier().getRelatedItemIdentifierType()!=null){
                    identifierType.setLabel(r.getRelatedItemIdentifier().getRelatedItemIdentifierType());
                    identifierType.setId(DATACITE_ID_URL + r.getRelatedItemIdentifier().getRelatedItemIdentifierType());
                }
                volume.setValue(r.getVolume());
                issue.setValue(r.getIssue());
                firstPage.setValue(r.getFirstPage());
                lastPage.setValue(r.getLastPage());
                publisher.setValue(r.getPublisher());
                edition.setValue(r.getEdition());
                identifier.setValue(r.getRelatedItemIdentifier().getRelatedItemIdentifier());
                relatedMetadataScheme.setValue(r.getRelatedItemIdentifier().getRelatedMetadataScheme());
                schemeType.setValue(r.getRelatedItemIdentifier().getSchemeType());
                schemeUri.setId(r.getRelatedItemIdentifier().getSchemeUri());
                number.setValue(r.getNumber());
                String givenNumberType = r.getNumberType();
                if (givenNumberType != null){
                    numberType.setLabel(givenNumberType);
                    switch (givenNumberType) {
                        case "Report":
                            numberType.setId("http://purl.bioontology.org/ontology/SNOMEDCT/229059009");
                            break;
                        case "Article":
                            numberType.setId("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C47902");
                            break;
                        case "Chapter":
                            numberType.setId("http://purl.org/ontology/bibo/Chapter");
                            break;
                        case "Others":
                            numberType.setId("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C17649");
                            break;
                    }
                }

                relatedItem.setRelatedItemType(relatedItemType);
                relatedItem.setRelationType(relationType);
                relatedItem.setRelatedIdentifierType(identifierType);
                relatedItem.setVolume(volume);
                relatedItem.setIssue(issue);
                relatedItem.setFirstPage(firstPage);
                relatedItem.setLastPage(lastPage);
                relatedItem.setPublisher(publisher);
                relatedItem.setEdition(edition);
                relatedItem.setNumber(number);
                relatedItem.setNumberType(numberType);
                relatedItem.setRelatedIdentifier(identifier);
                relatedItem.setRelatedMetadataScheme(relatedMetadataScheme);
                relatedItem.setSchemeType(schemeType);
                relatedItem.setSchemeURi(schemeUri);
                relatedItem.setPublicationYear(publicationYear);
                relatedItem.setCreators(parseRelatedItemCreatorValue(r.getCreators()));
                relatedItem.setContributors(parserRelatedItemContributorValue(r.getContributors()));
                relatedItem.setTitles(parseRelatedItemTitleValue(r.getTitles()));

                Map<String, String> context = new HashMap<>();
                context.put("Contributor", "https://schema.metadatacenter.org/properties/b5c80c42-6fb3-40c6-b995-2f40eb3a2e5f");
                context.put("Creator", "https://schema.metadatacenter.org/properties/d4b21626-059a-4241-b0eb-c27ca857ba5d");
                context.put("PublicationYear", "https://schema.metadatacenter.org/properties/2d6ae075-bf91-4f28-9288-7e3daed7d5cd");
                context.put("Publisher", "https://schema.metadatacenter.org/properties/26dfc091-8eed-4411-bd4f-7e867c3ea26e");
                context.put("Title", "https://schema.metadatacenter.org/properties/93b034c4-c719-4d5f-9963-c8297c081863");
                context.put("edition", "https://schema.metadatacenter.org/properties/97dd4fbb-b5c2-4d80-b410-b48315470d9c");
                context.put("firstPage", "https://schema.metadatacenter.org/properties/344171db-4639-4c95-9785-70a0182b3cba");
                context.put("issue", "https://schema.metadatacenter.org/properties/e402cccc-d8b2-41a3-baf3-516a296e780c");
                context.put("lastPage", "https://schema.metadatacenter.org/properties/4f8e526e-98bd-46c6-b42f-a569c5cc093b");
                context.put("number", "https://schema.metadatacenter.org/properties/fbf488d1-8dd3-4eac-bf7a-d27999756900");
                context.put("numberType", "https://schema.metadatacenter.org/properties/96efcb35-06cd-4c37-b12c-6247def3bf76");
                context.put("relatedItemType", "https://schema.metadatacenter.org/properties/234f4448-88d6-461c-9ea0-fd465cdbbbf1");
                context.put("relationType", "https://schema.metadatacenter.org/properties/4e314807-2064-4af1-88c5-e411549c24fd");
                context.put("volume", "https://schema.metadatacenter.org/properties/9bdeae88-1488-468a-b94a-e15470e2a9cf");
                context.put("relatedIdentifier", "https://schema.metadatacenter.org/properties/6424638b-acd5-42b2-ba56-f9eb905f8fd2");
                context.put("relatedIdentifierType", "https://schema.metadatacenter.org/properties/11e459bf-29de-48bb-8951-01e5c5883a76");
                context.put("relatedMetadataScheme", "https://schema.metadatacenter.org/properties/a146013c-626f-43e7-a61c-8fad6da3a445");
                context.put("schemeURI", "https://schema.metadatacenter.org/properties/46505fed-903b-462f-ae3a-6db6337b8ff4");
                context.put("schemeType", "https://schema.metadatacenter.org/properties/33495b52-778e-49ca-8d49-b19b85006c80");
                relatedItem.setContext(context);
                relatedItem.setId("https://repo.metadatacenter.org/template-element-instances/e20355b6-2c2c-480a-b258-268564158b23");

                relatedItemList.add(relatedItem);
            }
        }
        return relatedItemList;
    }

    private static Context setContext(){
        Context context = new Context();
        context.setRdfs("http://www.w3.org/2000/01/rdf-schema#");
        context.setXsd("http://www.w3.org/2001/XMLSchema#");
        context.setPav("http://purl.org/pav/");
        context.setSchema("http://schema.org/");
        context.setOslc("http://open-services.net/ns/core#");
        context.setSkos("http://www.w3.org/2004/02/skos/core#");

        TypeFormat label = new TypeFormat(),
            isBasedOn = new TypeFormat(),
            name = new TypeFormat(),
            description = new TypeFormat(),
            derivedFrom = new TypeFormat(),
            createdOn = new TypeFormat(),
            createdBy = new TypeFormat(),
            lastUpdatedOn = new TypeFormat(),
            modifiedBy = new TypeFormat(),
            notation = new TypeFormat();

        label.setType("xsd:string");
        isBasedOn.setType("@id");
        name.setType("xsd:string");
        description.setType("xsd:string");
        derivedFrom.setType("@id");
        createdOn.setType("xsd:dateTime");
        createdBy.setType("@id");
        lastUpdatedOn.setType("xsd:dateTime");
        modifiedBy.setType("@id");
        notation.setType("xsd:string");

        context.setLabel(label);
        context.setIsBasedOn(isBasedOn);
        context.setName(name);
        context.setDescription(description);
        context.setDerivedFrom(derivedFrom);
        context.setCreatedOn(createdOn);
        context.setCreatedBy(createdBy);
        context.setLastUpdatedOn(lastUpdatedOn);
        context.setModifiedBy(modifiedBy);
        context.setNotation(notation);

        context.setPrefix("https://schema.metadatacenter.org/properties/c5c19810-b058-48cf-b0ae-de2e9f82a95e");
        context.setCreator("https://schema.metadatacenter.org/properties/fcbe7f27-e429-4204-9e02-c743fe13fa01");
        context.setTitle("https://schema.metadatacenter.org/properties/88556557-7e94-4738-8fe7-1a8bd0f5ad0a");
        context.setPublisher("https://schema.metadatacenter.org/properties/0bc8215e-6b07-42c0-9613-9209f5393b08");
        context.setPublicationYear("https://schema.metadatacenter.org/properties/7fe11cba-9053-42b6-aa8e-3ed25e951600");
//        context.setResourceType("https://schema.metadatacenter.org/properties/20792991-6163-40fd-8352-5d9b6d24ff26");
        context.setSubject("https://schema.metadatacenter.org/properties/6c9e03ad-9629-424e-a894-0b47f6e64a0f");
        context.setContributor("https://schema.metadatacenter.org/properties/f422fff1-11c0-4533-8e7f-2fb0dbaea367");
        context.setDateElement("https://schema.metadatacenter.org/properties/fd947c0a-9d99-4a72-ae9c-84e09ac64f14");
        context.setLanguage("https://schema.metadatacenter.org/properties/3399c7e8-80cd-4c75-973e-afaec9bf78f8");
        context.setAlternateIdentifierElement("https://schema.metadatacenter.org/properties/2e854cb7-4846-4848-9ab1-be35e96a1a35");
        context.setRelatedIdentifierElement("https://schema.metadatacenter.org/properties/51774857-8b6a-4577-890c-eca0f3670eb0");
        context.setSize("https://schema.metadatacenter.org/properties/618f3be1-ad89-4032-b803-2ef24a3ff31b");
        context.setFormat("https://schema.metadatacenter.org/properties/dfeb2217-9a0e-4aff-b429-3c03e61774df");
        context.setVersion("https://schema.metadatacenter.org/properties/4354b3d4-9c46-4ad8-a9c3-09c3e5bd6adc");
        context.setRightsElement("https://schema.metadatacenter.org/properties/bbdae27a-aacb-4934-8c6b-0251cf8d4191");
        context.setDescriptionElement("https://schema.metadatacenter.org/properties/793c5f5b-db52-448b-a867-f6e12411acfe");
        context.setGeoLocation("https://schema.metadatacenter.org/properties/7052fcee-8170-40f1-a4c3-f88a6da60148");
        context.setFundingReference("https://schema.metadatacenter.org/properties/e12e7d4a-332b-4d2c-9bd4-028e5edb5abe");
        context.setRelatedItem("https://schema.metadatacenter.org/properties/938f153e-6500-440d-8817-19066408dfff");

        return context;
    }


}
