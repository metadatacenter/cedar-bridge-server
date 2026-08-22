package org.metadatacenter.cedar.bridge.resources.extauth;

import org.metadatacenter.cedar.bridge.resources.Substance;
import org.metadatacenter.cedar.bridge.resources.SubstanceRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PFAS substances, from the EPA CompTox registry CEDAR loads and holds.
 *
 * <p>The one authority that answers from memory rather than by proxying, which is why it is also
 * the one that can be unable to answer at all: until the first load completes there is nothing to
 * search. It reports that as {@link AuthorityNotReadyException} rather than as an empty result,
 * so "we do not have the data yet" stays distinguishable from "the data does not contain that".
 *
 * <p>Served under {@code comp-tox}, which is the registry's name rather than the field's.
 */
public class PfasAuthority implements ExternalAuthority {

  static final String PATH_SEGMENT = "comp-tox";

  private static final String SUBSTANCE_IRI_BASE = "https://comptox.epa.gov/dashboard/chemical/details/";

  /** Used only before the loader has scheduled a retry, i.e. while the first load is in flight. */
  private static final long DEFAULT_RETRY_AFTER_SECONDS = 30L;

  private final SubstanceRegistry substanceRegistry;

  public PfasAuthority(SubstanceRegistry substanceRegistry) {
    this.substanceRegistry = substanceRegistry;
  }

  @Override
  public String pathSegment() {
    return PATH_SEGMENT;
  }

  @Override
  public AuthoritySearchAnswer search(String query, int page, int pageSize) {
    requireLoaded();

    if (query == null || query.trim().isEmpty()) {
      return AuthoritySearchAnswer.nothing();
    }

    final String fragmentLower = query.toLowerCase();
    Map<String, Substance> substances = substanceRegistry.getSubstanceInfoByDtxsid();

    List<Map.Entry<String, Substance>> matches = new ArrayList<>();
    for (Map.Entry<String, Substance> entry : substances.entrySet()) {
      String preferredName = entry.getValue().getPreferredName();
      if (preferredName != null && preferredName.toLowerCase().contains(fragmentLower)) {
        matches.add(entry);
      }
    }

    // Deterministic order: sort by preferred name (case-insensitive), then by DTXSID.
    matches.sort((a, b) -> {
      String na = (a.getValue() == null) ? "" : a.getValue().getPreferredName();
      String nb = (b.getValue() == null) ? "" : b.getValue().getPreferredName();
      int cmp = na.compareToIgnoreCase(nb);
      if (cmp != 0) {
        return cmp;
      }
      String ida = (a.getValue() == null) ? a.getKey() : a.getValue().getDtxsid();
      String idb = (b.getValue() == null) ? b.getKey() : b.getValue().getDtxsid();
      return ida.compareTo(idb);
    });

    Map<String, Map<String, Object>> results = new LinkedHashMap<>();
    int fromIndex = page * pageSize;
    if (fromIndex < matches.size()) {
      int toIndex = Math.min(fromIndex + pageSize, matches.size());
      for (Map.Entry<String, Substance> entry : matches.subList(fromIndex, toIndex)) {
        Substance substance = entry.getValue();
        Map<String, Object> term = new HashMap<>();
        term.put("name", substance.getPreferredName());
        term.put("details", buildCompToxDetails(substance));
        results.put(SUBSTANCE_IRI_BASE + entry.getKey(), term);
      }
    }

    return AuthoritySearchAnswer.of(results);
  }

  @Override
  public AuthorityDetailsAnswer details(String id) {
    requireLoaded();

    // Normalize: extract the DTXSID if a full IRI arrived, otherwise treat it as the fragment.
    final String ctxsid = id.startsWith(SUBSTANCE_IRI_BASE) ? id.substring(SUBSTANCE_IRI_BASE.length()) : id;

    Map<String, Substance> substances = substanceRegistry.getSubstanceInfoByDtxsid();
    if (!substances.containsKey(ctxsid)) {
      return AuthorityDetailsAnswer.notFound(new HashMap<>());
    }

    Map<String, Object> body = new HashMap<>();
    body.put("id", SUBSTANCE_IRI_BASE + ctxsid);
    body.put("name", substances.get(ctxsid).getPreferredName());
    return AuthorityDetailsAnswer.found(body);
  }

  /**
   * Tell the caller to come back when there is a point in coming back.
   *
   * <p>This was a fixed 30 seconds, which is a fair guess while the first load is in flight but
   * wrong once the loader has backed off — its interval grows to ten minutes, and an EPA outage
   * can run for days. Deriving the value from the loader's next scheduled attempt means a client
   * that honours {@code Retry-After} stops re-asking a question whose answer cannot have changed.
   */
  private void requireLoaded() {
    if (substanceRegistry.isLoaded()) {
      return;
    }
    SubstanceRegistry.LoadStatus status = substanceRegistry.getLoadStatus();
    long retryAfterSeconds =
        status.retryAfterSeconds(System.currentTimeMillis()).orElse(DEFAULT_RETRY_AFTER_SECONDS);
    throw new AuthorityNotReadyException("Substance data is still loading from EPA CompTox API.", retryAfterSeconds);
  }

  private static String buildCompToxDetails(Substance s) {
    if (s == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();

    // Core identifiers
    if (notBlank(s.getCasrn())) {
      append(sb, "CAS", s.getCasrn());
    }
    if (notBlank(s.getDsstoxCompoundId())) {
      append(sb, "DSSToxCID", s.getDsstoxCompoundId());
    }

    // Chemistry
    if (notBlank(s.getMolecularFormula())) {
      append(sb, "Formula", s.getMolecularFormula());
    }
    if (s.getMolecularWeight() != null) {
      append(sb, "MW", stripZeros(s.getMolecularWeight()) + " g/mol");
    }
    if (notBlank(s.getSmiles())) {
      append(sb, "SMILES", s.getSmiles());
    }
    if (notBlank(s.getInchi())) {
      append(sb, "InChI", s.getInchi());
    }
    if (notBlank(s.getInchiKey())) {
      append(sb, "InChIKey", s.getInchiKey());
    }

    // Metadata
    if (s.getSynonymCount() != null && s.getSynonymCount() > 0) {
      append(sb, "Synonyms", String.valueOf(s.getSynonymCount()));
    }
    if (notBlank(s.getQcLevel())) {
      append(sb, "QCLevel", s.getQcLevel());
    }

    return sb.length() == 0 ? null : sb.toString();
  }

  private static void append(StringBuilder sb, String key, String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    if (sb.length() > 0) {
      sb.append("; ");
    }
    sb.append(key).append('=').append(value);
  }

  private static boolean notBlank(String s) {
    return s != null && !s.isBlank();
  }

  private static String stripZeros(Double d) {
    return java.math.BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
  }
}
