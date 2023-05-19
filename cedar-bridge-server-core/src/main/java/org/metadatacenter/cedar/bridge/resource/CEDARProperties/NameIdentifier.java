package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NameIdentifier {
    @JsonIgnore
    @JsonProperty("@context")
    private String context;

    @JsonIgnore
    @JsonProperty("@id")
    private String id;

    @JsonProperty("nameIdentifier")
    private ValueFormat nameIdentifierName;

    @JsonProperty("nameIdentifierScheme")
    private ValueFormat nameIdentifierScheme;

    @JsonProperty("nameIdentifierSchemeURI")
    private SchemeURI nameIdentifierSchemeURI;

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

    public SchemeURI getNameIdentifierSchemeURI() {
        return nameIdentifierSchemeURI;
    }

    public void setNameIdentifierSchemeURI(SchemeURI nameIdentifierSchemeURI) {
        this.nameIdentifierSchemeURI = nameIdentifierSchemeURI;
    }
}