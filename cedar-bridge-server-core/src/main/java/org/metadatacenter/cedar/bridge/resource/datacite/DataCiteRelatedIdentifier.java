package org.metadatacenter.cedar.bridge.resource.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteRelatedIdentifier {
    @JsonProperty("schemeUri")
    private String schemeUri;
    @JsonProperty("schemeType")
    private String schemeType;
    @JsonProperty("relationType")
    private String relationType;
    @JsonProperty("relatedIdentifier")
    private String relatedIdentifier;
    @JsonProperty("resourceTypeGeneral")
    private String resourceTypeGeneral;
    @JsonProperty("relatedIdentifierType")
    private String relatedIdentifierType;
    @JsonProperty("relatedMetadataScheme")
    private String relatedMetadataScheme;

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

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getRelatedIdentifier() {
        return relatedIdentifier;
    }

    public void setRelatedIdentifier(String relatedIdentifier) {
        this.relatedIdentifier = relatedIdentifier;
    }

    public String getResourceTypeGeneral() {
        return resourceTypeGeneral;
    }

    public void setResourceTypeGeneral(String resourceTypeGeneral) {
        this.resourceTypeGeneral = resourceTypeGeneral;
    }

    public String getRelatedIdentifierType() {
        return relatedIdentifierType;
    }

    public void setRelatedIdentifierType(String relatedIdentifierType) {
        this.relatedIdentifierType = relatedIdentifierType;
    }

    public String getRelatedMetadataScheme() {
        return relatedMetadataScheme;
    }

    public void setRelatedMetadataScheme(String relatedMetadataScheme) {
        this.relatedMetadataScheme = relatedMetadataScheme;
    }
}
