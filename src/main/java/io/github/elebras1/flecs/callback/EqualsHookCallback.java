package io.github.elebras1.flecs.callback;

@FunctionalInterface
public interface EqualsHookCallback<T> {
    boolean invoke(T a, T b);
}
