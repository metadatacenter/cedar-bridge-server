package org.metadatacenter.cedar.bridge.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.CharEncoding;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.util.http.ProxyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
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

  // Why the registry is in the state it is in. Written by SubstanceRegistryLoader,
  // read by the health check (to describe the condition) and by the CompTox
  // resource (to tell a caller when it is worth trying again).
  private volatile int failedAttempts = 0;
  private volatile String lastError = null;
  private volatile long backoffMs = 0L;
  private volatile long nextAttemptAtMs = 0L;

  /**
   * A consistent snapshot of what the loader has managed so far.
   *
   * @param loaded          whether PFAS lookups can be served
   * @param substanceCount  how many substances are held
   * @param failedAttempts  consecutive failures; 0 before the first attempt finishes
   * @param lastError       the most recent failure's message, or null if none
   * @param backoffMs       the wait the loader applied after the last failure
   * @param nextAttemptAtMs epoch millis of the next scheduled retry, or 0 if none is scheduled
   */
  public record LoadStatus(boolean loaded, int substanceCount, int failedAttempts, String lastError,
                           long backoffMs, long nextAttemptAtMs) {

    /** Whether the loader is still waiting, as opposed to having an attempt due or in flight. */
    public boolean waitingToRetry(long nowMs) {
      return nextAttemptAtMs > nowMs;
    }

    /**
     * How long a caller should wait before asking again, in seconds, or empty when no retry is
     * scheduled at all.
     *
     * <p>While the loader is waiting, that is the remaining wait. Once the attempt is due it is the
     * backoff interval instead, because the attempt is running and, if it fails as the last one
     * did, that interval is what follows. Reporting the remaining wait in that state collapses to
     * "1 second" — which is what a failing attempt that takes longer than its own backoff produces,
     * and it would invite a client to hammer an endpoint whose answer cannot change for minutes.
     */
    public java.util.OptionalLong retryAfterSeconds(long nowMs) {
      if (nextAttemptAtMs <= 0L) {
        return java.util.OptionalLong.empty();
      }
      long millis = waitingToRetry(nowMs) ? nextAttemptAtMs - nowMs : backoffMs;
      return java.util.OptionalLong.of(Math.max(1L, (millis + 999L) / 1000L));
    }
  }

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

  public LoadStatus getLoadStatus() {
    return new LoadStatus(loaded, substanceInfoByDtxsid.size(), failedAttempts, lastError, backoffMs,
        nextAttemptAtMs);
  }

  /** Called by the loader after a failed attempt, with the backoff it is about to apply. */
  public void recordLoadFailure(int failedAttempts, String lastError, long backoffMs, long nextAttemptAtMs) {
    this.failedAttempts = failedAttempts;
    this.lastError = lastError;
    this.backoffMs = backoffMs;
    this.nextAttemptAtMs = nextAttemptAtMs;
  }

  public void clearSubstances() {
    substanceInfoByDtxsid.clear();
    loaded = false;
    failedAttempts = 0;
    lastError = null;
    backoffMs = 0L;
    nextAttemptAtMs = 0L;
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
    ClassicHttpResponse proxyResponse = ProxyUtil.proxyGet(pfasStructUrl, headers);
    int statusCode = proxyResponse.getCode();
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

      ClassicHttpResponse detailResponse = ProxyUtil.proxyPost(dtxsidBatchLookupUrl, headers, payloadJson);
      int detailStatus = detailResponse.getCode();

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
    failedAttempts = 0;
    lastError = null;
    backoffMs = 0L;
    nextAttemptAtMs = 0L;
  }
}
