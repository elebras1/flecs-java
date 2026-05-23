package io.github.elebras1.flecs.examples.components;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedString;

@Component
public record Label(@FixedString(length = 32) String label) {
}
