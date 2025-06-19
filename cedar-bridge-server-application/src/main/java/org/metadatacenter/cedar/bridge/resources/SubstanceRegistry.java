package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.CharEncoding;
import org.apache.http.util.EntityUtils;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.util.http.ProxyUtil;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SubstanceRegistry  {

  private final String apiKey;
  private final String PFASSTRUCT_URL =
      "https://api-ccte.epa.gov/chemical/list/chemicals/search/by-listname/PFASSTRUCT";
  private static final String DTXSID_BATCH_LOOKUP_URL = "https://api-ccte.epa.gov/chemical/detail/search/by-dtxsid/";
  private static final String DTXSID_FIELD = "dtxsid";
  private static final String PREFERRED_NAME_FIELD = "preferredName";
  private static final int BATCH_SIZE = 1000;

  private final Map<String, String> substancesByDtxsid = new ConcurrentHashMap<>();

  private volatile boolean loaded = false;

  public SubstanceRegistry(CedarConfig cedarConfig) {
    this.apiKey = cedarConfig.getExternalAuthorities().getEpaCompTox().getApiKey();
  }

  public Map<String, String> getSubstancesByDtxsid() {
    return substancesByDtxsid;
  }

  public boolean isLoaded() { return loaded; }

  public void loadSubstances() throws Exception {
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
    List<String> dtxsids = mapper.readValue(json, new TypeReference<>() {
    });

    for (int i = 0; i < dtxsids.size(); i += BATCH_SIZE) {
      List<String> batch = dtxsids.subList(i, Math.min(i + BATCH_SIZE, dtxsids.size()));
      String payloadJson = mapper.writeValueAsString(batch);
      headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

      HttpResponse detailResponse = ProxyUtil.proxyPost(DTXSID_BATCH_LOOKUP_URL, headers, payloadJson);

      int detailStatus = detailResponse.getStatusLine().getStatusCode();
      if (detailStatus != HttpConstants.OK) {
        // TODO Log System.err.println("Batch " + i + " failed: HTTP " + detailStatus);
        continue;
      }

      HttpEntity detailEntity = detailResponse.getEntity();
      if (detailEntity == null) {
        // TODO Log System.err.println("Batch " + i + " returned empty response");
        continue;
      }

      String detailJson = EntityUtils.toString(detailEntity, CharEncoding.UTF_8);
      List<Map<String, Object>> results = mapper.readValue(detailJson, new TypeReference<>() {});

      for (Map<String, Object> result : results) {
        String id = (String) result.get(DTXSID_FIELD);
        String name = (String) result.get(PREFERRED_NAME_FIELD);
        if (id != null && name != null) {
          substancesByDtxsid.put(id, name);
        }
      }
    }
    loaded = true;
  }

  public void clearSubstances() {
    substancesByDtxsid.clear();
    loaded = false;
  }
}
