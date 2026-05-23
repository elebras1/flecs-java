package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface RowCallback<V extends ComponentView> {
    void accept(V view, int row);
}
