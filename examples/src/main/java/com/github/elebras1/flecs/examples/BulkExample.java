package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Flecs;
import com.github.elebras1.flecs.collection.EcsLongList;
import com.github.elebras1.flecs.examples.components.Health;
import com.github.elebras1.flecs.examples.components.Position;
import com.github.elebras1.flecs.examples.components.Velocity;

public class BulkExample {
    public static void main(String[] args) {
        try (Flecs world = new Flecs()) {

            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Health.class);

            int totalEntities = 1_000_000;
            System.out.println("Creating " + totalEntities + " entities...");

            long startCreate = System.nanoTime();

            EcsLongList entityIds = world.entityBulk(totalEntities, Position.class, Velocity.class, Health.class);

            long endCreate = System.nanoTime();
            System.out.printf("Entity creation time: %.3f ms%n", (endCreate - startCreate) / 1_000_000.0);
        }
    }
}
