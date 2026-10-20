package org.metadatacenter.cedar.bridge.resource.datacite;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.ControlledTermField;
import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
import org.metadatacenter.artifacts.model.core.ParentSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.BranchValueConstraint;
import org.metadatacenter.artifacts.model.core.fields.constraints.ClassValueConstraint;
import org.metadatacenter.artifacts.model.core.fields.constraints.ControlledTermValueConstraints;
import org.metadatacenter.artifacts.model.renderer.JsonArtifactRenderer;
import org.metadatacenter.artifacts.model.tools.EmptyFieldInstances;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Context;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Element;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Elements;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Entry;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Part;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Value;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Values;
import org.metadatacenter.cedar.bridge.resource.datacite.form.Attributes;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteType;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.id.CedarFQResourceId;
import org.metadatacenter.model.CedarResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Builds the instance of the DataCite metadata template that the DOI form opens with, by walking
 * {@link DataCiteMapping#TEMPLATE} over DataCite's attributes.
 * <p>
 * The instance holds only the values DataCite gives, and the template supplies the rest when the
 * instance is rendered: the empty entries, the property IRIs and the order. A value takes its kind
 * and its {@code @type} from the template's field, and a controlled term takes its IRI from the terms
 * the template allows.
 */
public final class DataCiteMetadataParser {

  private static final Logger log = LoggerFactory.getLogger(DataCiteMetadataParser.class);

  private DataCiteMetadataParser() {
  }

  /** The instance for a draft DOI, from the attributes DataCite holds for it. */
  public static ObjectNode parseDataCiteSchema(Attributes dataCiteAttributes, String userID,
                                               TemplateSchemaArtifact template, CedarConfig cedarConfig) {
    return instance(dataCiteAttributes, "The name", "", userID, template, cedarConfig);
  }

  /**
   * The instance for an artifact DataCite holds no draft for. Besides the DOI prefix, it carries what
   * CEDAR knows already: the landing page, the publisher, the resource type and the current year.
   */
  public static ObjectNode getDefaultInstance(String sourceArtifactId, String userID, TemplateSchemaArtifact template,
                                              CedarConfig cedarConfig) {
    String resourceType = CedarFQResourceId.build(sourceArtifactId).getType().getValue();
    DataCiteType types = new DataCiteType();
    types.setResourceType(resourceType.substring(0, 1).toUpperCase(Locale.ROOT) + resourceType.substring(1));
    Attributes known = new Attributes();
    known.setUrl(GenerateOpenViewUrl.getOpenViewUrl(sourceArtifactId, cedarConfig));
    known.setPublisher(DataciteConstants.PUBLISHER);
    known.setPublicationYear(Year.now().getValue());
    known.setTypes(types);
    return instance(known, "Default DataCite Instance", "A new DataCite Instance with default values", userID,
        template, cedarConfig);
  }

  private static ObjectNode instance(Attributes attributes, String name, String description, String userID,
                                     TemplateSchemaArtifact template, CedarConfig cedarConfig) {
    URI templateId = template.jsonLdId()
        .orElseThrow(() -> new IllegalArgumentException("The DataCite template has no identifier"));
    URI user = URI.create(userID);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    TemplateInstanceArtifact.Builder builder = TemplateInstanceArtifact.builder()
        .withIsBasedOn(templateId)
        .withDerivedFrom(templateId)
        .withName(name)
        .withDescription(description)
        .withCreatedOn(now)
        .withCreatedBy(user)
        .withLastUpdatedOn(now)
        .withModifiedBy(user);

    Children children = new Children();
    fill(template, DataCiteMapping.TEMPLATE, attributes,
        Context.forInstance(cedarConfig.getBridgeConfig().getDataCite().getPrefix()), children);
    children.addTo(builder);

    ObjectNode instance = new JsonArtifactRenderer().renderTemplateInstanceArtifact(template, builder.build());
    cedarConfig.getLinkedDataUtil().addElementInstanceIds(instance, CedarResourceType.INSTANCE);
    return instance;
  }

  private static <T> void fill(ParentSchemaArtifact schema, List<Entry<T>> entries, T source, Context context,
                               Children children) {
    for (Entry<T> entry : entries) {
      if (entry instanceof Value<T> value) {
        FieldInstanceArtifact field =
            fieldValue(schema.getFieldSchemaArtifact(value.field()), value.part(), value.reader().read(source, context));
        if (field != null) {
          children.fields.put(value.field(), field);
        }
      } else if (entry instanceof Values<T> values) {
        List<String> contents = values.get().apply(source);
        if (contents != null && !contents.isEmpty()) {
          FieldSchemaArtifact fieldSchema = schema.getFieldSchemaArtifact(values.field());
          List<FieldInstanceArtifact> fields = new ArrayList<>();
          for (String content : contents) {
            FieldInstanceArtifact field = fieldValue(fieldSchema, Part.VALUE, content);
            if (field != null) {
              fields.add(field);
            }
          }
          children.multiFields.put(values.field(), fields);
        }
      } else if (entry instanceof Elements<T, ?> elements) {
        fillElements(schema, elements, source, context, children);
      } else if (entry instanceof Element<T, ?> element) {
        fillElement(schema, element, source, context, children);
      }
    }
  }

  private static <T, C> void fillElements(ParentSchemaArtifact schema, Elements<T, C> elements, T source,
                                          Context context, Children children) {
    List<C> items = elements.get().apply(source);
    if (items == null || items.isEmpty()) {
      return;
    }
    ElementSchemaArtifact elementSchema = schema.getElementSchemaArtifact(elements.field());
    List<ElementInstanceArtifact> entries = new ArrayList<>();
    for (C item : items) {
      Children itemChildren = new Children();
      fill(elementSchema, elements.children(), item, context, itemChildren);
      entries.add(itemChildren.element());
    }
    children.multiElements.put(elements.field(), entries);
  }

  private static <T, C> void fillElement(ParentSchemaArtifact schema, Element<T, C> element, T source,
                                         Context context, Children children) {
    C item = element.get().apply(source);
    if (item == null) {
      return;
    }
    Children itemChildren = new Children();
    fill(schema.getElementSchemaArtifact(element.field()), element.children(), item, context, itemChildren);
    children.elements.put(element.field(), itemChildren.element());
  }

  /**
   * The value a field takes for content from DataCite, or null for none. A literal keeps an empty
   * string, which the form shows as typed. A term or a link has no empty form, so an empty one is none.
   */
  private static FieldInstanceArtifact fieldValue(FieldSchemaArtifact schema, Part part, String content) {
    if (content == null || (part != Part.VALUE && content.isEmpty())) {
      return null;
    }
    List<URI> types = EmptyFieldInstances.emptyFor(schema).jsonLdTypes();
    return switch (part) {
      case VALUE -> FieldInstanceArtifact.create(types, Optional.empty(), Optional.of(content), Optional.empty(),
          Optional.empty(), Optional.empty(), Optional.empty());
      case LABEL -> term(schema, types, content);
      case IRI -> link(schema, types, content);
    };
  }

  private static FieldInstanceArtifact term(FieldSchemaArtifact schema, List<URI> types, String label) {
    Optional<String> iri = termIri(schema, label);
    try {
      return iriValue(types, iri, label);
    } catch (IllegalArgumentException e) {
      log.warn("DataCite term {} of {} has no usable IRI: {}", label, schema.name(), e.getMessage());
      return iriValue(types, Optional.empty(), label);
    }
  }

  private static FieldInstanceArtifact link(FieldSchemaArtifact schema, List<URI> types, String iri) {
    try {
      return iriValue(types, Optional.of(iri), null);
    } catch (IllegalArgumentException e) {
      log.warn("DataCite value {} of {} is not an IRI and is left out: {}", iri, schema.name(), e.getMessage());
      return null;
    }
  }

  private static FieldInstanceArtifact iriValue(List<URI> types, Optional<String> iri, String label) {
    return FieldInstanceArtifact.createWithIri(types, iri, Optional.empty(), Optional.ofNullable(label),
        Optional.empty(), Optional.empty(), Optional.empty(), false);
  }

  /**
   * The IRI of the term the template allows under a label. A field that lists its terms gives the
   * IRI of the one with that label. A field constrained to a branch of DataCite's vocabulary allows
   * the terms under it, which that vocabulary names by label in the namespace of the branch itself.
   */
  private static Optional<String> termIri(FieldSchemaArtifact schema, String label) {
    if (!(schema instanceof ControlledTermField)) {
      return Optional.empty();
    }
    Optional<ControlledTermValueConstraints> constraints = schema.valueConstraints()
        .filter(ControlledTermValueConstraints.class::isInstance)
        .map(ControlledTermValueConstraints.class::cast);
    if (constraints.isEmpty()) {
      return Optional.empty();
    }
    for (ClassValueConstraint term : constraints.get().classes()) {
      if (label.equals(term.label())) {
        return Optional.of(term.uri().toString());
      }
    }
    for (BranchValueConstraint branch : constraints.get().branches()) {
      String root = branch.uri().toString();
      int end = Math.max(root.lastIndexOf('/'), root.lastIndexOf('#'));
      if (end > 0) {
        return Optional.of(root.substring(0, end + 1) + label);
      }
    }
    return Optional.empty();
  }

  /** The children of one template or element instance, gathered before the instance is built. */
  private static final class Children {
    private final LinkedHashMap<String, FieldInstanceArtifact> fields = new LinkedHashMap<>();
    private final LinkedHashMap<String, List<FieldInstanceArtifact>> multiFields = new LinkedHashMap<>();
    private final LinkedHashMap<String, ElementInstanceArtifact> elements = new LinkedHashMap<>();
    private final LinkedHashMap<String, List<ElementInstanceArtifact>> multiElements = new LinkedHashMap<>();

    void addTo(TemplateInstanceArtifact.Builder builder) {
      fields.forEach(builder::withSingleInstanceFieldInstance);
      multiFields.forEach(builder::withMultiInstanceFieldInstances);
      elements.forEach(builder::withSingleInstanceElementInstance);
      multiElements.forEach(builder::withMultiInstanceElementInstances);
    }

    ElementInstanceArtifact element() {
      ElementInstanceArtifact.Builder builder = ElementInstanceArtifact.builder();
      fields.forEach(builder::withSingleInstanceFieldInstance);
      multiFields.forEach(builder::withMultiInstanceFieldInstances);
      elements.forEach(builder::withSingleInstanceElementInstance);
      multiElements.forEach(builder::withMultiInstanceElementInstances);
      return builder.build();
    }
  }
}
