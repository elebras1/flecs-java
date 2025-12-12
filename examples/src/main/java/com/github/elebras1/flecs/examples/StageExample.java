package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Flecs;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.examples.components.Minister;
import com.github.elebras1.flecs.examples.components.Position;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StageExample {

    public static void main(String[] args) throws InterruptedException {
        int totalEntities = 100_000;
        int threadCount = 4;
        int entitiesPerThread = totalEntities / threadCount;
        AtomicInteger entityCounter = new AtomicInteger(0);

        try (Flecs world = new Flecs()) {

            world.component(Position.class);
            world.setStageCount(threadCount);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final int tid = t;

                executor.submit(() -> {
                    Flecs.runScoped(() -> {
                        try (Flecs stage = world.getStage(tid)) {
                            System.out.println("Creating " + tid + " entities using " + threadCount + " threads...");

                            for (int i = 0; i < entitiesPerThread; i++) {
                                long entityId = stage.entity();
                                Entity entity = stage.obtainEntity(entityId);

                                entity.set(new Position(10.0f, 20.0f));

                                int count = entityCounter.incrementAndGet();
                                System.out.printf("Created entity %d (count %d)%n", entityId, count);
                            }
                        }
                    });
                });
            }

            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);

            for (int t = 0; t < threadCount; t++) {
                try (Flecs stageWrapper = world.getStage(t)) {
                    stageWrapper.merge();
                }
            }

            Flecs.runScoped(() -> {
                try (Query q = world.query().with(Minister.class).build()) {
                    System.out.println("Querying entities...");
                    final int[] count = {0};
                    q.each(_ -> count[0]++);
                    System.out.println("Total entities found: " + count[0]);
                }
            });
        }
    }
}