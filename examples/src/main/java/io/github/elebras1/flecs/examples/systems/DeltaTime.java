package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.Flecs;

/**
 * Demonstrates how to print the delta time. This system does not query for any
 * components, but is still ran once for each call to progress.
 */
public class DeltaTime {

    static void main(String[] args) throws InterruptedException {
        World world = new World();

        // Create a system that prints delta_time.
        world.system("DeltaTime")
                .kind(Flecs.OnUpdate)
                .run(it -> {
                    System.out.println("delta_time: " + it.deltaTime());
                });

        // Call progress with 0.0f for the delta_time parameter. This will cause
        // progress to measure the time passed since the last frame.
        world.progress();

        // The following calls should print a delta_time of approximately 100ms.
        Thread.sleep(100);
        world.progress();

        Thread.sleep(100);
        world.progress();

        world.destroy();
    }

    // Output:
    // delta_time: 0.016666668
    // delta_time: 0.1...
    // delta_time: 0.1...
}
