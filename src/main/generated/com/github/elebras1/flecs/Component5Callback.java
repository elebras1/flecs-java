package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface Component5Callback<A, B, C, D, E> {
  void accept(A componentA, B componentB, C componentC, D componentD, E componentE);
}
