package io.github.elebras1.flecs.examples.reflection;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Palette;

import java.util.Arrays;

public class FixedArrayExample {

    static void main(String[] args) {
        try (World world = new World()) {
            world.component(Palette.class);

            // rgb reserves exactly 3 floats.
            Entity sky = world.obtainEntity(world.entity("Sky"))
                    .set(new Palette(new float[] { 0.25f, 0.5f, 1.0f }));

            // Reads always return an array with the declared length.
            System.out.println("rgb: " + Arrays.toString(sky.get(Palette.class).rgb()));

            // Reflection serializes the inline array as a JSON array.
            System.out.println("json: " + sky.toJson(Palette.class));
        }
    }

    // Output:
    // rgb: [0.25, 0.5, 1.0]
    // json: {"rgb":[0.25, 0.5, 1]}
}
