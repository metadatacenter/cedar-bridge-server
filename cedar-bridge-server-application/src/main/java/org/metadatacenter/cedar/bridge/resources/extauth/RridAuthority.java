package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.MediaType;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Research resources — antibodies, cell lines, tools — from SciCrunch. */
public class RridAuthority implements ExternalAuthority {

  static final String PATH_SEGMENT = "rrid";

  private static final String SCICRUNCH_API_PREFIX = "https://api.scicrunch.io/elastic/v1/*_pr/_search";
  private static final String IDENTIFIERS_ORG_RRID_PREFIX = "https://identifiers.org/RRID:";
  private static final String SCICRUNCH_RESOLVER_API = "https://scicrunch.org/resolver/";

  private final String rridApiKey;

  public RridAuthority(CedarConfig cedarConfig) {
    this.rridApiKey = cedarConfig.getExternalAuthorities().getRrid().getApiKey();
  }

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public AuthoritySearchAnswer search(String query, int page, int pageSize) {
    final String q = (query == null) ? "" : query;
    String requestBody = elasticQuery(q, page * pageSize, pageSize);

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", MediaType.APPLICATION_JSON);
    headers.put("apikey", rridApiKey);

    try {
      ClassicHttpResponse proxyResponse = ProxyUtil.proxyPost(SCICRUNCH_API_PREFIX, headers, requestBody);
      int statusCode = proxyResponse.getCode();
      JsonNode root = JsonMapper.MAPPER.readTree(EntityUtils.toString(proxyResponse.getEntity()));

      if (statusCode != HttpConstants.OK) {
        return AuthoritySearchAnswer.failed(statusCode, null);
      }

      Map<String, Object> results = new LinkedHashMap<>();
      for (JsonNode hit : root.path("hits").path("hits")) {
        JsonNode itemNode = hit.path("_source").path("item");
        String identifier = itemNode.path("identifier").asText(null);
        String name = itemNode.path("name").asText(null);
        if (identifier != null && name != null) {
          Map<String, Object> term = new HashMap<>();
          term.put("name", name);
          term.put("details", buildDetails(itemNode));
          results.put(IDENTIFIERS_ORG_RRID_PREFIX + identifier, term);
        }
      }
      return AuthoritySearchAnswer.of(results);
    } catch (CedarProcessingException | IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public AuthorityDetailsAnswer details(String id) {
    final String resolverUrl = SCICRUNCH_RESOLVER_API + extractBaseRrid(id) + ".json";

    Map<String, String> headers = new HashMap<>();
    headers.put("apikey", rridApiKey);

    try {
      ClassicHttpResponse proxyResponse = ProxyUtil.proxyGet(resolverUrl, headers);
      if (proxyResponse.getCode() != HttpConstants.OK) {
        return AuthorityDetailsAnswer.notFound(new HashMap<>());
      }

      JsonNode root = JsonMapper.MAPPER.readTree(EntityUtils.toString(proxyResponse.getEntity()));
      JsonNode hits = root.path("hits").path("hits");
      if (!hits.isArray() || hits.isEmpty()) {
        return AuthorityDetailsAnswer.notFound(new HashMap<>());
      }

      JsonNode item = hits.get(0).path("_source").path("item");
      String identifier = item.path("identifier").asText(null);
      String name = item.path("name").asText(null);
      if (identifier == null || name == null) {
        return AuthorityDetailsAnswer.notFound(new HashMap<>());
      }

      Map<String, Object> body = new HashMap<>();
      body.put("id", IDENTIFIERS_ORG_RRID_PREFIX + identifier);
      body.put("name", name);
      return AuthorityDetailsAnswer.found(body);
    } catch (CedarProcessingException | IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * The Elasticsearch query SciCrunch is asked, which scores an exact name far above a prefix and
   * a prefix above a substring.
   */
  private static String elasticQuery(String q, int from, int size) {
    ObjectNode root = JsonMapper.MAPPER.createObjectNode();
    root.put("from", from);
    root.put("size", size);

    ArrayNode should = root.putObject("query").putObject("bool").putArray("should");
    should.add(clause("term", q, 100));
    should.add(clause("prefix", q, 10));
    should.add(clause("wildcard", "*" + q + "*", 1));

    try {
      return JsonMapper.MAPPER.writeValueAsString(root);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize SciCrunch query body", e);
    }
  }

  private static ObjectNode clause(String kind, String value, int boost) {
    ObjectNode boosted = JsonMapper.MAPPER.createObjectNode();
    boosted.put("value", value);
    boosted.put("boost", boost);

    ObjectNode field = JsonMapper.MAPPER.createObjectNode();
    field.set("item.name.aggregate", boosted);

    ObjectNode clause = JsonMapper.MAPPER.createObjectNode();
    clause.set(kind, field);
    return clause;
  }

  private static String extractBaseRrid(String rridId) {
    String id = rridId;
    if (id.startsWith("http://") || id.startsWith("https://")) {
      id = id.substring(id.lastIndexOf('/') + 1);
    }
    return id.toUpperCase().startsWith("RRID:") ? id : "RRID:" + id;
  }

  private static String buildDetails(JsonNode itemNode) {
    String name = itemNode.path("name").asText("");

    String type = "resource";
    for (JsonNode typeNode : itemNode.path("types")) {
      String t = typeNode.path("name").asText(null);
      if (t != null) {
        type = t;
        break;
      }
    }

    int xrefCount = itemNode.path("alternateIdentifiers").size();

    return String.format("%s is a %s. Alternate identifiers exist in %d database(s).", name, type, xrefCount);
  }
}
