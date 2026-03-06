package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface Component3WithEntityCallback<A, B, C> {
  void accept(long entityId, A componentA, B componentB, C componentC);
}
