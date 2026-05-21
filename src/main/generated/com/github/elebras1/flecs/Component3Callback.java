package com.github.elebras1.flecs;

@FunctionalInterface
public interface Component3Callback<A, B, C> {
    void accept(A componentA, B componentB, C componentC);
}
