package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.lifecycle.Managed;
import org.apache.commons.codec.CharEncoding;
import org.apache.http.util.EntityUtils;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.util.http.ProxyUtil;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;

import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SubstanceRegistry implements Managed {

  private final String apiKey;
  private final String PFASSTRUCT_URL =
      "https://api-ccte.epa.gov/chemical/list/chemicals/search/by-listname/PFASSTRUCT";

  private final Map<String, String> chemicalsByDtxsid = new ConcurrentHashMap<>();

  public SubstanceRegistry(CedarConfig cedarConfig) {
    this.apiKey = cedarConfig.getExternalAuthorities().getEpaCompTox().getApiKey();
  }

  public Map<String, String> getSubstancesByDtxsid() {
    return chemicalsByDtxsid;
  }

  @Override
  public void start() throws Exception {
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", MediaType.APPLICATION_JSON);
    if (apiKey != null && !apiKey.isEmpty()) {
      headers.put("x-api-key", apiKey);
    }

    HttpResponse proxyResponse = ProxyUtil.proxyGet(PFASSTRUCT_URL, headers);
    int statusCode = proxyResponse.getStatusLine().getStatusCode();

    if (statusCode != HttpConstants.OK) {
      throw new RuntimeException("Failed to fetch PFASSTRUCT list from EPA CompTox API: HTTP " + statusCode);
    }

    HttpEntity entity = proxyResponse.getEntity();
    if (entity == null) {
      throw new RuntimeException("PFASSTRUCT response entity from EPA CompTox API is null");
    }

    String json = EntityUtils.toString(entity, CharEncoding.UTF_8);
    ObjectMapper mapper = new ObjectMapper();
    List<String> dtxsids = mapper.readValue(json, new TypeReference<>() {});

    for (String dtxsid : dtxsids) {
      if (dtxsid == null || dtxsid.isEmpty())
        continue;

      String detailUrl = "https://api-ccte.epa.gov/chemical/detail/search/by-dtxsid/" + dtxsid;
      HttpResponse detailResponse = ProxyUtil.proxyGet(detailUrl, headers);

      int detailStatus = detailResponse.getStatusLine().getStatusCode();
      if (detailStatus != HttpConstants.OK) {
        // TODO Log ("Failed to fetch detail for " + dtxsid + ": HTTP " + detailStatus);
        continue;
      }

      HttpEntity detailEntity = detailResponse.getEntity();
      if (detailEntity != null) {
        String detailJson = EntityUtils.toString(detailEntity, CharEncoding.UTF_8);
        Map<String, Object> detailMap = mapper.readValue(detailJson, new TypeReference<>() {
        });
        Object preferredNameObj = detailMap.get("preferredName");

        if (preferredNameObj instanceof String) {
          chemicalsByDtxsid.put(dtxsid, (String) preferredNameObj);
        } else {
          // TODO Log ("No preferredName found for " + dtxsid);
        }
      }
    }
  }

  @Override
  public void stop() throws Exception {
    chemicalsByDtxsid.clear();
  }
}
