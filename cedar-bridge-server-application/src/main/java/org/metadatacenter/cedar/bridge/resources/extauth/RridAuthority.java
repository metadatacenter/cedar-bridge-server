package org.metadatacenter.cedar.bridge.resources.extauth;

import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.MediaType;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.util.json.JsonMapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Research resources — antibodies, cell lines, tools — from SciCrunch. */
public class RridAuthority implements ExternalAuthority {

  static final String PATH_SEGMENT = "rrid";

  private static final String SCICRUNCH_API_PREFIX = "https://api.scicrunch.io/elastic/v1/*_pr/_search";
  private static final String IDENTIFIERS_ORG_PREFIX = "https://identifiers.org/";
  private static final String SCICRUNCH_RESOLVER_API = "https://scicrunch.org/resolver/";

  private final String searchApi;
  private final String resolverApi;
  private final String rridApiKey;

  public RridAuthority(CedarConfig cedarConfig) {
    this(SCICRUNCH_API_PREFIX, SCICRUNCH_RESOLVER_API, cedarConfig.getExternalAuthorities().getRrid().getApiKey());
  }

  /** An authority reading other SciCrunch endpoints, so a test can stand them up locally. */
  RridAuthority(String searchApi, String resolverApi, String rridApiKey) {
    this.searchApi = searchApi;
    this.resolverApi = resolverApi;
    this.rridApiKey = rridApiKey;
  }

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public String registryName() {
    return "SciCrunch";
  }

  @Override
  public AuthoritySearchAnswer search(String query, int offset, int limit) throws CedarProcessingException {
    final String q = (query == null) ? "" : query;
    String requestBody = elasticQuery(q, offset, limit);

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", MediaType.APPLICATION_JSON);
    headers.put("apikey", rridApiKey);

    // The status first: SciCrunch's error pages are not JSON, and reading one first made its
    // refusal a 500.
    RegistryReply reply = RegistryReply.post(searchApi, headers, requestBody);
    if (!reply.ok()) {
      return AuthoritySearchAnswer.failed(reply.status(), null);
    }
    JsonNode root = reply.json();

    Map<String, Object> results = new LinkedHashMap<>();
    for (JsonNode hit : root.path("hits").path("hits")) {
      JsonNode source = hit.path("_source");
      JsonNode itemNode = source.path("item");
      String identifier = canonicalRrid(source);
      String name = itemNode.path("name").asText(null);
      if (identifier != null && name != null) {
        Map<String, Object> term = new HashMap<>();
        term.put("name", name);
        term.put("details", buildDetails(itemNode));
        results.put(IDENTIFIERS_ORG_PREFIX + identifier, term);
      }
    }
    return answer(results, root.path("hits").path("total"));
  }

  @Override
  public boolean couldIdentify(String id) {
    String rrid = extractBaseRrid(id);
    return AuthorityIdentifier.printable(rrid.substring(rrid.indexOf(':') + 1));
  }

  @Override
  public AuthorityDetailsAnswer details(String id) throws CedarProcessingException {
    // SciCrunch reads the RRID from one path segment, colons and all.
    final String resolverUrl = resolverApi + AuthorityIdentifier.requestPath(extractBaseRrid(id), false) + ".json";

    Map<String, String> headers = new HashMap<>();
    headers.put("apikey", rridApiKey);

    RegistryReply reply = RegistryReply.get(resolverUrl, headers);
    if (reply.status() == HttpConstants.NOT_FOUND) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }
    if (!reply.ok()) {
      // An RRID SciCrunch failed to resolve is not one it does not hold.
      return AuthorityDetailsAnswer.failed(reply.status(), new HashMap<>());
    }

    JsonNode hits = reply.json().path("hits").path("hits");
    if (!hits.isArray()) return AuthorityDetailsAnswer.incomplete(new HashMap<>());
    if (hits.isEmpty()) return AuthorityDetailsAnswer.notFound(new HashMap<>());

    JsonNode source = hits.get(0).path("_source");
    JsonNode item = source.path("item");
    String identifier = canonicalRrid(source);
    String name = item.path("name").asText(null);
    if (identifier == null || name == null) {
      return AuthorityDetailsAnswer.incomplete(new HashMap<>());
    }

