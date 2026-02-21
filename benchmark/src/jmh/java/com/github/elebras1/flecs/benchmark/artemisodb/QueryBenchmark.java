package com.github.elebras1.flecs.benchmark.artemisodb;

import com.artemis.*;
import com.artemis.utils.IntBag;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class QueryBenchmark {
    private World ecsWorld;
    private EntitySubscription subscription;

    private ComponentMapper<Health> mHealth;
    private ComponentMapper<Ideology> mIdeology;

    @Setup(Level.Trial)
    public void setup() {
        WorldConfiguration configuration = new WorldConfigurationBuilder().build();
        this.ecsWorld = new World(configuration);

        this.mHealth = this.ecsWorld.getMapper(Health.class);
        this.mIdeology = this.ecsWorld.getMapper(Ideology.class);

        this.subscription = this.ecsWorld.getAspectSubscriptionManager().get(Aspect.all(Health.class, Ideology.class));

        for (int i = 0; i < 100_000; i++) {
            int entityId = this.ecsWorld.create();
            mHealth.create(entityId).value = 100;
            Ideology ideology = mIdeology.create(entityId);
            ideology.color = 0xFF0000;
            ideology.factionDriftingSpeed = 10;
        }

        this.ecsWorld.process();
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void query(Blackhole bh) {
        IntBag entities = this.subscription.getEntities();
        int[] ids = entities.getData();
        for (int i = 0, s = entities.size(); i < s; i++) {
            int entityId = ids[i];

            Health health = this.mHealth.get(entityId);
            Ideology ideology = this.mIdeology.get(entityId);

            health.value += 1;
            ideology.color += 1;
            ideology.factionDriftingSpeed += 1;

            bh.consume(health.value);
            bh.consume(ideology.color);
            bh.consume(ideology.factionDriftingSpeed);
        }
    }
}
