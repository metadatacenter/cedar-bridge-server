package org.metadatacenter.cedar.bridge.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    try {
      //Deserialize givenMetadata to CedarDataCiteInstance Class
      String givenMetadataString = givenMetadata.toString();
      CEDARDataCiteInstance cedarInstance = mapper.readValue(givenMetadataString, CEDARDataCiteInstance.class);
      DataCiteSchema cedarConvertedDataCiteSchema = new DataCiteSchema();

      // Pass the value from cedarDataCiteInstance to dataCiteRequest
      CedarInstanceParser.parseCEDARInstance(cedarInstance, cedarConvertedDataCiteSchema);

      //Deserialize responseMetadata to DtaCiteSchema Class
      String responseMetadaString = responseMetadata.toString();
      DataCiteSchema responseDataCiteSchema = mapper.readValue(responseMetadaString, DataCiteSchema.class);


      //Compare cedarConvertedDataCiteSchema vs responseDataCiteSchema
//      if (! (compareCreators(cedarConvertedDataCiteSchema, responseDataCiteSchema)
      if ( !(true
          && compareTitles(cedarConvertedDataCiteSchema, responseDataCiteSchema))
          && comparePublisher(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && comparePublicationYear(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareResourceType(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareSubjects(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareContributors(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareDates(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareLang(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareAlternateIdentifier(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareRelatedIdentifier(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareSizes(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareFormat(cedarConvertedDataCiteSchema, responseDataCiteSchema)
          && compareVersion(cedarConvertedDataCiteSchema, responseDataCiteSchema)
      ){
        return false;
      }
      return true;
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

      String givenSchemeUri = givenDataCiteNameIdentifier.getSchemeURI();
      String responseSchemeUri = responseDataCiteNameIdentifier.getSchemeURI();

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


  private static boolean compareCreators(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    List<DataCiteCreator> givenCreators = cedarConvertedDataCiteSchema.getData().getAttributes().getCreators();
    List<DataCiteCreator> responseCreators = responseDataCiteSchema.getData().getAttributes().getCreators();
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
        System.out.println("given creator: " + givenName + " " + givenFamilyName + " " + givenGivenName + " " + givenNameType);
        System.out.println("response creator: " + responseName + " " + responseFamilyName + " " + responseGivenName + " " + responseNameType);
        break;
      }
    }
    return equals;
  }

  private static boolean compareTitles(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
      List<DataCiteTitle> givenTitles = cedarConvertedDataCiteSchema.getData().getAttributes().getTitles();;
      List<DataCiteTitle> responseTitles = responseDataCiteSchema.getData().getAttributes().getTitles();
      boolean equals = true;

      if (givenTitles == null || givenTitles.isEmpty()){
        return responseTitles == null || responseTitles.isEmpty();
      }

      if (givenTitles.size() != responseTitles.size()){
        System.out.println("given creators size is: " + givenTitles.size());
        System.out.println("response creators size is: " + responseTitles.size());
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
    return Objects.equals(cedarConvertedDataCiteSchema.getData().getAttributes().getPublisher(),
        responseDataCiteSchema.getData().getAttributes().getPublisher());
  }

  private static boolean comparePublicationYear(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema) {
    return Objects.equals(cedarConvertedDataCiteSchema.getData().getAttributes().getPublicationYear(),
        responseDataCiteSchema.getData().getAttributes().getPublicationYear());
  }

  private static boolean compareResourceType(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
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
        System.out.println("given contributor: " + givenName + " " + givenFamilyName + " " + givenGivenName + " " + givenNameType + " " + givenContributorType);
        System.out.println("response contributor: " + responseName + " " + responseFamilyName + " " + responseGivenName + " " + responseNameType + " " + responseContributorType);
        break;
      }
    }
    return equals;
  }

  private static boolean compareDates(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
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
    return Objects.equals(cedarConvertedDataCiteSchema.getData().getAttributes().getLanguage(), responseDataCiteSchema.getData().getAttributes().getLanguage());
  }

  private static boolean compareAlternateIdentifier(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
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
      if (givenSizes.get(i) != responseSizes.get(i)){
        equals = false;
        System.out.println("given size is: " + givenSizes.get(i));
        System.out.println("response size is: " + responseSizes.get(i));
        break;
      }
    }
    return equals;
  }

  private static boolean compareFormat(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
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
      if (givenFormat.get(i) != responseFormat.get(i)){
        equals = false;
        System.out.println("given Format is: " + givenFormat.get(i));
        System.out.println("response Format is: " + responseFormat.get(i));
        break;
      }
    }
    return equals;
  }

  private static boolean compareVersion(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    return Objects.equals(cedarConvertedDataCiteSchema.getData().getAttributes().getVersion(),
        responseDataCiteSchema.getData().getAttributes().getVersion());
  }
}
