package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Health;
import com.github.elebras1.flecs.examples.components.HealthView;

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

        World world = new World();
        world.setStageCount(THREADS);
        world.component(Health.class);

        for (int i = 0; i < 1000; i++) {
            Entity entity = world.obtainEntity(world.entity("entity_" + i));
            entity.set(new Health(i));
        }

        world.readonlyBegin();
        for (int i = 0; i < THREADS; i++) {
            final int stageId = i;
            futures.add(executor.submit(() -> {
                World stage = world.getStage(stageId);
                for(int j = stageId * 250; j < (stageId + 1) * 250; j++) {
                    long entityId = stage.lookup("entity_" + j);
                    EntityView entity = stage.obtainEntityView(entityId);
                    HealthView health = entity.getMutView(Health.class);
                    health.value(health.value() + 1);
                }
            }));
        }

        try {
            for (Future<?> f : futures) {
                f.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        world.readonlyEnd();

        executor.shutdown();

        world.destroy();
    }
}
