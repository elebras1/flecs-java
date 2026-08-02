package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView1Predicate<VA extends ComponentView> {
    boolean test(VA componentViewA);
}
