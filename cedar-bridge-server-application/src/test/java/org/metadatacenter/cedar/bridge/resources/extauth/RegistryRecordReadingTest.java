package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.metadatacenter.cedar.bridge.resources.SubstanceRegistry;
import org.metadatacenter.cedar.bridge.resources.SubstanceRegistryLoader;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What each authority makes of the records its registry returns.
 *
 * <p>{@link RegistryAnswerMatrixTest} answers every registry request with an empty result, one
 * minimal record or a failure. Its cases therefore pin statuses rather than the reading of records.
 * An authority that misreads a record misleads the user without any error: it shows a researcher
 * under the wrong name, drops a match, counts one twice, or looks up an identifier other than the one
 * typed. Each case answers an authority with records in its registry's shape, served from a registry
 * on a local port, and asserts the terms the authority makes of them.
 *
 * <p>An authority that asks the wrong question misleads the user in the same way, so some cases
 * assert what the registry was asked instead. What was typed must reach it as one value, and a
 * request this server could not send must not be counted against the registry.
 */
@Tag("integration")
public class RegistryRecordReadingTest {

  private static final HttpServer REGISTRY = startRegistry();
  private static final String BASE = "http://127.0.0.1:" + REGISTRY.getAddress().getPort();

  /** One request the registry received, with its path and query decoded, and as they were sent. */
  private record Received(String method, String path, String query, String body, String rawQuery, String rawPath) {}

  /** A registry's answer: a status and a body. */
  private record Reply(int status, String body) {
    static Reply ok(String body) {
      return new Reply(200, body);
    }
  }

  /** How the registry answers the case under way. */
  private static volatile Function<Received, Reply> registry = request -> Reply.ok("{}");

  private static final List<Received> RECEIVED = new CopyOnWriteArrayList<>();

  @BeforeEach
  public void forget() {
    RECEIVED.clear();
    registry = request -> Reply.ok("{}");
  }

  @AfterAll
  public static void stopRegistry() {
    REGISTRY.stop(0);
  }

  // ---- ORCID ----------------------------------------------------------------------------------

  private static final String ORCID_TOKEN = "{\"access_token\": \"token\", \"expires_in\": 3600}";

  private static OrcidAuthority orcid() {
    return new OrcidAuthority(BASE + "/orcid/", BASE + "/orcid/api/", "id", "secret");
  }

  /**
   * A researcher is named by the credit name they chose, then by given and family names, then by
   * whichever of the two they have, then by the first of their other names. A result without an
   * identifier or a name is no term. The IRI carries the prefix ORCID itself shows, which differs
   * between the sandbox and production.
   */
  @Test
  public void orcidNamesEachResearcherAsTheyChoseUnderThePrefixOrcidShows() throws Exception {
    registry = request -> switch (request.path()) {
      case "/orcid/oauth/token" -> Reply.ok(ORCID_TOKEN);
      case "/orcid/api/v3.0/search/" -> Reply.ok("""
          {"result": [{"orcid-identifier": {"uri": "https://sandbox.orcid.org/0000-0001-0000-0000",
                                            "path": "0000-0001-0000-0000"}}]}
          """);
      default -> Reply.ok("""
          {"num-found": 57, "expanded-result": [
            {"orcid-id": "0000-0002-0000-0001", "credit-name": "Ada King", "given-names": "Augusta Ada",
             "family-names": "Lovelace", "institution-name": ["University of London", " ", "Royal Society"]},
            {"orcid-id": "0000-0002-0000-0002", "credit-name": " ", "given-names": "Grace",
             "family-names": "Hopper"},
            {"orcid-id": "0000-0002-0000-0003", "given-names": "Hedy", "other-name": ["H. Lamarr", "H. Kiesler"]},
            {"credit-name": "Nobody in particular"},
            {"orcid-id": "0000-0002-0000-0004", "institution-name": ["Nowhere"]},
            {"orcid-id": "0000-0002-0000-0005", "given-names": "Alan", "family-names": "Turing"}]}
          """);
    };

    AuthoritySearchAnswer answer = orcid().search("smith", 20, 10);

    String sandbox = "https://sandbox.orcid.org/";
    assertEquals(List.of(sandbox + "0000-0002-0000-0001", sandbox + "0000-0002-0000-0002",
        sandbox + "0000-0002-0000-0003", sandbox + "0000-0002-0000-0005"), iris(answer));
    assertEquals("Ada King", term(answer, sandbox + "0000-0002-0000-0001", "name"));
    assertEquals("Grace Hopper", term(answer, sandbox + "0000-0002-0000-0002", "name"));
    assertEquals("Hedy", term(answer, sandbox + "0000-0002-0000-0003", "name"));
    assertEquals("Alan Turing", term(answer, sandbox + "0000-0002-0000-0005", "name"));
    assertEquals("University of London, Royal Society", term(answer, sandbox + "0000-0002-0000-0001", "details"));
    assertEquals("", term(answer, sandbox + "0000-0002-0000-0005", "details"));
    assertEquals(57L, answer.totalCount());
    String query = received("/orcid/api/v3.0/expanded-search/").get(0).query();
    assertTrue(query.endsWith("}smith&start=20&rows=10"), query);
  }

