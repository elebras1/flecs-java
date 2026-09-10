package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component2WithIterCallback<A, B> {
    void accept(Iter iter, int index, A componentA, B componentB);
}
