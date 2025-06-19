package org.metadatacenter.cedar.bridge.resources;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Substance {
  private String dtxsid;
  private String preferredName;

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
}
