package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Health;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ManualStagingExample {
    private static final int THREADS = 4;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Future<?>> futures = new ArrayList<>();

        try (World world = new World()) {
            world.setStageCount(THREADS);
            world.component(Health.class);

            for (int i = 0; i < 1000; i++) {
                Entity entity = world.obtainEntity(world.entity("entity_" + i));
                entity.set(new Health(i));
            }

            for (int i = 0; i < THREADS; i++) {
                final int stageId = i;
                futures.add(executor.submit(() -> {
                    World stage = world.getStage(stageId);
                    for(int j = stageId * 250; j < (stageId + 1) * 250; j++) {
                        long entityId = world.lookup("entity_" + j);
                        Entity entity = stage.obtainEntity(entityId);
                        entity.set(new Health(j));
                    }
                }));
            }

            for (Future<?> f : futures) {
                f.get();
            }

            world.merge();
            executor.shutdown();

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
