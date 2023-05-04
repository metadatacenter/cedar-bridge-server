package org.metadatacenter.cedar.bridge.health;

import com.codahale.metrics.health.HealthCheck;

public class BridgeServerHealthCheck extends HealthCheck {

  public BridgeServerHealthCheck() {
  }

  @Override
  protected Result check() throws Exception {
    if (2 * 2 == 5) {
      return Result.unhealthy("Unhealthy, because 2 * 2 == 5");
    }
    return Result.healthy();
  }
}
