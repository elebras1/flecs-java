package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class QueryBasicsExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            world.component(Velocity.class);

            Entity player = world.obtainEntity(world.entity("Player"));
            player.set(new Position(0, 0)).set(new Velocity(1, 0.5f));

            Entity enemy1 = world.obtainEntity(world.entity("Enemy1"));
            enemy1.set(new Position(10, 5)).set(new Velocity(-0.5f, 0));

            Entity enemy2 = world.obtainEntity(world.entity("Enemy2"));
            enemy2.set(new Position(-5, 10)).set(new Velocity(0, -1));

            Entity obstacle = world.obtainEntity(world.entity("Obstacle"));
            obstacle.set(new Position(20, 20));

            try (Query query = world.query().with(Position.class).with(Velocity.class).build()) {
                System.out.println("Entities with Position and Velocity: " + query.count());

                query.each(entityId -> {
                    Entity entity = world.obtainEntity(entityId);
                    System.out.println("  - " + entity.getName());
                });
            }

            System.out.println();

            try (Query query = world.query().with(Position.class).with(Velocity.class).build()) {
                query.iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);

                    for (int i = 0; i < it.count(); i++) {
                        Position pos = positions.get(i);
                        Velocity vel = velocities.get(i);
                        Entity entity = world.obtainEntity(it.entity(i));

                        System.out.printf("%s: pos=(%.1f, %.1f), vel=(%.1f, %.1f)%n",
                            entity.getName(), pos.x(), pos.y(), vel.dx(), vel.dy());
                    }
                });
            }
        }
    }
}

