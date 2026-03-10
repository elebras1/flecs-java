package com.github.elebras1.flecs.benchmark.artemisodb;

import com.artemis.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class EntityRemoveBenchmark {

    private World ecsWorld;
    private int[] entityIds;

    @Setup(Level.Invocation)
    public void setup() {
        Class<Health> healthClass = Health.class;
        Class<Ideology> ideologyClass = Ideology.class;
        WorldConfiguration configuration = new WorldConfiguration();
        this.ecsWorld = new World(configuration);
        this.entityIds = new int[100_000];
        for (int i = 0; i < 100_000; i++) {
            this.entityIds[i] = this.ecsWorld.create();
            int entityId = this.entityIds[i];
            EntityEdit edit = this.ecsWorld.edit(entityId);
            Health health = edit.create(healthClass);
            health.value = 100;
            Ideology ideology = edit.create(ideologyClass);
            ideology.color = 0xFF0000;
            ideology.factionDriftingSpeed = 10;
            ideology.stabilityIndex = 50;
        }
        this.ecsWorld.process();
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void destructWith2Components(Blackhole bh) {
        for (int entityId : this.entityIds) {
            this.ecsWorld.delete(entityId);
            bh.consume(entityId);
        }
        this.ecsWorld.process();
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void remove1Component(Blackhole bh) {
        for (int entityId : this.entityIds) {
            this.ecsWorld.edit(entityId).remove(Health.class);
            bh.consume(entityId);
        }
        this.ecsWorld.process();
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void remove2Components(Blackhole bh) {
        for (int entityId : this.entityIds) {
            this.ecsWorld.edit(entityId).remove(Health.class).remove(Ideology.class);
            bh.consume(entityId);
        }
        this.ecsWorld.process();
    }
}
