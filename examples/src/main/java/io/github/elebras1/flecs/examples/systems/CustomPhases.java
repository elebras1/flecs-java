package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.Flecs;

/**
 * Demonstrates how to use custom phases for systems. The default pipeline will
 * automatically run systems for custom phases as long as they have the
 * Flecs.Phase tag.
 */
public class CustomPhases {

    static void main(String[] args) {
        try (World world = new World()) {

            // Create two custom phases that branch off of EcsOnUpdate. Note that
            // the phases have the Phase tag, which is necessary for the builtin
            // pipeline to discover which systems it should run.
            Entity physics = world.obtainEntity(world.entity("Physics"));
            physics.add(Flecs.Phase);
            physics.dependsOn(Flecs.OnUpdate);

            Entity collisions = world.obtainEntity(world.entity("Collisions"));
            collisions.add(Flecs.Phase);
            collisions.dependsOn(physics);

            // Create 3 dummy systems.
            world.system("CollisionSystem")
                    .kind(collisions.id())
                    .run(it -> System.out.println("system CollisionSystem"));

            world.system("PhysicsSystem")
                    .kind(physics.id())
                    .run(it -> System.out.println("system PhysicsSystem"));

            world.system("GameSystem")
                    .kind(Flecs.OnUpdate)
                    .run(it -> System.out.println("system GameSystem"));

            // Run the pipeline once.
            world.progress(0.016f);

        }
    }

    // Output:
    // system GameSystem
    // system PhysicsSystem
    // system CollisionSystem
}
