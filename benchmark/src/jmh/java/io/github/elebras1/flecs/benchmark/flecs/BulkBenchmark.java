package io.github.elebras1.flecs.benchmark.flecs;

import io.github.elebras1.flecs.World;
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
public class BulkBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;

    @Setup(Level.Invocation)
    public void setup() {
        this.world = new World();
        this.world.component(Position.class);
        this.world.component(Velocity.class);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        if (this.world != null) {
            this.world.close();
            this.world = null;
        }
    }

    @Benchmark
    public double bulkCreate() {
        long[] ids = this.world.entityBulk(this.n, Position.class, Velocity.class);
        return ids[this.n - 1] / (double) this.n;
    }
}
