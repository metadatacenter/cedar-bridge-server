package org.metadatacenter.cedar.bridge.resources.extauth;

import org.junit.jupiter.api.Test;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarDependencyUnavailableException;
import org.metadatacenter.util.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorityVerificationTest {
  private static final Map<String, String> IDENTIFIERS = Map.of(
      "orcid", "https://orcid.org/0000-0001-2345-6789", "ror", "https://ror.org/05a28rw58",
      "doi", "https://doi.org/10.1234/a", "pmid", "https://pubmed.ncbi.nlm.nih.gov/123/",
      "nih-grant", "https://reporter.nih.gov/project-details/123", "rrid", "https://identifiers.org/RRID:AB_123",
      "comp-tox", "https://comptox.epa.gov/dashboard/chemical/details/DTXSID123");

  @FunctionalInterface interface Lookup { AuthorityDetailsAnswer run(String id) throws CedarException; }
  private ExternalAuthorityResource resource(String segment, Lookup lookup) {
    ExternalAuthority authority = new ExternalAuthority() {
      public String pathSegment() { return segment; }
      public String registryName() { return segment; }
      public AuthoritySearchAnswer search(String q, int offset, int limit) { throw new AssertionError("No label search"); }
      public AuthorityDetailsAnswer details(String id) throws CedarException { return lookup.run(id); }
    };
    return new ExternalAuthorityResource(mock(CedarConfig.class, RETURNS_DEEP_STUBS), List.of(authority));
  }
  private AuthorityVerification check(ExternalAuthorityResource resource, String segment, String iri) {
    var body = JsonMapper.STRICT_MAPPER.createObjectNode().put("iri", iri);
    try (var response = resource.verify(segment, body)) {
      assertEquals(200, response.getStatus());
      return (AuthorityVerification) response.getEntity();
    }
  }

  @Test void allAuthoritiesRecognizeOnlyTheirOwnIdentifiersAndPreserveStoredSpelling() {
    for (var entry : IDENTIFIERS.entrySet()) {
      AtomicInteger calls = new AtomicInteger();
      var resource = resource(entry.getKey(), id -> {
        calls.incrementAndGet();
        return AuthorityDetailsAnswer.found(Map.of("id", entry.getValue(), "name", "A label"));
      });
      AuthorityVerification answer = check(resource, entry.getKey(), entry.getValue());
      assertEquals("passed", answer.status(), entry.getKey());
      assertEquals(entry.getValue(), answer.iri());
      assertEquals("authorityIdentifier", check(resource, entry.getKey(), "https://attacker.example/123").code());
      assertEquals(1, calls.get(), "An unrelated URL must never reach a resolver");
    }
  }
  @Test void failuresOutagesAndUnusableOrDifferentRecordsStayDistinct() {
    String iri = IDENTIFIERS.get("ror");
    assertEquals("authorityMembership", check(resource("ror", id -> AuthorityDetailsAnswer.notFound(Map.of())), "ror", iri).code());
    assertEquals("authorityMembership", check(resource("ror", id -> AuthorityDetailsAnswer.failed(404, Map.of())), "ror", iri).code());
    for (int status : List.of(401, 403, 429, 500, 502, 503)) {
      assertEquals("unavailable", check(resource("ror", id -> AuthorityDetailsAnswer.failed(status, Map.of())), "ror", iri).status());
    }
    assertEquals("unavailable", check(resource("ror", id -> { throw new CedarDependencyUnavailableException("offline", new java.io.IOException()); }), "ror", iri).status());
    assertEquals("unavailable", check(resource("ror", id -> AuthorityDetailsAnswer.found(Map.of("name", "No ID"))), "ror", iri).status());
    assertEquals("unavailable", check(resource("ror", id -> AuthorityDetailsAnswer.found(Map.of("id", "https://ror.org/012345678"))), "ror", iri).status());
  }
  @Test void dataciteAbsenceCannotDisproveADoiFromAnotherAgency() {
    assertEquals("unavailable", check(resource("doi", id -> AuthorityDetailsAnswer.notFound(Map.of())), "doi", IDENTIFIERS.get("doi")).status());
  }
  @Test void malformedRequestsAreRejectedWithoutALookup() throws Exception {
    var resource = resource("ror", id -> { throw new AssertionError("No request"); });
    for (String body : List.of("null", "{}", "{\"iri\":42}", "{\"iri\":\"\"}", "{\"iri\":\"urn:x\",\"extra\":1}")) {
      assertEquals(400, resource.verify("ror", JsonMapper.STRICT_MAPPER.readTree(body)).getStatus());
    }
  }

  /** DataCite, holding the DOIs given, as it spells them. Each identifier it is asked for is recorded. */
  private ExternalAuthorityResource datacite(List<String> asked, String... held) {
    return resource("doi", id -> {
      asked.add(id);
      for (String doi : held) {
        if (doi.equalsIgnoreCase(id)) return AuthorityDetailsAnswer.found(Map.of("id", "https://doi.org/" + doi, "name", "A dataset"));
      }
      return AuthorityDetailsAnswer.notFound(Map.of());
    });
  }
  /**
   * A DOI IRI holding percent-escapes names the DOI they spell, and is looked up and compared as that
   * DOI. Compared as written, it was looked up with its escapes, as a DOI DataCite never holds.
   */
  @Test void aDoiIriHoldingEscapesIsVerifiedAsTheDoiItNames() {
    List<String> asked = new ArrayList<>();
    var resource = datacite(asked, "10.1234/café", "10.1234/a/b");
    for (String iri : List.of("https://doi.org/10.1234/caf%C3%A9", "https://doi.org/10.1234/A%2Fb")) {
      AuthorityVerification answer = check(resource, "doi", iri);
      assertEquals("passed", answer.status(), iri);
      assertEquals(iri, answer.iri(), "the stored IRI is never rewritten");
    }
    check(resource, "doi", "https://doi.org/10.1234/a%20b");
    assertEquals(List.of("10.1234/café", "10.1234/A/b", "10.1234/a b"), asked);
  }
  /**
   * Escapes are decoded once, as UTF-8, and a plus sign stays one. An identifier holding an escape that
   * is not well-formed spells no other identifier, so it is looked up as written.
   */
  @Test void aDoiIsDecodedOnceAsUtf8OrNotAtAll() {
    Map<String, String> lookedUp = new LinkedHashMap<>();
    lookedUp.put("https://doi.org/10.1234/50%2525", "10.1234/50%25");
    lookedUp.put("https://doi.org/10.1234/a+b", "10.1234/a+b");
    lookedUp.put("https://doi.org/10.1234/caf%C3", "10.1234/caf%C3");
    lookedUp.put("https://doi.org/10.1234/%FF", "10.1234/%FF");
    lookedUp.put("https://doi.org/10.1234/a%C0%AFb", "10.1234/a%C0%AFb");
    lookedUp.put("https://doi.org/10.1234/%ED%A0%80", "10.1234/%ED%A0%80");
    lookedUp.put("https://doi.org/10.1234/caf%C3%A9%FF", "10.1234/caf%C3%A9%FF");
    for (var entry : lookedUp.entrySet()) {
      List<String> asked = new ArrayList<>();
      check(datacite(asked), "doi", entry.getKey());
      assertEquals(List.of(entry.getValue()), asked, entry.getKey());
    }
    // An IRI holding an escape that is not a percent sign and two ASCII hexadecimal digits is refused
    // before any identifier is extracted from it, so only the decoding itself can meet one.
    for (String id : List.of("10.1234/a%zz", "10.1234/a%4", "10.1234/a%", "10.1234/a%４１")) {
      assertEquals(id, AuthorityIdentifier.decoded(id));
    }
  }
  /**
   * Every other authority verifies as it did before escapes were decoded. None of their identifiers
   * holds a percent sign, so an IRI spelling one of its characters as an escape is not one of theirs.
   */
  @Test void everyOtherAuthorityVerifiesAsBeforeEscapesWereDecoded() {
    Map<String, String> escaped = Map.of(
        "orcid", "https://orcid.org/0000-0001-2345-678%39", "ror", "https://ror.org/05a28rw5%38",
        "pmid", "https://pubmed.ncbi.nlm.nih.gov/12%33/", "nih-grant", "https://reporter.nih.gov/project-details/12%33",
        "rrid", "https://identifiers.org/RRID:AB_12%33", "comp-tox", "https://comptox.epa.gov/dashboard/chemical/details/DTXSID12%33");
    Map<String, String> lookedUp = Map.of("orcid", "0000-0001-2345-6789", "ror", "05a28rw58", "pmid", "123",
        "nih-grant", "https://reporter.nih.gov/project-details/123", "rrid", "RRID:AB_123", "comp-tox", "DTXSID123");
    for (var entry : escaped.entrySet()) {
      String segment = entry.getKey();
      List<String> asked = new ArrayList<>();
      var resource = resource(segment, id -> {
        asked.add(id);
        return AuthorityDetailsAnswer.found(Map.of("id", IDENTIFIERS.get(segment), "name", "A label"));
      });
      assertEquals("passed", check(resource, segment, IDENTIFIERS.get(segment)).status(), segment);
      AuthorityVerification answer = check(resource, segment, entry.getValue());
      assertEquals("failed", answer.status(), segment);
      assertEquals("authorityIdentifier", answer.code(), segment);
      assertEquals(List.of(lookedUp.get(segment)), asked, segment);
    }
  }
  /**
   * A DOI that cannot be one, such as one ending in a {@code .} or {@code ..} segment or holding a
   * space, is not looked up, as the lookup route does not look it up. DataCite is not asked, so its
   * breaker neither counts the request nor refuses it, and the answer is the one for a DOI it does
   * not hold.
   */
  @Test void aDoiThatCannotBeOneIsNotLookedUp() {
    List<String> asked = new ArrayList<>();
    DoiAuthority datacite = new DoiAuthority("http://127.0.0.1:1/dois") {
      @Override public AuthorityDetailsAnswer details(String id) {
        asked.add(id);
        return AuthorityDetailsAnswer.notFound(Map.of());
      }
    };
    var resource = new ExternalAuthorityResource(mock(CedarConfig.class, RETURNS_DEEP_STUBS), List.of(datacite));
    for (String iri : List.of("https://doi.org/10.1234/a/..", "https://doi.org/10.1234/a/.",
        "https://doi.org/10.1234/a/%2E%2E", "https://doi.org/10.1234/a/%2e", "https://doi.org/10.1234/a%20b")) {
      AuthorityVerification answer = check(resource, "doi", iri);
      assertEquals("unavailable", answer.status(), iri);
      assertEquals("DataCite does not hold this DOI; other DOI registration agencies were not checked.", answer.message(), iri);
    }
    assertEquals(List.of(), asked, "DataCite is not asked");
  }
}
