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
public class ThreadingBenchmark {

    private static final int THREADS = 4;

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;

    @Setup(Level.Trial)
    public void setup() {
        this.world = new World();
        this.world.component(Position.class);
        this.world.component(Velocity.class);
        this.world.setThreads(THREADS);
        for (int i = 0; i < this.n; i++) {
            this.world.obtainEntityView(this.world.entity())
                    .insert(Position.class, (PositionView position) -> {
                        position.x(1.0f);
                        position.y(2.0f);
                    })
                    .insert(Velocity.class, (VelocityView velocity) -> {
                        velocity.dx(0.5f);
                        velocity.dy(0.25f);
                    });
        }
        this.world.system("MultiThreadedMove", Position.class, Velocity.class)
                .kind(Flecs.OnUpdate)
                .multiThreaded(true)
                .eachView(Position.class, Velocity.class, (PositionView position, VelocityView velocity) -> {
                    position.x(position.x() + velocity.dx());
                    position.y(position.y() + velocity.dy());
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
    public double multiThreadedProgress() {
        this.world.progress();
        return 1.0;
    }
}
