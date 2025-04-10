package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.http.CedarResponseStatus;
import org.metadatacenter.util.http.CedarResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
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
}
