package org.metadatacenter.cedar.bridge.resource;

public class CheckValueRange {
  private static final float upperBoundLongitude = 180;
  private static final float lowerBoundLongitude = -180;
  private static final float upperBoundLatitude = 90;
  private static final float lowerBoundLatitude = -90;

  public static Boolean longitudeOutOfBound(Float value){
    boolean outOfBound = false;
    if (value != null) {
      if (value>upperBoundLongitude || value<lowerBoundLongitude){
        outOfBound = true;
      }
    }
    return outOfBound;
  }

  public static Boolean latitudeOutOfBound(Float value){
    boolean outOfBound = false;
    if (value != null) {
      if (value>upperBoundLatitude || value<lowerBoundLatitude){
        outOfBound = true;
      }
    }
    return outOfBound;
  }
}
