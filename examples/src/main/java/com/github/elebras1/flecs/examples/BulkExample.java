package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.collection.LongList;
import com.github.elebras1.flecs.examples.components.Health;
import com.github.elebras1.flecs.examples.components.Position;
import com.github.elebras1.flecs.examples.components.Velocity;

public class BulkExample {
    public static void main(String[] args) {
        try (World world = new World()) {

            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Health.class);

            int totalEntities = 1_000_000;
            System.out.println("Creating " + totalEntities + " entities...");

            long startCreate = System.nanoTime();

            LongList entityIds = world.entityBulk(totalEntities, Position.class, Velocity.class, Health.class);

            long endCreate = System.nanoTime();
            System.out.printf("Entity creation time: %.3f ms%n", (endCreate - startCreate) / 1_000_000.0);
        }
    }
}
