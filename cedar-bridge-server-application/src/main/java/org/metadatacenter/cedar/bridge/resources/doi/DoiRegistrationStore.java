package org.metadatacenter.cedar.bridge.resources.doi;

import java.util.Optional;

/**
 * The durable record of each artifact's DOI, which associates a DOI with its source artifact directly.
 *
 * <p>An artifact has at most one record. The record is written before DataCite is asked to register
 * anything and before the DOI is written to the artifact, so a request that fails at either step, or a
 * server that stops between them, leaves the DOI recorded for whatever request comes next.
 */
public interface DoiRegistrationStore {

  /** The record for the artifact, if it has one. */
  Optional<DoiRegistration> find(String sourceArtifactId);

  /**
   * Records the DOI for the artifact, in the given state, unless a DOI is already recorded for it.
   *
   * @return the record that holds afterwards: this one, or the one recorded first, which a request
   *     that loses a race receives and continues with
   */
  DoiRegistration claim(String sourceArtifactId, String doi, DoiRegistrationState state, String userId);

  /**
   * Moves the artifact's record of this DOI forward to the given state. A record already at or past
   * that state is left as it is, so a late or repeated request cannot move it back.
   *
   * @param dataCiteResponse DataCite's answer to the request that published the DOI, kept with a move
   *                         to {@link DoiRegistrationState#PUBLISHED}, or null to keep what is recorded
   * @return the record as it now stands
   * @throws IllegalStateException when no record for the artifact holds this DOI
   */
  DoiRegistration advance(String sourceArtifactId, String doi, DoiRegistrationState state, String dataCiteResponse);

  /**
   * Records that DataCite does not hold this DOI for the artifact, and reserves {@code replacement} in
   * its place. The replacement may be the same DOI, when DataCite has lost a draft, or another, when
   * the DOI turned out to be another artifact's.
   *
   * @return the record, reserved for the replacement
   * @throws IllegalStateException when no record for the artifact holds this DOI as a reservation or a
   *     draft. A published DOI is permanent and is never replaced.
   */
  DoiRegistration reserveInstead(String sourceArtifactId, String doi, String replacement);
}
