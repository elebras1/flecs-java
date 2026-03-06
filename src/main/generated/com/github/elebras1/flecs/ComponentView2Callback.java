package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface ComponentView2Callback<VA extends ComponentView, VB extends ComponentView> {
  void accept(VA componentViewA, VB componentViewB);
}
