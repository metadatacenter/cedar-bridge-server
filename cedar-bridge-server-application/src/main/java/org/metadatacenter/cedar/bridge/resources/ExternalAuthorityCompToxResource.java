package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.util.http.CedarResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;

@Path("/ext-auth/comp-tox")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityCompToxResource extends CedarMicroserviceResource {

  private static final String SUBSTANCE_IRI_BASE = "https://comptox.epa.gov/dashboard/chemical/details/";
  private final SubstanceRegistry substanceRegistry;
  private static final int DEFAULT_PAGE_SIZE = 100;

  public ExternalAuthorityCompToxResource(CedarConfig cedarConfig, SubstanceRegistry substanceRegistry) {
    super(cedarConfig);
    this.substanceRegistry = substanceRegistry;
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response getSubstanceDetails(@PathParam(PP_ID) String substanceId) throws CedarException {

    if (!substanceRegistry.isLoaded())
      return buildNotReadyResponse();

    Map<String, String> substances = substanceRegistry.getSubstancesByDtxsid();
    Map<String, Object> myResponse = new HashMap<>();

    // Normalize: extract ctxsid if full IRI, otherwise treat as fragment
    final String ctxsid = substanceId.startsWith(SUBSTANCE_IRI_BASE)
        ? substanceId.substring(SUBSTANCE_IRI_BASE.length())
        : substanceId;

    final String fullSubstanceIri = SUBSTANCE_IRI_BASE + ctxsid;
    myResponse.put("requestedId", substanceId);

    if (substances.containsKey(ctxsid)) {
      myResponse.put("found", true);
      myResponse.put("id", fullSubstanceIri);
      myResponse.put("name", substances.get(ctxsid));
      return CedarResponse.ok().entity(myResponse).build();
    } else {
      myResponse.put("found", false);
      return CedarResponse.ok().entity(myResponse).build();
    }
  }

  @GET
  @Timed
  @Path("/search-by-name")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchByName(@QueryParam("q") String searchTerm,
                               @QueryParam("page") Integer page,
                               @QueryParam("pageSize") Integer pageSize) throws CedarException {
    if (!substanceRegistry.isLoaded()) {
      return buildNotReadyResponse();
    }

    // Defaults with safe unboxing
    final int pageVal = (page != null) ? page.intValue() : 0;
    final int pageSizeVal = (pageSize != null) ? pageSize.intValue() : DEFAULT_PAGE_SIZE;

    // Validation -> plain-text 400 (no JSON)
    if (pageVal < 0 || pageSizeVal <= 1) {
      return CedarResponse.status(CedarResponseStatus.BAD_REQUEST)
          .entity("Invalid pagination parameters: page must be >= 0 and pageSize must be > 1")
          .build();
    }

    Map<String, String> substances = substanceRegistry.getSubstancesByDtxsid();
    Map<String, Object> myResponse = new HashMap<>();
    Map<String, Map<String, Object>> results = new LinkedHashMap<>();

    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      myResponse.put("found", false);
      myResponse.put("results", results);
      myResponse.put("page", pageVal);
      myResponse.put("pageSize", pageSizeVal);
      return CedarResponse.ok().entity(myResponse).build();
    }

    final String fragmentLower = searchTerm.toLowerCase();

    // Collect matching entries
    java.util.List<Map.Entry<String, String>> matches = new java.util.ArrayList<>();
    for (Map.Entry<String, String> e : substances.entrySet()) {
      String preferredName = e.getValue();
      if (preferredName != null && preferredName.toLowerCase().contains(fragmentLower)) {
        matches.add(e);
      }
    }

    // Deterministic order: sort by preferred name (case-insensitive), then by DTXSID
    matches.sort((a, b) -> {
      String na = (a.getValue() == null) ? "" : a.getValue();
      String nb = (b.getValue() == null) ? "" : b.getValue();
      int cmp = na.compareToIgnoreCase(nb);
      if (cmp != 0) return cmp;
      return a.getKey().compareTo(b.getKey());
    });

    // Paginate
    int fromIndex = pageVal * pageSizeVal;
    if (fromIndex < matches.size()) {
      int toIndex = Math.min(fromIndex + pageSizeVal, matches.size());
      for (Map.Entry<String, String> entry : matches.subList(fromIndex, toIndex)) {
        String dtxsid = entry.getKey();
        String preferredName = entry.getValue();
        String substanceIri = SUBSTANCE_IRI_BASE + dtxsid;

        Map<String, Object> substanceResultObject = new HashMap<>();
        substanceResultObject.put("name", preferredName);
        substanceResultObject.put("details", null);

        results.put(substanceIri, substanceResultObject);
      }
    }
    myResponse.put("found", !results.isEmpty());
    myResponse.put("results", results);
    myResponse.put("page", pageVal);
    myResponse.put("pageSize", pageSizeVal);

    return CedarResponse.ok().entity(myResponse).build();
  }

  private Response buildNotReadyResponse() {
    return CedarResponse.status(CedarResponseStatus.SERVICE_UNAVAILABLE)
        .header(HttpHeaders.RETRY_AFTER, "30")
        .entity(Map.of("message", "Substance data is still loading from EPA CompTox API."))
        .build();
  }

}