  /**
   * A record is named by its credit name, then by given and family names, then by whichever of the
   * two it has. Its identifier is the one the record shows, and the record goes back whole, because
   * the field that shows a researcher reads it. One token serves every request.
   */
  @Test
  public void orcidNamesARecordByItsCreditNameAndPassesTheRecordOn() throws Exception {
    Map<String, String> records = Map.of(
        "0000-0001-0000-0001", orcidRecord("0000-0001-0000-0001", "\"credit-name\": {\"value\": \"A. A. Lovelace\"}, "
            + "\"given-names\": {\"value\": \"Ada\"}, \"family-name\": {\"value\": \"Lovelace\"}"),
        "0000-0001-0000-0002", orcidRecord("0000-0001-0000-0002", "\"credit-name\": null, "
            + "\"given-names\": {\"value\": \"Grace\"}, \"family-name\": {\"value\": \"Hopper\"}"),
        "0000-0001-0000-0003", orcidRecord("0000-0001-0000-0003", "\"given-names\": {\"value\": \"Hedy\"}"),
        // ORCID sends a name the researcher has not given as null rather than leaving it out.
        "0000-0001-0000-0004", orcidRecord("0000-0001-0000-0004", "\"credit-name\": null, "
            + "\"given-names\": {\"value\": \"Marie\"}, \"family-name\": null"));
    Map<String, String> names = Map.of("0000-0001-0000-0001", "A. A. Lovelace", "0000-0001-0000-0002", "Grace Hopper",
        "0000-0001-0000-0003", "Hedy", "0000-0001-0000-0004", "Marie");
    registry = request -> request.path().equals("/orcid/oauth/token") ? Reply.ok(ORCID_TOKEN)
        : Reply.ok(records.get(request.path().split("/")[4]));

    OrcidAuthority orcid = orcid();
    for (Map.Entry<String, String> expected : names.entrySet()) {
      String id = expected.getKey();
      AuthorityDetailsAnswer answer = orcid.details("https://orcid.org/" + id);

      assertTrue(answer.found(), id);
      assertEquals(expected.getValue(), answer.body().get("name"), id);
      assertEquals("https://orcid.org/" + id, answer.body().get("id"));
      assertEquals(JsonMapper.STRICT_MAPPER.readTree(records.get(id)), answer.body().get("rawResponse"));
      assertEquals(1, received("/orcid/api/v3.0/" + id + "/record").size(), id);
    }
    assertEquals(1, received("/orcid/oauth/token").size(), "the token is bought once and reused");
  }

  /**
   * A search names a researcher as a lookup of their record does. ORCID sends a credit name the
   * researcher has not chosen as null, and a search dropped everyone it did; it also dropped a
   * researcher with only a given or only a family name, whom a lookup names. A lookup of a record
   * whose family name is null failed outright, and a record with only other names went unnamed.
   */
  @Test
  public void orcidSearchNamesEachResearcherAsALookupOfTheirRecordDoes() throws Exception {
    // The name fields as a search returns them, the record's person as it holds them, and the name.
    record Named(String id, String searched, String person, String name) {}
    List<Named> researchers = List.of(
        new Named("0000-0003-0000-0001", "\"credit-name\": null, \"given-names\": \"Grace\", \"family-names\": \"Hopper\"",
            "\"name\": {\"credit-name\": null, \"given-names\": {\"value\": \"Grace\"}, \"family-name\": {\"value\": \"Hopper\"}}",
            "Grace Hopper"),
        new Named("0000-0003-0000-0002", "\"credit-name\": null, \"given-names\": \"Hedy\", \"other-name\": [\"H. Lamarr\"]",
            "\"name\": {\"credit-name\": null, \"given-names\": {\"value\": \"Hedy\"}, \"family-name\": null}, "
                + "\"other-names\": {\"other-name\": [{\"content\": \"H. Lamarr\"}]}",
            "Hedy"),
        new Named("0000-0003-0000-0003", "\"family-names\": \"Curie\"",
            "\"name\": {\"given-names\": null, \"family-name\": {\"value\": \"Curie\"}}", "Curie"),
        new Named("0000-0003-0000-0004", "\"other-name\": [\"H. Kiesler\", \"H. Lamarr\"]",
            "\"name\": null, \"other-names\": {\"other-name\": [{\"content\": \"H. Kiesler\"}, {\"content\": \"H. Lamarr\"}]}",
            "H. Kiesler"));
    StringJoiner results = new StringJoiner(", ", "{\"num-found\": 4, \"expanded-result\": [", "]}");
    researchers.forEach(researcher -> results.add("{\"orcid-id\": \"" + researcher.id() + "\", " + researcher.searched() + "}"));
    registry = request -> switch (request.path()) {
      case "/orcid/oauth/token" -> Reply.ok(ORCID_TOKEN);
      case "/orcid/api/v3.0/search/" -> Reply.ok("""
          {"result": [{"orcid-identifier": {"uri": "https://orcid.org/0000-0001-0000-0000",
                                            "path": "0000-0001-0000-0000"}}]}
          """);
      case "/orcid/api/v3.0/expanded-search/" -> Reply.ok(results.toString());
      default -> {
        Named researcher = researchers.stream().filter(named -> request.path().contains(named.id())).findFirst().orElseThrow();
        yield Reply.ok("{\"orcid-identifier\": {\"uri\": \"https://orcid.org/" + researcher.id() + "\", \"path\": \""
            + researcher.id() + "\"}, \"person\": {" + researcher.person() + "}}");
      }
    };

    OrcidAuthority orcid = orcid();
    AuthoritySearchAnswer searched = orcid.search("anyone", 0, 10);

    assertEquals(researchers.stream().map(researcher -> "https://orcid.org/" + researcher.id()).toList(), iris(searched));
    for (Named researcher : researchers) {
      String iri = "https://orcid.org/" + researcher.id();
      assertEquals(researcher.name(), term(searched, iri, "name"), researcher.id());
      assertEquals(researcher.name(), orcid.details(iri).body().get("name"), researcher.id());
    }
  }

