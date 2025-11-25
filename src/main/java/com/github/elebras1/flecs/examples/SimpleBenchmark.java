package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class SimpleBenchmark {

    public static void main(String[] args) {
        FlecsLoader.load();

        try (Flecs world = new Flecs()) {

            // Register components
            Position posComp = new Position();
            Velocity velComp = new Velocity();
            Health healthComp = new Health();

            long posId = world.components().register(Position.class, posComp);
            long velId = world.components().register(Velocity.class, velComp);
            long healthId = world.components().register(Health.class, healthComp);

            int totalEntities = 1_000_000;
            System.out.println("Creating " + totalEntities + " entities...");

            long startCreate = System.nanoTime();

            for (int i = 0; i < totalEntities; i++) {
                Entity e = world.entity();
                e.set(posId, posComp, new Position.Data(i % 100, i % 100));
                e.set(velId, velComp, new Velocity.Data(1, 1));
                e.set(healthId, healthComp, new Health.Data(100));
            }

            long endCreate = System.nanoTime();
            System.out.printf("Entity creation time: %.3f ms%n", (endCreate - startCreate) / 1_000_000.0);

            // Benchmark query
            try (Query moveQuery = world.query().with(posId).with(velId).build()) {
                long startQuery = System.nanoTime();
                int count = moveQuery.count();
                long endQuery = System.nanoTime();
                System.out.println("Entities with Position + Velocity: " + count);
                System.out.printf("Query time: %.3f ms%n",
                        (endQuery - startQuery) / 1_000_000.0);
            }

            // Benchmark movement simulation
            int frames = 10;
            System.out.println("Simulating " + frames + " frames...");

            try (Query simQuery = world.query().with(posId).with(velId).build()) {
                long startSim = System.nanoTime();

                for (int frame = 0; frame < frames; frame++) {
                    simQuery.iter(it -> {
                        for (int i = 0; i < it.count(); i++) {
                            Entity e = it.entity(i);
                            Position.Data pos = e.get(posId, posComp);
                            Velocity.Data vel = e.get(velId, velComp);
                            e.set(posId, posComp, new Position.Data(
                                    pos.x() + vel.dx(),
                                    pos.y() + vel.dy()
                            ));
                        }
                    });
                    world.progress(0.016f); // simulation step
                }

                long endSim = System.nanoTime();
                System.out.printf("Simulation time for %d frames: %.3f ms%n",
                        frames, (endSim - startSim) / 1_000_000.0);
            }
        }
    }
}

