package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.Iter;

@FunctionalInterface
public interface IterHookContextCallback<T> {
    void invoke(Iter it, T[] components);
}
