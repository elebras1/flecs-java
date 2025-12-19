package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedString;

@Component
public record Minister(String name, @FixedString(size = 128) String imageFileName, float loyalty, int startDate, int deathDate) {
}
