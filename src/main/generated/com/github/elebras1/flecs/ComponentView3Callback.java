package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface ComponentView3Callback<VA extends ComponentView, VB extends ComponentView, VC extends ComponentView> {
  void accept(VA componentViewA, VB componentViewB, VC componentViewC);
}
