package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.Flecs;

/**
 * Demonstrates setting a target FPS. The system prints the delta time for each
 * frame. It does not query components, but is still ran once per frame.
 */
public class TargetFps {

    static void main(String[] args) {
        try (World world = new World()) {

            // Create a system that prints delta_time.
            world.system("DeltaTime")
                    .kind(Flecs.OnUpdate)
                    .run(it -> {
                        System.out.println("delta_time: " + it.deltaTime());
                    });

            // Set target FPS to 1 frame per second.
            world.setTargetFps(1);

            // Run 5 frames.
            for (int i = 0; i < 5; i++) {
                world.progress();
            }

        }
    }

    // Output:
    // delta_time: ...
    // delta_time: ...
    // ...
}
