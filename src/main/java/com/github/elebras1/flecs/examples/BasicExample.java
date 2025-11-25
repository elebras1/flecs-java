package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class BasicExample {

    public static void main(String[] args) {
        FlecsLoader.load();

        try (Flecs world = new Flecs()) {
            System.out.println("=== Flecs Wrapper - Comprehensive Example ===\n");

            // Register components
            Position posComp = new Position();
            Velocity velComp = new Velocity();
            Health healthComp = new Health();

            long posId = world.components().register(Position.class, posComp);
            long velId = world.components().register(Velocity.class, velComp);
            long healthId = world.components().register(Health.class, healthComp);

            // Create tags
            long playerTag = world.entity("PlayerTag").id();
            long enemyTag = world.entity("EnemyTag").id();
            long aiTag = world.entity("AIControlled").id();

            System.out.println("--- Entity Creation & Components ---");

            // Create player entity
            Entity player = world.entity("Player");
            player.add(playerTag);
            player.set(posId, posComp, new Position.Data(0, 0));
            player.set(velId, velComp, new Velocity.Data(5, 0));
            player.set(healthId, healthComp, new Health.Data(100));
            System.out.println("Created: " + player.getName() + " (ID: " + player.id() + ")");

            // Create enemies with different setups
            Entity enemy1 = world.entity("Enemy1");
            enemy1.add(enemyTag).add(aiTag);
            enemy1.set(posId, posComp, new Position.Data(100, 50));
            enemy1.set(velId, velComp, new Velocity.Data(-2, 1));
            enemy1.set(healthId, healthComp, new Health.Data(50));
            System.out.println("Created: " + enemy1.getName() + " (ID: " + enemy1.id() + ")");

            Entity enemy2 = world.entity("Enemy2");
            enemy2.add(enemyTag).add(aiTag);
            enemy2.set(posId, posComp, new Position.Data(150, -30));
            enemy2.set(velId, velComp, new Velocity.Data(-1, -1));
            enemy2.set(healthId, healthComp, new Health.Data(75));
            System.out.println("Created: " + enemy2.getName() + " (ID: " + enemy2.id() + ")");

            // Static object (no velocity)
            Entity obstacle = world.entity("Obstacle");
            obstacle.set(posId, posComp, new Position.Data(50, 50));
            System.out.println("Created: " + obstacle.getName() + " (ID: " + obstacle.id() + ")");

            System.out.println("\n--- Component Queries ---");

            // Query all entities with Position
            try (Query posQuery = world.query().with(posId).build()) {
                System.out.println("Entities with Position: " + posQuery.count());
                posQuery.iter(it -> {
                    for (int i = 0; i < it.count(); i++) {
                        Entity e = it.entity(i);
                        Position.Data pos = e.get(posId, posComp);
                        System.out.printf("  %s at (%.1f, %.1f)%n",
                                e.getName(), pos.x(), pos.y());
                    }
                });
            }

            // Query moving entities (Position + Velocity)
            try (Query moveQuery = world.query().with(posId).with(velId).build()) {
                System.out.println("\nMoving entities: " + moveQuery.count());
            }

            // Query enemies with AI
            try (Query aiQuery = world.query().with(enemyTag).with(aiTag).build()) {
                System.out.println("AI-controlled enemies: " + aiQuery.count());
            }

            System.out.println("\n--- Entity Relationships ---");

            // Check entity properties
            System.out.println("Player has Position: " + player.has(posId));
            System.out.println("Obstacle has Velocity: " + obstacle.has(velId));
            System.out.println("Enemy1 is AI: " + enemy1.has(aiTag));

            System.out.println("\n--- Movement Simulation ---");

            try (Query simQuery = world.query().with(posId).with(velId).build()) {
                for (int frame = 0; frame < 3; frame++) {
                    System.out.println("\nFrame " + (frame + 1) + ":");

                    simQuery.iter(it -> {
                        for (int i = 0; i < it.count(); i++) {
                            Entity e = it.entity(i);
                            Position.Data pos = e.get(posId, posComp);
                            Velocity.Data vel = e.get(velId, velComp);

                            float newX = pos.x() + vel.dx();
                            float newY = pos.y() + vel.dy();
                            e.set(posId, posComp, new Position.Data(newX, newY));

                            System.out.printf("  %s: (%.1f, %.1f)%n", e.getName(), newX, newY);
                        }
                    });

                    world.progress(0.016f);
                }
            }

            System.out.println("\n--- Component Modification ---");

            // Modify player velocity
            player.set(velId, velComp, new Velocity.Data(0, 3));
            Velocity.Data newVel = player.get(velId, velComp);
            System.out.println("Player new velocity: " + newVel);

            // Damage enemy
            Health.Data enemy1Health = enemy1.get(healthId, healthComp);
            enemy1.set(healthId, healthComp,
                    new Health.Data(enemy1Health.value() - 25));
            System.out.println("Enemy1 health: " +
                    enemy1.get(healthId, healthComp).value());

            System.out.println("\n--- Component Removal ---");

            // Remove velocity from player (stop movement)
            player.remove(velId);
            System.out.println("Player has Velocity: " + player.has(velId));

            try (Query stillMoving = world.query().with(posId).with(velId).build()) {
                System.out.println("Still moving: " + stillMoving.count());
            }

            System.out.println("\n--- Entity Deletion ---");

            // Delete defeated enemy
            enemy1.delete();
            System.out.println("Enemy1 is alive: " + enemy1.isAlive());

            try (Query aliveEnemies = world.query().with(enemyTag).build()) {
                System.out.println("Remaining enemies: " + aliveEnemies.count());
            }

            System.out.println("\n--- Entity Lookup ---");

            Entity foundPlayer = world.lookup("Player");
            System.out.println("Found by name: " +
                    (foundPlayer != null ? foundPlayer.getName() : "null"));

            System.out.println("\n=== Example completed successfully! ===");
        }
    }
}


