package com.github.elebras1.flecs.callback;

@FunctionalInterface
public interface XtorCallback<T> {
    void invoke(T[] components, int count);
}