  private static String orcidRecord(String id, String name) {
    return "{\"orcid-identifier\": {\"uri\": \"https://orcid.org/" + id + "\", \"path\": \"" + id + "\"}, "
        + "\"person\": {\"name\": {" + name + "}}}";
  }

  // ---- PubMed ---------------------------------------------------------------------------------

  /**
   * Text that is all digits is asked as a PubMed ID and as a title at once. The publication the
   * identifier names leads the first page and is counted once, whether or not its title also matches,
   * and a later page does not repeat it. A summary without a title is no term.
   */
  @Test
  public void aPubMedIdentifierLeadsTheFirstPageAndIsCountedOnce() throws Exception {
    String summaries = """
        {"result": {"uids": ["123", "777", "888"],
          "123": {"title": "Found by its identifier", "fulljournalname": "Nature", "pubdate": "2020 Jan"},
          "777": {"title": "Found by its title"},
          "888": {"title": ""}}}
        """;
    PubMedAuthority pubMed = new PubMedAuthority(BASE + "/eutils/", "key");
    String byId = "https://pubmed.ncbi.nlm.nih.gov/123/";
    String byTitle = "https://pubmed.ncbi.nlm.nih.gov/777/";

    registry = pubMed(summaries, "{\"esearchresult\": {\"count\": \"41\", \"idlist\": [\"777\", \"123\", \"888\"]}}");
    AuthoritySearchAnswer alsoATitleMatch = pubMed.search("123", 0, 10);
    registry = pubMed(summaries, "{\"esearchresult\": {\"count\": \"1\", \"idlist\": [\"777\"]}}");
    AuthoritySearchAnswer onlyAnIdentifierMatch = pubMed.search("123", 0, 10);
    RECEIVED.clear();
    AuthoritySearchAnswer laterPage = pubMed.search("123", 10, 10);

    assertEquals(List.of(byId, byTitle), iris(alsoATitleMatch));
    assertEquals(41L, alsoATitleMatch.totalCount());
    assertEquals(List.of(byId, byTitle), iris(onlyAnIdentifierMatch));
    assertEquals(2L, onlyAnIdentifierMatch.totalCount());
    assertEquals(List.of(byTitle), iris(laterPage));
    assertEquals("Found by its identifier", term(alsoATitleMatch, byId, "name"));
    String details = (String) term(alsoATitleMatch, byId, "details");
    assertTrue(details.contains("Nature") && details.contains("(2020 Jan)") && details.endsWith("; PMID 123"), details);
    // A later page asks only for titles: one search, and the summaries of what it found.
    assertEquals(List.of("/eutils/esearch.fcgi", "/eutils/esummary.fcgi"),
        RECEIVED.stream().map(Received::path).toList());
    assertTrue(RECEIVED.get(0).query().contains("&retstart=10&retmax=10&term=123*[Title]"), RECEIVED.get(0).query());
    assertTrue(RECEIVED.get(1).query().contains("&id=777&"), RECEIVED.get(1).query());
  }

  /**
   * Only a PubMed identifier is looked up. Other text was stripped to its digits, so a DOI asked for
   * PMID 101234 and reported whatever paper that is as the one the caller asked for.
   */
  @Test
  public void pubMedLooksUpNothingThatIsNotAPubMedIdentifier() throws Exception {
    registry = request -> Reply.ok("""
        {"result": {"uids": ["101234", "5678", "1234"],
          "101234": {"title": "An unrelated paper"}, "5678": {"title": "Another"}, "1234": {"title": "A third"}}}
        """);
    PubMedAuthority pubMed = new PubMedAuthority(BASE + "/eutils/", "key");

    for (String notAPubMedId : List.of("10.1234/abc", "https://doi.org/10.1234/5678", "PMID:12a34", "1234 and more")) {
      assertFalse(pubMed.details(notAPubMedId).found(), notAPubMedId);
    }
    assertEquals(List.of(), RECEIVED, "NCBI is asked about none of them");
  }

  /**
   * A lookup identifies a publication by the IRI a search offers for it, so the value an instance
   * stores is the same whichever way the publication was chosen.
   */
  @Test
  public void aPubMedLookupIdentifiesAPublicationByTheIriASearchOffers() throws Exception {
    registry = pubMed("{\"result\": {\"uids\": [\"123\"], \"123\": {\"title\": \"A paper\"}}}",
        "{\"esearchresult\": {\"count\": \"1\", \"idlist\": [\"123\"]}}");
    PubMedAuthority pubMed = new PubMedAuthority(BASE + "/eutils/", "key");

    AuthoritySearchAnswer searched = pubMed.search("A paper", 0, 10);
    AuthorityDetailsAnswer lookedUp = pubMed.details("123");

    assertEquals(List.of("https://pubmed.ncbi.nlm.nih.gov/123/"), iris(searched));
    assertEquals("https://pubmed.ncbi.nlm.nih.gov/123/", lookedUp.body().get("id"));
  }

