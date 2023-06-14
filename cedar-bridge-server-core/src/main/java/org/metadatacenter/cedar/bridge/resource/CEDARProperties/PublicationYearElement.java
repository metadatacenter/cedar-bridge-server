package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicationYearElement {
    @JsonProperty("@context")
    private Map<String, String> context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("PublicationYear")
    private PublicationYear publicationYear;

    public PublicationYear getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(PublicationYear publicationYear) {
        this.publicationYear = publicationYear;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
