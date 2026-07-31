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
        world.obtainEntity(world.entity("e3")).set(new Position(30, 40));

        // Create a simple query that matches Position.
        Query query = world.query().with(Position.class).build();

        // Find the first entity whose Position.x equals 20.
        AtomicLong found = new AtomicLong(0L);
        AtomicBoolean foundFlag = new AtomicBoolean(false);
        query.each((entityId) -> {
            if (!foundFlag.get()) {
                Position p = world.obtainEntity(entityId).get(Position.class);
                if (p != null && p.x() == 20) {
                    found.set(entityId);
                    foundFlag.set(true);
                }
            }
        });

        if (foundFlag.get()) {
            Entity entity = world.obtainEntity(found.get());
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
