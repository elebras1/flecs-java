package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView3WithIterCallback<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView> {
    void accept(Iter iter, int index, VA componentViewA, VB componentViewB, VC componentViewC);
}
