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
public class CreationBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;
    private long prefabId;

    @Setup(Level.Invocation)
    public void setup() {
        this.world = new World();
        this.world.component(Position.class);
        this.world.component(Velocity.class);
        this.prefabId = this.world.prefab();
        this.world.obtainEntityView(this.prefabId)
                .insert(Position.class, (PositionMutView position) -> {
                    position.x(1.0f);
                    position.y(2.0f);
                })
                .insert(Velocity.class, (VelocityMutView velocity) -> {
                    velocity.dx(0.5f);
                    velocity.dy(0.25f);
                });
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        if (this.world != null) {
            this.world.close();
            this.world = null;
        }
    }

    @Benchmark
    public double createEmpty() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            checksum += this.world.entity();
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double create1() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            long entityId = this.world.entity();
            this.world.obtainEntityView(entityId).insert(Position.class, (PositionMutView position) -> {
                position.x(1.0f);
                position.y(2.0f);
            });
            checksum += entityId;
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double create2() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            long entityId = this.world.entity();
            this.world.obtainEntityView(entityId)
                    .insert(Position.class, (PositionMutView position) -> {
                        position.x(1.0f);
                        position.y(2.0f);
                    })
                    .insert(Velocity.class, (VelocityMutView velocity) -> {
                        velocity.dx(0.5f);
                        velocity.dy(0.25f);
                    });
            checksum += entityId;
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double createFromPrefab() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            long entityId = this.world.entity();
            this.world.obtainEntity(entityId).isA(this.prefabId);
            checksum += entityId;
        }
        return (double) checksum / this.n;
    }
}
