package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component5WithIterCallback<A, B, C, D, E> {
    void accept(Iter iter, int index, A componentA, B componentB, C componentC, D componentD, E componentE);
}
