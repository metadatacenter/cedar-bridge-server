package org.metadatacenter.cedar.bridge.resources.extauth;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.util.json.JsonMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 */
@Tag("integration")
public class RegistryRecordReadingTest {

  private static final HttpServer REGISTRY = startRegistry();
  private static final String BASE = "http://127.0.0.1:" + REGISTRY.getAddress().getPort();

  /** One request the registry received, with its query decoded. */
  private record Received(String method, String path, String query, String body) {}

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
   * A researcher is named by the credit name they chose, then by given and family names, then by the
   * first of their other names. A result without an identifier or a name is no term. The IRI carries
   * the prefix ORCID itself shows, which differs between the sandbox and production.
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
    assertEquals("H. Lamarr", term(answer, sandbox + "0000-0002-0000-0003", "name"));
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
        "0000-0001-0000-0003", orcidRecord("0000-0001-0000-0003", "\"given-names\": {\"value\": \"Hedy\"}"));
    Map<String, String> names = Map.of("0000-0001-0000-0001", "A. A. Lovelace", "0000-0001-0000-0002", "Grace Hopper",
        "0000-0001-0000-0003", "Hedy");
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
        new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
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
