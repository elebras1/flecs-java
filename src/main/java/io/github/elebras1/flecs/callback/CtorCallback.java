package io.github.elebras1.flecs.callback;

@FunctionalInterface
public interface CtorCallback<T> {
    T[] invoke(int count);
}
