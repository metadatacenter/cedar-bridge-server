package org.metadatacenter.cedar.bridge.resources.extauth;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Recognizes stored authority IRIs before any resolver is called. This never fetches the given URL.
 * The extracted identifier is only a lookup argument; the caller's stored IRI is never rewritten. */
final class AuthorityIdentifier {
  private AuthorityIdentifier() {}

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
