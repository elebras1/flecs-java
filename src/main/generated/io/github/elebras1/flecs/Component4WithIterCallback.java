package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component4WithIterCallback<A, B, C, D> {
    void accept(Iter iter, int index, A componentA, B componentB, C componentC, D componentD);
}
