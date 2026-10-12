package io.github.elebras1.flecs.examples.components;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Palette(@FixedArray(length = 3) float[] rgb) {
}
