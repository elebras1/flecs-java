package io.github.elebras1.flecs.examples.components;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedString;

@Component
public record Minister(String name, @FixedString(length = 128) String imageFileName, float loyalty, int startDate, int deathDate) {
}
