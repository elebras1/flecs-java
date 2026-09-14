package io.github.elebras1.flecs.callback;

@FunctionalInterface
public interface DtorCallback<T> {
    void invoke(T[] components);
}
