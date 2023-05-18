package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Affiliation {
    @JsonIgnore
    @JsonProperty("@context")
    private String context;

    @JsonIgnore
    @JsonProperty("@id")
    private String id;

    @JsonProperty("affiliation")
    private ValueFormat affiliation;

    @JsonProperty("affiliationIdentifier")
    private ValueFormat affiliationIdentifier;

    @JsonProperty("affiliationIdentifierScheme")
    private ValueFormat affiliationIdentifierScheme;

    @JsonProperty("affiliationIdentifierSchemeURI")
    private SchemeURI affiliationIdentifierSchemeURI;

    public ValueFormat getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(ValueFormat affiliation) {
        this.affiliation = affiliation;
    }

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
}
