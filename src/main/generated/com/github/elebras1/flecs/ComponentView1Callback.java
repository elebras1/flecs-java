package com.github.elebras1.flecs;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface ComponentView1Callback<VA extends ComponentView> {
  void accept(VA componentViewA);
}
