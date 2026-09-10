package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView2WithIterCallback<VA extends ComponentView, VB extends ComponentView> {
    void accept(Iter iter, int index, VA componentViewA, VB componentViewB);
}
