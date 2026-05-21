package com.github.elebras1.flecs;

import com.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView4Callback<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView, VD extends ComponentView> {
    void accept(VA componentViewA, VB componentViewB, VC componentViewC, VD componentViewD);
}
