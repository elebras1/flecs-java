package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component4Predicate<A, B, C, D> {
    boolean test(A componentA, B componentB, C componentC, D componentD);
}
