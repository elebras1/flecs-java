package io.github.elebras1.flecs.benchmark.artemisodb;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfiguration;
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

    @Param({"1000", "10000", "100000"})
    public int n;

    private World ecsWorld;
    private ComponentMapper<Position> mPosition;
    private ComponentMapper<Velocity> mVelocity;
    private int[] positionIds;
    private int[] positionVelocityIds;
    private int[] order;

    @Setup(Level.Invocation)
    public void setup() {
        this.ecsWorld = new World(new WorldConfiguration());
        this.mPosition = this.ecsWorld.getMapper(Position.class);
        this.mVelocity = this.ecsWorld.getMapper(Velocity.class);

        this.positionIds = new int[this.n];
        for (int i = 0; i < this.n; i++) {
            this.positionIds[i] = this.ecsWorld.create();
            this.mPosition.create(this.positionIds[i]);
        }

        this.positionVelocityIds = new int[this.n];
        for (int i = 0; i < this.n; i++) {
            this.positionVelocityIds[i] = this.ecsWorld.create();
            this.mPosition.create(this.positionVelocityIds[i]);
            this.mVelocity.create(this.positionVelocityIds[i]);
        }
        this.ecsWorld.process();

        this.order = BenchUtils.shuffledIndices(this.n);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        this.ecsWorld = null;
    }

    @Benchmark
    public double addComponent() {
        long checksum = 0L;
        int[] order = this.order;
        int[] ids = this.positionIds;
        for (int i = 0; i < this.n; i++) {
            int entityId = ids[order[i]];
            this.mVelocity.create(entityId);
            checksum += entityId;
        }
        this.ecsWorld.process();
        return (double) checksum / this.n;
    }

    @Benchmark
    public double removeComponent() {
        long checksum = 0L;
        int[] order = this.order;
        int[] ids = this.positionVelocityIds;
        for (int i = 0; i < this.n; i++) {
            int entityId = ids[order[i]];
            this.ecsWorld.edit(entityId).remove(Velocity.class);
            checksum += entityId;
        }
        this.ecsWorld.process();
        return (double) checksum / this.n;
    }

    @Benchmark
    public double get() {
        double checksum = 0.0;
        int[] order = this.order;
        int[] ids = this.positionIds;
        for (int i = 0; i < this.n; i++) {
            Position position = this.mPosition.get(ids[order[i]]);
            checksum += position.x + position.y;
        }
        return checksum / this.n;
    }

    @Benchmark
    public double getSet() {
        double checksum = 0.0;
        int[] order = this.order;
        int[] ids = this.positionIds;
        for (int i = 0; i < this.n; i++) {
            Position position = this.mPosition.get(ids[order[i]]);
            float x = position.x;
            float y = position.y;
            position.x = x + 1.0f;
            position.y = y + 1.0f;
            checksum += x;
        }
        return checksum / this.n;
    }

    @Benchmark
    public double has() {
        long checksum = 0L;
        int[] order = this.order;
        int[] ids = this.positionIds;
        for (int i = 0; i < this.n; i++) {
            if (this.mPosition.has(ids[order[i]])) {
                checksum++;
            }
        }
        return (double) checksum / this.n;
    }
}
