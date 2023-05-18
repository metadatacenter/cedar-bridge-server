package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rights {
    @JsonProperty("Rights")
    private ValueFormat rights;

    @JsonProperty("rightsURI")
    private IdFormat rightsURI;

    @JsonProperty("rightsIdentifier")
    private ValueFormat rightsIdentifier;

    @JsonProperty("rightsIdentifierScheme")
    private ValueFormat rightsIdentifierScheme;

    @JsonProperty("schemeURI")
    private IdFormat schemeURI;

    public ValueFormat getRights() {
        return rights;
    }

    public void setRights(ValueFormat rights) {
        this.rights = rights;
    }

    public IdFormat getRightsURI() {
        return rightsURI;
    }

    public void setRightsURI(IdFormat rightsURI) {
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

    public IdFormat getSchemeURI() {
        return schemeURI;
    }

    public void setSchemeURI(IdFormat schemeURI) {
        this.schemeURI = schemeURI;
    }
}
