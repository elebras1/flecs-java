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
public class HierarchyBenchmark {

    @State(Scope.Benchmark)
    public static class BuildState {

        @Param({"1000", "10000", "100000"})
        public int n;

        private World world;

        @Setup(Level.Invocation)
        public void setup() {
            this.world = new World();
            this.world.component(Position.class);
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
    public static class TraverseState {

        @Param({"1000", "10000", "100000"})
        public int n;

        private World world;
        private Query query;
        private double checksum;

        @Setup(Level.Iteration)
        public void setup() {
            this.world = new World();
            this.world.component(Position.class);
            int pairs = this.n / 2;
            long[] parents = this.world.entityBulk(pairs, Position.class);
            long[] children = this.world.entityBulk(pairs);
            for (int i = 0; i < pairs; i++) {
                this.world.obtainEntity(children[i]).childOf(parents[i]);
            }
            this.query = this.world.query().with(Position.class).up(Flecs.ChildOf).build();
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
    public double hierarchyBuild(BuildState state) {
        long checksum = 0L;
        int pairs = state.n / 2;
        for (int i = 0; i < pairs; i++) {
            long parent = state.world.entity();
            long child = state.world.entity();
            state.world.obtainEntity(child).childOf(parent);
            checksum += parent + child;
        }
        return (double) checksum / state.n;
    }

    @Benchmark
    public double hierarchyTraverse(TraverseState state) {
        state.checksum = 0.0;
        state.query.eachView(Position.class, (PositionView position) -> state.checksum += position.x() + 1.0);
        return state.checksum / state.n;
    }
}
