package com.github.elebras1.flecs.benchmark.flecs;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.World;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class EntityRemoveBenchmark {

    private World ecsWorld;
    private long[] entityIds;

    @Setup(Level.Invocation)
    public void setup() {
        this.ecsWorld = new World();
        this.ecsWorld.component(Health.class);
        this.ecsWorld.component(Ideology.class);
        this.entityIds = new long[100_000];
        for (int i = 0; i < 100_000; i++) {
            this.entityIds[i] = this.ecsWorld.entity();
            EntityView entity = this.ecsWorld.obtainEntityView(this.entityIds[i]);
            entity.set(new Health(100));
            entity.set(new Ideology(0xFF0000, 10, 50));
        }
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        if (this.ecsWorld != null) {
            this.ecsWorld.close();
        }
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void destructWith2Component(Blackhole bh) {
        for (long entityId : this.entityIds) {
            EntityView entity = this.ecsWorld.obtainEntityView(entityId);
            entity.destruct();
            bh.consume(entity);
        }
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void remove1Component(Blackhole bh) {
        for (long entityId : this.entityIds) {
            EntityView entity = this.ecsWorld.obtainEntityView(entityId);
            entity.remove(Health.class);
            bh.consume(entity);
        }
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void remove2Components(Blackhole bh) {
        for (long entityId : this.entityIds) {
            EntityView entity = this.ecsWorld.obtainEntityView(entityId);
            entity.remove(Health.class).remove(Ideology.class);
            bh.consume(entity);
        }
    }
}
