package com.github.elebras1.flecs;

@FunctionalInterface
public interface Component4WithEntityCallback<A, B, C, D> {
    void accept(long entityId, A componentA, B componentB, C componentC, D componentD);
}
