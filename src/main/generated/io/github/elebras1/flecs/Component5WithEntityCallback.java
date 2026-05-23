package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component5WithEntityCallback<A, B, C, D, E> {
    void accept(long entityId, A componentA, B componentB, C componentC, D componentD, E componentE);
}
