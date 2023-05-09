package org.metadatacenter.cedar.bridge.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.cedar.util.dw.CedarMicroserviceResource;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.server.service.TemplateInstanceService;
import org.metadatacenter.server.service.TemplateService;
import org.metadatacenter.util.http.CedarResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.metadatacenter.constant.CedarPathParameters.PP_ID;
import static org.metadatacenter.rest.assertion.GenericAssertions.LoggedIn;

@Path("/datacite")
@Produces(MediaType.APPLICATION_JSON)
public class DataCiteResource extends CedarMicroserviceResource {

  private static TemplateService<String, JsonNode> templateService;
  private static TemplateInstanceService<String, JsonNode> templateInstanceService;


  public DataCiteResource(CedarConfig cedarConfig, TemplateService<String, JsonNode> templateService,
                          TemplateInstanceService<String, JsonNode> templateInstanceService) {
    super(cedarConfig);
    DataCiteResource.templateService = templateService;
    DataCiteResource.templateInstanceService = templateInstanceService;
  }

  @GET
  @Timed
  @Path("/get-doi-metadata/{id}")
  public Response getDOIMetadata(@PathParam(PP_ID) String id) throws CedarException {

    CedarRequestContext c = buildRequestContext();

    c.must(c.user()).be(LoggedIn);

    Map<String, Object> response = new HashMap<>();
    Map<String, Object> request = new HashMap<>();
    request.put("doi", id);
    response.put("request", request);
    Map<String, Object> dataCiteReponse = new HashMap<>();
    response.put("dataCiteResponse", dataCiteReponse);

    response.put("repositoryId", cedarConfig.getBridgeConfig().getDataCite().getRepositoryId());


//    if ("badDOI".equals(id)) {
//      return CedarResponse.notFound().errorMessage("DOI not found by ID").parameter("id", id).build();
//    } else if ("extraBadDOI".equals(id)) {
//        try {
//          if (true) {
//            throw new Exception("Something happened");
//          }
//        } catch (Exception e) {
//          return CedarResponse.internalServerError().exception(e).build();
//        }
//    }

    return Response.ok().entity(response).build();
  }

}
