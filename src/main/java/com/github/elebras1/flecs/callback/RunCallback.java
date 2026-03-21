package com.github.elebras1.flecs.callback;

import com.github.elebras1.flecs.Iter;

@FunctionalInterface
public interface RunCallback {
    void accept(Iter iter);
}
