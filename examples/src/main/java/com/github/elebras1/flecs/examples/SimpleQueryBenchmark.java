package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class SimpleQueryBenchmark {

    private static final int ENTITY_COUNT = 1_000_000;
    private static final int WARMUP_ITERATIONS = 5;
    private static final int BENCHMARK_ITERATIONS = 10;

    public static void main(String[] args) {
        System.out.println("=== Flecs-Java Query Performance Benchmark ===");
        System.out.println("Entity count: " + ENTITY_COUNT);
        System.out.println("Warmup iterations: " + WARMUP_ITERATIONS);
        System.out.println("Benchmark iterations: " + BENCHMARK_ITERATIONS);
        System.out.println();

        try (World world = new World()) {
            world.component(Position.class);
            world.component(Velocity.class);

            System.out.println("Creating " + ENTITY_COUNT + " entities...");
            long startCreate = System.nanoTime();

            for (int i = 0; i < ENTITY_COUNT; i++) {
                long entityId = world.entity();
                Entity entity = world.obtainEntity(entityId);
                entity.set(new Position(i % 1000, i % 1000));
                entity.set(new Velocity(1.0f, 0.5f));
            }

            long endCreate = System.nanoTime();
            System.out.printf("Entity creation time: %.3f ms%n", (endCreate - startCreate) / 1_000_000.0);
            System.out.println();

            try (Query query = world.query().with(Position.class).with(Velocity.class).build()) {

                System.out.println("Query matches " + query.count() + " entities");
                System.out.println();

                System.out.println("--- Benchmark 1: each() + entity.get(Component.class) ---");
                runBenchmark("each+get", () -> benchmarkEachGet(world, query));

                System.out.println("--- Benchmark 2: iter() + Field<T>.get(i) ---");
                runBenchmark("iter+Field", () -> benchmarkIterField(query));

                System.out.println("--- Benchmark 3: iter() + fieldFloat() (native) ---");
                runBenchmark("iter+native", () -> benchmarkIterNative(query));

                System.out.println();
                System.out.println("=== Benchmark Complete ===");
            }
        }
    }

    private static float benchmarkEachGet(World world, Query query) {
        final float[] sum = {0.0f};

        query.each(entityId -> {
            Entity entity = world.obtainEntity(entityId);
            Position pos = entity.get(Position.class);
            Velocity vel = entity.get(Velocity.class);
            sum[0] += pos.x() + pos.y() + vel.dx() + vel.dy();
        });

        return sum[0];
    }

    private static float benchmarkIterField(Query query) {
        final float[] sum = {0.0f};

        query.iter(iter -> {
            Field<Position> positions = iter.field(Position.class, 0);
            Field<Velocity> velocities = iter.field(Velocity.class, 1);

            for (int i = 0; i < iter.count(); i++) {
                Position pos = positions.get(i);
                Velocity vel = velocities.get(i);
                sum[0] += pos.x() + pos.y() + vel.dx() + vel.dy();
            }
        });

        return sum[0];
    }

    private static float benchmarkIterNative(Query query) {
        final float[] sum = {0.0f};

        query.iter(iter -> {
            for (int i = 0; i < iter.count(); i++) {
                float posX = iter.fieldFloat(Position.class, 0, "x", i);
                float posY = iter.fieldFloat(Position.class, 0, "y", i);
                float velDx = iter.fieldFloat(Velocity.class, 1, "dx", i);
                float velDy = iter.fieldFloat(Velocity.class, 1, "dy", i);
                sum[0] += posX + posY + velDx + velDy;
            }
        });

        return sum[0];
    }

    private static void runBenchmark(String name, java.util.function.Supplier<Float> benchmark) {
        System.out.print("  Warming up... ");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            benchmark.get();
        }
        System.out.println("done");

        long[] times = new long[BENCHMARK_ITERATIONS];
        float result = 0;

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.nanoTime();
            result = benchmark.get();
            long end = System.nanoTime();
            times[i] = end - start;
        }

        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        for (long time : times) {
            totalTime += time;
            minTime = Math.min(minTime, time);
            maxTime = Math.max(maxTime, time);
        }

        double avgTime = totalTime / (double) BENCHMARK_ITERATIONS;

        System.out.printf("  Results for '%s':%n", name);
        System.out.printf("    Average: %.3f ms%n", avgTime / 1_000_000.0);
        System.out.printf("    Min:     %.3f ms%n", minTime / 1_000_000.0);
        System.out.printf("    Max:     %.3f ms%n", maxTime / 1_000_000.0);
        System.out.printf("    Sum check: %.0f (verification)%n", result);
        System.out.println();
    }
}


