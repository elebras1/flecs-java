package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.DummyTag;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates how to print the delta time. This system does not query for any
 * components, which means it would not normally match any entities. Because the
 * Java binding currently requires systems to have at least one matching term
 * to be scheduled, a dummy DummyTag term and entity are added.
 */
public class DeltaTime {

    static void main(String[] args) throws InterruptedException {
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
