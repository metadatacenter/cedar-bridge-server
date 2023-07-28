package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Affiliation {
    @JsonProperty("@context")
    private Map<String, String> context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("affiliationIdentifier")
    private ValueFormat affiliationIdentifier;

    @JsonProperty("affiliationIdentifierScheme")
    private ValueFormat affiliationIdentifierScheme;

    @JsonProperty("affiliationIdentifierSchemeURI")
    private SchemeURI affiliationIdentifierSchemeURI;

    public ValueFormat getAffiliationIdentifier() {
        return affiliationIdentifier;
    }

    public void setAffiliationIdentifier(ValueFormat affiliationIdentifier) {
        this.affiliationIdentifier = affiliationIdentifier;
    }

    public ValueFormat getAffiliationIdentifierScheme() {
        return affiliationIdentifierScheme;
    }

    public void setAffiliationIdentifierScheme(ValueFormat affiliationIdentifierScheme) {
        this.affiliationIdentifierScheme = affiliationIdentifierScheme;
    }

    public SchemeURI getAffiliationIdentifierSchemeURI() {
        return affiliationIdentifierSchemeURI;
    }

    public void setAffiliationIdentifierSchemeURI(SchemeURI affiliationIdentifierSchemeURI) {
        this.affiliationIdentifierSchemeURI = affiliationIdentifierSchemeURI;
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
