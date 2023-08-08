package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NameIdentifier {
    @JsonProperty("@context")
    private Map<String, String> context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("nameIdentifierName")
    private ValueFormat nameIdentifierName;

    @JsonProperty("nameIdentifierScheme")
    private ValueFormat nameIdentifierScheme;

    @JsonProperty("nameIdentifierSchemeURI")
    private SchemeUri nameIdentifierSchemeUri;

    public ValueFormat getNameIdentifierName() {
        return nameIdentifierName;
    }

    public void setNameIdentifierName(ValueFormat nameIdentifierName) {
        this.nameIdentifierName = nameIdentifierName;
    }

    public ValueFormat getNameIdentifierScheme() {
        return nameIdentifierScheme;
    }

    public void setNameIdentifierScheme(ValueFormat nameIdentifierScheme) {
        this.nameIdentifierScheme = nameIdentifierScheme;
    }

    public SchemeUri getNameIdentifierSchemeURI() {
        return nameIdentifierSchemeUri;
    }

    public void setNameIdentifierSchemeURI(SchemeUri nameIdentifierSchemeUri) {
        this.nameIdentifierSchemeUri = nameIdentifierSchemeUri;
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