  /** A publication is described in a sentence giving its title, its journal, its date and its identifier. */
  @Test
  public void pubMedDescribesAPublicationInASentence() throws Exception {
    registry = pubMed("""
        {"result": {"uids": ["1", "2"],
          "1": {"title": "A paper", "fulljournalname": "Annals of Oncology", "pubdate": "2020 Jan"},
          "2": {"title": "Another paper"}}}
        """, "{\"esearchresult\": {\"count\": \"2\", \"idlist\": [\"1\", \"2\"]}}");

    AuthoritySearchAnswer answer = new PubMedAuthority(BASE + "/eutils/", "key").search("paper", 0, 10);

    assertEquals("A paper is an article in Annals of Oncology (2020 Jan); PMID 1",
        term(answer, "https://pubmed.ncbi.nlm.nih.gov/1/", "details"));
    assertEquals("Another paper is a journal article; PMID 2", term(answer, "https://pubmed.ncbi.nlm.nih.gov/2/", "details"));
  }

  private static Function<Received, Reply> pubMed(String summaries, String search) {
    return request -> Reply.ok(request.path().endsWith("esearch.fcgi") ? search : summaries);
  }

  // ---- EPA CompTox ----------------------------------------------------------------------------

  private static SubstanceRegistry compTox() {
    CedarConfig config = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(config.getExternalAuthorities().getEpaCompTox().getApiPrefix()).thenReturn(BASE + "/ctx/");
    when(config.getExternalAuthorities().getEpaCompTox().getApiKey()).thenReturn("key");
    return new SubstanceRegistry(config);
  }

  /**
   * The PFAS list is read, and its substances are fetched a thousand at a time. A substance without
   * an identifier is passed over, and a field the EPA adds is ignored.
   */
  @Test
  public void compToxFetchesTheListsSubstancesAThousandAtATime() throws Exception {
    List<String> listed = IntStream.range(0, 1001).mapToObj(i -> "DTXSID" + i).toList();
    registry = request -> {
      if (request.method().equals("GET")) {
        return Reply.ok(json(listed));
      }
      List<Map<String, Object>> details = new ArrayList<>();
      for (String id : list(request.body())) {
        details.add(Map.of("dtxsid", id, "preferredName", "Substance " + id, "addedByTheEpaLater", true));
      }
      details.add(Map.of("preferredName", "A substance without an identifier"));
      return Reply.ok(json(details));
    };

    SubstanceRegistry substances = compTox();
    substances.loadSubstances();

    assertTrue(substances.isLoaded());
    assertEquals(1001, substances.getSubstanceInfoByDtxsid().size());
    assertEquals("Substance DTXSID1000", substances.getSubstanceInfoByDtxsid().get("DTXSID1000").getPreferredName());
    assertEquals("/ctx/chemical/list/chemicals/search/by-listname/PFASSTRUCTV5", RECEIVED.get(0).path());
    assertEquals(List.of(1000, 1), RECEIVED.stream().filter(request -> request.method().equals("POST"))
        .map(request -> list(request.body()).size()).toList());
  }

  /**
   * The registry is loaded only when its substances arrived. With every batch refused it held none
   * and still counted as loaded, so a PFAS search found nothing where it should have answered 503,
   * and the loader, which retries only a load that failed, never asked again.
   */
  @Test
  public void compToxIsNotLoadedWhenItsSubstancesDoNotArrive() throws Exception {
    registry = request -> request.method().equals("GET") ? Reply.ok("[\"DTXSID1\", \"DTXSID2\"]") : new Reply(503, "{}");

    SubstanceRegistry substances = compTox();
    assertThrows(Exception.class, substances::loadSubstances);
    assertFalse(substances.isLoaded());
    assertThrows(AuthorityNotReadyException.class, () -> new PfasAuthority(substances).search("fluoro", 0, 10));

    SubstanceRegistryLoader loader = new SubstanceRegistryLoader(substances);
    loader.start();
    try {
      long deadline = System.currentTimeMillis() + 10_000;
      while (substances.getLoadStatus().nextAttemptAtMs() == 0 && System.currentTimeMillis() < deadline) {
        Thread.sleep(20);
      }
      assertTrue(substances.getLoadStatus().nextAttemptAtMs() > 0, "the loader schedules another attempt");
    } finally {
      loader.stop();
    }
  }

