package org.metadatacenter.cedar.bridge.resource.DataCiteProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteRelatedItemIdentifier {
    @JsonProperty("relatedItemIdentifier")
    private String relatedItemIdentifier;
    @JsonProperty("relatedItemIdentifierType")
    private String relatedItemIdentifierType ;
    @JsonProperty("relatedMetadataScheme")
    private String relatedMetadataScheme;
    @JsonProperty("schemeUri")
    private String schemeUri;
    @JsonProperty("schemeType")
    private String schemeType;

    public String getRelatedItemIdentifier() {
        return relatedItemIdentifier;
    }

    public void setRelatedItemIdentifier(String relatedItemIdentifier) {
        this.relatedItemIdentifier = relatedItemIdentifier;
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
