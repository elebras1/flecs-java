package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class BasicExample {

    public static void main(String[] args) {
        FlecsLoader.load();

        try (Flecs world = new Flecs()) {
            System.out.println("=== Flecs Wrapper - Comprehensive Example ===\n");

            long posId = world.component(Position.class);
            long velId = world.component(Velocity.class);
            long healthId = world.component(Health.class);

            // Create tags
            long playerTagId = world.entity("PlayerTag");
            long enemyTagId = world.entity("EnemyTag");
            long aiTagId = world.entity("AIControlled");

            System.out.println("--- Entity Creation & Components ---");

            // Create player entity
            long playerId = world.entity("Player");
            Entity player = world.obtainEntity(playerId);
            player.add(playerTagId);
            player.set(new Position(0, 0)).set(new Velocity(5, 0)).set(new Health(100));
            System.out.println("Created: " + player.getName() + " (ID: " + player.id() + ")");

            // Create enemies with different setups
            long enemy1Id = world.entity("Enemy1");
            Entity enemy1 = world.obtainEntity(enemy1Id);
            enemy1.add(enemyTagId).add(aiTagId);
            enemy1.set(new Position(100, 50)).set(new Velocity(-2, 1)).set(new Health(50));
            System.out.println("Created: " + enemy1.getName() + " (ID: " + enemy1.id() + ")");

            long enemy2Id = world.entity("Enemy2");
            Entity enemy2 = world.obtainEntity(enemy2Id);
            enemy2.add(enemyTagId).add(aiTagId).set(new Position(150, -30)).set(new Velocity(-1, -1)).set(new Health(75));
            System.out.println("Created: " + enemy2.getName() + " (ID: " + enemy2.id() + ")");

            // Static object (no velocity)
            long obstacleId = world.entity("Obstacle");
            Entity obstacle = world.obtainEntity(obstacleId);
            obstacle.set(new Position(50, 50));
            System.out.println("Created: " + obstacle.getName() + " (ID: " + obstacle.id() + ")");

            System.out.println("\n--- Component Queries ---");

            // Query all entities with Position
            try (Query posQuery = world.query().with(posId).build()) {
                System.out.println("Entities with Position: " + posQuery.count());
                posQuery.iter(it -> {
                    for (int i = 0; i < it.count(); i++) {
                        long entityId = it.entity(i);
                        Entity entity = world.obtainEntity(entityId);
                        Position pos = entity.get(posId);
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
                            Position pos = entity.get(posId);
                            Velocity vel = entity.get(velId);

                            float newX = pos.x() + vel.dx();
                            float newY = pos.y() + vel.dy();
                            entity.set(new Position(newX, newY));

                            System.out.printf("  %s: (%.1f, %.1f)%n", entity.getName(), newX, newY);
                        }
                    });

                    world.progress(0.016f);
                }
            }

            System.out.println("\n--- Component Modification ---");

            // Modify player velocity
            player.set(new Velocity(0, 3));
            Velocity newVel = player.get(velId);
            System.out.println("Player new velocity: " + newVel);

            // Damage enemy
            Health enemy1Health = enemy1.get(healthId);
            enemy1.set(new Health(enemy1Health.value() - 25));
            System.out.println("Enemy1 health: " + enemy1.get(healthId));

            System.out.println("\n--- Component Removal ---");

            // Remove velocity from player (stop movement)
            player.remove(velId);
            System.out.println("Player has Velocity: " + player.has(velId));

            try (Query stillMoving = world.query().with(posId).with(velId).build()) {
                System.out.println("Still moving: " + stillMoving.count());
            }

            System.out.println("\n--- Entity Deletion ---");

            // Delete defeated enemy
            enemy1.destruct();
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