  /**
   * A search matches any part of a preferred name in any case, and lists the matches by name and then
   * by identifier, so paging through them is stable. A lookup takes the identifier bare or as the
   * dashboard IRI a search offers.
   */
  @Test
  public void compToxFindsASubstanceByAnyPartOfItsNameInAStableOrder() throws Exception {
    registry = request -> Reply.ok(request.method().equals("GET")
        ? "[\"DTXSID3\", \"DTXSID1\", \"DTXSID2\", \"DTXSID4\", \"DTXSID5\"]"
        : """
          [{"dtxsid": "DTXSID3", "preferredName": "Perfluorooctanoic acid", "casrn": "335-67-1",
            "molecularFormula": "C8HF15O2", "molecularWeight": 414.07, "smiles": " ", "synonymCount": 0,
            "qcLevel": "Level 1"},
           {"dtxsid": "DTXSID1", "preferredName": "perfluorobutanoic acid", "molecularWeight": 500.0,
            "synonymCount": 12},
           {"dtxsid": "DTXSID2", "preferredName": "Perfluorooctanoic acid"},
           {"dtxsid": "DTXSID4", "preferredName": "Bisphenol A"},
           {"dtxsid": "DTXSID5"}]
          """);
    SubstanceRegistry substances = compTox();
    substances.loadSubstances();
    PfasAuthority pfas = new PfasAuthority(substances);
    String dashboard = "https://comptox.epa.gov/dashboard/chemical/details/";

    AuthoritySearchAnswer all = pfas.search("FLUORO", 0, 10);
    AuthoritySearchAnswer second = pfas.search("fluoro", 1, 1);

    assertEquals(List.of(dashboard + "DTXSID1", dashboard + "DTXSID2", dashboard + "DTXSID3"), iris(all));
    assertEquals(3L, all.totalCount());
    assertEquals(List.of(dashboard + "DTXSID2"), iris(second));
    assertEquals(3L, second.totalCount());
    assertEquals("CAS=335-67-1; Formula=C8HF15O2; MW=414.07 g/mol; QCLevel=Level 1",
        term(all, dashboard + "DTXSID3", "details"));
    assertEquals("MW=500 g/mol; Synonyms=12", term(all, dashboard + "DTXSID1", "details"));
    assertNull(term(all, dashboard + "DTXSID2", "details"));
    for (String id : List.of("DTXSID3", dashboard + "DTXSID3")) {
      AuthorityDetailsAnswer found = pfas.details(id);
      assertTrue(found.found(), id);
      assertEquals(dashboard + "DTXSID3", found.body().get("id"));
      assertEquals("Perfluorooctanoic acid", found.body().get("name"));
    }
    assertFalse(pfas.details("DTXSID9").found());
  }

  // ---- ROR ------------------------------------------------------------------------------------

  /**
   * An organisation is named by the name ROR displays it under, or by its first name when none is
   * typed for display. One without an identifier or a name is no term. ROR is asked to match what has
   * been typed so far as the start of a word.
   */
  @Test
  public void rorNamesAnOrganisationAsRorDisplaysIt() throws Exception {
    registry = request -> Reply.ok("""
        {"number_of_results": 4, "items": [
          {"id": "https://ror.org/00f54p054", "types": ["education", "funder"],
           "names": [{"value": "SU", "types": ["acronym"]},
                     {"value": "Stanford University", "types": ["ror_display", "label"]}]},
          {"id": "https://ror.org/05a28rw58",
           "names": [{"value": "Eidgenössische Technische Hochschule Zürich", "types": ["label"]},
                     {"value": "ETH Zurich", "types": ["alias"]}]},
          {"names": [{"value": "An organisation without an identifier", "types": ["ror_display"]}]},
          {"id": "https://ror.org/00000000", "names": []}]}
        """);
    CedarConfig config = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(config.getExternalAuthorities().getRor().getApiPrefix()).thenReturn(BASE + "/ror/");
    RorAuthority ror = new RorAuthority(config);

    AuthoritySearchAnswer answer = ror.search("stan", 0, 10);
    ror.search("stan*", 0, 10);

    assertEquals(List.of("https://ror.org/00f54p054", "https://ror.org/05a28rw58"), iris(answer));
    assertEquals("Stanford University", term(answer, "https://ror.org/00f54p054", "name"));
    assertEquals("Eidgenössische Technische Hochschule Zürich", term(answer, "https://ror.org/05a28rw58", "name"));
    assertEquals("Resource is of type education, funder", term(answer, "https://ror.org/00f54p054", "details"));
    assertNull(term(answer, "https://ror.org/05a28rw58", "details"));
    assertEquals(List.of("query=stan*", "query=stan*"), RECEIVED.stream().map(Received::query).toList());
  }

  /**
   * An organisation is described by its types, the country of its first location and its website, read
   * from the version 2 record the configured API returns. The description read the version 1 shape,
   * a top-level country and links as plain strings, so it gave only the types.
   */
  @Test
  public void rorDescribesAnOrganisationFromItsVersionTwoRecord() throws Exception {
    registry = request -> Reply.ok("""
        {"number_of_results": 1, "items": [
          {"id": "https://ror.org/00f54p054", "types": ["education", "funder"],
           "names": [{"value": "Stanford University", "types": ["ror_display", "label"]}],
           "locations": [{"geonames_id": 5398563, "geonames_details": {"country_code": "US",
                          "country_name": "United States", "name": "Stanford"}}],
           "links": [{"type": "wikipedia", "value": "http://en.wikipedia.org/wiki/Stanford_University"},
                     {"type": "website", "value": "https://www.stanford.edu"}]}]}
        """);
    CedarConfig config = mock(CedarConfig.class, RETURNS_DEEP_STUBS);
    when(config.getExternalAuthorities().getRor().getApiPrefix()).thenReturn(BASE + "/ror/");

    AuthoritySearchAnswer answer = new RorAuthority(config).search("stanford", 0, 10);

    assertEquals("Resource is of type education, funder, located in United States, with URL https://www.stanford.edu",
        term(answer, "https://ror.org/00f54p054", "details"));
  }

  // ---- NIH RePORTER ---------------------------------------------------------------------------

