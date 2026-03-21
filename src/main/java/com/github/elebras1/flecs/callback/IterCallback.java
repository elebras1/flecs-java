package com.github.elebras1.flecs.callback;

import com.github.elebras1.flecs.Iter;

@FunctionalInterface
public interface IterCallback {
    void accept(Iter iter);
}
