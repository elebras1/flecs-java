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
public class SetValueBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;
    private long[] ids;
    private int[] order;
    private int cursor;

    @Setup(Level.Invocation)
    public void setup() {
        this.world = new World();
        this.world.component(Position.class);
        this.ids = this.world.entityBulk(this.n);
        this.order = BenchUtils.shuffledIndices(this.n);
        this.cursor = 0;
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        if (this.world != null) {
            this.world.close();
            this.world = null;
        }
    }

    @Benchmark
    public double setValue() {
        long checksum = 0L;
        int[] order = this.order;
        long[] ids = this.ids;
        for (int i = 0; i < this.n; i++) {
            long entityId = ids[order[this.cursor]];
            this.cursor++;
            if (this.cursor == this.n) {
                this.cursor = 0;
            }
            this.world.obtainEntity(entityId).set(new Position(1.0f, 2.0f));
            checksum += entityId;
        }
        return (double) checksum / this.n;
    }
}
