package org.metadatacenter.cedar.bridge.resources.doi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The DOI records in memory, for tests that drive the workflow without a graph. The store contract
 * holds this one to the same behaviour as {@link Neo4jDoiRegistrationStore}.
 */
public final class InMemoryDoiRegistrationStore implements DoiRegistrationStore {

  private final Map<String, DoiRegistration> records = new ConcurrentHashMap<>();

  @Override
  public Optional<DoiRegistration> find(String sourceArtifactId) {
    return Optional.ofNullable(records.get(sourceArtifactId));
  }

  @Override
  public DoiRegistration claim(String sourceArtifactId, String doi, DoiRegistrationState state, String userId) {
    return records.computeIfAbsent(sourceArtifactId,
        id -> new DoiRegistration(id, doi, state, null, List.of()));
  }

  @Override
  public synchronized DoiRegistration advance(String sourceArtifactId, String doi, DoiRegistrationState state,
                                              String dataCiteResponse) {
    DoiRegistration current = holding(sourceArtifactId, doi);
    if (current.state().compareTo(state) >= 0) {
      return current;
    }
    DoiRegistration advanced = new DoiRegistration(sourceArtifactId, doi, state,
        dataCiteResponse == null ? current.dataCiteResponse() : dataCiteResponse, current.supersededDois());
    records.put(sourceArtifactId, advanced);
    return advanced;
  }

  @Override
  public synchronized DoiRegistration reserveInstead(String sourceArtifactId, String doi, String replacement) {
    DoiRegistration current = holding(sourceArtifactId, doi);
    if (current.state().isPublished()) {
      throw new IllegalStateException("No DOI record for " + sourceArtifactId + " holds " + doi
          + " as a reservation or a draft");
    }
    List<String> superseded = new ArrayList<>(current.supersededDois());
    if (!doi.equals(replacement)) {
      superseded.add(doi);
    }
    DoiRegistration reserved =
        new DoiRegistration(sourceArtifactId, replacement, DoiRegistrationState.RESERVED, null, superseded);
    records.put(sourceArtifactId, reserved);
    return reserved;
  }

  /** Records a DOI as a test needs it to stand, whatever was recorded before. */
  public void put(DoiRegistration registration) {
    records.put(registration.sourceArtifactId(), registration);
  }

  private DoiRegistration holding(String sourceArtifactId, String doi) {
    DoiRegistration current = records.get(sourceArtifactId);
    if (current == null || !current.doi().equals(doi)) {
      throw new IllegalStateException("No DOI record for " + sourceArtifactId + " holds " + doi);
    }
    return current;
  }
}
