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
public class ComponentBenchmark {

    @State(Scope.Benchmark)
    public static class MutationState {

        @Param({"1000", "10000", "100000"})
        public int n;

        private World world;
        private long[] positionIds;
        private long[] positionVelocityIds;
        private int[] order;

        @Setup(Level.Invocation)
        public void setup() {
            this.world = new World();
            this.world.component(Position.class);
            this.world.component(Velocity.class);
            this.positionIds = this.world.entityBulk(this.n, Position.class);
            this.positionVelocityIds = this.world.entityBulk(this.n, Position.class, Velocity.class);
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
    public static class LookupState {

        @Param({"1000", "10000", "100000"})
        public int n;

        private World world;
        private String[] names;
        private int[] order;

        @Setup(Level.Iteration)
        public void setup() {
            this.world = new World();
            this.names = new String[this.n];
            for (int i = 0; i < this.n; i++) {
                this.names[i] = "e" + i;
                this.world.entity(this.names[i]);
            }
            this.order = BenchUtils.shuffledIndices(this.n);
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            if (this.world != null) {
                this.world.close();
                this.world = null;
            }
        }
    }

    @State(Scope.Benchmark)
    public static class AccessState {

        @Param({"1000", "10000", "100000"})
        public int n;

        private World world;
        private long[] ids;
        private int[] order;
        private int cursor;

        @Setup(Level.Iteration)
        public void setup() {
            this.world = new World();
            this.world.component(Position.class);
            this.ids = this.world.entityBulk(this.n, Position.class);
            this.order = BenchUtils.shuffledIndices(this.n);
            this.cursor = 0;
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            if (this.world != null) {
                this.world.close();
                this.world = null;
            }
        }

        long nextId() {
            int index = this.order[this.cursor];
            this.cursor++;
            if (this.cursor == this.n) {
                this.cursor = 0;
            }
            return this.ids[index];
        }
    }

    @Benchmark
    public double addComponent(MutationState state) {
        long checksum = 0L;
        int[] order = state.order;
        long[] ids = state.positionIds;
        for (int i = 0; i < state.n; i++) {
            long entityId = ids[order[i]];
            state.world.obtainEntity(entityId).add(Velocity.class);
            checksum += entityId;
        }
        return (double) checksum / state.n;
    }

    @Benchmark
    public double removeComponent(MutationState state) {
        long checksum = 0L;
        int[] order = state.order;
        long[] ids = state.positionVelocityIds;
        for (int i = 0; i < state.n; i++) {
            long entityId = ids[order[i]];
            state.world.obtainEntity(entityId).remove(Velocity.class);
            checksum += entityId;
        }
        return (double) checksum / state.n;
    }

    @Benchmark
    public double get(AccessState state) {
        double checksum = 0.0;
        for (int i = 0; i < state.n; i++) {
            PositionView position = state.world.obtainEntityView(state.nextId()).getMutView(Position.class);
            checksum += position.x() + position.y();
        }
        return checksum / state.n;
    }

    @Benchmark
    public double getSet(AccessState state) {
        double checksum = 0.0;
        for (int i = 0; i < state.n; i++) {
            PositionView position = state.world.obtainEntityView(state.nextId()).getMutView(Position.class);
            float x = position.x();
            position.x(x + 1.0f);
            position.y(position.y() + 1.0f);
            checksum += x;
        }
        return checksum / state.n;
    }

    @Benchmark
    public double has(AccessState state) {
        long checksum = 0L;
        for (int i = 0; i < state.n; i++) {
            if (state.world.obtainEntityView(state.nextId()).has(Position.class)) {
                checksum++;
            }
        }
        return (double) checksum / state.n;
    }

    @Benchmark
    public double lookup(LookupState state) {
        long checksum = 0L;
        for (int i = 0; i < state.n; i++) {
            checksum += state.world.lookup(state.names[state.order[i]]);
        }
        return (double) checksum / state.n;
    }
}
