package org.metadatacenter.cedar.bridge.resources.doi;

import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.DataCiteConfig;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * How this deployment reaches DataCite, read once from the configuration.
 *
 * <p>The repository credential is kept only as the header DataCite expects, and {@link #toString()}
 * leaves it out, so logging the settings cannot disclose it.
 */
final class DataCiteSettings {

  private final boolean enabled;
  private final String endpointUrl;
  private final String repositoryId;
  private final String authorization;
  private final String templateId;
  private final String prefix;

  DataCiteSettings(boolean enabled, String endpointUrl, String repositoryId, String password, String templateId,
                   String prefix) {
    this.enabled = enabled;
    this.endpointUrl = endpointUrl;
    this.repositoryId = repositoryId;
    this.authorization = "Basic " + Base64.getEncoder().encodeToString(
        (repositoryId + ":" + password).getBytes(StandardCharsets.UTF_8));
    this.templateId = templateId;
    this.prefix = prefix;
  }

  static DataCiteSettings from(CedarConfig cedarConfig) {
    DataCiteConfig dataCite = cedarConfig.getBridgeConfig().getDataCite();
    return new DataCiteSettings(dataCite.isEnabled(), dataCite.getEndpointUrl(), dataCite.getRepositoryId(),
        dataCite.getPassword(), dataCite.getTemplateId(), dataCite.getPrefix());
  }

  /** Whether DataCite integration is switched on. Every DOI route refuses its request when it is not. */
  boolean enabled() {
    return enabled;
  }

  /** DataCite's collection of DOIs, such as {@code https://api.datacite.org/dois}. */
  String endpointUrl() {
    return endpointUrl;
  }

  /** The DataCite repository CEDAR registers its DOIs in. */
  String repositoryId() {
    return repositoryId;
  }

  /** The Authorization header that carries the repository credential. */
  String authorization() {
    return authorization;
  }

  /** The identifier of the CEDAR template the DOI form is an instance of. */
  String templateId() {
    return templateId;
  }

  /** The DOI prefix the repository registers under. */
  String prefix() {
    return prefix;
  }

  @Override
  public String toString() {
    return "DataCiteSettings[enabled=" + enabled + ", endpointUrl=" + endpointUrl + ", repositoryId=" + repositoryId
        + ", templateId=" + templateId + ", prefix=" + prefix + "]";
  }
}
