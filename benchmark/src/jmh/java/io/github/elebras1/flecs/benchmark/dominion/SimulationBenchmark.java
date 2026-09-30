package io.github.elebras1.flecs.benchmark.dominion;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;
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

    static {
        System.setProperty("dominion.show-banner", "false");
    }

    @Param({"1000", "10000", "100000"})
    public int n;

    private Dominion dominion;
    private Results<Results.With2<Position, Velocity>> movement;
    private Entity[] pool;
    private Entity[] created;
    private int cursor;

    @Setup(Level.Iteration)
    public void setup() {
        this.dominion = Dominion.create();
        this.pool = new Entity[this.n];
        for (int i = 0; i < this.n; i++) {
            this.pool[i] = this.dominion.createEntity(
                    new Position(0.0f, 0.0f), new Velocity(0.0f, 0.0f));
        }
        this.movement = this.dominion.findCompositionsWith(Position.class, Velocity.class);
        this.created = new Entity[this.n / 10];
        this.cursor = 0;
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        this.dominion.close();
    }

    @Benchmark
    public double mixedSimulation() {
        int tenth = this.n / 10;
        double checksum = 0.0;

        for (int i = 0; i < tenth; i++) {
            this.created[i] = this.dominion.createEntity(
                    new Position(1.0f, 2.0f), new Velocity(0.5f, 0.25f));
        }

        for (Results.With2<Position, Velocity> result : this.movement) {
            Position position = result.comp1();
            Velocity velocity = result.comp2();
            position.x += velocity.dx;
            position.y += velocity.dy;
            checksum += position.x + position.y;
        }

        for (int i = 0; i < tenth; i++) {
            int slot = (this.cursor + i) % this.n;
            this.dominion.deleteEntity(this.pool[slot]);
            this.pool[slot] = this.created[i];
        }
        this.cursor = (this.cursor + tenth) % this.n;

        return checksum / this.n;
    }
}
