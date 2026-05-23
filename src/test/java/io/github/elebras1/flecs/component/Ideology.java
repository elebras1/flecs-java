package io.github.elebras1.flecs.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record Ideology(int color, int factionDriftingSpeed, int stabilityIndex) {
}

