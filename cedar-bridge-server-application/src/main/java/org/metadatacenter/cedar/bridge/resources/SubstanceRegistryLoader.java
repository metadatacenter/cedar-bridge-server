package org.metadatacenter.cedar.bridge.resources;

import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.Executors;

public class SubstanceRegistryLoader implements Managed {

  private final SubstanceRegistry sustanceRegistry;

  public SubstanceRegistryLoader(SubstanceRegistry sustanceRegistry) {
    this.sustanceRegistry = sustanceRegistry;
  }

  @Override
  public void start() {
    Executors.newSingleThreadExecutor().submit(() -> {
      try {
        sustanceRegistry.loadSubstances();
      } catch (Exception e) {
        throw new RuntimeException("Error loading substances: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public void stop() {
    this.sustanceRegistry.clearSubstances();
  }
}