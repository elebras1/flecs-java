package com.github.elebras1.flecs.callback;

import com.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComparatorComponentView {
    <VA extends ComponentView, VB extends ComponentView> int compare(VA componentViewA, VB componentViewB);
}
