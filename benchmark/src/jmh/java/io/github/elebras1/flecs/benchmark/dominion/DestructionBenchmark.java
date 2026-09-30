package io.github.elebras1.flecs.benchmark.dominion;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@State(Scope.Benchmark)
public class DestructionBenchmark {

    static {
        System.setProperty("dominion.show-banner", "false");
    }

    @Param({"1000", "10000", "100000"})
    public int n;

    private Dominion dominion;
    private Entity[] emptyEntities;
    private Entity[] componentEntities;
    private int[] order;
    private Entity[] destroyed;

    @Setup(Level.Trial)
    public void setup() {
        this.dominion = Dominion.create();

        this.emptyEntities = new Entity[this.n];
        for (int i = 0; i < this.n; i++) {
            this.emptyEntities[i] = this.dominion.createEntity();
        }

        this.componentEntities = new Entity[this.n];
        for (int i = 0; i < this.n; i++) {
            this.componentEntities[i] =
                    this.dominion.createEntity(new Position(0.0f, 0.0f), new Velocity(0.0f, 0.0f));
        }

        this.order = BenchUtils.shuffledIndices(this.n);
    }

    @TearDown(Level.Invocation)
    public void restoreDestroyed() {
        Entity[] entities = this.destroyed;
        if (entities == null) {
            return;
        }
        if (entities == this.emptyEntities) {
            for (int i = 0; i < this.n; i++) {
                this.emptyEntities[i] = this.dominion.createEntity();
            }
        } else {
            for (int i = 0; i < this.n; i++) {
                this.componentEntities[i] =
                        this.dominion.createEntity(new Position(0.0f, 0.0f), new Velocity(0.0f, 0.0f));
            }
        }
        this.destroyed = null;
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        this.dominion.close();
    }

    @Benchmark
    public double destroyEmpty() {
        long checksum = 0L;
        int[] order = this.order;
        Entity[] entities = this.emptyEntities;
        this.destroyed = entities;
        for (int i = 0; i < this.n; i++) {
            if (this.dominion.deleteEntity(entities[order[i]])) {
                checksum++;
            }
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double destroy2() {
        long checksum = 0L;
        int[] order = this.order;
        Entity[] entities = this.componentEntities;
        this.destroyed = entities;
        for (int i = 0; i < this.n; i++) {
            if (this.dominion.deleteEntity(entities[order[i]])) {
                checksum++;
            }
        }
        return (double) checksum / this.n;
    }
}
