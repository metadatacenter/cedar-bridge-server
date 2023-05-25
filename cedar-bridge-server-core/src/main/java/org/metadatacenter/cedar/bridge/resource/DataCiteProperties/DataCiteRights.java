package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteRights {
    @JsonProperty("rights")
    private String rights;
    @JsonProperty("rightsUri")
    private String rightsUri;
    @JsonProperty("schemeUri")
    private String schemeUri;
    @JsonProperty("rightsIdentifier")
    private String rightsIdentifier;
    @JsonProperty("rightsIdentifierScheme")
    private String rightsIdentifierScheme;

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public String getRightsUri() {
        return rightsUri;
    }

    public void setRightsUri(String rightsUri) {
        this.rightsUri = rightsUri;
    }

    public String getSchemeUri() {
        return schemeUri;
    }

    public void setSchemeUri(String schemeUri) {
        this.schemeUri = schemeUri;
    }

    public String getRightsIdentifier() {
        return rightsIdentifier;
    }

    public void setRightsIdentifier(String rightsIdentifier) {
        this.rightsIdentifier = rightsIdentifier;
    }

    public String getRightsIdentifierScheme() {
        return rightsIdentifierScheme;
    }

    public void setRightsIdentifierScheme(String rightsIdentifierScheme) {
        this.rightsIdentifierScheme = rightsIdentifierScheme;
    }
}
