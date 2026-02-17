package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedString;

@Component
public record Label(@FixedString(size = 32) String label) {
}
