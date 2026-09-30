package io.github.elebras1.flecs.benchmark.artemisodb;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
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
public class CreationBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World ecsWorld;
    private ComponentMapper<Position> mPosition;
    private ComponentMapper<Velocity> mVelocity;
    private Archetype prefabArchetype;

    @Setup(Level.Invocation)
    public void setup() {
        this.ecsWorld = new World(new WorldConfiguration());
        this.mPosition = this.ecsWorld.getMapper(Position.class);
        this.mVelocity = this.ecsWorld.getMapper(Velocity.class);
        this.prefabArchetype = new ArchetypeBuilder().add(Position.class).add(Velocity.class).build(this.ecsWorld);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        this.ecsWorld = null;
    }

    @Benchmark
    public double createEmpty() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            checksum += this.ecsWorld.create();
        }
        this.ecsWorld.process();
        return (double) checksum / this.n;
    }

    @Benchmark
    public double create1() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            int entityId = this.ecsWorld.create();
            Position position = this.mPosition.create(entityId);
            position.x = 1.0f;
            position.y = 2.0f;
            checksum += entityId;
        }
        this.ecsWorld.process();
        return (double) checksum / this.n;
    }

    @Benchmark
    public double create2() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            int entityId = this.ecsWorld.create();
            Position position = this.mPosition.create(entityId);
            position.x = 1.0f;
            position.y = 2.0f;
            Velocity velocity = this.mVelocity.create(entityId);
            velocity.dx = 0.5f;
            velocity.dy = 0.25f;
            checksum += entityId;
        }
        this.ecsWorld.process();
        return (double) checksum / this.n;
    }

    @Benchmark
    public double createFromPrefab() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            checksum += this.ecsWorld.create(this.prefabArchetype);
        }
        this.ecsWorld.process();
        return (double) checksum / this.n;
    }
}
