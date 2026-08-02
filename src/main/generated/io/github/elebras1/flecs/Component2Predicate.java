package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component2Predicate<A, B> {
    boolean test(A componentA, B componentB);
}
