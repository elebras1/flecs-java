package com.github.elebras1.flecs.callback;

@FunctionalInterface
public interface EntityCallback {
    void accept(long entityId);
}
