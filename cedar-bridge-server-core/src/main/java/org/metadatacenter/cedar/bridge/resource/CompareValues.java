package org.metadatacenter.cedar.bridge.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.metadatacenter.cedar.bridge.resource.CEDARProperties.CEDARDataCiteInstance;
import org.metadatacenter.cedar.bridge.resource.DataCiteProperties.*;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompareValues {
  public static void main(String[] args) {
    ObjectMapper objectMapper = new ObjectMapper();
    String givenMetadataFilePath = "/Users/ycao77/CEDAR/cedar-bridge-server/cedar-bridge-server-core/src/main/java/org/metadatacenter/cedar/bridge/resource/JSONFiles/GivenMetaData.json";
    String responseMetadataFilePath = "/Users/ycao77/CEDAR/cedar-bridge-server/cedar-bridge-server-core/src/main/java/org/metadatacenter/cedar/bridge/resource/JSONFiles/ResponseMetaData.json";

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
      boolean equal = true;
      ArrayList<String> notEqualElements = new ArrayList<>();

//      compareCreators(givenMetadata, responseMetadata);
      boolean creatorEqual = compareCreators(cedarConvertedDataCiteSchema, responseDataCiteSchema);



      List<DataCiteTitle> givenTitles = cedarConvertedDataCiteSchema.getData().getAttributes().getTitles();;
      List<DataCiteTitle> responseTitles = responseDataCiteSchema.getData().getAttributes().getTitles();

      List<DataCiteSubject> givenSubjects = cedarConvertedDataCiteSchema.getData().getAttributes().getSubjects();
      List<DataCiteSubject> responseSubjects = responseDataCiteSchema.getData().getAttributes().getSubjects();

      String givenPublisher = cedarConvertedDataCiteSchema.getData().getAttributes().getPublisher();
      String responsePublisher = responseDataCiteSchema.getData().getAttributes().getPublisher();

      int givenPublicationYear = cedarConvertedDataCiteSchema.getData().getAttributes().getPublicationYear();
      int responsePublicationYear = responseDataCiteSchema.getData().getAttributes().getPublicationYear();

      if (!givenCreators.equals(responseCreators)){
        equal = false;
        notEqualElements.add("Creators");
        for (DataCiteCreator c: givenCreators) {
          System.out.println("given_name: " + c.getName());
          System.out.println("given_affiliation: " + c.getAffiliations());
          System.out.println("given_nameIdentifiers: " + c.getNameIdentifiers());
        }
        for (DataCiteCreator c : responseCreators) {
          System.out.println("response_name: " + c.getName());
          System.out.println("response_affiliation: " + c.getAffiliations());
          System.out.println("response_nameIdentifiers: " + c.getNameIdentifiers());
        }

      } else if (!givenTitles.equals(responseTitles)) {
        equal = false;
        notEqualElements.add("Titles");
        System.out.println("given: " + givenTitles);
        System.out.println("response: " + responseTitles);
      } else if (!givenPublisher.equals(responsePublisher)) {
        equal = false;
        notEqualElements.add("Publisher");
        System.out.println("given: " + givenPublisher);
        System.out.println("response: " + responsePublisher);
      } else if (!givenPublisher.equals(responsePublisher)) {
        equal = false;
        notEqualElements.add("Publisher");
        System.out.println("given: " + givenPublisher);
        System.out.println("response: " + responsePublisher);
      }
      System.out.println(notEqualElements);
      return equal;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean compareCreators(DataCiteSchema cedarConvertedDataCiteSchema, DataCiteSchema responseDataCiteSchema){
    List<DataCiteCreator> givenCreators = cedarConvertedDataCiteSchema.getData().getAttributes().getCreators();
    List<DataCiteCreator> responseCreators = responseDataCiteSchema.getData().getAttributes().getCreators();

    if (givenCreators.size() != responseCreators.size()){
      return false;
    }
    for (int i=0; i < givenCreators.size(); i++ ){
      DataCiteCreator givenCreator = givenCreators.get(i);
      DataCiteCreator responseCreator = responseCreators.get(i);

      String givenName = givenCreator.getName();
      String responseName = responseCreator.getName();

    }

  }

}
