package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface ComponentView12WithEntityCallback<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView, VD extends ComponentView, VE extends ComponentView, VF extends ComponentView, VG extends ComponentView, VH extends ComponentView, VI extends ComponentView, VJ extends ComponentView, VK extends ComponentView, VL extends ComponentView> {
  void accept(long entityId, VA componentViewA, VB componentViewB, VC componentViewC,
      VD componentViewD, VE componentViewE, VF componentViewF, VG componentViewG, VH componentViewH,
      VI componentViewI, VJ componentViewJ, VK componentViewK, VL componentViewL);
}
