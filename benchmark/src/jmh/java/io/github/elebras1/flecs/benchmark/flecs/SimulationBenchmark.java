package io.github.elebras1.flecs.benchmark.flecs;

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
public class SimulationBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;
    private Query movement;
    private long[] pool;
    private long[] created;
    private int cursor;
    private double checksum;

    @Setup(Level.Iteration)
    public void setup() {
        this.world = new World();
        this.world.component(Position.class);
        this.world.component(Velocity.class);
        this.pool = this.world.entityBulk(this.n, Position.class, Velocity.class);
        this.created = new long[this.n / 10];
        this.movement = this.world.query().with(Position.class).with(Velocity.class).build();
        this.cursor = 0;
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        if (this.movement != null) {
            this.movement = null;
        }
        if (this.world != null) {
            this.world.close();
            this.world = null;
        }
    }

    @Benchmark
    public double mixedSimulation() {
        int tenth = this.n / 10;
        this.checksum = 0.0;

        for (int i = 0; i < tenth; i++) {
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
            this.created[i] = entityId;
        }

        this.movement.eachView(Position.class, Velocity.class, (PositionMutView position, VelocityView velocity) -> {
            position.x(position.x() + velocity.dx());
            position.y(position.y() + velocity.dy());
            this.checksum += position.x() + position.y();
        });

        for (int i = 0; i < tenth; i++) {
            int slot = (this.cursor + i) % this.n;
            this.world.obtainEntity(this.pool[slot]).destruct();
            this.pool[slot] = this.created[i];
        }
        this.cursor = (this.cursor + tenth) % this.n;

        return this.checksum / this.n;
    }
}