  /**
   * RePORTER matches a title loosely, so it is asked for five times the page and its answers are
   * narrowed to the titles that contain what was typed, in any case. The offset is RePORTER's to
   * apply, and the narrowing leaves the total unknown.
   */
  @Test
  public void nihNarrowsWhatRePorterMatchesToTitlesContainingTheQuery() throws Exception {
    registry = request -> Reply.ok("""
        {"results": [
          {"project_id": 11, "project_title": "Cancer Genomics Atlas", "org_name": "STANFORD UNIVERSITY",
           "project_num": "5R01CA000011-02",
           "principal_investigators": [{"full_name": "Jane Doe"}, {"full_name": "John Roe"}]},
          {"project_id": 12, "project_title": "Heart failure"},
          {"project_title": "Cancer without a project identifier"},
          {"project_id": 13, "project_title": "Pediatric cancer cohort"},
          {"project_id": 14, "project_title": "Cancer screening"}]}
        """);

    AuthoritySearchAnswer page = new NihGrantAuthority(BASE + "/nih").search("CANCER", 40, 2);

    String project = "https://reporter.nih.gov/project-details/";
    assertEquals(List.of(project + "11", project + "13"), iris(page));
    assertEquals("Cancer Genomics Atlas at STANFORD UNIVERSITY (PI: Jane Doe); Grant 5R01CA000011-02",
        term(page, project + "11", "details"));
    assertEquals("Pediatric cancer cohort", term(page, project + "13", "details"));
    assertNull(page.totalCount());
    assertEquals(JsonMapper.STRICT_MAPPER.readTree("{\"criteria\":{\"project_title\":\"CANCER\"},\"offset\":40,\"limit\":10}"),
        JsonMapper.STRICT_MAPPER.readTree(RECEIVED.get(0).body()));
  }

  /**
   * What was typed reaches RePORTER as the title to match, quotation marks and all, and RePORTER is
   * never asked for more than the 500 records it documents as the most one request returns. The body
   * was formatted as a string, so a quotation mark ended the title and the request was not JSON.
   */
  @Test
  public void nihAsksRePorterForWhatWasTypedAndNoMoreThanItReturns() throws Exception {
    registry = request -> Reply.ok("{\"results\": []}");
    NihGrantAuthority nih = new NihGrantAuthority(BASE + "/nih");

    nih.search("the \"p53\" pathway", 0, 10);
    nih.search("cancer", 0, 101);

    JsonNode quoted = JsonMapper.STRICT_MAPPER.readTree(RECEIVED.get(0).body());
    assertEquals("the \"p53\" pathway", quoted.path("criteria").path("project_title").asText());
    assertEquals(50, quoted.path("limit").asInt());
    assertEquals(500, JsonMapper.STRICT_MAPPER.readTree(RECEIVED.get(1).body()).path("limit").asInt());
  }

  // ---- DataCite -------------------------------------------------------------------------------

  /**
   * What was typed is one value of the query DataCite is asked. A space could not be sent at all, and
   * the search failed as though DataCite were down; an ampersand ended the query, and the text after
   * it set DataCite's other parameters.
   *
   * <p>Within that value the words are grouped, and DataCite requires every word of a group, so each
   * must appear in the title, in any order. Ungrouped, only the first word was a title term, and the
   * rest matched any field DataCite searches. Each character DataCite's query syntax reads as an
   * operator is escaped, and so is each operator word, so what was typed stays text. The slash is
   * left as typed, because DataCite escapes it itself and refuses one escaped twice.
   */
  @Test
  public void aDoiSearchAsksForEveryWordTypedInTheTitleAsText() throws Exception {
    DoiAuthority doi = new DoiAuthority(BASE + "/dois");

    doi.search("climate", 0, 10);
    doi.search("climate change", 0, 10);
    doi.search("a&page[size]=1000", 0, 10);
    doi.search("NOT \"sea-level\" rise* (OR) a:b AND ~2^ \\!", 0, 10);
    doi.search("10.5061/dryad", 0, 10);

    assertEquals(List.of("titles.title:(climate)", "titles.title:(climate change)",
            "titles.title:(a\\&page\\[size\\]\\=1000)",
            "titles.title:(\\NOT \\\"sea\\-level\\\" rise\\* \\(OR\\) a\\:b \\AND \\~2\\^ \\\\\\!)",
            "titles.title:(10.5061/dryad)"),
        RECEIVED.stream().map(request -> parameters(request).get("query")).map(values -> values.get(0)).toList());
    assertEquals(Map.of("query", List.of("titles.title:(climate change)"), "page[number]", List.of("1"),
        "page[size]", List.of("10")), parameters(RECEIVED.get(1)));
  }

  /**
   * A request this server cannot build is no evidence that the registry has stopped answering. Five
   * searches containing a space opened DataCite's breaker, after which every DOI search answered 503.
   * A lookup no longer builds such a request: the DOI is percent-encoded into the path, as
   * {@link #anIdentifierIsAskedForInThePathExactlyAsTyped} pins.
   */
  @Test
  public void aRequestThisServerCannotBuildDoesNotOpenTheBreaker() throws Exception {
    DoiAuthority doi = new DoiAuthority(BASE + "/dois");
    AuthorityCircuitBreaker breaker = new AuthorityCircuitBreaker(DoiAuthority.PATH_SEGMENT);

    for (int i = 0; i < AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING; i++) {
      breaker.call(() -> doi.search("climate change", 0, 10));
    }

    assertFalse(breaker.isOpen());
    breaker.call(() -> doi.search("climate", 0, 10));
    assertEquals(AuthorityCircuitBreaker.FAILURES_BEFORE_OPENING + 1, received("/dois").size());
  }

