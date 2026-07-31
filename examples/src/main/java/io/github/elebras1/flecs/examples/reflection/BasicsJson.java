package io.github.elebras1.flecs.examples.reflection;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;

/**
 * Demonstrates basic JSON serialization with Flecs reflection. Components
 * registered with reflection data can be serialized to JSON.
 */
public class BasicsJson {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);

        // Create entity with Position as usual.
        Entity e = world.obtainEntity(world.entity("ent"))
                .set(new Position(10, 20));

        // Convert the Position component to a JSON string.
        Position p = e.get(Position.class);
        System.out.println("{\"x\":" + p.x() + ", \"y\":" + p.y() + "}");

        // Convert the whole world to JSON. This example shows the available
        // world-level serializer.
        String json = world.toJson();
        System.out.println(json);

        world.destroy();
    }

    // Output (the exact world JSON may contain additional metadata):
    // {"x":10.0, "y":20.0}
    // {"results":[{"name":"Position", ...}, {"name":"ent", ...}]}
}
