package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

import java.lang.System;

public class SystemExample {

    public static void main(String[] args) {
        try (Flecs world = new Flecs()) {
            System.out.println("=== Flecs Systems Example ===\n");

            // Register components
            long posId = world.component(Position.class);
            long velId = world.component(Velocity.class);

            // Create entities
            Entity player = world.obtainEntity(world.entity("Player"));
            player.set(new Position(0, 0)).set(new Velocity(1, 0.5f));

            Entity enemy1 = world.obtainEntity(world.entity("Enemy1"));
            enemy1.set(new Position(10, 5)).set(new Velocity(-0.5f, 0));

            Entity enemy2 = world.obtainEntity(world.entity("Enemy2"));
            enemy2.set(new Position(-5, 10)).set(new Velocity(0, -1));

            System.out.println("--- Creating Systems ---\n");

            // Create a movement system using the iter pattern
            FlecsSystem moveSystem = world.system("MoveSystem")
                .with(Position.class)
                .with(Velocity.class)
                .kind(FlecsConstants.EcsOnUpdate)
                .iter(it -> {
                    System.out.println("MoveSystem running (delta_time: " + it.deltaTime() + "s)");
                    
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    
                    for (int i = 0; i < it.count(); i++) {
                        Position pos = positions.get(i);
                        Velocity vel = velocities.get(i);
                        
                        float newX = pos.x() + vel.dx() * it.deltaTime();
                        float newY = pos.y() + vel.dy() * it.deltaTime();

                        long entityId = it.entity(i);
                        Entity entity = world.obtainEntity(entityId);
                        entity.set(new Position(newX, newY));
                        System.out.printf("  %s: (%.2f, %.2f) -> (%.2f, %.2f)%n", entity.getName(), pos.x(), pos.y(), newX, newY);
                    }
                });

            // Create a debug system using the each pattern
            FlecsSystem debugSystem = world.system("DebugSystem")
                .with(posId)
                .kind(FlecsConstants.EcsPostUpdate)
                .each(entityId -> {
                    Entity entity = world.obtainEntity(entityId);
                    Position pos = entity.get(Position.class);
                    System.out.printf("  Debug: %s at (%.2f, %.2f)%n", 
                        entity.getName(), pos.x(), pos.y());
                });

            // Create a task system (no entities)
            FlecsSystem taskSystem = world.system("TaskSystem")
                .kind(FlecsConstants.EcsPreUpdate)
                .run(it -> {
                    System.out.println("TaskSystem: Frame " + " starting...");
                });

            System.out.println("\n--- Running Progress (automatic system execution) ---\n");

            // Run multiple frames
            for (int frame = 0; frame < 3; frame++) {
                System.out.println("=== Frame " + (frame + 1) + " ===");
                world.progress(0.016f); // ~60 FPS
                System.out.println();
            }

            System.out.println("\n--- Manual System Execution ---\n");
            
            // Manually run a system
            System.out.println("Manually running MoveSystem:");
            moveSystem.run(0.1f);

            System.out.println("\n--- System Control ---\n");

            // Disable a system
            System.out.println("Disabling DebugSystem...");
            debugSystem.disable();
            System.out.println("DebugSystem enabled: " + debugSystem.isEnabled());

            System.out.println("\nRunning progress with DebugSystem disabled:");
            world.progress(0.016f);

            // Re-enable the system
            System.out.println("\nRe-enabling DebugSystem...");
            debugSystem.enable();
            System.out.println("DebugSystem enabled: " + debugSystem.isEnabled());

            System.out.println("\nRunning progress with DebugSystem re-enabled:");
            world.progress(0.016f);

            System.out.println("\n--- Final Positions ---\n");
            
            try (Query posQuery = world.query().with(posId).build()) {
                posQuery.iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        Position pos = positions.get(i);
                        Entity entity = world.obtainEntity(it.entity(i));
                        System.out.printf("%s: (%.2f, %.2f)%n", entity.getName(), pos.x(), pos.y());
                    }
                });
            }

            // Clean up systems
            moveSystem.close();
            debugSystem.close();
            taskSystem.close();

            System.out.println("\n=== Systems Example Complete ===");
        }
    }
}