  // ---- SciCrunch ------------------------------------------------------------------------------

  /** A resource is described by its type, with the article its type takes. */
  @Test
  public void rridDescribesAResourceByItsType() throws Exception {
    registry = request -> Reply.ok("""
        {"hits": {"total": 3, "hits": [
          {"_source": {"item": {"identifier": "AB_1", "name": "Anti-GFAP", "types": [{"name": "antibody"}],
                                "alternateIdentifiers": [{}, {}]}}},
          {"_source": {"item": {"identifier": "CVCL_1", "name": "HeLa", "types": [{"name": "cell line"}]}}},
          {"_source": {"item": {"identifier": "SCR_1", "name": "A tool"}}}]}}
        """);

    AuthoritySearchAnswer answer = new RridAuthority(BASE + "/rrid/search", BASE + "/rrid/resolver/", "key")
        .search("a", 0, 10);

    String rrid = "https://identifiers.org/RRID:";
    assertEquals("Anti-GFAP is an antibody. Alternate identifiers exist in 2 database(s).",
        term(answer, rrid + "AB_1", "details"));
    assertEquals("HeLa is a cell line. Alternate identifiers exist in 0 database(s).",
        term(answer, rrid + "CVCL_1", "details"));
    assertEquals("A tool is a resource. Alternate identifiers exist in 0 database(s).",
        term(answer, rrid + "SCR_1", "details"));
  }

  // ---- The identifier a lookup asks for -------------------------------------------------------

  /** A record each registry holds for the identifier asked for. */
  private static final Map<String, String> RECORDS = Map.of(
      "doi", "{\"data\": {\"attributes\": {\"doi\": \"10.1234/abc\", \"titles\": [{\"title\": \"A dataset\"}]}}}",
      "pmid", "{\"result\": {\"123\": {\"title\": \"A paper\"}}}",
      "rrid", "{\"hits\": {\"hits\": [{\"_source\": {\"item\": {\"identifier\": \"AB_123\", \"name\": \"An antibody\"}}}]}}",
      "nih-grant", "{\"results\": [{\"project_id\": \"1\", \"project_title\": \"A grant\"}]}");

  static Stream<Arguments> spellings() {
    return Stream.of(
        arguments("doi", "https://doi.org/10.1234/abc", "/dois/10.1234/abc?"),
        arguments("doi", "http://doi.org/10.1234/abc", "/dois/10.1234/abc?"),
        arguments("doi", "doi:10.1234/abc", "/dois/10.1234/abc?"),
        arguments("doi", " 10.1234/abc ", "/dois/10.1234/abc?"),
        arguments("pmid", "PMID:123", "&id=123&"),
        arguments("pmid", "pubmed:123", "&id=123&"),
        arguments("pmid", "PMID: 123", "&id=123&"),
        arguments("pmid", "https://www.ncbi.nlm.nih.gov/pubmed/123", "&id=123&"),
        // The IRI a PubMed search offers ends in a slash.
        arguments("pmid", "https://pubmed.ncbi.nlm.nih.gov/123/", "&id=123&"),
        arguments("rrid", "AB_123", "/rrid/resolver/RRID:AB_123.json?"),
        arguments("rrid", "https://identifiers.org/RRID:AB_123", "/rrid/resolver/RRID:AB_123.json?"),
        arguments("nih-grant", " r01gm000001 ", "{\"criteria\":{\"project_nums\":[\"R01GM000001\"]}}"));
  }

  /**
   * A lookup asks the registry for the bare identifier, whether the caller sent it bare, with a
   * scheme prefix, or as the IRI a search offered, and finds the record the registry holds for it.
   */
  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("spellings")
  public void anIdentifierIsLookedUpBareHoweverItIsSpelled(String authority, String spelled, String asked)
      throws Exception {
    Map<String, ExternalAuthority> authorities = Map.of(
        "doi", new DoiAuthority(BASE + "/dois"),
        "pmid", new PubMedAuthority(BASE + "/eutils/", "key"),
        "rrid", new RridAuthority(BASE + "/rrid/search", BASE + "/rrid/resolver/", "key"),
        "nih-grant", new NihGrantAuthority(BASE + "/nih"));
    registry = request -> Reply.ok(RECORDS.get(authority));

    AuthorityDetailsAnswer answer = authorities.get(authority).details(spelled);

    Received request = RECEIVED.get(0);
    String sent = request.path() + "?" + request.query() + " " + request.body();
    assertTrue(sent.contains(asked), sent);
    assertTrue(answer.found(), answer.toString());
  }

