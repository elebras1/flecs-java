package com.github.elebras1.flecs;

@FunctionalInterface
public interface Component2Callback<A, B> {
    void accept(A componentA, B componentB);
}
