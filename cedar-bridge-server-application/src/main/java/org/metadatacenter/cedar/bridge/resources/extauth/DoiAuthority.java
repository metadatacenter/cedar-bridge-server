package org.metadatacenter.cedar.bridge.resources.extauth;

import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.exception.CedarProcessingException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/** Publications and datasets, from DataCite. */
public class DoiAuthority implements ExternalAuthority {

  static final String PATH_SEGMENT = "doi";

  private static final String DATACITE_API_PREFIX = "https://api.datacite.org/dois";
  private static final String DOI_IRI_BASE = "https://doi.org/";

  /**
   * The characters DataCite's query syntax, Elasticsearch's query string, reads as operators. The
   * slash is one of them, but DataCite escapes every slash in a query itself, and refuses a query
   * whose slash was escaped already.
   */
  private static final String QUERY_OPERATOR_CHARACTERS = "+-=&|<>!(){}[]^\"~*?:\\";

  /** The words the same syntax reads as operators when one stands alone. */
  private static final Set<String> QUERY_OPERATOR_WORDS = Set.of("AND", "OR", "NOT");

  /** What the query syntax separates words with. */
  private static final Pattern QUERY_WHITESPACE = Pattern.compile("[ \\t\\n\\r\\u3000]+");

  private final String dataciteApiPrefix;

  public DoiAuthority() {
    this(DATACITE_API_PREFIX);
  }

  /** An authority reading another DataCite endpoint, so a test can stand one up locally. */
  DoiAuthority(String dataciteApiPrefix) {
    this.dataciteApiPrefix = dataciteApiPrefix;
  }

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public String registryName() {
    return "DataCite";
  }

  @Override
  public AuthoritySearchAnswer search(String query, int offset, int limit) throws CedarProcessingException {
    final String q = (query == null) ? "" : query;
    // DataCite pages by number, counting from one, and cannot start at an arbitrary offset. An
    // offset that falls inside a page is served from the two pages it straddles.
    final int firstPage = offset / limit + 1;
    final int skip = offset % limit;

    RegistryReply upstream = RegistryReply.get(pageUrl(q, firstPage, limit), new HashMap<>());
    if (!upstream.ok()) {
      // DataCite's own status is passed on, which is what this route has always done: a registry
      // that is down is not a search that found nothing.
      return AuthoritySearchAnswer.failed(upstream.status(), null);
    }
    JsonNode document = upstream.json();
    List<Map.Entry<String, Object>> terms = new ArrayList<>(terms(document).entrySet());
    long total = document.path("meta").path("total").asLong(0);
    if (skip > 0 && offset + limit > (long) firstPage * limit && (long) firstPage * limit < total) {
      RegistryReply next = RegistryReply.get(pageUrl(q, firstPage + 1, limit), new HashMap<>());
      if (!next.ok()) {
        return AuthoritySearchAnswer.failed(next.status(), null);
      }
      terms.addAll(terms(next.json()).entrySet());
    }

    Map<String, Object> results = new LinkedHashMap<>();
    for (Map.Entry<String, Object> term : terms.subList(Math.min(skip, terms.size()),
        Math.min(skip + limit, terms.size()))) {
      results.put(term.getKey(), term.getValue());
    }
    return AuthoritySearchAnswer.of(results, total);
  }

  private String pageUrl(String q, int page, int size) {
    // Titles only, so a search for a name does not match an abstract. The query is encoded, so what
    // was typed is one value. Unencoded, a space could not be sent, and an ampersand set DataCite's
    // other parameters.
    return String.format("%s?query=%s&page[number]=%d&page[size]=%d",
        dataciteApiPrefix, URLEncoder.encode(titleQuery(q), StandardCharsets.UTF_8), page, size);
  }

  /**
   * The query for titles holding every word typed, in any order.
   *
   * <p>The words are grouped, and DataCite requires every term of a group. Ungrouped, only the first
   * word was a title term, and the rest matched any field DataCite searches. Each operator character
   * is escaped, and so is each operator word, so what was typed stays text.
   */
  private static String titleQuery(String typed) {
    StringJoiner query = new StringJoiner(" ", "titles.title:(", ")");
    for (String word : QUERY_WHITESPACE.split(typed.trim())) {
      StringBuilder escaped = new StringBuilder(QUERY_OPERATOR_WORDS.contains(word) ? "\\" : "");
      for (char c : word.toCharArray()) {
        if (QUERY_OPERATOR_CHARACTERS.indexOf(c) >= 0) {
          escaped.append('\\');
        }
        escaped.append(c);
      }
      query.add(escaped);
    }
    return query.toString();
  }

