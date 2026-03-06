package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface Component2Callback<A, B> {
  void accept(A componentA, B componentB);
}
