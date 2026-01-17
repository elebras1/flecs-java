package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Inventory(@FixedArray(length = 10) int[] elements) {
}
