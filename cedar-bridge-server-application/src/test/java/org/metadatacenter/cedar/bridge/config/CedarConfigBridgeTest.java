package org.metadatacenter.cedar.bridge.config;

import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.DataCiteConfig;
import org.metadatacenter.config.ExternalAuthoritiesConfig;
import org.metadatacenter.model.SystemComponent;
import org.metadatacenter.util.test.AbstractCedarConfigTest;

public class CedarConfigBridgeTest extends AbstractCedarConfigTest {

  @Override
  protected SystemComponent getSystemComponent() {
    return SystemComponent.SERVER_BRIDGE;
  }

  /**
   * The bridge server is the only component granted the DataCite credentials and the external
   * authority prefixes, and it is the only one that calls those services. Every other component
   * loads both sections with their placeholders intact.
   */
  @Override
  protected void assertServerSpecificConfig(CedarConfig config) {
    DataCiteConfig dataCite = config.getBridgeConfig().getDataCite();
    assertResolved("bridge.dataCite.repositoryId", dataCite.getRepositoryId());
    assertResolved("bridge.dataCite.prefix", dataCite.getPrefix());
    assertResolved("bridge.dataCite.endpointUrl", dataCite.getEndpointUrl());
    assertResolved("bridge.dataCite.templateId", dataCite.getTemplateId());

    ExternalAuthoritiesConfig authorities = config.getExternalAuthorities();
    assertResolved("externalAuthorities.ror.apiPrefix", authorities.getRor().getApiPrefix());
    assertResolved("externalAuthorities.orcid.apiPrefix", authorities.getOrcid().getApiPrefix());
    assertResolved("externalAuthorities.orcid.tokenPrefix", authorities.getOrcid().getTokenPrefix());
    assertResolved("externalAuthorities.epaCompTox.apiPrefix", authorities.getEpaCompTox().getApiPrefix());
  }

}
