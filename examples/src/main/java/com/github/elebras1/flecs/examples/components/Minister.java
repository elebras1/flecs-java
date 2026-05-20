package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedString;

@Component
public value record Minister(String name, @FixedString(length = 128) String imageFileName, float loyalty, int startDate, int deathDate) {
}
