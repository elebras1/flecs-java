package io.github.elebras1.flecs.benchmark.flecs;

import io.github.elebras1.flecs.Entity;
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
public class SingletonBenchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;
    private Entity singleton;

    @Setup(Level.Trial)
    public void setup() {
        this.world = new World();
        this.world.component(Position.class);
        this.singleton = this.world.singleton(Position.class);
        this.singleton.insert(Position.class, (PositionView position) -> {
            position.x(1.0f);
            position.y(2.0f);
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (this.world != null) {
            this.world.destroy();
            this.world = null;
        }
    }

    @Benchmark
    public double singletonGetSet() {
        double checksum = 0.0;
        for (int i = 0; i < this.n; i++) {
            PositionView position = this.singleton.getMutView(Position.class);
            position.x(i);
            position.y(i + 1.0f);
            checksum += position.x();
        }
        return checksum / this.n;
    }
}
