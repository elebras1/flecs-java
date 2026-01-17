package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class QueryFeaturesExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Health.class);

            long playerTag = world.entity("PlayerTag");
            long enemyTag = world.entity("EnemyTag");

            Entity player = world.obtainEntity(world.entity("Player"));
            player.add(playerTag).set(new Position(10, 20)).set(new Velocity(5, 0)).set(new Health(100));

            Entity enemy1 = world.obtainEntity(world.entity("Enemy1"));
            enemy1.add(enemyTag).set(new Position(50, 30)).set(new Velocity(-2, 1)).set(new Health(50));

            Entity enemy2 = world.obtainEntity(world.entity("Enemy2"));
            enemy2.add(enemyTag).set(new Position(80, 40)).set(new Health(75));

            Entity obstacle = world.obtainEntity(world.entity("Obstacle"));
            obstacle.set(new Position(100, 100));

            try (Query q1 = world.query().with(Position.class).with(Velocity.class).build()) {
                System.out.println("Position AND Velocity: " + q1.count());
            }

            try (Query q2 = world.query().with(enemyTag).with(Health.class).build()) {
                System.out.println("Enemy with Health: " + q2.count());
            }

            try (Query q3 = world.query().with(Position.class).or().with(Velocity.class).build()) {
                System.out.println("Position OR Velocity: " + q3.count());
            }

            try (Query q4 = world.query().with(Position.class).with(Velocity.class).not().build()) {
                System.out.println("Position NOT Velocity: " + q4.count());
            }

            try (Query q5 = world.query().with(Position.class).with(Velocity.class).optional().build()) {
                System.out.println("Position + optional Velocity: " + q5.count());
            }

            try (Query q6 = world.query().with(Position.class).with(Velocity.class).build()) {
                q6.iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);

                    for (int i = 0; i < it.count(); i++) {
                        Position pos = positions.get(i);
                        Velocity vel = velocities.get(i);
                        Entity e = world.obtainEntity(it.entity(i));
                        System.out.printf("%s: pos=(%.1f,%.1f) vel=(%.1f,%.1f)%n", e.getName(), pos.x(), pos.y(), vel.dx(), vel.dy());
                    }
                });
            }

            try (Query q7 = world.query().with(Position.class).with(Health.class).build()) {
                q7.run(it -> {
                    System.out.println("Total entities: " + q7.count());
                    while (it.next()) {
                        for (int i = 0; i < it.count(); i++) {
                            Entity e = world.obtainEntity(it.entity(i));
                            Health h = e.get(Health.class);
                            System.out.println(e.getName() + " HP: " + h.value());
                        }
                    }
                });
            }
        }
    }
}
