package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component3Predicate<A, B, C> {
    boolean test(A componentA, B componentB, C componentC);
}
