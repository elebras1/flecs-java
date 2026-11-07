package io.github.elebras1.flecs.callback;

@FunctionalInterface
public interface CompareHookCallback<T> {
    int invoke(T a, T b);
}
