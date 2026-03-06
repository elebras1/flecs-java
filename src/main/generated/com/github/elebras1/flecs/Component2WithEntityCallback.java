package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface Component2WithEntityCallback<A, B> {
  void accept(long entityId, A componentA, B componentB);
}
