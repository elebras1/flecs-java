package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.Entity;

@FunctionalInterface
public interface ValidateHookCallback<T> {
    boolean invoke(Entity entity, T value);
}
