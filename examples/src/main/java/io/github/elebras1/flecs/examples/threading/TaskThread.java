package io.github.elebras1.flecs.examples.threading;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.OsApi;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Health;
import io.github.elebras1.flecs.util.Flecs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demonstrates custom task threads. The world delegates task execution to a
 * Java thread pool through OsApi. The executor is shut down after the
 * world is destroyed so that no callbacks are invoked once the world is gone.
 */
public class TaskThread {

    private static final int NUMBER_THREADS = 4;

    static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
        Map<Long, Future<?>> futures = new ConcurrentHashMap<>();
        AtomicLong counter = new AtomicLong();

        OsApi osApi = new OsApi();
        osApi.taskNew(task -> {
                    long id = counter.incrementAndGet();
                    futures.put(id, executor.submit(task));
                    return id;
                })
                .taskJoin(id -> {
                    try {
                        futures.remove(id).get();
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }
                })
                .set();

        World world = new World();
        world.setTaskThreads(NUMBER_THREADS);
        world.component(Health.class);

        for (int i = 0; i < 100_000; i++) {
            EntityView entity = world.obtainEntityView(world.entity());
            entity.set(new Health(100));
        }

        world.system().with(Health.class).kind(Flecs.OnUpdate).multiThreaded().iter(iter -> {
            Field<Health> healthField = iter.field(Health.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                Health health = healthField.get(i);
                healthField.set(i, new Health(health.value() - 1));
            }
        });

        for (int i = 0; i < 1000; i++) {
            world.progress();
        }

        // Tear down the world before the OS API so callbacks are no longer
        // invoked, then shut down the executor.
        world.destroy();
        osApi.destroy();
        executor.close();
    }

    // Output:
    // (no deterministic output; 100,000 entities each had their Health
    // decremented 1000 times across the worker threads)
}
