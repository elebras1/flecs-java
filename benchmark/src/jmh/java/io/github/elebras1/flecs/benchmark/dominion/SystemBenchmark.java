package io.github.elebras1.flecs.benchmark.dominion;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Scheduler;
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
public class SystemBenchmark {

    static {
        System.setProperty("dominion.show-banner", "false");
    }

    @Param({"1000", "10000", "100000"})
    public int n;

    private Dominion dominion;
    private Dominion dominion5;
    private Scheduler scheduler;
    private Scheduler scheduler5;
    private double checksum;

    @Setup(Level.Trial)
    public void setup() {
        this.dominion = Dominion.create();
        for (int i = 0; i < this.n; i++) {
            this.dominion.createEntity(new Position(1.0f, 2.0f), new Velocity(0.5f, 0.25f));
        }

        final Results<Results.With2<Position, Velocity>> results =
                this.dominion.findCompositionsWith(Position.class, Velocity.class);
        Runnable system = new Runnable() {
            @Override
            public void run() {
                double sum = 0.0;
                for (Results.With2<Position, Velocity> result : results) {
                    Position position = result.comp1();
                    Velocity velocity = result.comp2();
                    position.x += velocity.dx;
                    position.y += velocity.dy;
                    sum += position.x;
                }
                checksum = sum;
            }
        };

        this.scheduler = this.dominion.createScheduler();
        this.scheduler.schedule(system);

        this.dominion5 = Dominion.create();
        for (int i = 0; i < this.n; i++) {
            this.dominion5.createEntity(
                    new Position(1.0f, 2.0f),
                    new Velocity(0.5f, 0.25f),
                    new Health(100),
                    new Mass(1.0f),
                    new Age(10));
        }

        final Results<Results.With5<Position, Velocity, Health, Mass, Age>> results5 =
                this.dominion5.findCompositionsWith(Position.class, Velocity.class, Health.class, Mass.class, Age.class);
        Runnable system5 = new Runnable() {
            @Override
            public void run() {
                double sum = 0.0;
                for (Results.With5<Position, Velocity, Health, Mass, Age> result : results5) {
                    Position position = result.comp1();
                    Velocity velocity = result.comp2();
                    position.x += velocity.dx;
                    position.y += velocity.dy;
                    result.comp3().value += 1;
                    result.comp4().value += 0.5f;
                    result.comp5().value += 1;
                    sum += position.x;
                }
                checksum = sum;
            }
        };

        this.scheduler5 = this.dominion5.createScheduler();
        this.scheduler5.schedule(system5);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        this.scheduler.shutDown();
        this.dominion.close();
        this.scheduler5.shutDown();
        this.dominion5.close();
    }

    @Benchmark
    public double systemRun() {
        this.checksum = 0.0;
        this.scheduler.tick();
        return this.checksum / this.n;
    }

    @Benchmark
    public double systemRun5() {
        this.checksum = 0.0;
        this.scheduler5.tick();
        return this.checksum / this.n;
    }
}