  static Stream<Arguments> pathCharacters() {
    return Stream.of(
        // Letters, digits and the characters a path carries as themselves stay as typed.
        arguments("doi", "10.1234/aZ09-._~!$&'()*,;=:@", "/dois/10.1234/aZ09-._~!$&'()*,;=:@"),
        // DataCite reads the DOI from the rest of the path, slashes included.
        arguments("doi", "10.5061/dryad.2343k/2", "/dois/10.5061/dryad.2343k/2"),
        // Characters a URL cannot carry, which made the request one that could not be sent.
        arguments("doi", "10.1234/<>\"{}|\\^`[]", "/dois/10.1234/%3C%3E%22%7B%7D%7C%5C%5E%60%5B%5D"),
        // A percent sign is the DOI's own, even where it looks like an escape.
        arguments("doi", "10.1234/50%", "/dois/10.1234/50%25"),
        arguments("doi", "10.1234/a%20b", "/dois/10.1234/a%2520b"),
        // A question mark began a query and a number sign a fragment, so another DOI was asked for.
        arguments("doi", "10.1234/a?b=c", "/dois/10.1234/a%3Fb=c"),
        arguments("doi", "10.1234/a#b", "/dois/10.1234/a%23b"),
        // The DOI Handbook encodes a plus sign, which a server may read as a space.
        arguments("doi", "10.1234/a+b", "/dois/10.1234/a%2Bb"),
        arguments("doi", "10.1234/café", "/dois/10.1234/caf%C3%A9"),
        // A server may resolve a dot segment, which asked for another DOI. The DOI Handbook encodes the
        // slash after one.
        arguments("doi", "10.1234/x/../../10.5555/real", "/dois/10.1234/x/..%2F..%2F10.5555/real"),
        arguments("doi", "10.1234/./a", "/dois/10.1234/.%2Fa"),
        // SciCrunch reads an RRID with its colons, and an RRID holds no slash.
        arguments("rrid", "IMSR_JAX:000664", "/rrid/resolver/RRID:IMSR_JAX:000664.json"),
        arguments("rrid", "AB_<1>", "/rrid/resolver/RRID:AB_%3C1%3E.json"),
        arguments("rrid", "AB_1%", "/rrid/resolver/RRID:AB_1%25.json"),
        arguments("rrid", "AB_1?x", "/rrid/resolver/RRID:AB_1%3Fx.json"),
        arguments("rrid", "AB_1#x", "/rrid/resolver/RRID:AB_1%23x.json"),
        arguments("rrid", "AB_1/../2", "/rrid/resolver/RRID:AB_1%2F..%2F2.json"),
        arguments("rrid", "AB_é", "/rrid/resolver/RRID:AB_%C3%A9.json"));
  }

  /**
   * A DOI or an RRID is asked for in the request's path, each character the path cannot carry as itself
   * percent-encoded, so the registry is asked for exactly the identifier typed. The DOI Handbook's
   * rules for a DOI in a URL decide which characters those are. A character a URL cannot carry made a
   * request that could not be sent, and the lookup answered 500; a question mark or a number sign cut
   * the identifier short, and another record was looked up in its place.
   */
  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("pathCharacters")
  public void anIdentifierIsAskedForInThePathExactlyAsTyped(String authority, String id, String sent)
      throws Exception {
    ExternalAuthority lookup = authority.equals("doi") ? new DoiAuthority(BASE + "/dois")
        : new RridAuthority(BASE + "/rrid/search", BASE + "/rrid/resolver/", "key");

    lookup.details(id);

    assertEquals(1, RECEIVED.size(), RECEIVED.toString());
    assertEquals(sent, RECEIVED.get(0).rawPath());
    assertNull(RECEIVED.get(0).rawQuery(), "nothing typed reaches the query");
    assertEquals(authority.equals("doi") ? "/dois/" + id : "/rrid/resolver/RRID:" + id + ".json",
        RECEIVED.get(0).path(), "the registry reads the identifier typed");
  }

  // ---- helpers --------------------------------------------------------------------------------

  /** The IRIs a search offered, in the order it offered them. */
  private static List<String> iris(AuthoritySearchAnswer answer) {
    return new ArrayList<>(answer.results().keySet());
  }

  /** One property of the term a search offered under an IRI. */
  private static Object term(AuthoritySearchAnswer answer, String iri, String property) {
    return ((Map<?, ?>) answer.results().get(iri)).get(property);
  }

  /** The requests the registry received at a path, in order. */
  private static List<Received> received(String path) {
    return RECEIVED.stream().filter(request -> request.path().equals(path)).toList();
  }

  /** The parameters a request's query carried, each with its values in the order sent. */
  private static Map<String, List<String>> parameters(Received request) {
    Map<String, List<String>> parameters = new LinkedHashMap<>();
    for (String pair : request.rawQuery().split("&")) {
      int equals = pair.indexOf('=');
      parameters.computeIfAbsent(URLDecoder.decode(pair.substring(0, equals), StandardCharsets.UTF_8),
          name -> new ArrayList<>()).add(URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8));
    }
    return parameters;
  }

  private static String json(Object value) {
    try {
      return JsonMapper.STRICT_MAPPER.writeValueAsString(value);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static List<String> list(String json) {
    try {
      return JsonMapper.STRICT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  // ---- the registry ---------------------------------------------------------------------------

  private static HttpServer startRegistry() {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext("/", RegistryRecordReadingTest::answer);
      server.start();
      return server;
    } catch (IOException e) {
      throw new IllegalStateException("Could not bind the stub registry", e);
    }
  }

  private static void answer(HttpExchange exchange) throws IOException {
    String rawQuery = exchange.getRequestURI().getRawQuery();
    Received request = new Received(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
        rawQuery == null ? null : URLDecoder.decode(rawQuery, StandardCharsets.UTF_8),
        new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), rawQuery,
        exchange.getRequestURI().getRawPath());
    RECEIVED.add(request);
    Reply reply = registry.apply(request);
    byte[] payload = reply.body().getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(reply.status(), payload.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(payload);
    }
  }
}
