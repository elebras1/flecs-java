package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Border(@FixedArray(length = 2000) int[] xPixels, @FixedArray(length = 2000) int[] yPixels) {
}
