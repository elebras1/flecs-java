package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.Iter;

@FunctionalInterface
public interface ReplaceHookContextCallback<T> {
    void invoke(Iter it, T[] previous, T[] current);
}
