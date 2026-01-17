package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.collection.LongList;
import com.github.elebras1.flecs.examples.components.*;

public class TableExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Health.class);

            world.obtainEntity(world.entity()).set(new Position(0, 0)).set(new Velocity(1, 1));
            world.obtainEntity(world.entity()).set(new Position(10, 10)).set(new Velocity(2, 2));
            world.obtainEntity(world.entity()).set(new Position(20, 20)).set(new Health(100));
            world.obtainEntity(world.entity()).set(new Position(30, 30));

            try (Query q = world.query().with(Position.class).with(Velocity.class).build()) {
                q.iter(it -> {
                    Table table = it.table();

                    System.out.println("Table: " + table.str());
                    System.out.println("Entities: " + table.count());
                    System.out.println("Columns: " + table.columnCount());

                    LongList typeIds = table.type();
                    for (int i = 0; i < typeIds.size(); i++) {
                        long id = typeIds.get(i);
                        Entity component = world.obtainEntity(id);
                        System.out.println("  - " + component.getName() + " (" + id + ")");
                    }
                });
            }

            System.out.println();

            try (Query q = world.query().with(Position.class).with(Velocity.class).build()) {
                q.iter(it -> {
                    Table table = it.table();

                    for (int row = 0; row < table.count(); row++) {
                        Position pos = table.get(Position.class, row);
                        Velocity vel = table.get(Velocity.class, row);

                        LongList entities = table.entities();
                        Entity e = world.obtainEntity(entities.get(row));
                        System.out.printf("%s: pos=(%.1f, %.1f), vel=(%.1f, %.1f)%n",
                                e.getName(), pos.x(), pos.y(), vel.dx(), vel.dy());
                    }
                });
            }
        }
    }
}

