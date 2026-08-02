package io.github.elebras1.flecs;

import io.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView8Predicate<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView, VD extends ComponentView, VE extends ComponentView, VF extends ComponentView, VG extends ComponentView, VH extends ComponentView> {
    boolean test(VA componentViewA, VB componentViewB, VC componentViewC, VD componentViewD, VE componentViewE, VF componentViewF, VG componentViewG, VH componentViewH);
}
