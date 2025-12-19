package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record Ideology(int color, byte factionDriftingSpeed, short stabilityIndex) {
}

