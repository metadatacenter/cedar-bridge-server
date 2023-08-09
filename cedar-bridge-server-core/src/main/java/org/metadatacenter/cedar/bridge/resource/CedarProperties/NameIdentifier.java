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

    @JsonProperty("name")
    private ValueFormat name;

    @JsonProperty("nameIdentifierScheme")
    private ValueFormat nameIdentifierScheme;

    @JsonProperty("schemeURI")
    private SchemeUri schemeUri;

    public ValueFormat getName() {
        return name;
    }

    public void setName(ValueFormat name) {
        this.name = name;
    }

    public ValueFormat getNameIdentifierScheme() {
        return nameIdentifierScheme;
    }

    public void setNameIdentifierScheme(ValueFormat nameIdentifierScheme) {
        this.nameIdentifierScheme = nameIdentifierScheme;
    }

    public SchemeUri getSchemeURI() {
        return schemeUri;
    }

    public void setSchemeURI(SchemeUri nameIdentifierSchemeUri) {
        this.schemeUri = nameIdentifierSchemeUri;
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
