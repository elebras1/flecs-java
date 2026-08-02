package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component5Predicate<A, B, C, D, E> {
    boolean test(A componentA, B componentB, C componentC, D componentD, E componentE);
}
