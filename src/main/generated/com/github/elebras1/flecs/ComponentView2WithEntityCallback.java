package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface ComponentView2WithEntityCallback<VA extends ComponentView, VB extends ComponentView> {
  void accept(long entityId, VA componentViewA, VB componentViewB);
}
