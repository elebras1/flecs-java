package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Flecs;
import com.github.elebras1.flecs.collection.EcsLongList;
import com.github.elebras1.flecs.examples.components.Health;
import com.github.elebras1.flecs.examples.components.Position;
import com.github.elebras1.flecs.examples.components.Velocity;

public class BulkExample {
    public static void main(String[] args) {
        FlecsLoader.load();

        try (Flecs world = new Flecs()) {

            long posId = world.component(Position.class);
            long velId = world.component(Velocity.class);
            long healthId = world.component(Health.class);

            int totalEntities = 1_000_000;
            System.out.println("Creating " + totalEntities + " entities...");

            long startCreate = System.nanoTime();

            EcsLongList entityIds = world.entityBulk(totalEntities);
            for (long entityId : entityIds) {
                Entity entity = world.obtainEntity(entityId);
                /*entity.set(posId, posComp, new Position.Data(entityId % 100, entityId % 100));
                entity.set(velId, velComp, new Velocity.Data(1, 1));
                entity.set(healthId, healthComp, new Health.Data(100));*/
            }

            long endCreate = System.nanoTime();
            System.out.printf("Entity creation time: %.3f ms%n", (endCreate - startCreate) / 1_000_000.0);
        }
    }
}
