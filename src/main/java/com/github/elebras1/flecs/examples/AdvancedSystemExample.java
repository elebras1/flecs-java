package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

import java.lang.System;

public class AdvancedSystemExample {

    public static void main(String[] args) {
        FlecsLoader.load();

        try (Flecs world = new Flecs()) {
            System.out.println("=== Advanced Systems Example ===\n");

            // Register components
            long posId = world.component(Position.class);
            long velId = world.component(Velocity.class);

            System.out.println("--- Creating Test Entities ---\n");

            // Create many entities for multi-threading demonstration
            for (int i = 0; i < 20; i++) {
                Entity entity = world.obtainEntity(world.entity("Entity_" + i));
                entity.set(new Position(i * 10.0f, i * 5.0f))
                      .set(new Velocity((i % 2 == 0 ? 1 : -1) * 0.5f, (i % 3 == 0 ? 1 : -1) * 0.3f));
            }

            System.out.println("Created 20 entities\n");

            System.out.println("--- Creating Timer-Based Systems ---\n");

            // Create a timer that ticks every second
            Entity timerOneSecond = world.timer()
                .interval(1.0f)
                .build();

            // System that runs every second
            FlecsSystem oneSecondSystem = world.system("OneSecondSystem")
                .tickSource(timerOneSecond.id())
                .run(it -> {
                    System.out.println("  [1s Timer] One second has passed! (delta_system_time: " 
                        + it.deltaSystemTime() + "s)");
                });

            // Create a rate-based system (every 2 frames)
            FlecsSystem everyOtherFrame = world.system("EveryOtherFrame")
                .with(posId)
                .rate(2)
                .kind(FlecsConstants.EcsOnUpdate)
                .run(it -> {
                    System.out.println("  [Rate Filter] Running every 2nd frame (matched: " + it.count() + " entities)");
                });

            System.out.println("--- Creating Multi-Threaded System ---\n");

            // Set thread count for multi-threading
            world.setThreads(4);
            System.out.println("Set worker threads to 4\n");

            // Create a multi-threaded movement system
            FlecsSystem mtMoveSystem = world.system("MultiThreadedMove")
                .with(posId)
                .with(velId)
                .kind(FlecsConstants.EcsOnUpdate)
                .multiThreaded(true)
                .iter(it -> {
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
                    }
                });

            System.out.println("--- Creating Immediate System ---\n");

            // Create an immediate system that can see operations immediately
            FlecsSystem immediateSystem = world.system("ImmediateSystem")
                .kind(FlecsConstants.EcsPreUpdate)
                .immediate(true)
                .run(it -> {
                    System.out.println("  [Immediate] Running in non-readonly mode");
                    world.deferSuspend();
                    long entityId = world.entity();
                    Entity e = world.obtainEntity(entityId).set(new Position(0, 0));
                    world.deferResume();
                    System.out.println("  [Immediate] Spawned entity " + e);
                });

            System.out.println("--- Running Simulation ---\n");

            // Run frames to trigger timers and systems
            for (int frame = 0; frame < 10; frame++) {
                System.out.println("=== Frame " + (frame + 1) + " ===");
                world.progress(0.016f); // ~60 FPS
                
                // Show some entity positions every few frames
                if (frame % 3 == 0) {
                    Entity sampleEntity = world.obtainEntity(world.lookup("Entity_0"));
                    Position pos = sampleEntity.get(Position.class);
                    System.out.printf("  Sample position (Entity_0): (%.2f, %.2f)%n", pos.x(), pos.y());
                }
                
                System.out.println();
            }

            System.out.println("\n--- Testing System Control ---\n");

            // Pause the timer
            System.out.println("Stopping one-second timer...");
            timerOneSecond.add(FlecsConstants.EcsDisabled);

            System.out.println("Running 3 more frames with timer stopped:");
            for (int frame = 0; frame < 3; frame++) {
                System.out.println("Frame " + (11 + frame));
                world.progress(0.016f);
            }

            // Resume the timer
            System.out.println("\nRestarting one-second timer...");
            timerOneSecond.remove(FlecsConstants.EcsDisabled);

            System.out.println("Running 3 more frames with timer restarted:");
            for (int frame = 0; frame < 3; frame++) {
                System.out.println("Frame " + (14 + frame));
                world.progress(0.016f);
            }

            System.out.println("\n--- Testing System Phases ---\n");

            // Create systems in different phases
            FlecsSystem preUpdateSys = world.system("PreUpdatePhase")
                .kind(FlecsConstants.EcsPreUpdate)
                .run(it -> System.out.println("  [PreUpdate] Running"));

            FlecsSystem onUpdateSys = world.system("OnUpdatePhase")
                .kind(FlecsConstants.EcsOnUpdate)
                .run(it -> System.out.println("  [OnUpdate] Running"));

            FlecsSystem postUpdateSys = world.system("PostUpdatePhase")
                .kind(FlecsConstants.EcsPostUpdate)
                .run(it -> System.out.println("  [PostUpdate] Running"));

            System.out.println("\nRunning one frame to show phase order:");
            world.progress(0.016f);

            // Clean up
            oneSecondSystem.close();
            everyOtherFrame.close();
            mtMoveSystem.close();
            immediateSystem.close();
            preUpdateSys.close();
            onUpdateSys.close();
            postUpdateSys.close();

            System.out.println("\n=== Advanced Systems Example Complete ===");
        }
    }
}

