package org.metadatacenter.cedar.bridge.resources.doi;

/**
 * How far the registration of one artifact's DOI has gone. The states are ordered, and a record only
 * moves forward through them, with one exception: a reservation or draft DataCite turns out not to
 * hold becomes a reservation again.
 */
public enum DoiRegistrationState {

  /**
   * CEDAR has chosen the DOI and recorded it for the artifact, and DataCite may or may not hold it.
   * Whether DataCite does is settled by reading the DOI there, which is what lets a request repeated
   * after a timeout tell whether the earlier attempt registered it.
   */
  RESERVED,

  /** DataCite holds the DOI as a draft, which can still change and resolves nowhere. */
  DRAFT,

  /**
   * DataCite holds the DOI as findable, which is permanent, and the artifact does not yet carry it.
   * Recording it on the artifact is all that remains, and needs nothing more of DataCite.
   */
  PUBLISHED,

  /** The artifact carries the DOI. Nothing remains to be done. */
  ATTACHED;

  /** Whether DataCite holds the DOI as findable, so that it can no longer change or be replaced. */
  public boolean isPublished() {
    return this == PUBLISHED || this == ATTACHED;
  }
}
