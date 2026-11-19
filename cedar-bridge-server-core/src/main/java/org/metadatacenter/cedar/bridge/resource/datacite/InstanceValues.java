package org.metadatacenter.cedar.bridge.resource.datacite;

import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.ParentInstanceArtifact;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Elements;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Entry;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Part;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What part of an instance holds, judged from the model alone.
 * <p>
 * A user who leaves an entry of a repeated element empty has said nothing in it. So the conversion to
 * DataCite reads an entry that holds no value at any depth as blank, and a repeated element whose
 * entries are all blank as absent. A value is content in {@code @value}, {@code @id} or
 * {@code rdfs:label}. A string that is empty or holds only white space is no content, since DataCite
 * would register it as a value that says nothing. An element's own {@code @id} identifies the element
 * and is not a value in it.
 */
public final class InstanceValues {

  private InstanceValues() {
  }

  /** The content of a field value in the given part, or null where it has none. */
  public static String content(FieldInstanceArtifact field, Part part) {
    if (field == null) {
      return null;
    }
    Optional<String> content = switch (part) {
      case VALUE -> field.jsonLdValue();
      case LABEL -> field.label();
      case IRI -> field.jsonLdIdIri();
    };
    return content.orElse(null);
  }

  public static boolean isBlank(String content) {
    return content == null || content.isBlank();
  }

  public static boolean isBlank(FieldInstanceArtifact field) {
    return field == null
        || isBlank(field.jsonLdValue().orElse(null))
        && isBlank(field.jsonLdIdIri().orElse(null))
        && isBlank(field.label().orElse(null));
  }

  public static boolean isBlank(ParentInstanceArtifact parent) {
    return parent == null
        || parent.singleInstanceFieldInstances().values().stream().allMatch(InstanceValues::isBlank)
        && parent.multiInstanceFieldInstances().values().stream().flatMap(List::stream)
            .allMatch(InstanceValues::isBlank)
        && parent.singleInstanceElementInstances().values().stream().allMatch(InstanceValues::isBlank)
        && parent.multiInstanceElementInstances().values().stream().flatMap(List::stream)
            .allMatch(InstanceValues::isBlank)
        && parent.attributeValueFieldInstanceGroups().values().stream().map(Map::values)
            .flatMap(Collection::stream).allMatch(InstanceValues::isBlank);
  }

  /**
   * Whether an entry of a repeated element fills a child that holds its value, rather than only the
   * type or scheme that qualifies one. An element that identifies no such child holds a value in any
   * entry.
   */
  public static <C> boolean holdsAValue(ElementInstanceArtifact entry, Elements<?, C> elements) {
    if (elements.valuedBy().isEmpty()) {
      return true;
    }
    for (Entry<C> child : elements.children()) {
      if (child instanceof Value<C> value && elements.valuedBy().contains(value.field())
          && !isBlank(content(entry.singleInstanceFieldInstances().get(value.field()), value.part()))) {
        return true;
      }
    }
    return false;
  }

  /** Whether every entry of a repeated element is blank, which is true of none at all. */
  public static boolean allBlank(List<ElementInstanceArtifact> entries) {
    return entries.stream().allMatch(InstanceValues::isBlank);
  }
}
