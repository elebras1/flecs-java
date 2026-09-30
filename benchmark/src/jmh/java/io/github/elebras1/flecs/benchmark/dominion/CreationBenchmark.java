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
public class CreationBenchmark {

    static {
        System.setProperty("dominion.show-banner", "false");
    }

    @Param({"1000", "10000", "100000"})
    public int n;

    private Dominion dominion;
    private Entity prefab;
    private Entity[] created;

    @Setup(Level.Trial)
    public void setup() {
        this.dominion = Dominion.create();
        this.prefab = this.dominion.createEntity(new Position(1.0f, 2.0f), new Velocity(0.5f, 0.25f));
        this.created = new Entity[this.n];
    }

    @TearDown(Level.Invocation)
    public void deleteCreated() {
        for (int i = 0; i < this.n; i++) {
            this.dominion.deleteEntity(this.created[i]);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        this.dominion.close();
    }

    @Benchmark
    public double createEmpty() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            Entity entity = this.dominion.createEntity();
            this.created[i] = entity;
            checksum += entity.isDeleted() ? 0 : 1;
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double create1() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            Entity entity = this.dominion.createEntity(new Position(1.0f, 2.0f));
            this.created[i] = entity;
            checksum += entity.isDeleted() ? 0 : 1;
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double create2() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            Entity entity = this.dominion.createEntity(
                    new Position(1.0f, 2.0f), new Velocity(0.5f, 0.25f));
            this.created[i] = entity;
            checksum += entity.isDeleted() ? 0 : 1;
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double createFromPrefab() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            Entity entity = this.dominion.createEntityAs(this.prefab);
            this.created[i] = entity;
            checksum += entity.isDeleted() ? 0 : 1;
        }
        return (double) checksum / this.n;
    }
}
