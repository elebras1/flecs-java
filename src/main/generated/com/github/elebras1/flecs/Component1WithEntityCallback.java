package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface Component1WithEntityCallback<A> {
  void accept(long entityId, A componentA);
}
