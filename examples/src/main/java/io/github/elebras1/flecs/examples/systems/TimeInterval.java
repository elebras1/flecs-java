package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.util.Flecs;

public class TimeInterval {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);

        // The Java binding currently requires systems to have at least one
        // matching term to be scheduled, so we provide a dummy Position.
        world.obtainEntity(world.entity("Dummy")).set(new Position(0, 0));

        // Tick runs every second.
        world.system("Tick")
                .kind(Flecs.OnUpdate)
                .interval(1.0f)
                .with(Position.class)
                .iter(it -> System.out.println("Tick"));

        // FastTick runs twice as often.
        world.system("FastTick")
                .kind(Flecs.OnUpdate)
                .interval(0.5f)
                .with(Position.class)
                .iter(it -> System.out.println("FastTick"));

        // Run the main loop at 60 FPS.
        world.setTargetFps(60);

        // Only run a few frames for the example.
        for (int i = 0; i < 120; i++) {
            world.progress(1.0f / 60.0f);
        }

        world.destroy();
    }

    // Output:
    // FastTick
    // FastTick
    // Tick
    // FastTick
    // FastTick
}
