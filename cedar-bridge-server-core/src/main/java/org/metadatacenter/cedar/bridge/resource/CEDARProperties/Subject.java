package org.metadatacenter.cedar.bridge.resource.CEDARProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject {
    @JsonProperty("@context")
    private Map<String, String> context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("subjectName")
    private ValueFormat subjectName;

    @JsonProperty("subjectScheme")
    private ValueFormat subjectScheme;

    @JsonProperty("subjectSchemeURI")
    private SchemeURI subjectSchemeURI;

    @JsonProperty("valueURI")
    private SchemeURI valueURI;

    @JsonProperty("classificationCode")
    private ValueFormat classificationCode;

    public ValueFormat getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(ValueFormat subjectName) {
        this.subjectName = subjectName;
    }

    public ValueFormat getSubjectScheme() {
        return subjectScheme;
    }

    public void setSubjectScheme(ValueFormat subjectScheme) {
        this.subjectScheme = subjectScheme;
    }

    public SchemeURI getSubjectSchemeURI() {
        return subjectSchemeURI;
    }

    public void setSubjectSchemeURI(SchemeURI subjectSchemeURI) {
        this.subjectSchemeURI = subjectSchemeURI;
    }

    public SchemeURI getValueURI() {
        return valueURI;
    }

    public void setValueURI(SchemeURI valueURI) {
        this.valueURI = valueURI;
    }

    public ValueFormat getClassificationCode() {
        return classificationCode;
    }

    public void setClassificationCode(ValueFormat classificationCode) {
        this.classificationCode = classificationCode;
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
