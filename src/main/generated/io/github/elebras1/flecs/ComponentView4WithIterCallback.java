package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView4WithIterCallback<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView, VD extends ComponentView> {
    void accept(Iter iter, int index, VA componentViewA, VB componentViewB, VC componentViewC, VD componentViewD);
}
