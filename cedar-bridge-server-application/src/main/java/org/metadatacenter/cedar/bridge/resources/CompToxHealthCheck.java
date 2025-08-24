package org.metadatacenter.cedar.bridge.resources;
import com.codahale.metrics.health.HealthCheck;

public class CompToxHealthCheck extends HealthCheck {
  private final SubstanceRegistry registry;

  public CompToxHealthCheck(SubstanceRegistry registry) { this.registry = registry; }

  @Override protected Result check() {
    return registry.isLoaded()
        ? Result.healthy()
        : Result.unhealthy("CompTox registry not loaded yet");
  }
}
