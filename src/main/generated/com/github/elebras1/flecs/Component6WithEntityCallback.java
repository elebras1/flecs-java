package com.github.elebras1.flecs;

@FunctionalInterface
public interface Component6WithEntityCallback<A, B, C, D, E, F> {
    void accept(long entityId, A componentA, B componentB, C componentC, D componentD, E componentE, F componentF);
}
