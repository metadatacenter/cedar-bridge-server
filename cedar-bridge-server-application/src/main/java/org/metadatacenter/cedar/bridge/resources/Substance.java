package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Substance {

  private String dtxsid;
  private String preferredName;
  private String casrn;
  private String molecularFormula;
  private Double molecularWeight;
  private String smiles;
  private String inchi;
  private String inchiKey;
  private Integer synonymCount;
  private String dsstoxCompoundId;
  private String qcLevel;

  public Substance() {}

  @JsonProperty("dtxsid")
  public String getDtxsid() {
    return dtxsid;
  }

  @JsonProperty("dtxsid")
  public void setDtxsid(String dtxsid) {
    this.dtxsid = dtxsid;
  }

  @JsonProperty("preferredName")
  public String getPreferredName() {
    return preferredName;
  }

  @JsonProperty("preferredName")
  public void setPreferredName(String preferredName) {
    this.preferredName = preferredName;
  }

  @JsonProperty("casrn")
  public String getCasrn() {
    return casrn;
  }

  @JsonProperty("casrn")
  public void setCasrn(String casrn) {
    this.casrn = casrn;
  }

  @JsonProperty("molecularFormula")
  public String getMolecularFormula() {
    return molecularFormula;
  }

  @JsonProperty("molecularFormula")
  public void setMolecularFormula(String molecularFormula) {
    this.molecularFormula = molecularFormula;
  }

  @JsonProperty("molecularWeight")
  public Double getMolecularWeight() {
    return molecularWeight;
  }

  @JsonProperty("molecularWeight")
  public void setMolecularWeight(Double molecularWeight) {
    this.molecularWeight = molecularWeight;
  }

  @JsonProperty("smiles")
  public String getSmiles() {
    return smiles;
  }

  @JsonProperty("smiles")
  public void setSmiles(String smiles) {
    this.smiles = smiles;
  }

  @JsonProperty("inchi")
  public String getInchi() {
    return inchi;
  }

  @JsonProperty("inchi")
  public void setInchi(String inchi) {
    this.inchi = inchi;
  }

  @JsonProperty("inchikey")
  public String getInchiKey() {
    return inchiKey;
  }

  @JsonProperty("inchikey")
  public void setInchiKey(String inchiKey) {
    this.inchiKey = inchiKey;
  }

  @JsonProperty("synonymCount")
  public Integer getSynonymCount() {
    return synonymCount;
  }

  @JsonProperty("synonymCount")
  public void setSynonymCount(Integer synonymCount) {
    this.synonymCount = synonymCount;
  }

  @JsonProperty("dsstoxCompoundId")
  public String getDsstoxCompoundId() {
    return dsstoxCompoundId;
  }

  @JsonProperty("dsstoxCompoundId")
  public void setDsstoxCompoundId(String dsstoxCompoundId) {
    this.dsstoxCompoundId = dsstoxCompoundId;
  }

  @JsonProperty("qcLevel")
  public String getQcLevel() {
    return qcLevel;
  }

  @JsonProperty("qcLevel")
  public void setQcLevel(String qcLevel) {
    this.qcLevel = qcLevel;
  }
}
