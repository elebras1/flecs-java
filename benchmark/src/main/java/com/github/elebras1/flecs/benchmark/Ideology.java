package com.github.elebras1.flecs.benchmark;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record Ideology(int color, int factionDriftingSpeed, int stabilityIndex) {
}

