package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView1Callback<VA extends ComponentView> {
    void accept(VA componentViewA);
}
