package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedIdentifier {
    @JsonProperty("RelatedIdentifier")
    private ValueFormat relatedIdentifier;

    @JsonProperty("relatedIdentifierType")
    private IdFormat relatedIdentifierType;

    @JsonProperty("relationType")
    private IdFormat relationType;

    @JsonProperty("relatedMetadataScheme")
    private ValueFormat relatedMetadataScheme;

    @JsonProperty("schemeURI")
    private SchemeURI schemeURi;

    @JsonProperty("schemeType")
    private ValueFormat schemeType;

    @JsonProperty("resourceTypeGeneral")
    private IdFormat resourceTypeGeneral;

    public ValueFormat getRelatedIdentifier() {
        return relatedIdentifier;
    }

    public void setRelatedIdentifier(ValueFormat relatedIdentifier) {
        this.relatedIdentifier = relatedIdentifier;
    }

    public IdFormat getRelatedIdentifierType() {
        return relatedIdentifierType;
    }

    public void setRelatedIdentifierType(IdFormat relatedIdentifierType) {
        this.relatedIdentifierType = relatedIdentifierType;
    }

    public IdFormat getRelationType() {
        return relationType;
    }

    public void setRelationType(IdFormat relationType) {
        this.relationType = relationType;
    }

    public ValueFormat getRelatedMetadataScheme() {
        return relatedMetadataScheme;
    }

    public void setRelatedMetadataScheme(ValueFormat relatedMetadataScheme) {
        this.relatedMetadataScheme = relatedMetadataScheme;
    }

    public SchemeURI getSchemeURi() {
        return schemeURi;
    }

    public void setSchemeURi(SchemeURI schemeURi) {
        this.schemeURi = schemeURi;
    }

    public ValueFormat getSchemeType() {
        return schemeType;
    }

    public void setSchemeType(ValueFormat schemeType) {
        this.schemeType = schemeType;
    }

    public IdFormat getResourceTypeGeneral() {
        return resourceTypeGeneral;
    }

    public void setResourceTypeGeneral(IdFormat resourceTypeGeneral) {
        this.resourceTypeGeneral = resourceTypeGeneral;
    }
}
