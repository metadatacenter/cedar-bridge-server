package org.metadatacenter.cedar.bridge.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.metadatacenter.cedar.bridge.resource.CEDARProperties.CEDARDataCiteInstance;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.*;


import java.io.File;
import java.io.IOException;
import java.util.*;

public class CompareValues {
  public static void main(String[] args) {
    ObjectMapper objectMapper = new ObjectMapper();
    String givenMetadataFilePath = "/Users/ycao77/CEDAR/cedar-bridge-server/cedar-bridge-server-core/src/main/java/org/metadatacenter/cedar/bridge/resource/JSONFiles/GivenRichMetaData.json";
    String responseMetadataFilePath = "/Users/ycao77/CEDAR/cedar-bridge-server/cedar-bridge-server-core/src/main/java/org/metadatacenter/cedar/bridge/resource/JSONFiles/ResponseRichMetaData.json";

    try{
      JsonNode givenMetadata = objectMapper.readTree(new File(givenMetadataFilePath));
      JsonNode responseMetadata = objectMapper.readTree(new File(responseMetadataFilePath));
      boolean result = compareResponseWithGivenMetadata(givenMetadata, responseMetadata);
      System.out.println(result);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public static boolean compareResponseWithGivenMetadata(JsonNode givenMetadata, JsonNode responseMetadata) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    try {
      //Deserialize givenMetadata to CedarDataCiteInstance Class
      String givenMetadataString = givenMetadata.toString();
      CEDARDataCiteInstance cedarInstance = mapper.readValue(givenMetadataString, CEDARDataCiteInstance.class);
      DataCiteSchema cedarConvertedDataCiteSchema = new DataCiteSchema();

      // Pass the value from cedarDataCiteInstance to dataCiteRequest
      CedarInstanceParser.parseCEDARInstance(cedarInstance, cedarConvertedDataCiteSchema);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      String cedarConvertedDataCiteSchemaString = mapper.writeValueAsString(cedarConvertedDataCiteSchema);
      System.out.println("Cedar Converted DataCite Schema: " + cedarConvertedDataCiteSchemaString);

      //Deserialize responseMetadata to DtaCiteSchema Class
      String responseMetadaString = responseMetadata.toString();
      DataCiteSchema responseDataCiteSchema = mapper.readValue(responseMetadaString, DataCiteSchema.class);
      String responseConvertedDataCiteSchemaString = mapper.writeValueAsString(responseDataCiteSchema);
      System.out.println("DataCite response converted Schema: " + responseConvertedDataCiteSchemaString);


      //Compare cedarConvertedDataCiteSchema vs responseDataCiteSchema
//      if (!(compareCreators(cedarConvertedDataCiteSchema.getData().getAttributes().getCreators(), responseDataCiteSchema.getData().getAttributes().getCreators())
      if (!(true
          && compareTitles(cedarConvertedDataCiteSchema.getData().getAttributes().getTitles(), responseDataCiteSchema.getData().getAttributes().getTitles())
          && comparePublisher(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && comparePublicationYear(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareResourceType(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareSubjects(cedarConvertedDataCiteSchema, responseDataCiteSchema)
//          && compareContributors(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareDates(cedarConvertedDataCiteSchema, responseDataCiteSchema)
//          && compareLang(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareAlternateIdentifier(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareRelatedIdentifier(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareSizes(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareFormat(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareVersion(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareRights(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareDescriptions(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareGeoLocations(cedarConvertedDataCiteSchema, responseDataCiteSchema)
//          && compareFundingReferences(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareRelatedItems(cedarConvertedDataCiteSchema, responseDataCiteSchema)
      )){
        System.out.println("The response metadata is different with the given metadata");
        return false;
      } else{
        System.out.println("The response metadata is the same with the given metadata");
        return true;
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean compareAffiliations(List<DataCiteAffiliation> givenAffiliations, List<DataCiteAffiliation> responseAffiliations){
    boolean equals = true;

    if (givenAffiliations == null || givenAffiliations.isEmpty()) {
      return responseAffiliations == null || responseAffiliations.isEmpty();
    }

    if (givenAffiliations.size() != responseAffiliations.size()){
      System.out.println("given affiliation size: " + givenAffiliations.size());
      System.out.println("response affiliation size: " + responseAffiliations.size());
      return false;
    }

    Comparator<DataCiteAffiliation> comparator = Comparator.comparing(DataCiteAffiliation::getAffiliationIdentifier);
    givenAffiliations.sort(comparator);
    responseAffiliations.sort(comparator);

    for (int i =0; i < givenAffiliations.size(); i++){
      DataCiteAffiliation givenAffiliation = givenAffiliations.get(i);
      DataCiteAffiliation responseAffiliation = responseAffiliations.get(i);

      String givenAffiliationName = givenAffiliation.getAffiliation();
      String responseAffiliationName = responseAffiliation.getAffiliation();

      String givenAffiliationIdentifier = givenAffiliation.getAffiliationIdentifier();
      String responseAffiliationIdentifier = responseAffiliation.getAffiliationIdentifier();

      String givenAffiliationIdentifierScheme = givenAffiliation.getAffiliationIdentifierScheme();
      String responseAffiliationIdentifierScheme = responseAffiliation.getAffiliationIdentifierScheme();


      if (!(Objects.equals(givenAffiliationName, responseAffiliationName)
          && Objects.equals(givenAffiliationIdentifier, responseAffiliationIdentifier)
          && Objects.equals(givenAffiliationIdentifierScheme, responseAffiliationIdentifierScheme))){
        equals = false;
        System.out.println("given: " + givenAffiliationName + " " + givenAffiliationIdentifier + " " + givenAffiliationIdentifierScheme);
        System.out.println("response: " + responseAffiliationName + " " + responseAffiliationIdentifier + " " + responseAffiliationIdentifierScheme);
        break;
      }
    }
    return equals;
  }

  private static boolean compareNameIdentifiers(List<DataCiteNameIdentifier> givenNameIdentifiers, List<DataCiteNameIdentifier> responseNameIdentifiers){
    boolean equals = true;

    if (givenNameIdentifiers == null || givenNameIdentifiers.isEmpty()){
      return responseNameIdentifiers == null || responseNameIdentifiers.isEmpty();
    }

    if (givenNameIdentifiers.size() != responseNameIdentifiers.size()) {
      System.out.println("name identifier size are diff");
      return false;
    }

    Comparator<DataCiteNameIdentifier> comparator = Comparator.comparing(DataCiteNameIdentifier::getNameIdentifier);
    givenNameIdentifiers.sort(comparator);
    responseNameIdentifiers.sort(comparator);

    for (int i=0; i<givenNameIdentifiers.size(); i ++) {
      DataCiteNameIdentifier givenDataCiteNameIdentifier = givenNameIdentifiers.get(i);
      DataCiteNameIdentifier responseDataCiteNameIdentifier = responseNameIdentifiers.get(i);

      String givenNameIdentifier = givenDataCiteNameIdentifier.getNameIdentifier();
      String responseNameIdentifier = responseDataCiteNameIdentifier.getNameIdentifier();

      String givenNameIdentifierScheme = givenDataCiteNameIdentifier.getNameIdentifierScheme();
      String responseNameIdentifierScheme = responseDataCiteNameIdentifier.getNameIdentifierScheme();

      String givenSchemeUri = givenDataCiteNameIdentifier.getSchemeUri();
      String responseSchemeUri = responseDataCiteNameIdentifier.getSchemeUri();

      if (!(Objects.equals(givenNameIdentifier, responseNameIdentifier)
          && Objects.equals(givenNameIdentifierScheme, responseNameIdentifierScheme)
          && Objects.equals(givenSchemeUri, responseSchemeUri))) {
        equals = false;
        System.out.println("given name identifier: " + givenNameIdentifier + " " + givenNameIdentifierScheme + " " + givenSchemeUri);
        System.out.println("response name identifier: " + responseNameIdentifier + " " + responseNameIdentifierScheme + " " + responseSchemeUri);
        break;
      }
    }
    return equals;
  }


  private static boolean compareCreators(List<DataCiteCreator> givenCreators, List<DataCiteCreator> responseCreators){
    boolean equals = true;

    if (givenCreators == null || givenCreators.isEmpty()){
      return responseCreators == null || responseCreators.isEmpty();
    }

    if (givenCreators.size() != responseCreators.size()){
      System.out.println("given creators size is: " + givenCreators.size());
      System.out.println("response creators size is:" + responseCreators.size());
      return false;
    }

    // Sort based on the name property
    Comparator<DataCiteCreator> comparator = Comparator.comparing(DataCiteCreator::getName);
    givenCreators.sort(comparator);
    responseCreators.sort(comparator);

    for (int i=0; i < givenCreators.size(); i++ ){
      DataCiteCreator givenCreator = givenCreators.get(i);
      DataCiteCreator responseCreator = responseCreators.get(i);

      String givenName = givenCreator.getName();
      String responseName = responseCreator.getName();

      String givenNameType = givenCreator.getNameType();
      String responseNameType = responseCreator.getNameType();

      String givenGivenName = givenCreator.getGivenName();
      String responseGivenName = responseCreator.getGivenName();

      String givenFamilyName = givenCreator.getFamilyName();
      String responseFamilyName = responseCreator.getFamilyName();

      List<DataCiteAffiliation> givenAffiliations = givenCreator.getAffiliations();
      List<DataCiteAffiliation> responseAffiliations = responseCreator.getAffiliations();

      List<DataCiteNameIdentifier> givenNameIdentifiers = givenCreator.getNameIdentifiers();
      List<DataCiteNameIdentifier> responseNameIdentifiers = responseCreator.getNameIdentifiers();

      if (!(Objects.equals(givenCreator.getName(), responseCreator.getName())
          && Objects.equals(givenNameType, responseNameType)
          && Objects.equals(givenGivenName, responseGivenName)
          && Objects.equals(givenFamilyName, responseFamilyName)
          && compareAffiliations(givenAffiliations, responseAffiliations)
          && compareNameIdentifiers(givenNameIdentifiers, responseNameIdentifiers))) {
        equals = false;
        System.out.println("equals of creator: " + equals);
        System.out.println("given creator: " + givenName + " " + givenFamilyName + " " + givenGivenName + " " + givenNameType);
        System.out.println("response creator: " + responseName + " " + responseFamilyName + " " + responseGivenName + " " + responseNameType);
        break;
      }
    }
    return equals;
  }

  private static boolean compareTitles(List<DataCiteTitle> givenTitles, List<DataCiteTitle> responseTitles){
      System.out.println("----------------Comparing titles");
      boolean equals = true;

      if (givenTitles == null || givenTitles.isEmpty()){
        return responseTitles == null || responseTitles.isEmpty();
      }

      if (givenTitles.size() != responseTitles.size()){
        System.out.println("given titles size is: " + givenTitles.size());
        System.out.println("response titles size is: " + responseTitles.size());
        return false;
      }

      Comparator<DataCiteTitle> comparator = Comparator.comparing(DataCiteTitle::getTitle);
      givenTitles.sort(comparator);
      responseTitles.sort(comparator);

      for(int i=0; i < givenTitles.size(); i++){
        DataCiteTitle givenTitle = givenTitles.get(i);
        DataCiteTitle responseTitle = responseTitles.get(i);

        String givenTitleName = givenTitle.getTitle();
        String responseTitleName = responseTitle.getTitle();

        String givenTitleType = givenTitle.getTitleType();
        String responseTitleType = responseTitle.getTitleType();

        String givenLang = givenTitle.getLang();
        String responseLang = responseTitle.getLang();

        if (! (Objects.equals(givenTitleName, responseTitleName))
            && (Objects.equals(givenTitleType, responseTitleType))
            && (Objects.equals(givenLang, responseLang))){
          equals = false;
          System.out.println("given titles: " + givenTitleName + " " + givenTitleType + " " + givenLang);
          System.out.println("response titles: " + responseTitleName + " " + responseTitleType + " " + responseLang);
          break;
        }
      }
      return equals;
  }

  private static boolean comparePublisher(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("--------Comparing publisher");
    return Objects.equals(cedarConvertedDataCiteSchema.getData().getAttributes().getPublisher(),
        responseDataCiteSchema.getData().getAttributes().getPublisher());
  }

  private static boolean comparePublicationYear(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema) {
    System.out.println("-----------Comparing publication year");
    return Objects.equals(cedarConvertedDataCiteSchema.getData().getAttributes().getPublicationYear(),
        responseDataCiteSchema.getData().getAttributes().getPublicationYear());
  }

  private static boolean compareResourceType(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("---------Comparing resource type");
    DataCiteType givenType = cedarConvertedDataCiteSchema.getData().getAttributes().getTypes();
    DataCiteType responseType = responseDataCiteSchema.getData().getAttributes().getTypes();

    String givenTitle = givenType.getResourceType();
    String responseTitle = responseType.getResourceType();

    String givenTypeGeneral = givenType.getResourceTypeGeneral();
    String responseTypeGeneral = responseType.getResourceTypeGeneral();

    if (!(Objects.equals(givenTitle, responseTitle) && Objects.equals(givenTypeGeneral, responseTypeGeneral))){
      System.out.println("given type: " + givenTitle + " " + givenTypeGeneral);
      System.out.println("response type: " + responseType + " " + givenTypeGeneral);
      return false;
    }
    return true;
  }

  private static boolean compareSubjects(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("---------Comparing subjects");
    List<DataCiteSubject> givenSubjects = cedarConvertedDataCiteSchema.getData().getAttributes().getSubjects();
    List<DataCiteSubject> responseSubjects = responseDataCiteSchema.getData().getAttributes().getSubjects();
    boolean equals = true;

    if (givenSubjects == null || givenSubjects.isEmpty()){
      return responseSubjects == null || responseSubjects.isEmpty();
    }

    if (givenSubjects.size() != responseSubjects.size()){
      System.out.println("given subjects size is: " + givenSubjects.size());
      System.out.println("Response subjects size is: " + responseSubjects.size());
      return false;
    }

    Comparator<DataCiteSubject> comparator = Comparator.comparing(DataCiteSubject::getSubject);
    givenSubjects.sort(comparator);
    responseSubjects.sort(comparator);

    for (int i=0; i < givenSubjects.size(); i++){
      DataCiteSubject givenSubject = givenSubjects.get(i);
      DataCiteSubject responseSubject = responseSubjects.get(i);

      String givenName = givenSubject.getSubject();
      String responseName = responseSubject.getSubject();

      String givenScheme = givenSubject.getSubjectScheme();
      String responseScheme = responseSubject.getSubjectScheme();

      String givenSchemeUri = givenSubject.getSchemeUri();
      String responseSchemeUri = responseSubject.getSchemeUri();

      String givenCode = givenSubject.getClassificationCode();
      String responseCode = responseSubject.getClassificationCode();

      String givenValueUri = givenSubject.getValueUri();
      String responseValueUri = responseSubject.getValueUri();

      if (!(Objects.equals(givenName, responseName)
          && Objects.equals(givenScheme, responseScheme)
          && Objects.equals(givenSchemeUri, responseSchemeUri)
          && Objects.equals(givenCode, responseCode)
          && Objects.equals(givenValueUri, responseValueUri))){
        System.out.println("given subject: " + givenName + " " + givenScheme + " " + givenSchemeUri + " " + givenCode + " " + givenValueUri);
        System.out.println("response subject: " + responseName + " " + responseScheme + " " + responseSchemeUri + " " + responseCode + " " + responseValueUri);
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareContributors(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("------------Comparing contributors");
    List<DataCiteContributor> givenContributors = cedarConvertedDataCiteSchema.getData().getAttributes().getContributors();
    List<DataCiteContributor> responseContributors = responseDataCiteSchema.getData().getAttributes().getContributors();
    boolean equals = true;

    if (givenContributors == null || givenContributors.isEmpty()){
      return responseContributors == null || responseContributors.isEmpty();
    }

    if (givenContributors.size() != responseContributors.size()){
      System.out.println("given Contributors size is: " + givenContributors.size());
      System.out.println("Response Contributors size is: " + responseContributors.size());
      return false;
    }

    Comparator<DataCiteContributor> comparator = Comparator.comparing(DataCiteContributor::getName);
    givenContributors.sort(comparator);
    responseContributors.sort(comparator);

    for(int i=0; i<givenContributors.size(); i++){
      DataCiteContributor givenContributor = givenContributors.get(i);
      DataCiteContributor responseContributor = responseContributors.get(i);

      String givenName = givenContributor.getName();
      String responseName = responseContributor.getName();

      String givenNameType = givenContributor.getNameType();
      String responseNameType = responseContributor.getNameType();

      String givenGivenName = givenContributor.getGivenName();
      String responseGivenName = responseContributor.getGivenName();

      String givenFamilyName = givenContributor.getFamilyName();
      String responseFamilyName = responseContributor.getFamilyName();

      String givenContributorType = givenContributor.getContributorType();
      String responseContributorType = responseContributor.getContributorType();

      List<DataCiteAffiliation> givenAffiliations = givenContributor.getAffiliations();
      List<DataCiteAffiliation> responseAffiliations = responseContributor.getAffiliations();

      List<DataCiteNameIdentifier> givenNameIdentifiers = givenContributor.getNameIdentifiers();
      List<DataCiteNameIdentifier> responseNameIdentifiers = responseContributor.getNameIdentifiers();


      if (!(Objects.equals(givenName, responseName)
          && Objects.equals(givenNameType, responseNameType)
          && Objects.equals(givenGivenName, responseGivenName)
          && Objects.equals(givenFamilyName, responseFamilyName)
          && Objects.equals(givenContributorType, responseContributorType)
          && compareAffiliations(givenAffiliations, responseAffiliations)
          && compareNameIdentifiers(givenNameIdentifiers, responseNameIdentifiers))) {
        equals = false;
        System.out.println("equals of contributor: " + equals);
        System.out.println("given contributor: " + givenName + " " + givenFamilyName + " " + givenGivenName + " " + givenNameType + " " + givenContributorType);
        System.out.println("response contributor: " + responseName + " " + responseFamilyName + " " + responseGivenName + " " + responseNameType + " " + responseContributorType);
        break;
      }
    }
    return equals;
  }

  private static boolean compareDates(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("------------Comparing dates");
    List<DataCiteDate> givenDates = cedarConvertedDataCiteSchema.getData().getAttributes().getDates();
    List<DataCiteDate> responseDates = responseDataCiteSchema.getData().getAttributes().getDates();
    boolean equals = true;

    if (givenDates == null || givenDates.isEmpty()){
      return responseDates == null || responseDates.isEmpty();
    }

    if (givenDates.size() != responseDates.size()) {
      System.out.println("given dates size :" + givenDates.size());
      System.out.println("response dates size: " + responseDates.size());
      return false;
    }

    Comparator<DataCiteDate> comparator = Comparator.comparing(DataCiteDate::getDate);
    givenDates.sort(comparator);
    responseDates.sort(comparator);

    for (int i=0; i < givenDates.size(); i++){
      DataCiteDate givenDate = givenDates.get(i);
      DataCiteDate responseDate = responseDates.get(i);

      String givenDay = givenDate.getDate();
      String responseDay = responseDate.getDate();

      String givenDateType = givenDate.getDateType();
      String responseDateType = responseDate.getDateType();

      String givenDateInformation = givenDate.getDateInformation();
      String responseDateInformation = responseDate.getDateInformation();

      if (!(Objects.equals(givenDay, responseDay)
          && Objects.equals(givenDateType,responseDateType)
          && Objects.equals(givenDateInformation, responseDateInformation))) {
        System.out.println("given Dates is: " + givenDay + " " + givenDateType + " " + givenDateInformation);
        System.out.println("response Dates is: " + responseDay + " " + responseDateType + " " + responseDateInformation);
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareLang(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("------------Comparing lang");
    System.out.println("given lang: " + cedarConvertedDataCiteSchema.getData().getAttributes().getLanguage());
    System.out.println("response lang: " + responseDataCiteSchema.getData().getAttributes().getLanguage());
    return Objects.equals(cedarConvertedDataCiteSchema.getData().getAttributes().getLanguage(), responseDataCiteSchema.getData().getAttributes().getLanguage());
  }

  private static boolean compareAlternateIdentifier(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("-------------Comparing alternate identifier");
    List<DataCiteAlternateIdentifier> givenAlternateIdentifiers = cedarConvertedDataCiteSchema.getData().getAttributes().getAlternateIdentifiers();
    List<DataCiteAlternateIdentifier> responseAlternateIdentifiers = responseDataCiteSchema.getData().getAttributes().getAlternateIdentifiers();
    boolean equals = true;

    if (givenAlternateIdentifiers == null || givenAlternateIdentifiers.isEmpty()){
      return responseAlternateIdentifiers == null || responseAlternateIdentifiers.isEmpty();
    }

    if (givenAlternateIdentifiers.size() != responseAlternateIdentifiers.size()){
      System.out.println("given AlternateIdentifiers size is: " + givenAlternateIdentifiers.size());
      System.out.println("Response AlternateIdentifiers size is: " + responseAlternateIdentifiers.size());
      return false;
    }

    Comparator<DataCiteAlternateIdentifier> comparator = Comparator.comparing(DataCiteAlternateIdentifier::getAlternateIdentifier);
    givenAlternateIdentifiers.sort(comparator);
    responseAlternateIdentifiers.sort(comparator);

    for (int i=0; i < givenAlternateIdentifiers.size(); i++){
      DataCiteAlternateIdentifier givenAlternateIdentifier = givenAlternateIdentifiers.get(i);
      DataCiteAlternateIdentifier responseAlternateIdentifier = responseAlternateIdentifiers.get(i);

      String givenAlternateIdentifierName = givenAlternateIdentifier.getAlternateIdentifier();
      String responseAlternateIdentifierName = responseAlternateIdentifier.getAlternateIdentifier();

      String givenType = givenAlternateIdentifier.getAlternateIdentifierType();
      String responseType = responseAlternateIdentifier.getAlternateIdentifierType();

      if (!(Objects.equals(givenAlternateIdentifierName, responseAlternateIdentifierName)
          && Objects.equals(givenType, responseType))){
        System.out.println("given AlternateIdentifier: " + givenAlternateIdentifierName + " " + givenType);
        System.out.println("response AlternateIdentifier: " + responseAlternateIdentifierName + " " + responseType);
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareRelatedIdentifier(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("-------------Comparing related Identifier");
    List<DataCiteRelatedIdentifier> givenRelatedIdentifiers = cedarConvertedDataCiteSchema.getData().getAttributes().getRelatedIdentifiers();
    List<DataCiteRelatedIdentifier> responseRelatedIdentifiers = responseDataCiteSchema.getData().getAttributes().getRelatedIdentifiers();
    boolean equals = true;

    if (givenRelatedIdentifiers == null || givenRelatedIdentifiers.isEmpty()){
      return responseRelatedIdentifiers == null || responseRelatedIdentifiers.isEmpty();
    }

    if (givenRelatedIdentifiers.size() != responseRelatedIdentifiers.size()){
      System.out.println("given subjects size is: " + givenRelatedIdentifiers.size());
      System.out.println("Response subjects size is: " + responseRelatedIdentifiers.size());
      return false;
    }

    Comparator<DataCiteRelatedIdentifier> comparator = Comparator.comparing(DataCiteRelatedIdentifier::getRelatedIdentifier);
    givenRelatedIdentifiers.sort(comparator);
    responseRelatedIdentifiers.sort(comparator);

    for (int i=0; i < givenRelatedIdentifiers.size(); i++){
      DataCiteRelatedIdentifier givenAlternateIdentifer = givenRelatedIdentifiers.get(i);
      DataCiteRelatedIdentifier responseAlternateIdentifier = responseRelatedIdentifiers.get(i);

      String givenName = givenAlternateIdentifer.getRelatedIdentifier();
      String responseName = responseAlternateIdentifier.getRelatedIdentifier();

      String givenRelatedIdentifierType = givenAlternateIdentifer.getRelatedIdentifierType();
      String responseRelatedIdentifierType = responseAlternateIdentifier.getRelatedIdentifierType();

      String givenRelationType = givenAlternateIdentifer.getRelationType();
      String responseRelationType = responseAlternateIdentifier.getRelationType();

      String givenRelatedMetaDataScheme = givenAlternateIdentifer.getRelatedMetadataScheme();
      String responseRelatedMetaDataScheme = responseAlternateIdentifier.getRelatedMetadataScheme();

      String givenSchemeUri = givenAlternateIdentifer.getSchemeUri();
      String responseSchemeUri = responseAlternateIdentifier.getSchemeUri();

      String givenSchemeType = givenAlternateIdentifer.getSchemeType();
      String responseSchemeType = responseAlternateIdentifier.getSchemeType();

      String givenResourceTypeGeneral = givenAlternateIdentifer.getResourceTypeGeneral();
      String responseResourceTypeGeneral = responseAlternateIdentifier.getResourceTypeGeneral();

      if (!(Objects.equals(givenName, responseName)
          && Objects.equals(givenRelatedIdentifierType, responseRelatedIdentifierType)
          && Objects.equals(givenRelationType, responseRelationType)
          && Objects.equals(givenRelatedMetaDataScheme, responseRelatedMetaDataScheme)
          && Objects.equals(givenSchemeUri, responseSchemeUri)
          && Objects.equals(givenSchemeType, responseSchemeType)
          && Objects.equals(givenResourceTypeGeneral, responseResourceTypeGeneral))){
        System.out.println("given RelatedIdentifiers: " + givenName + " " + givenRelatedIdentifierType + " " + givenRelationType + " " + givenRelatedMetaDataScheme + " " + givenSchemeUri + " " + givenSchemeType + " " + givenResourceTypeGeneral);
        System.out.println("response RelatedIdentifiers: " + responseName + " " + responseRelatedIdentifierType + " " + responseRelationType + " " + responseRelatedMetaDataScheme + " " + responseSchemeUri + " " + responseSchemeType + " " + responseResourceTypeGeneral);
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareSizes(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("--------------Comparing sizes");
    List<String> givenSizes = cedarConvertedDataCiteSchema.getData().getAttributes().getSizes();
    List<String> responseSizes = responseDataCiteSchema.getData().getAttributes().getSizes();

    if (givenSizes == null || givenSizes.isEmpty()){
      return responseSizes == null || responseSizes.isEmpty();
    }

    if (givenSizes.size() != responseSizes.size()){
      System.out.println("given subjects size is: " + givenSizes.size());
      System.out.println("Response subjects size is: " + responseSizes.size());
      return false;
    }

    boolean equals = true;

    givenSizes.sort(null);
    responseSizes.sort(null);

    for (int i=0; i < givenSizes.size(); i++){
      String given = givenSizes.get(i);
      String response = responseSizes.get(i);
      if (!given.equals(response)){
        equals = false;
        System.out.println("given size is: " + given);
        System.out.println("response size is: " + response);
        break;
      }
    }
    return equals;
  }

  private static boolean compareFormat(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("-------------------Comparing format");
    List<String> givenFormat = cedarConvertedDataCiteSchema.getData().getAttributes().getFormats();
    List<String> responseFormat = responseDataCiteSchema.getData().getAttributes().getFormats();

    if (givenFormat == null || givenFormat.isEmpty()){
      return responseFormat == null || responseFormat.isEmpty();
    }

    if (givenFormat.size() != responseFormat.size()){
      System.out.println("given Format size is: " + givenFormat.size());
      System.out.println("Response Format size is: " + responseFormat.size());
      return false;
    }

    boolean equals = true;

    givenFormat.sort(null);
    responseFormat.sort(null);

    for (int i=0; i < givenFormat.size(); i++){
      if (!givenFormat.get(i).equals(responseFormat.get(i))){
        equals = false;
        System.out.println("given Format is: " + givenFormat.get(i));
        System.out.println("response Format is: " + responseFormat.get(i));
        break;
      }
    }
    return equals;
  }

  private static boolean compareVersion(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("-------------Comparing version");
    return Objects.equals(cedarConvertedDataCiteSchema.getData().getAttributes().getVersion(),
        responseDataCiteSchema.getData().getAttributes().getVersion());
  }

  private static boolean compareRights(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("----------------Comparing rights");
    List<DataCiteRights> givenRights = cedarConvertedDataCiteSchema.getData().getAttributes().getRightsList();
    List<DataCiteRights> responseRights = responseDataCiteSchema.getData().getAttributes().getRightsList();
    boolean equals = true;

    if (givenRights == null || givenRights.isEmpty()){
      return responseRights == null || responseRights.isEmpty();
    }

    if (givenRights.size() != responseRights.size()){
      System.out.println("given rights size is: " + givenRights.size());
      System.out.println("Response rights size is: " + responseRights.size());
      return false;
    }

    Comparator<DataCiteRights> comparator = Comparator.comparing(DataCiteRights::getRights);
    givenRights.sort(comparator);
    responseRights.sort(comparator);

    for (int i=0; i < givenRights.size(); i++){
      DataCiteRights given = givenRights.get(i);
      DataCiteRights response = responseRights.get(i);

      String givenRightsName = given.getRights();
      String responseRightsName = response.getRights();

      String givenRightsUri = given.getRightsUri();
      String responseRightsUri = response.getRightsUri();

      String givenSchemeUri = given.getSchemeUri();
      String responseSchemeUri = response.getSchemeUri();

      String givenRightsIdentifier = given.getRightsIdentifier();
      String responseRightsIdentifier = response.getRightsIdentifier();

      String givenRIghtsIdentifierScheme = given.getRightsIdentifierScheme();
      String responseRIghtsIdentifierScheme = response.getRightsIdentifierScheme();

      if (!(Objects.equals(givenRightsName, responseRightsName)
          && Objects.equals(givenRightsUri, responseRightsUri)
          && Objects.equals(givenSchemeUri, responseSchemeUri)
          && Objects.equals(givenRightsIdentifier, responseRightsIdentifier)
          && Objects.equals(givenRIghtsIdentifierScheme, responseRIghtsIdentifierScheme))){
        System.out.println("given rights: " + givenRightsName + " " + givenRightsUri + " " + givenSchemeUri + " " + givenRightsIdentifier + " " + givenRIghtsIdentifierScheme);
        System.out.println("response rights: " + responseRightsName + " " + responseRightsUri + " " + responseSchemeUri + " " + responseRightsIdentifier + " " + responseRIghtsIdentifierScheme);
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareDescriptions(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("----------------Comparing descriptions");
    List<DataCiteDescription> givenDescriptions = cedarConvertedDataCiteSchema.getData().getAttributes().getDescriptions();
    List<DataCiteDescription> responseDescriptions = responseDataCiteSchema.getData().getAttributes().getDescriptions();
    boolean equals = true;

    if (givenDescriptions == null || givenDescriptions.isEmpty()){
      return responseDescriptions == null || responseDescriptions.isEmpty();
    }

    if (givenDescriptions.size() != responseDescriptions.size()){
      System.out.println("given rights size is: " + givenDescriptions.size());
      System.out.println("Response rights size is: " + responseDescriptions.size());
      return false;
    }

    Comparator<DataCiteDescription> comparator = Comparator.comparing(DataCiteDescription::getDescription);
    givenDescriptions.sort(comparator);
    responseDescriptions.sort(comparator);

    for (int i=0; i < givenDescriptions.size(); i++){
      DataCiteDescription given = givenDescriptions.get(i);
      DataCiteDescription response = responseDescriptions.get(i);

      String givenDescriptionName = given.getDescription();
      String responseDescriptionName = response.getDescription();

      String givenDescriptionType = given.getDescriptionType();
      String responseDescriptionType = response.getDescriptionType();

      String givenLang = given.getLang();
      String responseLang = response.getLang();

      if (!(Objects.equals(givenDescriptionName, responseDescriptionName)
          && Objects.equals(givenDescriptionType, responseDescriptionType)
          && Objects.equals(givenLang, responseLang))){
        System.out.println("given description: " + givenDescriptionName + " " + givenDescriptionType + " " + givenLang);
        System.out.println("response description: " + responseDescriptionName + " " + responseDescriptionType + " " + responseLang);
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareGeoLocationPoints(List<DataCiteGeoLocationPoint> givenPoints, List<DataCiteGeoLocationPoint> responsePoints){
    boolean equals = true;
    return equals;
  }

  private static boolean compareLocationPolygons(List<DataCiteGeoLocationPolygon> givenPolygons, List<DataCiteGeoLocationPolygon> responsePolygons){
    boolean equals = true;

    if (responsePolygons == null || responsePolygons.isEmpty()){
      return givenPolygons == null || givenPolygons.isEmpty();
    }

    if (givenPolygons.size() != responsePolygons.size()){
      System.out.println("given geo location polygons size is: " + givenPolygons.size());
      System.out.println("Response geo location polygons size is: " + responsePolygons.size());
      return false;
    }

    for (int i=0; i < givenPolygons.size(); i++){
      DataCiteGeoLocationPolygon givenGeoLocationPolygon = givenPolygons.get(i);
      DataCiteGeoLocationPolygon responseGeoLocationPolygon = responsePolygons.get(i);

      Float givenInPolygonPointLongitude = givenGeoLocationPolygon.getInPolygonPoint().getPointLongitude();
      Float responseInPolygonPointLongitude = responseGeoLocationPolygon.getInPolygonPoint().getPointLongitude();

      Float givenInPolygonPointLatitude = givenGeoLocationPolygon.getInPolygonPoint().getPointLatitude();
      Float responseInPolygonPointLatitude = responseGeoLocationPolygon.getInPolygonPoint().getPointLatitude();

      List<DataCiteGeoLocationPoint> givenPolygonPoints = givenGeoLocationPolygon.getPolygonPointsList();
      List<DataCiteGeoLocationPoint> responsePolygonPoints = responseGeoLocationPolygon.getPolygonPointsList();

      Comparator<DataCiteGeoLocationPoint> comparator = Comparator.comparing(DataCiteGeoLocationPoint::getPointLongitude);
      givenPolygonPoints.sort(comparator);
      responsePolygonPoints.sort(comparator);

      if (!(Objects.equals(givenInPolygonPointLongitude, responseInPolygonPointLongitude)
          && Objects.equals(givenInPolygonPointLatitude, responseInPolygonPointLatitude)
          && compareGeoLocationPoints(givenPolygonPoints, responsePolygonPoints))){
        System.out.println("given geo location polygon: " + givenInPolygonPointLongitude + " " + givenInPolygonPointLatitude);
        System.out.println("response geo location polygon: " + responseInPolygonPointLongitude + " " + responseInPolygonPointLatitude);
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareGeoLocations(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("----------------Comparing geo locations");
    List<DataCiteGeoLocation> givenGeoLocations = cedarConvertedDataCiteSchema.getData().getAttributes().getGeoLocations();
    List<DataCiteGeoLocation> responseGeoLocations = responseDataCiteSchema.getData().getAttributes().getGeoLocations();
    boolean equals = true;

    if (givenGeoLocations == null || givenGeoLocations.isEmpty()){
      return responseGeoLocations == null || responseGeoLocations.isEmpty();
    }

    if (givenGeoLocations.size() != responseGeoLocations.size()){
      System.out.println("given geo locations size is: " + givenGeoLocations.size());
      System.out.println("Response geo locations size is: " + responseGeoLocations.size());
      return false;
    }

    Comparator<DataCiteGeoLocation> comparator = Comparator.comparing(DataCiteGeoLocation::getGeoLocationPlace);
    givenGeoLocations.sort(comparator);
    responseGeoLocations.sort(comparator);

    for (int i=0; i < givenGeoLocations.size(); i++){
      DataCiteGeoLocation givenGeoLocation = givenGeoLocations.get(i);
      DataCiteGeoLocation responseGeoLocation = responseGeoLocations.get(i);

      String givenPlace = givenGeoLocation.getGeoLocationPlace();
      String responsePlace = responseGeoLocation.getGeoLocationPlace();

      Float givenPointLongitude = givenGeoLocation.getGeoLocationPoint().getPointLongitude();
      Float responsePointLongitude = responseGeoLocation.getGeoLocationPoint().getPointLongitude();

      Float givenPointLatitude = givenGeoLocation.getGeoLocationPoint().getPointLatitude();
      Float responsePointLatitude = responseGeoLocation.getGeoLocationPoint().getPointLatitude();

      Float givenBoxEastLongitude = givenGeoLocation.getGeoLocationBox().getEastBoundLongitude();
      Float responseBoxEastLongitude = givenGeoLocation.getGeoLocationBox().getEastBoundLongitude();

      Float givenNorthLatitude = givenGeoLocation.getGeoLocationBox().getNorthBoundLatitude();
      Float responseNorthLatitude = responseGeoLocation.getGeoLocationBox().getNorthBoundLatitude();

      Float givenWestLongitude = givenGeoLocation.getGeoLocationBox().getWestBoundLongitude();
      Float responseWestLongitude = responseGeoLocation.getGeoLocationBox().getWestBoundLongitude();

      Float givenSouthLongitude = givenGeoLocation.getGeoLocationBox().getSouthBoundLatitude();
      Float responseSouthLongitude = responseGeoLocation.getGeoLocationBox().getSouthBoundLatitude();

      List<DataCiteGeoLocationPolygon> givenPolygons = givenGeoLocation.getGeoLocationPolygonList();
      List<DataCiteGeoLocationPolygon> responsePolygons = responseGeoLocation.getGeoLocationPolygonList();

      if (!(Objects.equals(givenPlace, responsePlace)
          && Objects.equals(givenPointLongitude, responsePointLongitude)
          && Objects.equals(givenPointLatitude, responsePointLatitude)
          && Objects.equals(givenBoxEastLongitude, responseBoxEastLongitude)
          && Objects.equals(givenNorthLatitude, responseNorthLatitude)
          && Objects.equals(givenWestLongitude, responseWestLongitude)
          && Objects.equals(givenSouthLongitude, responseSouthLongitude)
      )){
//          && compareLocationPolygons(givenPolygons, responsePolygons))){
        System.out.println("given geo location: " + givenPlace + " " + givenPointLongitude + " " + givenPointLatitude + " " + givenBoxEastLongitude + " " + givenNorthLatitude + " " + givenWestLongitude+ " " + givenSouthLongitude);
        System.out.println("response geo location: " + responsePlace + " " + responsePointLongitude + " " + responsePointLatitude + " " + responseBoxEastLongitude + " " + responseNorthLatitude + " " + responseWestLongitude + " " + responseSouthLongitude);
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareFundingReferences(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema) {
    System.out.println("----------------Comparing funding reference");
    List<DataCiteFundingReference> givenFundingReferences = cedarConvertedDataCiteSchema.getData().getAttributes().getFundingReferences();
    List<DataCiteFundingReference> responseFundingReferences = responseDataCiteSchema.getData().getAttributes().getFundingReferences();
    boolean equals = true;

    if (givenFundingReferences == null || givenFundingReferences.isEmpty()) {
      return responseFundingReferences == null || responseFundingReferences.isEmpty();
    }

    if (givenFundingReferences.size() != responseFundingReferences.size()) {
      System.out.println("given funding references size is: " + givenFundingReferences.size());
      System.out.println("Response funding references size is: " + responseFundingReferences.size());
      return false;
    }

    Comparator<DataCiteFundingReference> comparator = Comparator.comparing(DataCiteFundingReference::getFunderName);
    givenFundingReferences.sort(comparator);
    responseFundingReferences.sort(comparator);

    for (int i = 0; i < givenFundingReferences.size(); i++) {
      DataCiteFundingReference givenFundingReference = givenFundingReferences.get(i);
      DataCiteFundingReference responseFundingReference = responseFundingReferences.get(i);

      String givenFunderName = givenFundingReference.getFunderName();
      String responseFunderName = responseFundingReference.getFunderName();

      String givenFunderIdentifier = givenFundingReference.getFunderIdentifier();
      String responseFunderIdentifier = responseFundingReference.getFunderIdentifier();

      String givenFunderIdentifierType = givenFundingReference.getFunderIdentifierType();
      String responseFunderIdentifierType = responseFundingReference.getFunderIdentifierType();

      String givenFunderIdentifierSchemeUri = givenFundingReference.getSchemeUri();
      String responseFunderIdentifierSchemeUri = responseFundingReference.getSchemeUri();

      String givenAwardNumber = givenFundingReference.getAwardNumber();
      String responseAwardNumber = responseFundingReference.getAwardNumber();

      String givenAwardUri = givenFundingReference.getAwardUri();
      String responseAwardUri = responseFundingReference.getAwardUri();

      String givenAwardTitle = givenFundingReference.getAwardTitle();
      String responseAwardTitle = responseFundingReference.getAwardTitle();

      if (!(Objects.equals(givenFunderName, responseFunderName)
          && Objects.equals(givenFunderIdentifier, responseFunderIdentifier)
          && Objects.equals(givenFunderIdentifierType, responseFunderIdentifierType)
          && Objects.equals(givenFunderIdentifierSchemeUri, responseFunderIdentifierSchemeUri)
          && Objects.equals(givenAwardNumber, responseAwardNumber)
          && Objects.equals(givenAwardUri, responseAwardUri)
          && Objects.equals(givenAwardTitle, responseAwardTitle)
      )) {
//          && compareLocationPolygons(givenPolygons, responsePolygons))){
        System.out.println("given funding reference: " + givenFunderName + " " + givenFunderIdentifier + " " + givenFunderIdentifierType + " " + givenFunderIdentifierSchemeUri + " " + givenAwardNumber + " " + givenAwardUri + " " + givenAwardTitle);
        System.out.println("response funding reference: " + responseFunderName + " " + responseFunderIdentifier + " " + responseFunderIdentifierType + " " + responseFunderIdentifierSchemeUri + " " + responseAwardNumber + " " + responseAwardUri + " " + responseAwardTitle);
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareRelatedItems(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    System.out.println("----------------Comparing related items");
    List<DataCiteRelatedItem> givenRelatedItems = cedarConvertedDataCiteSchema.getData().getAttributes().getRelatedItems();
    List<DataCiteRelatedItem> responseRelatedItems = responseDataCiteSchema.getData().getAttributes().getRelatedItems();
    boolean equals = true;

    if (givenRelatedItems == null || givenRelatedItems.isEmpty()) {
      return responseRelatedItems == null || responseRelatedItems.isEmpty();
    }

    if (givenRelatedItems.size() != responseRelatedItems.size()) {
      System.out.println("Given related items size is: " + givenRelatedItems.size());
      System.out.println("Response related items size is: " + responseRelatedItems.size());
      return false;
    }

//    Comparator<DataCiteRelatedItem> comparator = Comparator.comparing(DataCiteRelatedItem::getRelatedItemType);
//    givenRelatedItems.sort(comparator);
//    responseRelatedItems.sort(comparator);

    for (int i = 0; i < givenRelatedItems.size(); i++) {
      DataCiteRelatedItem givenRelatedItem = givenRelatedItems.get(i);
      DataCiteRelatedItem responseRelatedItem = responseRelatedItems.get(i);

      String givenRelatedItemType = givenRelatedItem.getRelatedItemType();
      String responseRelatedItemType = responseRelatedItem.getRelatedItemType();

      String givenRelationType = givenRelatedItem.getRelationType();
      String responseRelationType = responseRelatedItem.getRelationType();

      String givenRelatedIdentifier = givenRelatedItem.getRelatedItemIdentifier().getRelatedItemIdentifier();
      String responseRelatedIdentifier = responseRelatedItem.getRelatedItemIdentifier().getRelatedItemIdentifier();

      String givenRelatedIdentifierType = givenRelatedItem.getRelatedItemIdentifier().getRelatedItemIdentifierType();
      String responseRelatedIdentifierType = responseRelatedItem.getRelatedItemIdentifier().getRelatedItemIdentifierType();

      String givenMetadataScheme = givenRelatedItem.getRelatedItemIdentifier().getRelatedMetadataScheme();
      String responseMetadataScheme = responseRelatedItem.getRelatedItemIdentifier().getRelatedMetadataScheme();

      String givenSchemeUri = givenRelatedItem.getRelatedItemIdentifier().getSchemeUri();
      String responseSchemeUri = responseRelatedItem.getRelatedItemIdentifier().getSchemeUri();

      String givenSchemeType = givenRelatedItem.getRelatedItemIdentifier().getSchemeType();
      String responseSchemeType = responseRelatedItem.getRelatedItemIdentifier().getSchemeType();

//      String givenNumber = givenRelatedItem.getNumber().getNumber();
//      String responseNumber = responseRelatedItem.getNumber().getNumber();
//
//      String givenNumberType = givenRelatedItem.getNumber().getNumberType();
//      String responseNumberType = responseRelatedItem.getNumber().getNumberType();

      String givenVolume = givenRelatedItem.getVolume();
      String responseVolume = responseRelatedItem.getVolume();

      String givenIssue = givenRelatedItem.getIssue();
      String responseIssue = responseRelatedItem.getIssue();

      String givenFirstPage = givenRelatedItem.getFirstPage();
      String responseFirstPage = responseRelatedItem.getFirstPage();

      String givenLastPage = givenRelatedItem.getLastPage();
      String responseLastPage = responseRelatedItem.getLastPage();

      int givenPublicationYear = givenRelatedItem.getPublicationYear();
      int responsePublicationYear = responseRelatedItem.getPublicationYear();

      String givenPublisher = givenRelatedItem.getPublisher();
      String responsePublisher = responseRelatedItem.getPublisher();

      String givenEdition = givenRelatedItem.getEdition();
      String responseEdition = responseRelatedItem.getEdition();

      List<DataCiteCreator> givenCreators = givenRelatedItem.getCreators();
      List<DataCiteCreator> responseCreators = responseRelatedItem.getCreators();

      List<DataCiteTitle> givenTitles = givenRelatedItem.getTitles();
      List<DataCiteTitle> responseTitles = responseRelatedItem.getTitles();

      List<DataCiteRelatedItemContributor> givenContributors = givenRelatedItem.getContributors();
      List<DataCiteRelatedItemContributor> responseContributors = responseRelatedItem.getContributors();

      if (!(Objects.equals(givenRelatedItemType, responseRelatedItemType)
          && Objects.equals(givenRelationType, responseRelationType)
          && Objects.equals(givenRelatedIdentifier, responseRelatedIdentifier)
          && Objects.equals(givenRelatedIdentifierType, responseRelatedIdentifierType)
          && Objects.equals(givenMetadataScheme, responseMetadataScheme)
//          && Objects.equals(givenSchemeUri, responseSchemeUri)
          && Objects.equals(givenSchemeType, responseSchemeType)
//          && Objects.equals(givenNumber, responseNumber)
//          && Objects.equals(givenNumberType, responseNumberType)
          && Objects.equals(givenVolume, responseVolume)
          && Objects.equals(givenIssue, responseIssue)
          && Objects.equals(givenFirstPage, responseFirstPage)
          && Objects.equals(givenLastPage, responseLastPage)
          && Objects.equals(givenPublicationYear, responsePublicationYear)
          && Objects.equals(givenPublisher, responsePublisher)
          && Objects.equals(givenEdition, responseEdition)
//          && compareCreators(givenCreators, responseCreators)
          && compareTitles(givenTitles, responseTitles)
          && compareRelatedItemContributors(givenContributors, responseContributors)
      )) {
//        System.out.println("given funding reference: " + givenRelatedItemType + " " + givenRelationType + " " + givenRelatedIdentifier + " " + givenRelatedIdentifierType + " " + givenMetadataScheme + " " + givenSchemeUri + " " + givenSchemeType + " " + givenVolume + " " + givenIssue + " " + givenFirstPage + " " + givenLastPage);
//        System.out.println("response funding reference: " + responseRelatedItemType + " " + responseRelationType + " " + responseRelatedIdentifier + " " + responseRelatedIdentifierType + " " + responseMetadataScheme + " " + responseSchemeUri + " " + responseVolume + " " + responseIssue + " " + responseFirstPage + " " + responseLastPage );
        System.out.println("related item is different");
        equals = false;
        break;
      }
    }
    return equals;
  }

  private static boolean compareRelatedItemContributors(List<DataCiteRelatedItemContributor> givenContributors, List<DataCiteRelatedItemContributor> responseContributors){
    System.out.println("----------------Comparing related items' contributors");
    boolean equals = true;

    if (givenContributors == null || givenContributors.isEmpty()) {
      return responseContributors == null || responseContributors.isEmpty();
    }

    if (givenContributors.size() != responseContributors.size()) {
      System.out.println("Given related item contributors size is: " + givenContributors.size());
      System.out.println("Response related item contributor size is: " + responseContributors.size());
      return false;
    }

    Comparator<DataCiteRelatedItemContributor> comparator = Comparator.comparing(DataCiteRelatedItemContributor::getName);
    givenContributors.sort(comparator);
    responseContributors.sort(comparator);

    for (int i = 0; i < givenContributors.size(); i++) {
      DataCiteRelatedItemContributor givenContributor = givenContributors.get(i);
      DataCiteRelatedItemContributor responseContributor = responseContributors.get(i);

      String givenName = givenContributor.getName();
      String responseName = responseContributor.getName();

      String givenNameType = givenContributor.getNameType();
      String responseNameType = responseContributor.getNameType();

      String givenGivenName = givenContributor.getGivenName();
      String responseGivenName = responseContributor.getGivenName();

      String givenFamilyName = givenContributor.getFamilyName();
      String responseFamilyName = responseContributor.getFamilyName();

      String givenContrubutorType = givenContributor.getContributorType();
      String responseContrubutorType = responseContributor.getContributorType();

      if (!(Objects.equals(givenName, responseName)
          && Objects.equals(givenNameType, responseNameType)
          && Objects.equals(givenGivenName, responseGivenName)
          && Objects.equals(givenFamilyName, responseFamilyName)
          && Objects.equals(givenContrubutorType, responseContrubutorType)
      )) {
//          && compareLocationPolygons(givenPolygons, responsePolygons))){
        System.out.println("given related item contributor: " + givenName + " " + givenNameType + " " + givenGivenName + " " + givenFamilyName + " " + givenContrubutorType );
        System.out.println("response related item contributor: " + responseName + " " + responseNameType + " " + responseGivenName + " " + responseFamilyName + " " + responseContrubutorType );
        equals = false;
        break;
      }
    }
    return equals;
  }
}
