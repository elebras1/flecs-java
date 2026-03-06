package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface Component7WithEntityCallback<A, B, C, D, E, F, G> {
  void accept(long entityId, A componentA, B componentB, C componentC, D componentD, E componentE,
      F componentF, G componentG);
}
