package com.github.elebras1.flecs.callback;

import com.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComparatorComponentView<V extends ComponentView> {
    int compare(V componentViewA, V componentViewB);
}
