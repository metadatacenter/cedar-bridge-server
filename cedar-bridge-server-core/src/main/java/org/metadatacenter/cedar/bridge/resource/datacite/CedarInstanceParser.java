package org.metadatacenter.cedar.bridge.resource.datacite;

import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.ParentInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Context;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Element;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Elements;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Entry;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Problems;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Value;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Values;
import org.metadatacenter.cedar.bridge.resource.datacite.form.Attributes;
import org.metadatacenter.cedar.bridge.resource.datacite.form.Data;
import org.metadatacenter.cedar.bridge.resource.datacite.form.DataCiteSchema;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.id.CedarFQResourceId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Converts an instance of the DataCite metadata template into the attributes DataCite registers, by
 * walking {@link DataCiteMapping#TEMPLATE} over it.
 * <p>
 * An instance that leaves a required property blank is refused, and the refusal lists every such
 * property. A wrong prefix, a publication year other than the current one and a coordinate out of
 * range are refused as soon as they are found.
 * <p>
 * A string that is empty or holds only white space is no value. Below the attributes, a property with
 * no value is left out, and so is an object or a list left with none. An entry that holds only a type
 * or a scheme, without the value it qualifies, is left out too. A property of the attributes
 * themselves is sent as null instead, because DataCite keeps a property that an update leaves out, and
 * a user who empties a field means DataCite to forget it.
 */
public final class CedarInstanceParser {
  private static final String STATE_DRAFT = "draft";
  private static final String STATE_PUBLISH = "publish";
  private static final String DATACITE_SCHEMA = "http://datacite.org/schema/kernel-4";

  private CedarInstanceParser() {
  }

  public static void parseCedarInstance(TemplateInstanceArtifact instance, DataCiteSchema dataCiteSchema,
                                        String sourceArtifactId, String state, CedarConfig cedarConfig)
      throws DataCiteInstanceValidationException {
    String resourceType = CedarFQResourceId.build(sourceArtifactId).getType().getValue();
    Context context = new Context(cedarConfig.getBridgeConfig().getDataCite().getPrefix(),
        GenerateOpenViewUrl.getOpenViewUrl(sourceArtifactId, cedarConfig),
        resourceType.substring(0, 1).toUpperCase(Locale.ROOT) + resourceType.substring(1), new Problems());

    Attributes attributes = new Attributes();
    if (STATE_PUBLISH.equals(state) || STATE_DRAFT.equals(state)) {
      attributes.setEvent(state);
    }
    attributes.setSchemaVersion(DATACITE_SCHEMA);
    fill(instance, DataCiteMapping.TEMPLATE, attributes, context);

    Data data = new Data();
    data.setType("dois");
    data.setAttributes(attributes);
    dataCiteSchema.setData(data);

    context.problems().throwIfMissing();
  }

  /**
   * Fills the target from the instance's children that the entries identify, and says whether the
   * target was given any value.
   */
  private static <T> boolean fill(ParentInstanceArtifact instance, List<Entry<T>> entries, T target,
                                  Context context) throws DataCiteInstanceValidationException {
    boolean filled = false;
    for (Entry<T> entry : entries) {
      if (entry instanceof Value<T> value) {
        String content = InstanceValues.content(instance.singleInstanceFieldInstances().get(value.field()),
            value.part());
        if (InstanceValues.isBlank(content)) {
          content = null;
          if (value.requiredAs() != null) {
            context.problems().missing(value.requiredAs());
          }
        }
        value.writer().write(target, content, context);
        filled |= content != null;
      } else if (entry instanceof Values<T> values) {
        List<String> contents = new ArrayList<>();
        for (FieldInstanceArtifact field : instance.multiInstanceFieldInstances().getOrDefault(values.field(),
            List.of())) {
          String content = field.jsonLdValue().orElse(null);
          if (!InstanceValues.isBlank(content)) {
            contents.add(content);
          }
        }
        if (!contents.isEmpty()) {
          values.set().accept(target, contents);
          filled = true;
        }
      } else if (entry instanceof Elements<T, ?> elements) {
        filled |= fillElements(instance, elements, target, context);
      } else if (entry instanceof Element<T, ?> element) {
        filled |= fillElement(instance, element, target, context);
      }
    }
    return filled;
  }

  private static <T, C> boolean fillElements(ParentInstanceArtifact instance, Elements<T, C> elements, T target,
                                             Context context) throws DataCiteInstanceValidationException {
    List<ElementInstanceArtifact> entries = instance.multiInstanceElementInstances().getOrDefault(elements.field(),
        List.of());
    List<C> items = new ArrayList<>();
    if (!InstanceValues.allBlank(entries)) {
      for (ElementInstanceArtifact entry : entries) {
        if (!InstanceValues.holdsAValue(entry, elements)) {
          continue;
        }
        C item = elements.create().get();
        boolean filled = fill(entry, elements.children(), item, context);
        context.problems().throwIfOutOfRange();
        if (filled) {
          items.add(item);
        }
      }
    }
    if (items.isEmpty()) {
      if (elements.requiredAs() != null) {
        context.problems().missing(elements.requiredAs());
      }
      return false;
    }
    elements.set().accept(target, items);
    return true;
  }

  private static <T, C> boolean fillElement(ParentInstanceArtifact instance, Element<T, C> element, T target,
                                            Context context) throws DataCiteInstanceValidationException {
    ElementInstanceArtifact entry = instance.singleInstanceElementInstances().get(element.field());
    if (InstanceValues.isBlank(entry)) {
      return false;
    }
    C item = element.create().get();
    if (!fill(entry, element.children(), item, context)) {
      return false;
    }
    element.set().accept(target, item);
    return true;
  }
}
