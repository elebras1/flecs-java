package io.github.elebras1.flecs.examples.threading;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Health;
import io.github.elebras1.flecs.examples.components.HealthView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Demonstrates manual staging: multiple threads perform writes on their own
 * stages while the world is in read-only mode. The changes are merged back
 * when the read-only section ends.
 */
public class ManualStaging {

    private static final int THREADS = 4;

    static void main(String[] args) {
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
                for (int j = stageId * 250; j < (stageId + 1) * 250; j++) {
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
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        world.readonlyEnd();

        Health health = world.obtainEntity(world.lookup("entity_42")).get(Health.class);
        System.out.println("entity_42 health after staging: " + health.value());

        executor.shutdown();
        world.destroy();
    }

    // Output:
    // entity_42 health after staging: 43
}
