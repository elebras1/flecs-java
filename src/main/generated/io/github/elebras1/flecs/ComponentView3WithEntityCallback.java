package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView3WithEntityCallback<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView> {
    void accept(long entityId, VA componentViewA, VB componentViewB, VC componentViewC);
}
