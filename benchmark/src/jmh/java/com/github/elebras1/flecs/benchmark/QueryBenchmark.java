package com.github.elebras1.flecs.benchmark;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class QueryBenchmark {

    private World ecsWorld;
    private Query query;

    @Setup(Level.Trial)
    public void setup() {
        this.ecsWorld = new World();
        this.ecsWorld.component(Health.class);
        this.ecsWorld.component(Ideology.class);
        for (int i = 0; i < 100_000; i++) {
            long entityId = this.ecsWorld.entity();
            Entity entity = this.ecsWorld.obtainEntity(entityId);
            entity.set(new Health(100));
            entity.set(new Ideology(0xFF0000, 10, 50));
        }
        this.query = this.ecsWorld.query().with(Health.class).with(Ideology.class).build();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (this.query != null) {
            this.query.close();
        }
        if (this.ecsWorld != null) {
            this.ecsWorld.close();
        }
    }

    @Benchmark
    @OperationsPerInvocation(100_000)
    public void query(Blackhole bh) {
        this.query.iter(iter -> {
            Field<Health> healthField = iter.field(Health.class, 0);
            Field<Ideology> ideologyField = iter.field(Ideology.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                HealthView healthView = healthField.getMutView(i);
                IdeologyView ideologyView = ideologyField.getMutView(i);

                int v1 = healthView.value() + 1;
                healthView.value(v1);

                int v2 = ideologyView.color() + 1;
                ideologyView.color(v2);

                int v3 = ideologyView.factionDriftingSpeed() + 1;
                ideologyView.factionDriftingSpeed(v3);

                bh.consume(v1);
                bh.consume(v2);
                bh.consume(v3);
            }
        });
    }
}
