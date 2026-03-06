package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface ComponentView4Callback<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView, VD extends ComponentView> {
  void accept(VA componentViewA, VB componentViewB, VC componentViewC, VD componentViewD);
}
