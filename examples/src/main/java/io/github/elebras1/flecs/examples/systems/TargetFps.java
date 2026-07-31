package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.DummyTag;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates setting a target FPS. The system prints the delta time for each
 * frame. Because it does not query components, a dummy DummyTag term
 * is used to schedule it.
 */
public class TargetFps {

    static void main(String[] args) {
        World world = new World();
        world.component(DummyTag.class);

        // Dummy entity so the no-term system is scheduled.
        world.obtainEntity(world.entity("Dummy")).add(DummyTag.class);

        // Create a system that prints delta_time.
        world.system("DeltaTime")
                .kind(Flecs.OnUpdate)
                .with(DummyTag.class)
                .iter(it -> {
                    System.out.println("delta_time: " + it.deltaTime());
                });

        // Set target FPS to 1 frame per second.
        world.setTargetFps(1);

        // Run 5 frames.
        for (int i = 0; i < 5; i++) {
            world.progress();
        }

        world.destroy();
    }

    // Output:
    // delta_time: ...
    // delta_time: ...
    // ...
}
