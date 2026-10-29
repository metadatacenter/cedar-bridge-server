package org.metadatacenter.cedar.bridge.resources.doi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.ArtifactParseException;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.cedar.bridge.resource.datacite.CedarInstanceParser;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteInstanceValidationException;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMetadataParser;
import org.metadatacenter.cedar.bridge.resource.datacite.DataciteConstants;
import org.metadatacenter.cedar.bridge.resource.datacite.GenerateOpenViewUrl;
import org.metadatacenter.cedar.bridge.resource.datacite.form.Attributes;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSchema;
import org.metadatacenter.cedar.bridge.resources.doi.DataCiteClient.Answer;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.LinkedData;
import org.metadatacenter.exception.CedarException;
import org.metadatacenter.exception.CedarProcessingException;
import org.metadatacenter.id.CedarArtifactId;
import org.metadatacenter.id.CedarFQResourceId;
import org.metadatacenter.id.CedarResourceAddress;
import org.metadatacenter.id.CedarTemplateId;
import org.metadatacenter.model.CedarResourceType;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.rest.context.CedarRequestContext;
import org.metadatacenter.util.http.CedarResponse;
import org.metadatacenter.util.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState.ATTACHED;
import static org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState.DRAFT;
import static org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState.PUBLISHED;
import static org.metadatacenter.cedar.bridge.resources.doi.DoiRegistrationState.RESERVED;

/**
 * Registers an artifact's DOI with DataCite and records it on the artifact, so that a request repeated
 * after any failure continues with the DOI the first attempt began.
 *
 * <p>The DOI is chosen here and recorded for the artifact before DataCite is asked to register it. A
 * request that times out therefore leaves a DOI that the next request reads from DataCite by its
 * identifier: if DataCite holds it, the earlier attempt registered it and the next request continues
 * from there, and if not, the next request registers that same DOI. A DOI DataCite has published is
 * recorded as published before the resource server is asked to record it on the artifact, so when that
 * write fails, repeating the publish request makes the write again and asks nothing of DataCite.
 *
 * <p>The record also associates a draft with its artifact. A draft begun before CEDAR kept records is
 * found the old way, by asking DataCite for drafts at the artifact's landing page, and is recorded when
 * it is next saved.
 */
public final class DoiMinting {

  private static final Logger log = LoggerFactory.getLogger(DoiMinting.class);

  /**
   * The bound on the steps one request takes. Each step answers, or settles something about the DOI
   * that the next step acts on, and the longest path, a DOI that turns out to be another artifact's,
   * takes five.
   */
  private static final int MAX_STEPS = 8;

  /** The writer of what DataCite receives. Its indentation is part of the request, as it always was. */
  private static final ObjectMapper DATACITE_REQUEST_WRITER = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .enable(SerializationFeature.INDENT_OUTPUT);

  private final CedarConfig cedarConfig;
  private final DataCiteSettings settings;
  private final DataCiteClient dataCite;
  private final CedarArtifactClient artifacts;
  private final DoiEligibility eligibility;
  private final DoiRegistrationStore registrations;
  private final DoiAnnotationWriter annotations;
  private final Supplier<String> suffixes;

  /**
   * @param suffixes the source of the suffixes of new DOIs, {@link DoiSuffixes} outside a test
   */
  public DoiMinting(CedarConfig cedarConfig, CedarDataServices dataServices, DataCiteHttpClient http,
                    DoiAnnotationWriter annotations, DoiRegistrationStore registrations, Supplier<String> suffixes) {
    this.cedarConfig = cedarConfig;
    this.settings = DataCiteSettings.from(cedarConfig);
    this.dataCite = new DataCiteClient(settings, http);
    this.artifacts = new CedarArtifactClient(cedarConfig.getMicroserviceUrlUtil().getResource());
    this.eligibility = new DoiEligibility(dataServices);
    this.registrations = registrations;
    this.annotations = annotations;
    this.suffixes = suffixes;
  }

  /** The refusal every route gives while DataCite integration is switched off, or null while it is on. */
  public Response refusalWhileDisabled() {
    return settings.enabled() ? null : DoiResponses.disabled();
  }

