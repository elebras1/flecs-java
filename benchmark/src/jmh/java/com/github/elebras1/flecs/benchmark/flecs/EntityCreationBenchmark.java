package com.github.elebras1.flecs.benchmark.flecs;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class EntityCreationBenchmark {

    private World ecsWorld;

    @Setup(Level.Invocation)
    public void setup() {
        this.ecsWorld = new World();
        this.ecsWorld.component(Health.class);
        this.ecsWorld.component(Ideology.class);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        if (this.ecsWorld != null) {
            this.ecsWorld.close();
        }
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void create(Blackhole bh) {
        for (int i = 0; i < 100_000; i++) {
            bh.consume(this.ecsWorld.entity());
        }
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void createWith2Components(Blackhole bh) {
        for (int i = 0; i < 100_000; i++) {
            long entityId = this.ecsWorld.entity();
            Entity entity = this.ecsWorld.obtainEntity(entityId);
            entity.set(new Health(100));
            entity.set(new Ideology(0xFF0000, 10, 50));
            bh.consume(entity);
        }
    }
}