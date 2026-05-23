package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component1WithEntityCallback<A> {
    void accept(long entityId, A componentA);
}
