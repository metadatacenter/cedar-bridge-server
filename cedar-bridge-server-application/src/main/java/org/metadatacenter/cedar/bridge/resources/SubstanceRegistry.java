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

/**
 * Loads PFASSTRUCTV5 membership and minimal chemical details from the
 * new CTX Chemical API into an in-memory map keyed by DTXSID.
 *
 * This preserves your existing pattern:
 *  1) GET list -> array of DTXSIDs
 *  2) POST batch -> array of detail objects (mapped to Substance)
 *
 * Configure in CedarConfig:
 *   externalAuthorities.epaCompTox.apiPrefix = "https://comptox.epa.gov/ctx-api/"
 *   externalAuthorities.epaCompTox.apiKey    = "<YOUR_NEW_CTX_KEY>"
 */
public class SubstanceRegistry {

  private static final Logger log = LoggerFactory.getLogger(SubstanceRegistry.class);

  /** Returns JSON array of DTXSIDs for the PFAS structure list (V5). */
  private static final String PFASSTRUCT_URL_SUFFIX =
      "chemical/list/chemicals/search/by-listname/PFASSTRUCTV5";
  // If your server expects query form instead of path segment, use:
  // "chemical/list/chemicals/search/by-listname?list_name=PFASSTRUCTV5"

  /** Batch details by DTXSID (POST body: ["DTXSID...", ...]) */
  private static final String DTXSID_BATCH_LOOKUP_URL_SUFFIX =
      "chemical/detail/search/by-dtxsid/";

  @SuppressWarnings("unused")
  private static final String DASHBOARD_DETAILS_BASE =
      "https://comptox.epa.gov/dashboard/chemical/details/";

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

  /**
   * Loads PFASSTRUCTV5 into memory.
   * Step 1: GET list to retrieve all DTXSIDs.
   * Step 2: POST batches of DTXSIDs to fetch details (projection defaults to chemicaldetailstandard).
   */
  public void loadSubstances() throws Exception {
    final Map<String, String> headers = new HashMap<>();
    headers.put("Accept", MediaType.APPLICATION_JSON);
    if (apiKey != null && !apiKey.isEmpty()) {
      headers.put("x-api-key", apiKey);
    }

    // ---- 1) Fetch PFASSTRUCTV5 DTXSIDs ----
    HttpResponse proxyResponse = ProxyUtil.proxyGet(pfasStructUrl, headers);
    int statusCode = proxyResponse.getStatusLine().getStatusCode();
    if (statusCode != HttpConstants.OK) {
      throw new RuntimeException("Failed to fetch PFASSTRUCTV5 list from EPA CTX API: HTTP " + statusCode);
    }

    HttpEntity entity = proxyResponse.getEntity();
    if (entity == null) {
      throw new RuntimeException("PFASSTRUCTV5 response entity from EPA CTX API is null");
    }

    ObjectMapper mapper = new ObjectMapper();
    String json = EntityUtils.toString(entity, CharEncoding.UTF_8);
    List<String> dtxsids = mapper.readValue(json, new TypeReference<List<String>>() {});

    // ---- 2) Batch-lookup details for those DTXSIDs ----
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

      // Endpoint returns an array of detail objects; unknown fields ignored by Substance
      List<Substance> subs = mapper.readValue(detailJson, new TypeReference<List<Substance>>() {});

      for (Substance s : subs) {
        if (s == null || s.getDtxsid() == null) {
          continue;
        }
        substanceInfoByDtxsid.put(s.getDtxsid(), s);
      }
    }
    loaded = true;
  }
}
