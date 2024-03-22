package org.metadatacenter.cedar.bridge.resource.datacite;

public class CheckValueRange {
  private static final float upperBoundLongitude = 180;
  private static final float lowerBoundLongitude = -180;
  private static final float upperBoundLatitude = 90;
  private static final float lowerBoundLatitude = -90;

  public static Boolean longitudeOutOfBound(String value) {
    boolean outOfBound = false;
    if (value != null) {
      float floatValue = Float.parseFloat(value);
      if (floatValue > upperBoundLongitude || floatValue < lowerBoundLongitude) {
        outOfBound = true;
      }
    }
    return outOfBound;
  }

  public static Boolean latitudeOutOfBound(String value) {
    boolean outOfBound = false;
    if (value != null) {
      float floatValue = Float.parseFloat(value);
      if (floatValue > upperBoundLatitude || floatValue < lowerBoundLatitude) {
        outOfBound = true;
      }
    }
    return outOfBound;
  }
}
