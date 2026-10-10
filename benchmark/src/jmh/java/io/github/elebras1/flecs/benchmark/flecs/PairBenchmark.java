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

    @State(Scope.Benchmark)
    public static class AddState {

        @Param({"1000", "10000", "100000"})
        public int n;

        private World world;
        private long[] plainIds;
        private int[] order;

        @Setup(Level.Invocation)
        public void setup() {
            this.world = new World();
            this.world.component(Likes.class);
            this.world.component(Apples.class);
            this.plainIds = this.world.entityBulk(this.n);
            this.order = BenchUtils.shuffledIndices(this.n);
        }

        @TearDown(Level.Invocation)
        public void tearDown() {
            if (this.world != null) {
                this.world.close();
                this.world = null;
            }
        }
    }

    @State(Scope.Benchmark)
    public static class IterateState {

        @Param({"1000", "10000", "100000"})
        public int n;

        private World world;
        private Query query;
        private double checksum;

        @Setup(Level.Iteration)
        public void setup() {
            this.world = new World();
            this.world.component(Likes.class);
            this.world.component(Apples.class);
            long[] pairIds = this.world.entityBulk(this.n);
            for (long entityId : pairIds) {
                this.world.obtainEntity(entityId).add(Likes.class, Apples.class);
            }
            this.query = this.world.query().with(Likes.class, Flecs.Wildcard).build();
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            this.query = null;
            if (this.world != null) {
                this.world.close();
                this.world = null;
            }
        }
    }

    @Benchmark
    public double pairAdd(AddState state) {
        long checksum = 0L;
        int[] order = state.order;
        long[] ids = state.plainIds;
        for (int i = 0; i < state.n; i++) {
            long entityId = ids[order[i]];
            state.world.obtainEntity(entityId).add(Likes.class, Apples.class);
            checksum += entityId;
        }
        return (double) checksum / state.n;
    }

    @Benchmark
    public double pairIterate(IterateState state) {
        state.checksum = 0.0;
        state.query.each(entityId -> state.checksum += entityId);
        return state.checksum / state.n;
    }
}