    Map<String, Object> body = new HashMap<>();
    body.put("id", IDENTIFIERS_ORG_PREFIX + identifier);
    body.put("name", name);
    return AuthorityDetailsAnswer.found(body);
  }

  /**
   * The Elasticsearch query SciCrunch is asked, which scores an exact name far above a prefix and
   * a prefix above a substring.
   */
  /**
   * The terms, out of the total Elasticsearch reported. It reports one either as a number or, past
   * its tracking limit, as a value with a relation that says whether the value is exact.
   */
  static AuthoritySearchAnswer answer(Map<String, ?> results, JsonNode total) {
    if (total.isObject()) {
      return AuthoritySearchAnswer.of(results, total.path("value").asLong(0),
          !"eq".equals(total.path("relation").asText("eq")));
    }
    return AuthoritySearchAnswer.of(results, total.asLong(0));
  }

  private static String elasticQuery(String q, int from, int size) {
    ObjectNode root = JsonMapper.STRICT_MAPPER.createObjectNode();
    root.put("from", from);
    root.put("size", size);

    ArrayNode should = root.putObject("query").putObject("bool").putArray("should");
    should.add(clause("term", q, 100));
    should.add(clause("prefix", q, 10));
    should.add(clause("wildcard", "*" + q + "*", 1));

    try {
      return JsonMapper.STRICT_MAPPER.writeValueAsString(root);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize SciCrunch query body", e);
    }
  }

  private static ObjectNode clause(String kind, String value, int boost) {
    ObjectNode boosted = JsonMapper.STRICT_MAPPER.createObjectNode();
    boosted.put("value", value);
    boosted.put("boost", boost);

    ObjectNode field = JsonMapper.STRICT_MAPPER.createObjectNode();
    field.set("item.name.aggregate", boosted);

    ObjectNode clause = JsonMapper.STRICT_MAPPER.createObjectNode();
    clause.set(kind, field);
    return clause;
  }

  private static String extractBaseRrid(String rridId) {
    String id = rridId.trim();
    if (id.startsWith("http://") || id.startsWith("https://")
        || id.startsWith("identifiers.org/") || id.startsWith("scicrunch.org/resolver/")) {
      id = id.substring(id.lastIndexOf('/') + 1);
    }
    return id.toUpperCase(Locale.ROOT).startsWith("RRID:") ? "RRID:" + id.substring(5) : "RRID:" + id;
  }

  /**
   * A source's local identifier is not necessarily its RRID: NCBI Taxonomy's 1260732
   * is RRID:NCBITaxon_1260732, and Addgene's 114536 is RRID:Addgene_114536.
   * Prefer SciCrunch's RRID, retaining the qualified identifiers older records supplied.
   * A bare local number without a CURIE cannot safely be turned into an RRID.
   */
  private static String canonicalRrid(JsonNode source) {
    for (JsonNode node : new JsonNode[] {source.path("rrid").path("curie"),
        source.path("item").path("curie"), source.path("item").path("identifier")}) {
      if (!node.isTextual()) continue;
      String value = node.asText().trim();
      if (value.regionMatches(true, 0, "RRID:", 0, 5) && value.length() > 5) {
        return "RRID:" + value.substring(5);
      }
    }
    String identifier = source.path("item").path("identifier").asText("").trim();
    if (identifier.matches("[A-Za-z][A-Za-z0-9-]*[_:][A-Za-z0-9][A-Za-z0-9_.:-]*|nif-[0-9]{4}-[0-9]+")) {
      return "RRID:" + identifier;
    }
    return null;
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

    // SciCrunch's types are common nouns such as "antibody" and "cell line", whose article follows
    // their first letter.
    boolean vowel = !type.isEmpty() && "aeiou".indexOf(Character.toLowerCase(type.charAt(0))) >= 0;
    String article = vowel ? "an" : "a";
    return String.format("%s is %s %s. Alternate identifiers exist in %d database(s).", name, article, type, xrefCount);
  }
}
