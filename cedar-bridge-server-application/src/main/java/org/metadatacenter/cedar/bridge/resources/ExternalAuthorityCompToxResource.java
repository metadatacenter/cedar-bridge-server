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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;

@Path("/ext-auth/comp-tox")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityCompToxResource extends CedarMicroserviceResource {

  private static final String SUBSTANCE_IRI_BASE = "https://comptox.epa.gov/dashboard/chemical/details/";
  private final SubstanceRegistry substanceRegistry;

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

    String ctxsid = substanceId.startsWith(SUBSTANCE_IRI_BASE) ? substanceId.substring(SUBSTANCE_IRI_BASE.length()) : substanceId;

    myResponse.put("requestedId", substanceId);

    if (substances.containsKey(ctxsid)) {
      myResponse.put("found", true);
      myResponse.put("id", substanceId);
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
  public Response searchByName(@QueryParam("q") String searchTerm) throws CedarException {
    if (!substanceRegistry.isLoaded())
      return buildNotReadyResponse();

    Map<String, String> substances = substanceRegistry.getSubstancesByDtxsid();
    Map<String, Object> myResponse = new HashMap<>();
    Map<String, Map<String, Object>> results = new LinkedHashMap<>();

    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      myResponse.put("found", false);
      myResponse.put("results", results);
      return CedarResponse.ok().entity(myResponse).build();
    }

    String fragmentLower = searchTerm.toLowerCase();

    for (Map.Entry<String, String> entry : substances.entrySet()) {
      String dtxsid = entry.getKey();
      String preferredName = entry.getValue();

      if (preferredName != null && preferredName.toLowerCase().contains(fragmentLower)) {
        String substanceIri = SUBSTANCE_IRI_BASE + dtxsid;
        Map<String, Object> substanceResultObject = new HashMap<>();
        substanceResultObject.put("name", preferredName);
        substanceResultObject.put("details", null);

        results.put(substanceIri, substanceResultObject);
      }
    }

    myResponse.put("found", !results.isEmpty());
    myResponse.put("results", results);

    return CedarResponse.ok().entity(myResponse).build();
  }

  private Response buildNotReadyResponse() {
    return CedarResponse.status(CedarResponseStatus.SERVICE_UNAVAILABLE)
        .header(HttpHeaders.RETRY_AFTER, "30")
        .entity(Map.of("message", "Substance data is still loading from EPA CompTox API."))
        .build();
  }

}
