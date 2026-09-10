package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component3WithIterCallback<A, B, C> {
    void accept(Iter iter, int index, A componentA, B componentB, C componentC);
}
