package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.collection.EcsLongList;
import com.github.elebras1.flecs.examples.components.*;

public class TableExample {

    public static void main(String[] args) {
        try (Flecs world = new Flecs()) {
            System.out.println("=== Table Example ===\n");

            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Health.class);

            world.obtainEntity(world.entity()).set(new Position(0, 0)).set(new Velocity(1, 1));
            world.obtainEntity(world.entity()).set(new Position(10, 10)).set(new Velocity(2, 2));
            world.obtainEntity(world.entity()).set(new Position(20, 20)).set(new Health(100));
            world.obtainEntity(world.entity()).set(new Position(30, 30));

            System.out.println("--- Table info and type ---");
            try (Query q = world.query().with(Position.class).with(Velocity.class).build()) {
                q.iter(it -> {
                    Table table = it.table();

                    System.out.println("Table type: " + table.str());
                    System.out.println("Entity count: " + table.count());
                    System.out.println("Column count: " + table.columnCount());

                    System.out.println("\nComponent IDs in table:");
                    EcsLongList typeIds = table.type();
                    for (int i = 0; i < typeIds.size(); i++) {
                        long id = typeIds.get(i);
                        Entity component = world.obtainEntity(id);
                        System.out.println("  - " + component.getName() + " (ID: " + id + ")");
                    }
                });
            }

            System.out.println("\n--- Direct component access via Table ---");
            try (Query q = world.query().with(Position.class).with(Velocity.class).build()) {
                q.iter(it -> {
                    Table table = it.table();

                    System.out.println("Reading components directly from table:");
                    for (int row = 0; row < table.count(); row++) {
                        Position pos = table.get(Position.class, row);
                        Velocity vel = table.get(Velocity.class, row);

                        EcsLongList entities = table.entities();
                        Entity e = world.obtainEntity(entities.get(row));
                        System.out.printf("  %s: pos=(%.1f, %.1f), vel=(%.1f, %.1f)%n",
                                e.getName(), pos.x(), pos.y(), vel.dx(), vel.dy());
                    }
                });
            }

            System.out.println("\n--- Different archetypes = Different tables ---");
            try (Query q = world.query().with(Position.class).build()) {
                q.iter(it -> {
                    Table table = it.table();
                    System.out.println("Table [" + table.str() + "] - " + table.count() + " entities");
                });
            }

            System.out.println("\n=== Example completed! ===");
        }
    }
}

