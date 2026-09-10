package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component6WithIterCallback<A, B, C, D, E, F> {
    void accept(Iter iter, int index, A componentA, B componentB, C componentC, D componentD, E componentE, F componentF);
}
