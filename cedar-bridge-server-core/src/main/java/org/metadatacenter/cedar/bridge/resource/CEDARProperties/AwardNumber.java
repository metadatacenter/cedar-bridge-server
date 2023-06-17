package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AwardNumber {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("awardNumber")
    private ValueFormat awardNumber;

    @JsonProperty("awardURI")
    private SchemeURI awardURI;

    public ValueFormat getAwardNumber() {
        return awardNumber;
    }

    public void setAwardNumber(ValueFormat awardNumber) {
        this.awardNumber = awardNumber;
    }

    public SchemeURI getAwardURI() {
        return awardURI;
    }

    public void setAwardURI(SchemeURI awardURI) {
        this.awardURI = awardURI;
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
