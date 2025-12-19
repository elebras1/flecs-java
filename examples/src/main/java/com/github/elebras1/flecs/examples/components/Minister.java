package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record Minister(String name, String imageFileName, float loyalty, int startDate, int deathDate) {
}
