package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComparatorComponentView<V extends ComponentView> {
    int compare(V componentViewA, V componentViewB);
}
