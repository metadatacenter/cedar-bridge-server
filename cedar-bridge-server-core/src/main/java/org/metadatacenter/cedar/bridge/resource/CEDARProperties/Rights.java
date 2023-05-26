package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rights {
    @JsonProperty("Rights")
    private ValueFormat rights;

    @JsonProperty("rightsURI")
    private SchemeURI rightsURI;

    @JsonProperty("rightsIdentifier")
    private ValueFormat rightsIdentifier;

    @JsonProperty("rightsIdentifierScheme")
    private ValueFormat rightsIdentifierScheme;

    @JsonProperty("schemeURI")
    private SchemeURI schemeURI;

    public ValueFormat getRights() {
        return rights;
    }

    public void setRights(ValueFormat rights) {
        this.rights = rights;
    }

    public SchemeURI getRightsURI() {
        return rightsURI;
    }

    public void setRightsURI(SchemeURI rightsURI) {
        this.rightsURI = rightsURI;
    }

    public ValueFormat getRightsIdentifier() {
        return rightsIdentifier;
    }

    public void setRightsIdentifier(ValueFormat rightsIdentifier) {
        this.rightsIdentifier = rightsIdentifier;
    }

    public ValueFormat getRightsIdentifierScheme() {
        return rightsIdentifierScheme;
    }

    public void setRightsIdentifierScheme(ValueFormat rightsIdentifierScheme) {
        this.rightsIdentifierScheme = rightsIdentifierScheme;
    }

    public SchemeURI getSchemeURI() {
        return schemeURI;
    }

    public void setSchemeURI(SchemeURI schemeURI) {
        this.schemeURI = schemeURI;
    }
}
