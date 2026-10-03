package io.github.elebras1.flecs.benchmark.flecs;

import io.github.elebras1.flecs.Flecs;
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
public class PrefabBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;
    private long[] ids;
    private int[] order;

    @Setup(Level.Trial)
    public void setup() {
        this.world = new World();
        long positionId = this.world.component(Position.class);
        this.world.component(Velocity.class);
        this.world.obtainEntity(positionId).add(Flecs.OnInstantiate, Flecs.Inherit);
        long prefabId = this.world.prefab();
        this.world.obtainEntityView(prefabId)
                .insert(Position.class, (PositionMutView position) -> {
                    position.x(1.0f);
                    position.y(2.0f);
                })
                .insert(Velocity.class, (VelocityMutView velocity) -> {
                    velocity.dx(0.5f);
                    velocity.dy(0.25f);
                });
        this.ids = this.world.entityBulk(this.n);
        for (long entityId : this.ids) {
            this.world.obtainEntity(entityId).isA(prefabId);
        }
        this.order = BenchUtils.shuffledIndices(this.n);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (this.world != null) {
            this.world.close();
            this.world = null;
        }
    }

    @Benchmark
    public double prefabInheritGet() {
        double checksum = 0.0;
        int[] order = this.order;
        long[] ids = this.ids;
        for (int i = 0; i < this.n; i++) {
            Position position = this.world.obtainEntity(ids[order[i]]).get(Position.class);
            checksum += position.x();
        }
        return checksum / this.n;
    }
}
