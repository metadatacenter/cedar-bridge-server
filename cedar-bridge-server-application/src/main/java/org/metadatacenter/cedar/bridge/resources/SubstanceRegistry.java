package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.CharEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.util.http.ProxyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubstanceRegistry {

  private static final Logger log = LoggerFactory.getLogger(SubstanceRegistry.class);

  private static final String PFASSTRUCT_URL_SUFFIX = "chemical/list/chemicals/search/by-listname/PFASSTRUCT";
  private static final String DTXSID_BATCH_LOOKUP_URL_SUFFIX = "chemical/detail/search/by-dtxsid/";
  private static final String DASHBOARD_DETAILS_BASE = "https://comptox.epa.gov/dashboard/chemical/details/";

  private static final int BATCH_SIZE = 1000;

  private final String apiKey;
  private final String apiPrefix;
  private final String pfasStructUrl;
  private final String dtxsidBatchLookupUrl;

  // Single source of truth: DTXSID -> Substance
  private final Map<String, Substance> substanceInfoByDtxsid = new ConcurrentHashMap<>();

  private volatile boolean loaded = false;

  public SubstanceRegistry(CedarConfig cedarConfig) {
    this.apiKey = cedarConfig.getExternalAuthorities().getEpaCompTox().getApiKey();
    this.apiPrefix = cedarConfig.getExternalAuthorities().getEpaCompTox().getApiPrefix();
    this.pfasStructUrl = this.apiPrefix + PFASSTRUCT_URL_SUFFIX;
    this.dtxsidBatchLookupUrl = this.apiPrefix + DTXSID_BATCH_LOOKUP_URL_SUFFIX;
  }

  public Map<String, Substance> getSubstanceInfoByDtxsid() {
    return substanceInfoByDtxsid;
  }

  public boolean isLoaded() {
    return loaded;
  }

  public void clearSubstances() {
    substanceInfoByDtxsid.clear();
    loaded = false;
  }

  public void loadSubstances() throws Exception {
    final Map<String, String> headers = new HashMap<>();
    headers.put("Accept", MediaType.APPLICATION_JSON);
    if (apiKey != null && !apiKey.isEmpty()) {
      headers.put("x-api-key", apiKey);
    }

    // 1) Fetch PFASSTRUCT DTXSIDs
    HttpResponse proxyResponse = ProxyUtil.proxyGet(pfasStructUrl, headers);
    int statusCode = proxyResponse.getStatusLine().getStatusCode();
    if (statusCode != HttpConstants.OK) {
      throw new RuntimeException("Failed to fetch PFASSTRUCT list from EPA CompTox API: HTTP " + statusCode);
    }

    HttpEntity entity = proxyResponse.getEntity();
    if (entity == null) {
      throw new RuntimeException("PFASSTRUCT response entity from EPA CompTox API is null");
    }

    ObjectMapper mapper = new ObjectMapper();
    String json = EntityUtils.toString(entity, CharEncoding.UTF_8);
    List<String> dtxsids = mapper.readValue(json, new TypeReference<List<String>>() {});

    // 2) Batch-lookup details for those DTXSIDs
    headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

    for (int i = 0; i < dtxsids.size(); i += BATCH_SIZE) {
      List<String> batch = dtxsids.subList(i, Math.min(i + BATCH_SIZE, dtxsids.size()));
      String payloadJson = mapper.writeValueAsString(batch);

      HttpResponse detailResponse = ProxyUtil.proxyPost(dtxsidBatchLookupUrl, headers, payloadJson);
      int detailStatus = detailResponse.getStatusLine().getStatusCode();

      if (detailStatus != HttpConstants.OK) {
        log.warn("CompTox batch starting at {} failed: HTTP {}", i, detailStatus);
        continue;
      } else {
        log.info("CompTox batch starting at {} loaded.", i);
      }

      HttpEntity detailEntity = detailResponse.getEntity();
      if (detailEntity == null) {
        log.warn("CompTox batch starting at {} returned empty response", i);
        continue;
      }

      String detailJson = EntityUtils.toString(detailEntity, CharEncoding.UTF_8);

      // The endpoint returns an array of detail objects with keys that match your Substance POJO
      // (at least for dtxsid/preferredName). Unknown fields will be ignored.
      List<Substance> subs = mapper.readValue(detailJson, new TypeReference<List<Substance>>() {});

      for (Substance s : subs) {
        // Guard against malformed entries
        if (s == null || s.getDtxsid() == null) {
          continue;
        }

        substanceInfoByDtxsid.put(s.getDtxsid(), s);
      }
    }
    loaded = true;
  }
}
