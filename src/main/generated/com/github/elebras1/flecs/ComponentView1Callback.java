package com.github.elebras1.flecs;

import com.github.elebras1.flecs.ComponentView;

@FunctionalInterface
public interface ComponentView1Callback<VA extends ComponentView> {
    void accept(VA componentViewA);
}
