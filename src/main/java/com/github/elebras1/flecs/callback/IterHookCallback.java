package com.github.elebras1.flecs.callback;

@FunctionalInterface
public interface IterHookCallback<T> {
    void invoke(T[] components);
}
