package com.github.elebras1.flecs.callback;

@FunctionalInterface
public interface ComparatorComponent<T> {
    int compare(T componentA, T componentB);
}