  private Map<String, Object> terms(JsonNode root) {
    Map<String, Object> results = new LinkedHashMap<>();
    JsonNode data = root.path("data");
    if (data.isArray()) {
      for (JsonNode itemNode : data) {
        JsonNode attributes = itemNode.path("attributes");
        String doi = attributes.path("doi").asText(null);
        String title = title(attributes);
        if (doi != null && title != null) {
          Map<String, Object> term = new HashMap<>();
          term.put("name", title);
          term.put("details", detailsSentence(attributes, title));
          results.put(DOI_IRI_BASE + doi, term);
        }
      }
    }
    return results;
  }

  /**
   * Whether {@code id} could be a DOI DataCite holds. One holding a character that cannot be part of a
   * DOI could not. By this server's rule, neither could one whose last segment is {@code .} or
   * {@code ..}, because the path it would be asked for ends in that segment, and a server that resolves
   * the segment looks up another DOI. The slash after any other such segment is encoded, as
   * {@link AuthorityIdentifier#requestPath} explains, but a last segment has no slash after it.
   */
  @Override
  public boolean couldIdentify(String id) {
    String doi = extractBaseDoi(id);
    String lastSegment = doi.substring(doi.lastIndexOf('/') + 1);
    return AuthorityIdentifier.printable(doi) && !lastSegment.equals(".") && !lastSegment.equals("..");
  }

  @Override
  public AuthorityDetailsAnswer details(String id) throws CedarProcessingException {
    RegistryReply upstream = RegistryReply.get(
        dataciteApiPrefix + "/" + AuthorityIdentifier.requestPath(extractBaseDoi(id), true), new HashMap<>());
    if (upstream.status() == HttpConstants.NOT_FOUND) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }
    if (!upstream.ok()) {
      // An identifier DataCite failed to look up is not one it does not hold. This answered
      // "not found" for both, so an outage told the user the DOI did not exist.
      return AuthorityDetailsAnswer.failed(upstream.status(), new HashMap<>());
    }

    JsonNode attributes = upstream.json().path("data").path("attributes");
    String doi = attributes.path("doi").asText(null);
    String title = title(attributes);
    if (doi == null || title == null) {
      return AuthorityDetailsAnswer.incomplete(new HashMap<>());
    }

    Map<String, Object> body = new HashMap<>();
    body.put("id", DOI_IRI_BASE + doi);
    body.put("name", title);
    body.put("details", detailsSentence(attributes, title));
    return AuthorityDetailsAnswer.found(body);
  }

  private static String title(JsonNode attributes) {
    JsonNode titles = attributes.path("titles");
    return titles.isArray() && titles.size() > 0 ? titles.get(0).path("title").asText(null) : null;
  }

  private static String extractBaseDoi(String doiId) {
    String id = doiId.trim();
    if (id.startsWith("https://doi.org/")) {
      id = id.substring("https://doi.org/".length());
    } else if (id.startsWith("http://doi.org/")) {
      id = id.substring("http://doi.org/".length());
    } else if (id.toLowerCase(Locale.ROOT).startsWith("doi:")) {
      id = id.substring("doi:".length());
    }
    return id;
  }

  private static String detailsSentence(JsonNode attributes, String title) {
    String publisher = attributes.path("publisher").asText("");
    String pubYear = attributes.path("publicationYear").asText("");
    String resourceType = attributes.path("types").path("resourceTypeGeneral").asText("");

    StringBuilder sb = new StringBuilder(title);
    if (!publisher.isEmpty()) {
      sb.append(" was published by ").append(publisher);
    }
    if (!pubYear.isEmpty()) {
      sb.append(" in ").append(pubYear);
    }
    if (!resourceType.isEmpty()) {
      sb.append(" (resource type: ").append(resourceType).append(")");
    }
    sb.append(".");
    return sb.toString();
  }
}
