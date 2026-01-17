package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.Position;

public class PipelineExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);

            Entity e = world.obtainEntity(world.entity("Entity"));
            e.set(new Position(0, 0));

            long physics = world.entity("Physics");
            long render = world.entity("Render");

            Pipeline customPipeline = world.pipeline("CustomPipeline").with(physics).with(render).build();

            world.system("PhysicsSystem").kind(physics).with(Position.class).iter(it -> System.out.println("[Physics] " + it.count() + " entities"));

            world.system("RenderSystem").kind(render).with(Position.class).iter(it -> System.out.println("[Render] " + it.count() + " entities"));

            world.setPipeline(customPipeline.id());

            for (int i = 0; i < 3; i++) {
                System.out.println("Frame " + (i + 1));
                world.progress(0.016f);
            }
        }
    }
}
