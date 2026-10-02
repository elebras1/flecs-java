package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.Flecs;

public class StartupSystem {

    static void main(String[] args) {
        try (World world = new World()) {

            // Startup systems run once during the first frame.
            world.system("Startup")
                    .kind(Flecs.OnStart)
                    .iter(it -> System.out.println("Startup"));

            // Regular systems run every frame.
            world.system("Update")
                    .kind(Flecs.OnUpdate)
                    .iter(it -> System.out.println("Update"));

            // First frame: runs both Startup and Update.
            world.progress(0.016f);

            // Second frame: only Update runs.
            world.progress(0.016f);

        }
    }

    // Output:
    // Startup
    // Update
    // Update
}
