package com.github.elebras1.flecs.callback;

@FunctionalInterface
public interface ReplaceHookCallback<T> {
    void invoke(T[] oldComponents, T[] newComponents);
}
