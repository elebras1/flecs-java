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
public class QueryBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;
    private Query query1;
    private Query query2;
    private Query queryFiltered;
    private double checksum;

    @Setup(Level.Iteration)
    public void setup() {
        this.world = new World();
        this.world.component(Position.class);
        this.world.component(Velocity.class);
        this.world.component(Tag.class);
        for (int i = 0; i < this.n; i++) {
            this.world.obtainEntityView(this.world.entity())
                    .insert(Position.class, (PositionView position) -> {
                        position.x(1.0f);
                        position.y(2.0f);
                    })
                    .insert(Velocity.class, (VelocityView velocity) -> {
                        velocity.dx(0.5f);
                        velocity.dy(0.25f);
                    })
                    .add(Tag.class);
        }
        this.query1 = this.world.query().with(Position.class).build();
        this.query2 = this.world.query().with(Position.class).with(Velocity.class).build();
        this.queryFiltered = this.world.query().with(Position.class).with(Tag.class).build();
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        if (this.query1 != null) {
            this.query1.destroy();
            this.query1 = null;
        }
        if (this.query2 != null) {
            this.query2.destroy();
            this.query2 = null;
        }
        if (this.queryFiltered != null) {
            this.queryFiltered.destroy();
            this.queryFiltered = null;
        }
        if (this.world != null) {
            this.world.destroy();
            this.world = null;
        }
    }

    @Benchmark
    public double query1Read() {
        this.checksum = 0.0;
        this.query1.eachView(Position.class, (PositionView position) -> this.checksum += position.x());
        return this.checksum / this.n;
    }

    @Benchmark
    public double query2ReadWrite() {
        this.checksum = 0.0;
        this.query2.eachView(Position.class, Velocity.class, (PositionView position, VelocityView velocity) -> {
            position.x(position.x() + velocity.dx());
            position.y(position.y() + velocity.dy());
            this.checksum += position.x();
        });
        return this.checksum / this.n;
    }

    @Benchmark
    public double queryFiltered() {
        this.checksum = 0.0;
        this.queryFiltered.eachView(Position.class, (PositionView position) -> this.checksum += position.x());
        return this.checksum / this.n;
    }

    @Benchmark
    public double queryCreate() {
        long checksum = 0L;
        for (int i = 0; i < this.n; i++) {
            Query query = this.world.query().with(Position.class).with(Velocity.class).build();
            checksum++;
            query.destroy();
        }
        return (double) checksum / this.n;
    }
}