  /** The refusal of a registration state other than {@code draft} or {@code publish}, or null for either. */
  public static Response refusalOfState(String state) {
    if (DataciteConstants.DRAFT.equals(state) || DataciteConstants.PUBLISH.equals(state)) {
      return null;
    }
    return DoiResponses.invalidState(state);
  }

  /** The refusal of metadata that failed validation against the DataCite template, or null for valid metadata. */
  static Response refusalOfMetadata(Pair<Boolean, JsonNode> validationResult) {
    return Boolean.TRUE.equals(validationResult.getLeft()) ? null
        : DoiResponses.invalidMetadata(validationResult.getRight());
  }

  // Reading a DOI's metadata

  /** What DataCite holds for the DOI the resolver URL names. */
  public Response readMetadata(String doiIdUrl) {
    try {
      Answer answer = dataCite.readPath(new URI(doiIdUrl).getPath());
      if (!answer.succeeded()) {
        return DoiResponses.dataCiteRefusal(answer.status(), answer.body());
      }
      JsonNode document = JsonMapper.STRICT_MAPPER.readTree(answer.body());
      // A deliberate shape check, whose result is not used. This answer fills the DOI form, so relaying
      // one the DataCite model cannot map would put unreadable metadata into the form. Mapping it here
      // fails instead, and the failure is answered as a 502. DataCite owns this payload and adds fields
      // to it, so the read takes the tolerant policy, and only a shape the model cannot map fails it.
      JsonMapper.TOLERANT_MAPPER.readValue(answer.body(), DataCiteSchema.class);
      return Response.status(answer.status()).entity(document).build();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return DoiResponses.upstreamFailure("The DataCite metadata request was interrupted", e);
    } catch (IOException e) {
      return DoiResponses.upstreamFailure("The DataCite metadata request failed", e);
    } catch (Exception e) {
      return CedarResponse.internalServerError().exception(e).build();
    }
  }

  // Opening the DOI form

