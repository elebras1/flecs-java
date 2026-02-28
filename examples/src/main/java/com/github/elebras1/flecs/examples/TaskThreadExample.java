package com.github.elebras1.flecs.examples;


import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.Health;
import com.github.elebras1.flecs.examples.components.HealthView;
import com.github.elebras1.flecs.util.FlecsConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class TaskThreadExample {

    private static final int NUMBER_THREADS = 4;

    public static void main(String[] args) {
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

        try (World world = new World()) {
            world.setTaskThreads(NUMBER_THREADS);
            world.component(Health.class);

            for(int i = 0; i < 100_000; i++) {
                EntityView entity = world.obtainEntityView(world.entity());
                entity.set(Health.class, (HealthView health) -> health.value(100));
            }

            world.system().with(Health.class).kind(FlecsConstants.EcsOnUpdate).multiThreaded().iter(iter -> {
                Field<Health> healthField = iter.field(Health.class, 0);
                for(int i = 0; i < iter.count(); i++) {
                    HealthView health = healthField.getMutView(i);
                    health.value(health.value() - 1);
                }
            });

            for(int i = 0; i < 1000; i++) {
                world.progress();
            }

            executor.close();
        }

    }
}
