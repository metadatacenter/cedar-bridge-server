package org.metadatacenter.cedar.bridge.resources.doi;

import java.util.List;
import java.util.Objects;

/**
 * CEDAR's record of the DOI registered, or being registered, for one source artifact.
 *
 * @param sourceArtifactId the full identifier of the artifact the DOI is for
 * @param doi              the DOI without a resolver prefix, such as {@code 10.5072/abcd-1234}
 * @param state            how far its registration has gone
 * @param dataCiteResponse DataCite's answer to the request that published the DOI, as it arrived, or
 *                         null before the DOI is published
 * @param supersededDois   the DOIs recorded for the artifact earlier that DataCite turned out not to
 *                         hold for it, oldest first
 */
public record DoiRegistration(String sourceArtifactId, String doi, DoiRegistrationState state,
                              String dataCiteResponse, List<String> supersededDois) {

  public DoiRegistration {
    Objects.requireNonNull(sourceArtifactId, "sourceArtifactId");
    Objects.requireNonNull(doi, "doi");
    Objects.requireNonNull(state, "state");
    supersededDois = supersededDois == null ? List.of() : List.copyOf(supersededDois);
  }
}
