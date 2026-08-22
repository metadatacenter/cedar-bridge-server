package org.metadatacenter.cedar.bridge.resource.datacite;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.CreatorElement;
import static org.metadatacenter.cedar.bridge.resource.datacite.Cedar.MetadataInstance.TitleElement;

/**
 * The emptiness rules that decide what reaches DataCite.
 * <p>
 * A DOI form arrives with its optional sections present but blank, and these predicates decide
 * which of them to drop. They matter in both directions: calling a populated section empty loses
 * what the user entered, and calling a blank section populated sends DataCite a record full of
 * nulls. Neither shows up as an error - the DOI is just wrong.
 */
class CheckEmptyListTest {

  private static TitleElement title(String value, String typeLabel) {
    return new TitleElement("id", new TitleElement.TitleField2(value),
        typeLabel == null ? null : new TitleElement.TitleTypeField("type-id", typeLabel));
  }

  private static CreatorElement creator(String name, CreatorElement.AffiliationElementList affiliations) {
    return new CreatorElement("id", new CreatorElement.CreatorNameField(name),
        null, null, null, affiliations, null);
  }

  private static CreatorElement.AffiliationElementList affiliations(String name) {
    return new CreatorElement.AffiliationElementList(List.of(
        new CreatorElement.AffiliationElement("id",
            new CreatorElement.AffiliationElement.NameField(name), null, null, null)));
  }

  // emptyValueList - the generic one, and the only rule that looks at every element

  @Test
  void aValueListIsEmptyWhenEveryValueIsBlank() {
    assertTrue(CheckEmptyList.emptyValueList(List.of(
        new TitleElement.TitleField2(null),
        new TitleElement.TitleField2(""))));
  }

  @Test
  void aValueListIsNotEmptyWhenAnyValueIsPopulated() {
    assertTrue(CheckEmptyList.emptyValueList(List.of(new TitleElement.TitleField2(""))));
    assertFalse(CheckEmptyList.emptyValueList(List.of(new TitleElement.TitleField2("a title"))));
  }

  /**
   * The populated value is second, so this fails if the loop ever returns on the first blank
   * element instead of scanning the whole list.
   */
  @Test
  void aValueListIsScannedToTheEnd() {
    assertFalse(CheckEmptyList.emptyValueList(List.of(
        new TitleElement.TitleField2(""),
        new TitleElement.TitleField2("a title"))));
  }

  // emptyTitleList - representative of the size-one rules

  @Test
  void aSingleBlankTitleIsEmpty() {
    assertTrue(CheckEmptyList.emptyTitleList(List.of(title(null, null))));
    assertTrue(CheckEmptyList.emptyTitleList(List.of(title("", null))));
  }

  @Test
  void aPopulatedTitleIsNotEmpty() {
    assertFalse(CheckEmptyList.emptyTitleList(List.of(title("A title", null))));
  }

  /** The type alone counts as content: a title typed but not yet written is not nothing. */
  @Test
  void aTitleWithOnlyATypeIsNotEmpty() {
    assertFalse(CheckEmptyList.emptyTitleList(List.of(title(null, "Subtitle"))));
  }

  /**
   * The rule is "exactly one blank entry", not "every entry is blank". Two blank titles are treated
   * as populated and forwarded. Pinned because it is surprising, and because the two readings
   * diverge only on this input.
   */
  @Test
  void twoBlankTitlesAreNotEmpty() {
    assertFalse(CheckEmptyList.emptyTitleList(List.of(title(null, null), title(null, null))),
        "the size-one rule means a second blank entry makes the list non-empty");
  }

  // emptyCreatorList - the nested case: a creator is only empty if its sub-lists are too

  @Test
  void aSingleBlankCreatorIsEmpty() {
    assertTrue(CheckEmptyList.emptyCreatorList(List.of(creator(null, null))));
  }

  @Test
  void aNamedCreatorIsNotEmpty() {
    assertFalse(CheckEmptyList.emptyCreatorList(List.of(creator("Ada Lovelace", null))));
  }

  /**
   * The recursion that matters: the creator's own fields are all blank, and only the nested
   * affiliation carries anything. Dropping this record would silently lose the affiliation.
   */
  @Test
  void aCreatorWhoseOnlyContentIsAnAffiliationIsNotEmpty() {
    assertFalse(CheckEmptyList.emptyCreatorList(List.of(creator(null, affiliations("Stanford")))),
        "a populated affiliation should keep the creator");
  }

  @Test
  void aCreatorWithOnlyBlankAffiliationsIsEmpty() {
    assertTrue(CheckEmptyList.emptyCreatorList(List.of(creator(null, affiliations(null)))));
  }

  // emptyAffiliationList - shared by creators and contributors, dispatched by type

  @Test
  void aBlankAffiliationIsEmptyAndAPopulatedOneIsNot() {
    assertTrue(CheckEmptyList.emptyAffiliationList(affiliations(null).affiliationList()));
    assertFalse(CheckEmptyList.emptyAffiliationList(affiliations("Stanford").affiliationList()));
  }

  /**
   * Recorded rather than endorsed: the method reads its fields through an instanceof chain over the
   * creator and contributor affiliation types, and anything else falls through with every field
   * left null - so an unrecognized type is reported empty and its content dropped. A third
   * affiliation type would need a branch here, and this test is what would notice.
   */
  @Test
  void anUnrecognizedAffiliationTypeFallsThroughAsEmpty() {
    assertTrue(CheckEmptyList.emptyAffiliationList(List.of("not an affiliation")),
        "the instanceof chain has no default, so an unknown type reads as empty");
  }
}
