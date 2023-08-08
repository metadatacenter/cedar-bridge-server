package org.metadatacenter.cedar.bridge.resource.CedarProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedIdentifier {
    @JsonProperty("@context")
    private Map<String, String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("RelatedIdentifier")
    private ValueFormat relatedIdentifier;

    @JsonProperty("relatedIdentifierType")
    private IdFormat relatedIdentifierType;

    @JsonProperty("relationType")
    private IdFormat relationType;

    @JsonProperty("relatedMetadataScheme")
    private ValueFormat relatedMetadataScheme;

    @JsonProperty("schemeURI")
    private SchemeUri schemeURi;

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

    public SchemeUri getSchemeURi() {
        return schemeURi;
    }

    public void setSchemeURi(SchemeUri schemeURi) {
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

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
