package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteRelatedItemIdentifier {
    private String relatedIdentifier;

    private String relatedItemIdentifierType ;

    private String relatedMetadataScheme;

    private String schemeUri;

    private String schemeType;

    public String getRelatedIdentifier() {
        return relatedIdentifier;
    }

    public void setRelatedIdentifier(String relatedIdentifier) {
        this.relatedIdentifier = relatedIdentifier;
    }

    public String getRelatedItemIdentifierType() {
        return relatedItemIdentifierType;
    }

    public void setRelatedItemIdentifierType(String relatedItemIdentifierType) {
        this.relatedItemIdentifierType = relatedItemIdentifierType;
    }

    public String getRelatedMetadataScheme() {
        return relatedMetadataScheme;
    }

    public void setRelatedMetadataScheme(String relatedMetadataScheme) {
        this.relatedMetadataScheme = relatedMetadataScheme;
    }

    public String getSchemeUri() {
        return schemeUri;
    }

    public void setSchemeUri(String schemeUri) {
        this.schemeUri = schemeUri;
    }

    public String getSchemeType() {
        return schemeType;
    }

    public void setSchemeType(String schemeType) {
        this.schemeType = schemeType;
    }
}
