package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteRights {
    private String rights;
    private String rightsUri;
    private String schemeUri;
    private String rightsIdentifier;
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
