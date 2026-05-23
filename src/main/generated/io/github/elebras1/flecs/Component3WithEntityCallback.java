package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component3WithEntityCallback<A, B, C> {
    void accept(long entityId, A componentA, B componentB, C componentC);
}
