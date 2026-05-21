package com.github.elebras1.flecs;

import com.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView2WithEntityCallback<VA extends ComponentView, VB extends ComponentView> {
    void accept(long entityId, VA componentViewA, VB componentViewB);
}
