package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface Component1Callback<A> {
  void accept(A componentA);
}
