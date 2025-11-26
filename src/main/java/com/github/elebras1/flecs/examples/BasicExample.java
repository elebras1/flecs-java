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

            long posId = world.components().register(posComp);
            long velId = world.components().register(velComp);
            long healthId = world.components().register(healthComp);

            // Create tags
            long playerTagId = world.entity("PlayerTag");
            long enemyTagId = world.entity("EnemyTag");
            long aiTagId = world.entity("AIControlled");

            System.out.println("--- Entity Creation & Components ---");

            // Create player entity
            long playerId = world.entity("Player");
            Entity player = world.obtainEntity(playerId);
            player.add(playerTagId);
            player.set(posId, posComp, new Position.Data(0, 0));
            player.set(velId, velComp, new Velocity.Data(5, 0));
            player.set(healthId, healthComp, new Health.Data(100));
            System.out.println("Created: " + player.getName() + " (ID: " + player.id() + ")");

            // Create enemies with different setups
            long enemy1Id = world.entity("Enemy1");
            Entity enemy1 = world.obtainEntity(enemy1Id);
            enemy1.add(enemyTagId).add(aiTagId);
            enemy1.set(posId, posComp, new Position.Data(100, 50));
            enemy1.set(velId, velComp, new Velocity.Data(-2, 1));
            enemy1.set(healthId, healthComp, new Health.Data(50));
            System.out.println("Created: " + enemy1.getName() + " (ID: " + enemy1.id() + ")");

            long enemy2Id = world.entity("Enemy2");
            Entity enemy2 = world.obtainEntity(enemy2Id);
            enemy2.add(enemyTagId).add(aiTagId);
            enemy2.set(posId, posComp, new Position.Data(150, -30));
            enemy2.set(velId, velComp, new Velocity.Data(-1, -1));
            enemy2.set(healthId, healthComp, new Health.Data(75));
            System.out.println("Created: " + enemy2.getName() + " (ID: " + enemy2.id() + ")");

            // Static object (no velocity)
            long obstacleId = world.entity("Obstacle");
            Entity obstacle = world.obtainEntity(obstacleId);
            obstacle.set(posId, posComp, new Position.Data(50, 50));
            System.out.println("Created: " + obstacle.getName() + " (ID: " + obstacle.id() + ")");

            System.out.println("\n--- Component Queries ---");

            // Query all entities with Position
            try (Query posQuery = world.query().with(posId).build()) {
                System.out.println("Entities with Position: " + posQuery.count());
                posQuery.iter(it -> {
                    for (int i = 0; i < it.count(); i++) {
                        long entityId = it.entity(i);
                        Entity entity = world.obtainEntity(entityId);
                        Position.Data pos = entity.get(posId, posComp);
                        System.out.printf("  %s at (%.1f, %.1f)%n", entity.getName(), pos.x(), pos.y());
                    }
                });
            }

            // Query moving entities (Position + Velocity)
            try (Query moveQuery = world.query().with(posId).with(velId).build()) {
                System.out.println("\nMoving entities: " + moveQuery.count());
            }

            // Query enemies with AI
            try (Query aiQuery = world.query().with(enemyTagId).with(aiTagId).build()) {
                System.out.println("AI-controlled enemies: " + aiQuery.count());
            }

            System.out.println("\n--- Entity Relationships ---");

            // Check entity properties
            System.out.println("Player has Position: " + player.has(posId));
            System.out.println("Obstacle has Velocity: " + obstacle.has(velId));
            System.out.println("Enemy1 is AI: " + enemy1.has(aiTagId));

            System.out.println("\n--- Movement Simulation ---");

            try (Query simQuery = world.query().with(posId).with(velId).build()) {
                for (int frame = 0; frame < 3; frame++) {
                    System.out.println("\nFrame " + (frame + 1) + ":");

                    simQuery.iter(it -> {
                        for (int i = 0; i < it.count(); i++) {
                            long entityId = it.entity(i);
                            Entity entity = world.obtainEntity(entityId);
                            Position.Data pos = entity.get(posId, posComp);
                            Velocity.Data vel = entity.get(velId, velComp);

                            float newX = pos.x() + vel.dx();
                            float newY = pos.y() + vel.dy();
                            entity.set(posId, posComp, new Position.Data(newX, newY));

                            System.out.printf("  %s: (%.1f, %.1f)%n", entity.getName(), newX, newY);
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

            try (Query aliveEnemies = world.query().with(enemyTagId).build()) {
                System.out.println("Remaining enemies: " + aliveEnemies.count());
            }

            System.out.println("\n--- Entity Lookup ---");

            long foundPlayerId = world.lookup("Player");
            Entity foundPlayer = world.obtainEntity(foundPlayerId);
            System.out.println("Found by name: " + (foundPlayer != null ? foundPlayer.getName() : "null"));

            System.out.println("\n=== Example completed successfully! ===");
        }
    }
}


