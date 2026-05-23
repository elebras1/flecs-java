package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component4Callback<A, B, C, D> {
    void accept(A componentA, B componentB, C componentC, D componentD);
}
