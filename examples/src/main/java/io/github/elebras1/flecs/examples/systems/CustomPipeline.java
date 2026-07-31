package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Pipeline;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.util.Flecs;

public class CustomPipeline {

    // Custom tag used to identify systems that belong to this pipeline.
    public static class Physics {
    }

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);

        Entity physics = world.obtainEntity(world.entity("Physics"));

        // Create a custom pipeline that matches systems tagged with Physics.
        long systemTag = Flecs.System;
        Pipeline customPipeline = world.pipeline("CustomPipeline")
                .with(systemTag)
                .with(physics)
                .build();

        // Configure the world to use the custom pipeline.
        world.setPipeline(customPipeline.id());

        // Create a system that uses the custom tag as phase.
        world.system("PhysicsSystem")
                .kind(physics.id())
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
