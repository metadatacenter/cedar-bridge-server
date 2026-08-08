package org.metadatacenter.cedar.bridge.resource.datacite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The geographic bounds a DOI submission is checked against. Latitude and longitude have different
 * ranges, and the two methods are near-identical, which is exactly the shape that invites a
 * copy-paste error: the bounds pinned here are what stops a latitude being validated against the
 * longitude range.
 */
class CheckValueRangeTest {

  @ParameterizedTest
  @ValueSource(strings = {"0", "180", "-180", "179.9999", "-179.9999", "90", "-90"})
  void aLongitudeWithinBoundsIsAccepted(String longitude) {
    assertFalse(CheckValueRange.longitudeOutOfBound(longitude));
  }

  @ParameterizedTest
  @ValueSource(strings = {"180.0001", "-180.0001", "181", "-181", "360", "-360"})
  void aLongitudeOutsideBoundsIsRejected(String longitude) {
    assertTrue(CheckValueRange.longitudeOutOfBound(longitude));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "90", "-90", "89.9999", "-89.9999"})
  void aLatitudeWithinBoundsIsAccepted(String latitude) {
    assertFalse(CheckValueRange.latitudeOutOfBound(latitude));
  }

  @ParameterizedTest
  @ValueSource(strings = {"90.0001", "-90.0001", "91", "-91", "180", "-180"})
  void aLatitudeOutsideBoundsIsRejected(String latitude) {
    assertTrue(CheckValueRange.latitudeOutOfBound(latitude));
  }

  /**
   * The ranges must not be interchangeable. A value between 90 and 180 is a legal longitude and an
   * illegal latitude, so this fails if either method is checked against the other's bounds.
   */
  @Test
  void theTwoRangesAreNotInterchangeable() {
    assertFalse(CheckValueRange.longitudeOutOfBound("120"), "120 is a legal longitude");
    assertTrue(CheckValueRange.latitudeOutOfBound("120"), "120 is not a legal latitude");
  }

  /** An absent coordinate is not out of bounds - it is simply not given. */
  @Test
  void anAbsentCoordinateIsInBounds() {
    assertFalse(CheckValueRange.longitudeOutOfBound(null));
    assertFalse(CheckValueRange.latitudeOutOfBound(null));
  }

  /**
   * Recorded rather than endorsed: a non-numeric coordinate throws out of the parse rather than
   * being reported as out of bounds, so the caller sees a NumberFormatException, not a validation
   * failure. Pinning it means a change to that behaviour is a decision rather than an accident.
   */
  @Test
  void aNonNumericCoordinateThrowsRatherThanFailingValidation() {
    assertThrows(NumberFormatException.class, () -> CheckValueRange.longitudeOutOfBound("east"));
    assertThrows(NumberFormatException.class, () -> CheckValueRange.latitudeOutOfBound(""));
  }
}
