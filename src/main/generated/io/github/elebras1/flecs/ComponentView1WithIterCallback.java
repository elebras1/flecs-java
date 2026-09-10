package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView1WithIterCallback<VA extends ComponentView> {
    void accept(Iter iter, int index, VA componentViewA);
}
