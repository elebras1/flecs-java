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
public class DestructionBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World ecsWorld;
    private int[] emptyIds;
    private int[] componentIds;
    private int[] order;

    @Setup(Level.Invocation)
    public void setup() {
        this.ecsWorld = new World(new WorldConfiguration());
        ComponentMapper<Position> mPosition = this.ecsWorld.getMapper(Position.class);
        ComponentMapper<Velocity> mVelocity = this.ecsWorld.getMapper(Velocity.class);

        this.emptyIds = new int[this.n];
        for (int i = 0; i < this.n; i++) {
            this.emptyIds[i] = this.ecsWorld.create();
        }

        this.componentIds = new int[this.n];
        for (int i = 0; i < this.n; i++) {
            int entityId = this.ecsWorld.create();
            mPosition.create(entityId);
            mVelocity.create(entityId);
            this.componentIds[i] = entityId;
        }
        this.ecsWorld.process();

        this.order = BenchUtils.shuffledIndices(this.n);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        this.ecsWorld = null;
    }

    @Benchmark
    public double destroyEmpty() {
        long checksum = 0L;
        int[] order = this.order;
        int[] ids = this.emptyIds;
        for (int i = 0; i < this.n; i++) {
            int entityId = ids[order[i]];
            this.ecsWorld.delete(entityId);
            checksum += entityId;
        }
        this.ecsWorld.process();
        return (double) checksum / this.n;
    }

    @Benchmark
    public double destroy2() {
        long checksum = 0L;
        int[] order = this.order;
        int[] ids = this.componentIds;
        for (int i = 0; i < this.n; i++) {
            int entityId = ids[order[i]];
            this.ecsWorld.delete(entityId);
            checksum += entityId;
        }
        this.ecsWorld.process();
        return (double) checksum / this.n;
    }
}
