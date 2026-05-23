package io.github.elebras1.flecs.examples.components;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record Velocity(float dx, float dy) {
}

