package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.Iter;

@FunctionalInterface
public interface IterCallback {
    void accept(Iter iter);
}
