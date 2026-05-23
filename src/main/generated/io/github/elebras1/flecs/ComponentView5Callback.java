package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView5Callback<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView, VD extends ComponentView, VE extends ComponentView> {
    void accept(VA componentViewA, VB componentViewB, VC componentViewC, VD componentViewD, VE componentViewE);
}
