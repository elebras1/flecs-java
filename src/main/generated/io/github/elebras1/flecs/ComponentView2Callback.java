package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView2Callback<VA extends ComponentView, VB extends ComponentView> {
    void accept(VA componentViewA, VB componentViewB);
}
