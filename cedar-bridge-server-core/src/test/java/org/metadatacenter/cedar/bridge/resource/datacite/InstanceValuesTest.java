package org.metadatacenter.cedar.bridge.resource.datacite;

import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.ControlledTermFieldInstance;
import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.LinkFieldInstance;
import org.metadatacenter.artifacts.model.core.TextFieldInstance;
import org.metadatacenter.cedar.bridge.resource.datacite.DataCiteMapping.Part;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The emptiness rule that decides what reaches DataCite.
 * <p>
 * A DOI form arrives with its optional sections present but blank, and this rule decides which of
 * them are absent. It matters in both directions: calling a populated entry blank loses what the user
 * entered, and calling a blank one populated sends DataCite a record full of nulls. Neither shows up
 * as an error. The DOI is just wrong.
 */
class InstanceValuesTest {

  private static final URI ELEMENT_ID =
      URI.create("https://repo.metadatacenter.org/template-element-instances/7e0c6f0a-0a5d-4c55-9f1e-3c0b4a1b2c3d");

  private static FieldInstanceArtifact text(String value) {
    return TextFieldInstance.builder().withValue(value).build();
  }

  private static ElementInstanceArtifact element(String field, FieldInstanceArtifact value) {
    return ElementInstanceArtifact.builder().withJsonLdId(ELEMENT_ID).withSingleInstanceFieldInstance(field, value)
        .build();
  }

  @Test
  void aLiteralIsBlankWhenItHoldsNothingOrAnEmptyString() {
    assertTrue(InstanceValues.isBlank(TextFieldInstance.builder().build()));
    assertTrue(InstanceValues.isBlank(text("")));
    assertFalse(InstanceValues.isBlank(text("a title")));
    assertFalse(InstanceValues.isBlank(text(" ")), "a space is something the user typed");
  }

  @Test
  void aTermWithALabelOrAnIriHoldsAValue() {
    assertFalse(InstanceValues.isBlank(ControlledTermFieldInstance.builder().withLabel("Personal").build()));
    assertFalse(InstanceValues.isBlank(ControlledTermFieldInstance.builder()
        .withValue(URI.create("http://purl.org/datacite/v4.4/Other")).build()));
    assertTrue(InstanceValues.isBlank(ControlledTermFieldInstance.builder().build()));
  }

  @Test
  void aLinkHoldsAValue() {
    assertFalse(InstanceValues.isBlank(LinkFieldInstance.builder().withValue(URI.create("https://ror.org/")).build()));
    assertTrue(InstanceValues.isBlank(LinkFieldInstance.builder().build()));
  }

  /** An element's own identifier names the entry. It is not something the entry says. */
  @Test
  void anElementIsBlankWhenNothingInItHoldsAValue() {
    assertTrue(InstanceValues.isBlank(element("title", text(null))));
    assertTrue(InstanceValues.isBlank(element("title", text(""))));
    assertFalse(InstanceValues.isBlank(element("title", text("a title"))));
  }

  @Test
  void aValueAtAnyDepthMakesTheElementHoldOne() {
    ElementInstanceArtifact affiliation = element("name", text("Stanford University"));
    ElementInstanceArtifact creator = ElementInstanceArtifact.builder()
        .withSingleInstanceFieldInstance("creatorName", text(null))
        .withMultiInstanceElementInstances("affiliation", List.of(affiliation))
        .build();

    assertFalse(InstanceValues.isBlank(creator));
  }

  @Test
  void aRepeatedElementIsAbsentWhenEveryEntryIsBlankAndWhenItHasNone() {
    ElementInstanceArtifact blank = element("title", text(null));
    ElementInstanceArtifact filled = element("title", text("a title"));

    assertTrue(InstanceValues.allBlank(List.of()));
    assertTrue(InstanceValues.allBlank(List.of(blank, blank)));
    assertFalse(InstanceValues.allBlank(List.of(blank, filled)));
  }

  @Test
  void eachPartOfAValueIsReadWhereItIsCarried() {
    FieldInstanceArtifact term = ControlledTermFieldInstance.builder()
        .withValue(URI.create("http://purl.org/datacite/v4.4/Other")).withLabel("Other").build();

    assertEquals("Other", InstanceValues.content(term, Part.LABEL));
    assertEquals("http://purl.org/datacite/v4.4/Other", InstanceValues.content(term, Part.IRI));
    assertNull(InstanceValues.content(term, Part.VALUE));
    assertNull(InstanceValues.content(null, Part.VALUE), "a field the instance leaves out has no content");
  }
}
