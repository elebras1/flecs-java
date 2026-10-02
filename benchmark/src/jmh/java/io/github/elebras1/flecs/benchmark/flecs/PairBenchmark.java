package io.github.elebras1.flecs.benchmark.flecs;

import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Query;
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
public class PairBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;
    private long[] plainIds;
    private long[] pairIds;
    private Query query;
    private int[] order;
    private double checksum;

    @Setup(Level.Invocation)
    public void setup() {
        this.world = new World();
        this.world.component(Likes.class);
        this.world.component(Apples.class);
        this.plainIds = this.world.entityBulk(this.n);
        this.pairIds = this.world.entityBulk(this.n);
        for (long entityId : this.pairIds) {
            this.world.obtainEntity(entityId).add(Likes.class, Apples.class);
        }
        this.query = this.world.query().with(Likes.class, Flecs.Wildcard).build();
        this.order = BenchUtils.shuffledIndices(this.n);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        if (this.query != null) {
            this.query = null;
        }
        if (this.world != null) {
            this.world.close();
            this.world = null;
        }
    }

    @Benchmark
    public double pairAdd() {
        long checksum = 0L;
        int[] order = this.order;
        long[] ids = this.plainIds;
        for (int i = 0; i < this.n; i++) {
            long entityId = ids[order[i]];
            this.world.obtainEntity(entityId).add(Likes.class, Apples.class);
            checksum += entityId;
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double pairIterate() {
        this.checksum = 0.0;
        this.query.each(entityId -> this.checksum += entityId);
        return this.checksum / this.n;
    }
}
