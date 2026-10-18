package org.metadatacenter.cedar.bridge.resources.extauth;

import org.junit.jupiter.api.Test;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarDependencyUnavailableException;
import org.metadatacenter.util.json.JsonMapper;

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
}
