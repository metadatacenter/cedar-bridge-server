package org.metadatacenter.cedar.bridge;

import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.ControlledTermField;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
import org.metadatacenter.artifacts.model.core.IriFieldInstance;
import org.metadatacenter.artifacts.model.core.LinkField;
import org.metadatacenter.artifacts.model.core.ParentSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.BranchValueConstraint;
import org.metadatacenter.artifacts.model.core.fields.constraints.ControlledTermValueConstraints;
import org.metadatacenter.artifacts.model.tools.EmptyFieldInstances;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Element;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Elements;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Entry;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Value;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Values;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mapping table against the production DataCite template.
 * <p>
 * The table identifies the template's children by name, and both conversions trust it. An entry
 * that names no child of the template maps nothing, and a child the table leaves out never reaches
 * DataCite. So every entry must name a child the template has, of the kind the entry reads and with
 * the cardinality it expects, and every child of the template must have an entry.
 */
class DataCiteMappingTest {

  private static final TemplateSchemaArtifact TEMPLATE = DataCiteFixtures.template();

  @Test
  void everyEntryNamesAChildOfTheTemplateOfTheKindItReads() {
    List<String> problems = new ArrayList<>();
    checkEntries(TEMPLATE, DataCiteMapping.TEMPLATE, "", problems);
    assertEquals(List.of(), problems);
  }

  @Test
  void everyChildOfTheTemplateHasAnEntry() {
    List<String> problems = new ArrayList<>();
    checkChildren(TEMPLATE, DataCiteMapping.TEMPLATE, "", problems);
    assertEquals(List.of(), problems);
  }

  /**
   * The way back names a term under a branch by its label in the namespace of the branch itself,
   * which is how DataCite's vocabulary names its terms. Every branch the template uses must be one of
   * that vocabulary's.
   */
  @Test
  void everyBranchTheTemplateUsesIsDataCitesVocabulary() {
    List<BranchValueConstraint> branches = new ArrayList<>();
    collectBranches(TEMPLATE, branches);
    assertTrue(branches.size() > 0, "the template constrains its DataCite terms to branches");
    for (BranchValueConstraint branch : branches) {
      assertEquals("DATACITE-VOCAB", branch.acronym(), branch.toString());
      assertTrue(branch.uri().toString().startsWith("http://purl.org/datacite/v4.4/"), branch.toString());
    }
  }

  private static void checkEntries(ParentSchemaArtifact schema, List<? extends Entry<?>> entries, String path,
                                   List<String> problems) {
    for (Entry<?> entry : entries) {
      String at = path + "/" + entry.field();
      if (entry instanceof Value<?> value) {
        FieldSchemaArtifact field = schema.fieldSchemas().get(value.field());
        if (field == null) {
          problems.add(at + ": no such field");
        } else if (field.isMultiple()) {
          problems.add(at + ": repeated in the template, read once");
        } else {
          boolean kindMatches = switch (value.part()) {
            case VALUE -> !(EmptyFieldInstances.emptyFor(field) instanceof IriFieldInstance);
            case LABEL -> field instanceof ControlledTermField;
            case IRI -> field instanceof LinkField;
          };
          if (!kindMatches) {
            problems.add(at + ": read as " + value.part() + ", but the field is " + field.fieldUi().inputType());
          }
        }
      } else if (entry instanceof Values<?> values) {
        FieldSchemaArtifact field = schema.fieldSchemas().get(values.field());
        if (field == null) {
          problems.add(at + ": no such field");
        } else if (!field.isMultiple()) {
          problems.add(at + ": occurs once in the template, read as repeated");
        } else if (EmptyFieldInstances.emptyFor(field) instanceof IriFieldInstance) {
          problems.add(at + ": read as literals, but the field is " + field.fieldUi().inputType());
        }
      } else if (entry instanceof Elements<?, ?> elements) {
        ElementSchemaArtifact element = schema.elementSchemas().get(elements.field());
        if (element == null) {
          problems.add(at + ": no such element");
        } else {
          if (!element.isMultiple()) {
            problems.add(at + ": occurs once in the template, read as repeated");
          }
          checkEntries(element, elements.children(), at, problems);
        }
      } else if (entry instanceof Element<?, ?> single) {
        ElementSchemaArtifact element = schema.elementSchemas().get(single.field());
        if (element == null) {
          problems.add(at + ": no such element");
        } else {
          if (element.isMultiple()) {
            problems.add(at + ": repeated in the template, read once");
          }
          checkEntries(element, single.children(), at, problems);
        }
      }
    }
  }

  private static void checkChildren(ParentSchemaArtifact schema, List<? extends Entry<?>> entries, String path,
                                    List<String> problems) {
    Set<String> mapped = new LinkedHashSet<>();
    for (Entry<?> entry : entries) {
      mapped.add(entry.field());
    }
    for (String child : schema.getChildSchemas().keySet()) {
      if (!schema.isStaticField(child) && !mapped.contains(child)) {
        problems.add(path + "/" + child + ": in the template, not in the table");
      }
    }
    for (Entry<?> entry : entries) {
      if (entry instanceof Elements<?, ?> elements && schema.elementSchemas().containsKey(elements.field())) {
        checkChildren(schema.elementSchemas().get(elements.field()), elements.children(),
            path + "/" + elements.field(), problems);
      } else if (entry instanceof Element<?, ?> single && schema.elementSchemas().containsKey(single.field())) {
        checkChildren(schema.elementSchemas().get(single.field()), single.children(),
            path + "/" + single.field(), problems);
      }
    }
  }

  private static void collectBranches(ParentSchemaArtifact schema, List<BranchValueConstraint> branches) {
    for (FieldSchemaArtifact field : schema.fieldSchemas().values()) {
      field.valueConstraints()
          .filter(ControlledTermValueConstraints.class::isInstance)
          .map(ControlledTermValueConstraints.class::cast)
          .ifPresent(constraints -> branches.addAll(constraints.branches()));
    }
    for (ElementSchemaArtifact element : schema.elementSchemas().values()) {
      collectBranches(element, branches);
    }
  }
}
