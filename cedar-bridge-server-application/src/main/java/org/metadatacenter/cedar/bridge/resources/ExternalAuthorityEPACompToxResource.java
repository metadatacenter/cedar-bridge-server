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
import java.util.Map;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;

@Path("/ext-auth/epa-comp-tox")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalAuthorityEPACompToxResource extends CedarMicroserviceResource {

  private final SubstanceRegistry substanceRegistry;

  public ExternalAuthorityEPACompToxResource(CedarConfig cedarConfig, SubstanceRegistry substanceRegistry) {
    super(cedarConfig);
    this.substanceRegistry = substanceRegistry;
  }

  @GET
  @Timed
  @Path("/{id}")
  public Response getSubstanceDetails(@PathParam(PP_ID) String substanceId) throws CedarException {

    Map<String, String> substances = substanceRegistry.getSubstancesByDtxsid();
    int statusCode = 200;
    return CedarResponse.status(CedarResponseStatus.fromStatusCode(statusCode)).build();
  }
}
