package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.Pipeline;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Physics;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.Flecs;

public class CustomPipeline {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        long physicsId = world.component(Physics.class);

        // Create a custom pipeline that matches systems tagged with Physics.
        Pipeline customPipeline = world.pipeline("CustomPipeline")
                .with(Flecs.System)
                .with(Physics.class)
                .build();

        // Configure the world to use the custom pipeline.
        world.setPipeline(customPipeline);

        // Create a system that uses the custom tag as phase.
        world.system("PhysicsSystem")
                .kind(physicsId)
                .with(Position.class)
                .iter(it -> System.out.println("[Physics] " + it.count() + " entities"));

        // Create an entity that matches the systems.
        world.obtainEntity(world.entity("Entity")).set(new Position(0, 0));

        // Run the pipeline.
        world.progress(0.016f);

        world.destroy();
    }

    // Output:
    // [Physics] 1 entities
}
