package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.CharEncoding;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.util.http.ProxyUtil;
import org.metadatacenter.util.http.UrlUtil;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Organisations, from ROR.
 *
 * <p>One of the two authorities whose details answer carries the registry's whole record, which
 * the field that shows an organisation panel reads.
 */
public class RorAuthority implements ExternalAuthority {

  static final String PATH_SEGMENT = "ror";

  private static final String ROR_API_V2_ORGANIZATIONS_PREFIX = "organizations/";
  private static final String ROR_API_V2_ORGANIZATION_SEARCH_PREFIX = "organizations?query=";

  private final String rorApiPrefix;

  public RorAuthority(CedarConfig cedarConfig) {
    this.rorApiPrefix = cedarConfig.getExternalAuthorities().getRor().getApiPrefix();
  }

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public AuthoritySearchAnswer search(String query, int page, int pageSize) throws CedarException {
    // ROR matches a whole word unless asked otherwise, and a field is searched as it is typed.
    String fragment = query;
    if (fragment != null && !fragment.isEmpty() && !fragment.endsWith("*")) {
      fragment = fragment + "*";
    }

    String url = rorApiPrefix + ROR_API_V2_ORGANIZATION_SEARCH_PREFIX + UrlUtil.urlEncode(fragment);
    ClassicHttpResponse proxyResponse = ProxyUtil.proxyGet(url, new HashMap<>());
    int statusCode = proxyResponse.getCode();
    JsonNode root = read(proxyResponse);

    if (statusCode != HttpConstants.OK) {
      return AuthoritySearchAnswer.failed(statusCode, null);
    }

    // ROR pages its own results; this pages them again over what it returned, which is what this
    // route has always done.
    List<Map.Entry<String, Map<String, String>>> found = new ArrayList<>(searchNames(root).entrySet());
    Map<String, Map<String, String>> results = new LinkedHashMap<>();
    int fromIndex = page * pageSize;
    int toIndex = Math.min(fromIndex + pageSize, found.size());
    if (fromIndex < found.size()) {
      for (Map.Entry<String, Map<String, String>> entry : found.subList(fromIndex, toIndex)) {
        results.put(entry.getKey(), entry.getValue());
      }
    }

    return AuthoritySearchAnswer.of(results);
  }

  @Override
  public AuthorityDetailsAnswer details(String id) throws CedarException {
    String url = rorApiPrefix + ROR_API_V2_ORGANIZATIONS_PREFIX + UrlUtil.urlEncode(id);
    ClassicHttpResponse proxyResponse = ProxyUtil.proxyGet(url, new HashMap<>());
    int statusCode = proxyResponse.getCode();
    JsonNode root = read(proxyResponse);

    Map<String, Object> body = new HashMap<>();
    body.put("rawResponse", root);

    if (statusCode != HttpConstants.OK) {
      body.put("name", null);
      body.put("errors", errors(root));
      return AuthorityDetailsAnswer.failed(statusCode, body);
    }

    body.put("id", textOrNull(root, "id"));
    body.put("name", bestName(root));
    return AuthorityDetailsAnswer.found(body);
  }

  private static JsonNode read(ClassicHttpResponse proxyResponse) {
    try {
      return JsonMapper.MAPPER.readTree(EntityUtils.toString(proxyResponse.getEntity(), CharEncoding.UTF_8));
    } catch (IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * The name ROR displays an organisation under.
   *
   * <p>An organisation carries several names — its acronym, its name in other languages — and the
   * one to show is the one typed {@code ror_display}. The first name of any type is the fallback,
   * so a record without the type is still named rather than dropped.
   */
  private static String bestName(JsonNode node) {
    JsonNode namesNode = node.get("names");
    if (namesNode == null || !namesNode.isArray()) {
      return null;
    }

    String fallbackName = null;
    for (JsonNode name : namesNode) {
      JsonNode typesNode = name.get("types");
      JsonNode valueNode = name.get("value");
      if (typesNode != null && typesNode.isArray()) {
        for (JsonNode type : typesNode) {
          if ("ror_display".equals(type.textValue()) && valueNode != null) {
            return valueNode.textValue();
          }
        }
      }
      if (valueNode != null && fallbackName == null) {
        fallbackName = valueNode.textValue();
      }
    }
    return fallbackName;
  }

  private static Map<String, Map<String, String>> searchNames(JsonNode root) {
    Map<String, Map<String, String>> idToName = new LinkedHashMap<>();
    JsonNode itemsNode = root.get("items");
    if (itemsNode == null || !itemsNode.isArray()) {
      return idToName;
    }

    for (JsonNode item : itemsNode) {
      String rorId = textOrNull(item, "id");
      if (rorId == null) {
        continue;
      }
      String bestName = bestName(item);
      if (bestName != null) {
        Map<String, String> term = new HashMap<>();
        term.put("name", bestName);
        term.put("details", buildDetails(item));
        idToName.put(rorId, term);
      }
    }
    return idToName;
  }

  private static List<String> errors(JsonNode root) {
    List<String> errors = new ArrayList<>();
    JsonNode errorsNode = root.get("errors");
    if (errorsNode != null && errorsNode.isArray()) {
      for (JsonNode error : errorsNode) {
        if (error != null) {
          errors.add(error.textValue());
        }
      }
    }
    return errors;
  }

  private static String buildDetails(JsonNode item) {
    StringBuilder sb = new StringBuilder();

    JsonNode typesNode = item.get("types");
    if (typesNode != null && typesNode.isArray() && typesNode.size() > 0) {
      List<String> types = new ArrayList<>();
      for (JsonNode type : typesNode) {
        if (type != null && type.isTextual()) {
          types.add(type.textValue());
        }
      }
      if (!types.isEmpty()) {
        sb.append("Resource is of type ").append(String.join(", ", types));
      }
    }

    JsonNode countryNode = item.get("country");
    if (countryNode != null && countryNode.get("country_name") != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append("located in ").append(countryNode.get("country_name").textValue());
    }

    JsonNode linksNode = item.get("links");
    if (linksNode != null && linksNode.isArray() && linksNode.size() > 0) {
      JsonNode firstLink = linksNode.get(0);
      if (firstLink != null && firstLink.isTextual()) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        sb.append("with URL ").append(firstLink.textValue());
      }
    }
    return sb.length() > 0 ? sb.toString() : null;
  }

  private static String textOrNull(JsonNode node, String field) {
    JsonNode value = node.get(field);
    return (value != null && value.isTextual()) ? value.textValue() : null;
  }
}
