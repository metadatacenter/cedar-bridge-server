package org.metadatacenter.cedar.bridge.resources.extauth;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Recognizes stored authority IRIs before any resolver is called. This never fetches the given URL.
 * The extracted identifier is only a lookup argument; the caller's stored IRI is never rewritten. */
final class AuthorityIdentifier {
  private AuthorityIdentifier() {}

  /** A space or another separator, or a control, format, private-use or unassigned character. */
  private static final Pattern UNPRINTABLE = Pattern.compile("[\\p{Z}\\p{C}]");

  /**
   * Whether {@code id} has at least one character and every one of them is printable, as each
   * character of a DOI or an RRID must be.
   */
  static boolean printable(String id) {
    return !id.isEmpty() && !UNPRINTABLE.matcher(id).find();
  }

  /**
   * The ASCII characters a request path carries as themselves: RFC 3986's unreserved characters, its
   * sub-delimiters but {@code +}, and {@code :} and {@code @}.
   */
  private static final String PATH_LITERALS = "-._~!$&'()*,;=:@";

  /**
   * {@code id} as it is put into a registry's request path, so the registry reads exactly the
   * identifier asked for.
   *
   * <p>Each other character is percent-encoded, a non-ASCII one byte by byte as UTF-8. These are the
   * DOI Handbook's rules for a DOI in a URL (https://www.doi.org/factsheets/DOIProxy.html): it requires
   * {@code % " # ?} and the space to be encoded, and recommends encoding {@code < > { } ^ [ ] ` | \}
   * and {@code +}. Unencoded, a character a URL cannot carry made a request that could not be sent,
   * and a {@code ?} or {@code #} ended the path, so the registry was asked for the identifier's first
   * part.
   *
   * @param slashes whether a slash in {@code id} stays one, as in a DOI, which DataCite reads from
   *                the rest of the path. The slash after a {@code .} or {@code ..} segment is encoded
   *                even so, as the Handbook recommends, so that no server resolves the segment and asks
   *                for another identifier. When false, a slash is encoded like any other character.
   */
  static String requestPath(String id, boolean slashes) {
    StringBuilder path = new StringBuilder();
    String[] segments = slashes ? id.split("/", -1) : new String[]{id};
    for (int i = 0; i < segments.length; i++) {
      if (i > 0) {
        path.append(segments[i - 1].equals(".") || segments[i - 1].equals("..") ? "%2F" : "/");
      }
      for (byte octet : segments[i].getBytes(StandardCharsets.UTF_8)) {
        char c = (char) (octet & 0xFF);
        boolean literal = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
            || PATH_LITERALS.indexOf(c) >= 0;
        path.append(literal ? String.valueOf(c) : String.format("%%%02X", (int) c));
      }
    }
    return path.toString();
  }

  static Optional<String> extract(String authority, String iri) {
    String pattern = switch (authority) {
      case "orcid" -> "https?://orcid\\.org/(\\d{4}-\\d{4}-\\d{4}-\\d{3}[\\dX])/?";
      case "ror" -> "https?://ror\\.org/(0[a-z0-9]{6}[0-9]{2})/?";
      case "doi" -> "(?:https?://(?:dx\\.)?doi\\.org/|doi:)(10\\.[0-9]{4,9}/[^\\s?#]+)";
      case "pmid" -> "(?:https?://pubmed\\.ncbi\\.nlm\\.nih\\.gov/|https?://(?:www\\.)?ncbi\\.nlm\\.nih\\.gov/pubmed/|pmid:)([1-9][0-9]*)/?";
      case "rrid" -> "(?:https?://identifiers\\.org/|https?://scicrunch\\.org/resolver/)?(RRID:[A-Z][A-Z0-9]*_[A-Z0-9.-]+)";
      case "nih-grant" -> "https?://reporter\\.nih\\.gov/project-details/([1-9][0-9]*)/?";
      case "comp-tox" -> "https?://comptox\\.epa\\.gov/dashboard/chemical/details/(DTXSID[0-9]+)";
      default -> null;
    };
    if (pattern == null || iri == null) return Optional.empty();
    Matcher match = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(iri);
    return match.matches() ? Optional.of(match.group(1)) : Optional.empty();
  }
}
