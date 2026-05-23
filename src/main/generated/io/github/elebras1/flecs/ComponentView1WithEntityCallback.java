package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView1WithEntityCallback<VA extends ComponentView> {
    void accept(long entityId, VA componentViewA);
}
