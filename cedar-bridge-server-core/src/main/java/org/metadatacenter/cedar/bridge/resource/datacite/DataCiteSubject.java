package org.metadatacenter.cedar.bridge.resource.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCiteSubject {
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("schemeUri")
    private String schemeUri;
    @JsonProperty("subjectScheme")
    private String subjectScheme;
    @JsonProperty("classificationCode")
    private String classificationCode;
    @JsonProperty("valueUri")
    private String valueUri;

    public String getValueUri() {
        return valueUri;
    }

    public void setValueUri(String valueUri) {
        this.valueUri = valueUri;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSchemeUri() {
        return schemeUri;
    }

    public void setSchemeUri(String schemeUri) {
        this.schemeUri = schemeUri;
    }

    public String getSubjectScheme() {
        return subjectScheme;
    }

    public void setSubjectScheme(String subjectScheme) {
        this.subjectScheme = subjectScheme;
    }

    public String getClassificationCode() {
        return classificationCode;
    }

    public void setClassificationCode(String classificationCode) {
        this.classificationCode = classificationCode;
    }
}
