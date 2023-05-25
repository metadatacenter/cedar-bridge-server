package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteAffiliation {
    @JsonProperty("affiliation")
    private String affiliation;
    @JsonProperty("affiliationIdentifier")
    private String affiliationIdentifier;
    @JsonProperty("affiliationIdentifierScheme")
    private String affiliationIdentifierScheme;
    @JsonProperty("schemeUri")
    private String affiliationSchemeURI;

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getAffiliationIdentifier() {
        return affiliationIdentifier;
    }

    public void setAffiliationIdentifier(String affiliationIdentifier) {
        this.affiliationIdentifier = affiliationIdentifier;
    }

    public String getAffiliationIdentifierScheme() {
        return affiliationIdentifierScheme;
    }

    public void setAffiliationIdentifierScheme(String affiliationIdentifierScheme) {
        this.affiliationIdentifierScheme = affiliationIdentifierScheme;
    }

    public String getAffiliationSchemeURI() {
        return affiliationSchemeURI;
    }

    public void setAffiliationSchemeURI(String affiliationSchemeURI) {
        this.affiliationSchemeURI = affiliationSchemeURI;
    }
}
