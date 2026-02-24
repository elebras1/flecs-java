package com.github.elebras1.flecs.benchmark.artemisodb;

import com.artemis.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class EntityCreationBenchmark {

    private World ecsWorld;
    private Archetype prefabArchetype;

    @Setup(Level.Invocation)
    public void setup() {
        WorldConfiguration configuration = new WorldConfiguration();
        this.ecsWorld = new World(configuration);
        this.prefabArchetype = new ArchetypeBuilder().add(Health.class).add(Ideology.class).build(this.ecsWorld);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void create(Blackhole bh) {
        for (int i = 0; i < 100_000; i++) {
            bh.consume(this.ecsWorld.create());
        }
        this.ecsWorld.process();
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void createWith2Components(Blackhole bh) {
        Class<Health> healthClass = Health.class;
        Class<Ideology> ideologyClass = Ideology.class;
        for (int i = 0; i < 100_000; i++) {
            int entityId = this.ecsWorld.create();
            EntityEdit edit = this.ecsWorld.edit(entityId);
            Health health = edit.create(healthClass);
            health.value = 100;
            Ideology ideology = edit.create(ideologyClass);
            ideology.color = 0xFF0000;
            ideology.factionDriftingSpeed = 10;
            ideology.stabilityIndex = 50;
            bh.consume(entityId);
        }

        this.ecsWorld.process();
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void createWith2ComponentsFromPrefab(Blackhole bh) {
        ComponentMapper<Health> healthMapper = this.ecsWorld.getMapper(Health.class);
        ComponentMapper<Ideology> ideologyMapper = this.ecsWorld.getMapper(Ideology.class);

        for (int i = 0; i < 100_000; i++) {
            int entityId = this.ecsWorld.create(this.prefabArchetype);

            Health health = healthMapper.get(entityId);
            health.value = 100;

            Ideology ideology = ideologyMapper.get(entityId);
            ideology.color = 0xFF0000;
            ideology.factionDriftingSpeed = 10;
            ideology.stabilityIndex = 50;

            bh.consume(entityId);
        }

        this.ecsWorld.process();
    }
}