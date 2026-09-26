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
        System.out.println(e.toJson(Position.class));

        // Convert the entity to JSON.
        System.out.println(e.toJson());

        world.destroy();
    }

    // Output (the exact entity JSON may contain additional metadata):
    // {"x":10, "y":20}
    // {"name":"ent", "components":{"Position":{"x":10, "y":20}}}
}