  /**
   * What the DOI form opens with: the DataCite template, the source artifact, and the artifact's draft
   * or a pre-filled instance. Nothing is written, to DataCite, to the artifact or to the record.
   */
  public Response start(CedarRequestContext c, String sourceArtifactId) throws CedarException {
    String userId = c.getCedarUser().getId();
    JsonNode templateDocument = artifacts.read(CedarResourceType.TEMPLATE,
        CedarTemplateId.build(settings.templateId()), c);
    TemplateSchemaArtifact template;
    try {
      if (!(templateDocument instanceof ObjectNode document)) {
        throw new ArtifactParseException("The template is not a JSON object", "", "");
      }
      template = new JsonArtifactReader().readTemplateSchemaArtifact(document);
    } catch (ArtifactParseException e) {
      return CedarResponse.internalServerError()
          .message("The DataCite metadata template cannot be read: " + e.getMessage())
          .exception(e)
          .build();
    }

    Source source = source(c, sourceArtifactId);
    Response refusal = eligibility.refusal(c, source.type(), source.typedId(), source.document());
    if (refusal != null) {
      return refusal;
    }
    String carried = carriedDoi(source.document());
    if (carried != null) {
      return DoiResponses.alreadyHasDoi(source.type(), source.id(), carried, false);
    }
    Optional<DoiRegistration> recorded = registrations.find(source.id());
    if (recorded.isPresent() && recorded.get().state().isPublished()) {
      return DoiResponses.alreadyHasDoi(source.type(), source.id(), doiName(recorded.get().doi()), true);
    }

    Map<String, Object> response = new HashMap<>();
    try {
      Draft draft = recorded.isPresent() ? recordedDraft(source, recorded.get()) : unrecordedDraft(source);
      if (draft != null && draft.published()) {
        // DataCite published the DOI and the answer never arrived. A publish request records it.
        return DoiResponses.alreadyHasDoi(source.type(), source.id(), doiName(draft.doi()), true);
      }
      if (draft != null) {
        Attributes existing = JsonMapper.TOLERANT_MAPPER.treeToValue(draft.attributes(), Attributes.class);
        response.put(DataciteConstants.EXISTING_DATACITE_METADATA,
            DataCiteMetadataParser.parseDataCiteSchema(existing, userId, template, cedarConfig));
        response.put(DataciteConstants.DRAFT_DOI, TextNode.valueOf(draft.doi()));
      } else {
        response.put(DataciteConstants.EXISTING_DATACITE_METADATA,
            DataCiteMetadataParser.getDefaultInstance(source.id(), userId, template, cedarConfig));
        response.put(DataciteConstants.DRAFT_DOI, null);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return DoiResponses.upstreamFailure("The DataCite draft lookup was interrupted", e);
    } catch (IOException e) {
      return DoiResponses.upstreamFailure("The DataCite draft lookup failed", e);
    }
    response.put(DataciteConstants.SOURCE_ARTIFACT_TYPE, source.type().getValue());
    response.put(DataciteConstants.SOURCE_ARTIFACT, source.document());
    response.put(DataciteConstants.DATACITE_TEMPLATE, templateDocument);
    return CedarResponse.ok().entity(response).build();
  }

  /** A draft DOI the form can open on, or a DOI DataCite has already published. */
  private record Draft(String doi, JsonNode attributes, boolean published) {}

  /**
   * What DataCite holds for the recorded DOI, read by its identifier. A DOI DataCite does not hold, or
   * holds for another landing page, is no draft of this artifact's.
   */
  private Draft recordedDraft(Source source, DoiRegistration registration) throws IOException, InterruptedException {
    Answer held = dataCite.read(registration.doi());
    if (held.status() == 404) {
      return null;
    }
    if (!held.succeeded()) {
      throw new DoiResponses.DataCiteLookupRefused(held.status(), held.body());
    }
    JsonNode attributes = JsonMapper.STRICT_MAPPER.readTree(held.body()).path("data").path(DataciteConstants.ATTRIBUTES);
    if (!isFor(attributes, source)) {
      return null;
    }
    return new Draft(registration.doi(), attributes, !DataciteConstants.DRAFT.equals(attributes.path("state").asText()));
  }

  /**
   * The first draft DataCite lists for the artifact's landing page, for an artifact with no record. Only
   * a draft begun before CEDAR recorded its DOIs is found this way.
   */
  private Draft unrecordedDraft(Source source) throws IOException, InterruptedException {
    Answer listed = dataCite.listDrafts(landingPage(source));
    if (listed.status() != 200) {
      throw new DoiResponses.DataCiteLookupRefused(listed.status(), listed.body());
    }
    JsonNode listing = JsonMapper.STRICT_MAPPER.readTree(listed.body()).get("data");
    if (listing == null || !listing.isArray()) {
      return null;
    }
    // A DOI in any state but draft is not one the form can update: a findable DOI cannot go back to draft.
    for (JsonNode entry : listing) {
      JsonNode attributes = entry.path(DataciteConstants.ATTRIBUTES);
      if (DataciteConstants.DRAFT.equals(attributes.path("state").asText())) {
        String doi = attributes.path(DataciteConstants.DOI).asText(entry.path("id").asText(""));
        return doi.isBlank() ? null : new Draft(doi, attributes, false);
      }
    }
    return null;
  }

  // Registering a DOI

  /**
   * Registers the artifact's DOI as a draft or publishes it, and records a published DOI on the artifact.
   * A request repeated after a failure continues with the DOI the artifact's record holds.
   */
  public Response register(CedarRequestContext c, String sourceArtifactId, String state, JsonNode instance)
      throws CedarException {
    Source source = source(c, sourceArtifactId);
    Response refusal = eligibility.refusal(c, source.type(), source.typedId(), source.document());
    if (refusal != null) {
      return refusal;
    }
    boolean publish = DataciteConstants.PUBLISH.equals(state);
    Optional<DoiRegistration> recorded = registrations.find(source.id());
    String carried = carriedDoi(source.document());
    if (carried != null) {
      // The artifact carries a DOI. When it is the recorded one, the record is brought up to date: the
      // DOI may have been recorded on the artifact by the request whose answer was lost, or by hand.
      recorded.filter(r -> r.state() != ATTACHED && doiName(r.doi()).equalsIgnoreCase(carried))
          .ifPresent(r -> registrations.advance(source.id(), r.doi(), ATTACHED, null));
      return DoiResponses.alreadyHasDoi(source.type(), source.id(), carried, false);
    }
    if (recorded.isPresent() && recorded.get().state().isPublished()) {
      // DataCite has published the DOI, and only recording it on the artifact remains. A draft request
      // never writes the artifact, so it is told what a publish request would complete.
      return publish ? attach(c, source, recorded.get())
          : DoiResponses.alreadyHasDoi(source.type(), source.id(), doiName(recorded.get().doi()), true);
    }

    Response invalid = refusalOfMetadata(artifacts.validate(settings.templateId(), instance, c));
    if (invalid != null) {
      return invalid;
    }
    Request request;
    try {
      request = new Request(c, source, publish, dataCiteRequest(instance, source.id(), state));
    } catch (RuntimeException e) {
      return DoiResponses.unconvertible(e);
    }

    try {
      DoiRegistration registration = recorded.orElse(null);
      boolean absent = false;
      if (registration == null) {
        registration = adoptUnrecordedDraft(source, c.getCedarUser().getId());
      }
      if (registration == null) {
        String doi = settings.prefix() + "/" + suffixes.get();
        registration = registrations.claim(source.id(), doi, RESERVED, c.getCedarUser().getId());
        // Nothing has asked DataCite for a DOI this request has just reserved, so DataCite cannot hold
        // it, unless it is another artifact's, which DataCite's refusal to register it shows.
        absent = registration.doi().equals(doi) && registration.state() == RESERVED;
        if (absent) {
          log.info("Reserved the DOI {} for {}", doi, source.id());
        }
      }
      return write(request, registration, absent);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return DoiResponses.upstreamFailure("The DataCite DOI request was interrupted; publication state is unknown", e);
    } catch (IOException e) {
      return DoiResponses.upstreamFailure("The DataCite DOI request failed; publication state is unknown", e);
    }
  }

  /** One registration request: the artifact, whether to publish, and the DataCite request built from the metadata. */
  private record Request(CedarRequestContext context, Source source, boolean publish, ObjectNode dataCiteRequest) {

    /** The request that updates a DOI DataCite holds, which names the DOI in its URL. */
    String updateBody() throws IOException {
      return DATACITE_REQUEST_WRITER.writeValueAsString(dataCiteRequest);
    }

    /** The request that registers the DOI, which names it among the attributes. */
    String createBody(String doi) throws IOException {
      ObjectNode request = dataCiteRequest.deepCopy();
      ((ObjectNode) request.path("data").path(DataciteConstants.ATTRIBUTES)).put(DataciteConstants.DOI, doi);
      return DATACITE_REQUEST_WRITER.writeValueAsString(request);
    }
  }

  /** Records the draft DataCite lists for an artifact with no record, so the artifact keeps that draft. */
  private DoiRegistration adoptUnrecordedDraft(Source source, String userId) throws IOException, InterruptedException {
    Draft draft = unrecordedDraft(source);
    return draft == null ? null : registrations.claim(source.id(), draft.doi(), DRAFT, userId);
  }

  /**
   * Brings DataCite's record of the DOI to what the request asks for, starting from what the artifact's
   * record says. A step either answers, or settles something that moves the record on: a reservation
   * DataCite turns out to hold becomes a draft or a published DOI, a draft DataCite has lost becomes a
   * reservation it is known not to hold, and a DOI that turns out to be another artifact's is replaced.
   *
   * @param absent whether DataCite is known not to hold the reserved DOI, so that it is registered
   *               without first being read
   */
  private Response write(Request request, DoiRegistration registration, boolean absent)
      throws IOException, InterruptedException {
    String sourceId = request.source().id();
    Answer refusedCreate = null;
    for (int step = 0; step < MAX_STEPS; step++) {
      if (registration.state().isPublished()) {
        return request.publish() ? attach(request.context(), request.source(), registration)
            : DoiResponses.alreadyHasDoi(request.source().type(), sourceId, doiName(registration.doi()), true);
      }
      if (registration.state() == DRAFT) {
        Answer answer = dataCite.update(registration.doi(), request.updateBody());
        if (answer.status() != 404) {
          return written(request, registration, answer);
        }
        // DataCite no longer holds the draft. It was never findable, so the same DOI is registered again.
        log.warn("DataCite no longer holds the draft DOI {} recorded for {}; registering it again",
            registration.doi(), sourceId);
        registration = registrations.reserveInstead(sourceId, registration.doi(), registration.doi());
        absent = true;
        continue;
      }
      if (!absent) {
        Answer held = dataCite.read(registration.doi());
        if (held.succeeded()) {
          String doi = registration.doi();
          registration = settled(request, registration, held);
          if (!registration.doi().equals(doi)) {
            refusedCreate = null;
          }
          continue;
        }
        if (held.status() != 404) {
          return DoiResponses.dataCiteRefusal(held.status(), held.body());
        }
        if (refusedCreate != null) {
          // DataCite does not hold the DOI, so it refused the metadata rather than the DOI.
          return DoiResponses.unprocessable(refusedCreate.body());
        }
      }
      Answer answer = dataCite.create(request.createBody(registration.doi()));
      if (answer.status() == 422 && refusedCreate == null) {
        // DataCite refuses to register a DOI it holds already, as it refuses metadata it cannot accept.
        // Reading the DOI tells which: an earlier attempt, a concurrent request or another artifact may
        // hold it.
        refusedCreate = answer;
        absent = false;
        continue;
      }
      return written(request, registration, answer);
    }
    throw new IllegalStateException("The DOI registration for " + sourceId + " did not settle in " + MAX_STEPS
        + " steps; the record holds " + registration.doi() + " as " + registration.state());
  }

  /**
   * The record as DataCite's answer to a read of the reserved DOI settles it. DataCite holding the DOI
   * for this artifact's landing page means an earlier attempt registered it. Holding it for another
   * means the suffix is another artifact's, and a new one is reserved.
   */
  private DoiRegistration settled(Request request, DoiRegistration registration, Answer held) throws IOException {
    String sourceId = request.source().id();
    JsonNode attributes = JsonMapper.STRICT_MAPPER.readTree(held.body()).path("data").path(DataciteConstants.ATTRIBUTES);
    if (!isFor(attributes, request.source())) {
      String replacement = settings.prefix() + "/" + suffixes.get();
      log.warn("DataCite holds the DOI {} reserved for {} for another landing page; reserving {} instead",
          registration.doi(), sourceId, replacement);
      return registrations.reserveInstead(sourceId, registration.doi(), replacement);
    }
    if (DataciteConstants.DRAFT.equals(attributes.path("state").asText())) {
      return registrations.advance(sourceId, registration.doi(), DRAFT, null);
    }
    return registrations.advance(sourceId, registration.doi(), PUBLISHED, held.body());
  }

  /** The answer to DataCite's answer to a registration or an update. */
  private Response written(Request request, DoiRegistration registration, Answer answer) {
    String sourceId = request.source().id();
    if (answer.status() == 200 || answer.status() == 201) {
      String registered = registeredDoi(answer.body(), registration.doi());
      if (!registered.equalsIgnoreCase(registration.doi())) {
        // DataCite chose a DOI of its own. The record keeps the one DataCite holds, not the one asked for.
        log.warn("DataCite registered {} for {}, which asked for {}", registered, sourceId, registration.doi());
        registration = registrations.reserveInstead(sourceId, registration.doi(), registered);
      }
      if (request.publish()) {
        // Recorded as published before the artifact is written, which is the step that can still fail.
        return attach(request.context(), request.source(),
            registrations.advance(sourceId, registration.doi(), PUBLISHED, answer.body()));
      }
      registrations.advance(sourceId, registration.doi(), DRAFT, null);
      return DoiResponses.created(registration.doi(), answer.body());
    }
    if (answer.status() == 422) {
      return DoiResponses.unprocessable(answer.body());
    }
    return DoiResponses.dataCiteRefusal(answer.status(), answer.body());
  }

  /**
   * Records a published DOI on the artifact and answers with it. A failure leaves the record published,
   * so repeating the request makes this write again and registers nothing at DataCite.
   */
  private Response attach(CedarRequestContext context, Source source, DoiRegistration registration) {
    Response failure = recordOnArtifact(context, source.id(), doiName(registration.doi()));
    if (failure != null) {
      return failure;
    }
    registrations.advance(source.id(), registration.doi(), ATTACHED, null);
    return DoiResponses.created(registration.doi(), registration.dataCiteResponse());
  }

  /** Asks the resource server to record the DOI on the artifact, and answers its failure, or null on success. */
  Response recordOnArtifact(CedarRequestContext context, String sourceArtifactId, String doiName) {
    String annotationUrl = cedarConfig.getMicroserviceUrlUtil().getResource().getCommandDOIUpdate();
    ObjectNode commandContent = JsonMapper.STRICT_MAPPER.createObjectNode();
    commandContent.put(LinkedData.ID, CedarResourceAddress.selector(sourceArtifactId));
    commandContent.put(DataciteConstants.DOI, doiName);

    ClassicHttpResponse annotationResponse = null;
    try {
      annotationResponse = annotations.post(annotationUrl, context, commandContent.toString());
      int statusCode = annotationResponse.getCode();
      if (Response.Status.Family.familyOf(statusCode) == Response.Status.Family.SUCCESSFUL) {
        return null;
      }
      log.error("DOI {} was published for {}, but CEDAR annotation write-back returned status {}",
          doiName, sourceArtifactId, statusCode);
      return DoiResponses.reconciliationRequired(sourceArtifactId, doiName, statusCode, null);
    } catch (CedarProcessingException e) {
      return DoiResponses.reconciliationRequired(sourceArtifactId, doiName, null, e);
    } finally {
      if (annotationResponse != null) {
        EntityUtils.consumeQuietly(annotationResponse.getEntity());
      }
    }
  }

  // The source artifact

  /** The artifact a DOI is for, as the resource server returned it. */
  private record Source(String id, CedarResourceType type, CedarArtifactId typedId, JsonNode document) {}

  private Source source(CedarRequestContext c, String sourceArtifactId) throws CedarException {
    String id = cedarConfig.getLinkedDataUtil().resolveResourceId(sourceArtifactId);
    CedarFQResourceId resourceId = CedarFQResourceId.build(id);
    CedarArtifactId typedId = CedarArtifactId.build(id, resourceId.getType());
    return new Source(id, resourceId.getType(), typedId, artifacts.read(resourceId.getType(), typedId, c));
  }

  /** The URL a DOI for the artifact resolves to, which CEDAR always registers. */
  private String landingPage(Source source) {
    return GenerateOpenViewUrl.getOpenViewUrl(source.id(), cedarConfig);
  }

  /** Whether DataCite's attributes for a DOI are this artifact's, which its landing page shows. */
  private boolean isFor(JsonNode attributes, Source source) {
    return landingPage(source).equals(attributes.path("url").asText(null));
  }

  /** The DOI the artifact carries in its annotations, as a resolver URL, or null when it carries none. */
  private static String carriedDoi(JsonNode sourceArtifact) {
    JsonNode doi = sourceArtifact.path(ModelNodeNames.ANNOTATIONS).path(ModelNodeNames.DATACITE_DOI_URI).get(LinkedData.ID);
    return doi == null ? null : doi.textValue();
  }

  private static String doiName(String doi) {
    return DataciteConstants.DOI_PREFIX + doi;
  }

  /** The DOI DataCite's answer names, or the one asked for when the answer names none. */
  private static String registeredDoi(String body, String requested) {
    try {
      String id = JsonMapper.STRICT_MAPPER.readTree(body).path("data").path("id").asText("");
      return id.isBlank() ? requested : id;
    } catch (IOException e) {
      return requested;
    }
  }

  /** The DataCite request the metadata instance converts into, before any DOI is named in it. */
  private ObjectNode dataCiteRequest(JsonNode metadata, String sourceArtifactId, String state) {
    try {
      // Read the instance into the model. It has been validated against the DataCite template, and a
      // child the mapping does not name is read and left alone.
      if (!(metadata instanceof ObjectNode instanceDocument)) {
        throw new IllegalArgumentException("The DataCite metadata must be a JSON object");
      }
      TemplateInstanceArtifact cedarInstance = new JsonArtifactReader().readTemplateInstanceArtifact(instanceDocument);
      DataCiteSchema dataCiteSchema = new DataCiteSchema();
      CedarInstanceParser.parseCedarInstance(cedarInstance, dataCiteSchema, sourceArtifactId, state, cedarConfig);
      return DATACITE_REQUEST_WRITER.valueToTree(dataCiteSchema);
    } catch (DataCiteInstanceValidationException e) {
      throw new RuntimeException(e);
    }
  }
}
