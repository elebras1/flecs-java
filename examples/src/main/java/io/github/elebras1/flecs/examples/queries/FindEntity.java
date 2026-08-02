package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FindEntity {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);

        world.obtainEntity(world.entity("e1")).set(new Position(10, 20));
        world.obtainEntity(world.entity("e2")).set(new Position(20, 30));

        // Create a simple query that matches Position.
        Query query = world.query().with(Position.class).build();

        // Find the entity for which Position.x is 20.
        long entityId = query.find(Position.class, position -> position.x() == 20);

        if (entityId != 0) {
            Entity entity = world.obtainEntity(entityId);
            System.out.println("Found entity " + entity.name());
        } else {
            System.out.println("No entity found");
        }

        query.destroy();
        world.destroy();
    }

    // Output:
    // Found entity e2
}
