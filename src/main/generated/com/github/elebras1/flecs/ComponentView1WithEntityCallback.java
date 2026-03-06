package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface ComponentView1WithEntityCallback<VA extends ComponentView> {
  void accept(long entityId, VA componentViewA);
}
