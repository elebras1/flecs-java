package io.github.elebras1.flecs.examples.components;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedString;

@Component
public record Nameplate(@FixedString(length = 64) String title, String subtitle) {
}
