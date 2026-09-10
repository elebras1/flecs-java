package io.github.elebras1.flecs;

@FunctionalInterface
public interface Component1WithIterCallback<A> {
    void accept(Iter iter, int index, A componentA);
}
