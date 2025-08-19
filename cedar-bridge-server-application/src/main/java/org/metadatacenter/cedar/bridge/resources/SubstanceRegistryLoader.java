package org.metadatacenter.cedar.bridge.resources;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SubstanceRegistryLoader implements Managed {
  private static final Logger log = LoggerFactory.getLogger(SubstanceRegistryLoader.class);

  private final SubstanceRegistry substanceRegistry;

  // single-threaded scheduler for retry/backoff
  private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread t = new Thread(r, "comp-tox-loader");
    t.setDaemon(true);
    return t;
  });

  // prevent overlapping loads
  private final AtomicBoolean loading = new AtomicBoolean(false);
  private final AtomicInteger attempts = new AtomicInteger(0);

  // backoff bounds
  private static final long INITIAL_BACKOFF_MS = 1_000L;      // 1s
  private static final long MAX_BACKOFF_MS     = 10 * 60_000L; // 10m

  public SubstanceRegistryLoader(SubstanceRegistry substanceRegistry) {
    this.substanceRegistry = substanceRegistry;
  }

  @Override
  public void start() {
    // kick off immediately
    schedule(0);
  }

  @Override
  public void stop() {
    try {
      exec.shutdownNow();
    } finally {
      substanceRegistry.clearSubstances();
    }
  }

  private void schedule(long delayMs) {
    exec.schedule(this::loadOnce, delayMs, TimeUnit.MILLISECONDS);
  }

  private void loadOnce() {
    if (substanceRegistry.isLoaded()) {
      return;
    }
    // avoid overlapping loads
    if (!loading.compareAndSet(false, true)) {
      return;
    }

    try {
      int n = attempts.get() + 1;
      log.info("CompTox: starting load attempt {}", n);
      substanceRegistry.loadSubstances();
      attempts.set(0);
      log.info("CompTox: load complete ({} substances)", substanceRegistry.getSubstanceInfoByDtxsid().size());
    } catch (Exception e) {
      int n = attempts.incrementAndGet();
      long backoff = Math.min(MAX_BACKOFF_MS, INITIAL_BACKOFF_MS << Math.min(n - 1, 16));
      log.warn("CompTox: load attempt {} failed: {}. Retrying in {}s",
          n, e.getMessage(), backoff / 1000, e);
      schedule(backoff);
    } finally {
      loading.set(false);
    }
  }
}
