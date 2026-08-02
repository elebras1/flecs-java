package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView2Predicate<VA extends ComponentView, VB extends ComponentView> {
    boolean test(VA componentViewA, VB componentViewB);
}
