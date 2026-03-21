package com.github.elebras1.flecs.callback;

@FunctionalInterface
public interface CopyMoveCallback<T> {
    void invoke(T[] dst, T[] src, int count);
}