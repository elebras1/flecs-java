package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface ComponentView8Callback<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView, VD extends ComponentView, VE extends ComponentView, VF extends ComponentView, VG extends ComponentView, VH extends ComponentView> {
  void accept(VA componentViewA, VB componentViewB, VC componentViewC, VD componentViewD,
      VE componentViewE, VF componentViewF, VG componentViewG, VH componentViewH);
}
