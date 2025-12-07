package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class PipelineExample {

    public static void main(String[] args) {
        try (Flecs world = new Flecs()) {
            System.out.println("=== Pipeline Example ===\n");

            world.component(Position.class);

            Entity e = world.obtainEntity(world.entity("Entity"));
            e.set(new Position(0, 0));

            System.out.println("--- Creating custom phases ---");
            long physics = world.entity("Physics");
            long render = world.entity("Render");

            System.out.println("Physics phase ID: " + physics);
            System.out.println("Render phase ID: " + render);

            System.out.println("\n--- Creating custom pipeline ---");
            Pipeline customPipeline = world.pipeline("CustomPipeline")
                    .with(physics)
                    .with(render)
                    .build();

            System.out.println("Pipeline ID: " + customPipeline.id());
            System.out.println("Pipeline entity: " + customPipeline.entity());

            System.out.println("\n--- Creating systems for phases ---");
            world.system("PhysicsSystem")
                    .kind(physics)
                    .with(Position.class)
                    .iter(it -> System.out.println("  [Physics] Processing " + it.count() + " entities"));

            world.system("RenderSystem")
                    .kind(render)
                    .with(Position.class)
                    .iter(it -> System.out.println("  [Render] Drawing " + it.count() + " entities"));

            System.out.println("\n--- Running with custom pipeline ---");
            world.setPipeline(customPipeline.id());

            for (int i = 0; i < 3; i++) {
                System.out.println("Frame " + (i + 1) + ":");
                world.progress(0.016f);
            }

            System.out.println("\n=== Example completed! ===");
        }
    }
}
