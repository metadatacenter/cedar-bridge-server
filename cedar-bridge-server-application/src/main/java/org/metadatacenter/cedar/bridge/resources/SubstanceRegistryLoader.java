package org.metadatacenter.cedar.bridge.resources;

import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.Executors;

public class SubstanceRegistryLoader implements Managed {

  private final SubstanceRegistry substanceRegistry;

  public SubstanceRegistryLoader(SubstanceRegistry substanceRegistry) {
    this.substanceRegistry = substanceRegistry;
  }

  @Override
  public void start() {
    Executors.newSingleThreadExecutor().submit(() -> {
      try {
        substanceRegistry.loadSubstances();
      } catch (Exception e) {
        throw new RuntimeException("Error loading substances: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public void stop() {
    this.substanceRegistry.clearSubstances();
  }
}
