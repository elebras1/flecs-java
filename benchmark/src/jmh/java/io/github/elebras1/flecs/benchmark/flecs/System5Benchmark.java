package io.github.elebras1.flecs.benchmark.flecs;

import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.FlecsSystem;
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
public class System5Benchmark {

    @Param({"1000", "10000", "100000"})
    public int n;

    private World world;
    private FlecsSystem system;
    private double checksum;

    @Setup(Level.Trial)
    public void setup() {
        this.world = new World();
        this.world.component(Position.class);
        this.world.component(Velocity.class);
        this.world.component(Health.class);
        this.world.component(Mass.class);
        this.world.component(Age.class);
        for (int i = 0; i < this.n; i++) {
            this.world.obtainEntityView(this.world.entity())
                    .insert(Position.class, (PositionMutView position) -> {
                        position.x(1.0f);
                        position.y(2.0f);
                    })
                    .insert(Velocity.class, (VelocityMutView velocity) -> {
                        velocity.dx(0.5f);
                        velocity.dy(0.25f);
                    })
                    .insert(Health.class, (HealthMutView health) -> health.value(100))
                    .insert(Mass.class, (MassMutView mass) -> mass.value(1.0f))
                    .insert(Age.class, (AgeMutView age) -> age.value(10));
        }
        this.system = this.world.system("Move5", Position.class, Velocity.class, Health.class, Mass.class, Age.class)
                .kind(Flecs.OnUpdate)
                .eachView(Position.class, Velocity.class, Health.class, Mass.class, Age.class,
                        (PositionMutView position, VelocityView velocity, HealthMutView health, MassMutView mass, AgeMutView age) -> {
                            position.x(position.x() + velocity.dx());
                            position.y(position.y() + velocity.dy());
                            health.value(health.value() + 1);
                            mass.value(mass.value() + 0.5f);
                            age.value(age.value() + 1);
                            this.checksum += position.x();
                        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (this.world != null) {
            this.world.close();
            this.world = null;
        }
    }

    @Benchmark
    public double systemRun5() {
        this.checksum = 0.0;
        this.system.run();
        return this.checksum / this.n;
    }
}
