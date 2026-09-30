package io.github.elebras1.flecs.benchmark.dominion;

import dev.dominion.ecs.api.Dominion;
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
public class QueryBenchmark {

    static {
        System.setProperty("dominion.show-banner", "false");
    }

    @Param({"1000", "10000", "100000"})
    public int n;

    private Dominion dominion;
    private Results<Position> positionResults;
    private Results<Results.With2<Position, Velocity>> positionVelocityResults;
    private Results<Results.With2<Position, Tag>> positionTagResults;

    @Setup(Level.Iteration)
    public void setup() {
        this.dominion = Dominion.create();
        for (int i = 0; i < this.n; i++) {
            this.dominion.createEntity(
                    new Position(1.0f, 2.0f), new Velocity(0.5f, 0.25f), new Tag());
        }
        this.positionResults = this.dominion.findCompositionsWith(Position.class);
        this.positionVelocityResults =
                this.dominion.findCompositionsWith(Position.class, Velocity.class);
        this.positionTagResults =
                this.dominion.findCompositionsWith(Position.class, Tag.class);
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        this.dominion.close();
    }

    @Benchmark
    public double query1Read() {
        double checksum = 0.0;
        for (Position position : this.positionResults) {
            checksum += position.x;
        }
        return checksum / this.n;
    }

    @Benchmark
    public double query2ReadWrite() {
        double checksum = 0.0;
        for (Results.With2<Position, Velocity> result : this.positionVelocityResults) {
            Position position = result.comp1();
            Velocity velocity = result.comp2();
            position.x += velocity.dx;
            position.y += velocity.dy;
            checksum += position.x;
        }
        return checksum / this.n;
    }

    @Benchmark
    public double queryFiltered() {
        double checksum = 0.0;
        for (Results.With2<Position, Tag> result : this.positionTagResults) {
            checksum += result.comp1().x;
        }
        return checksum / this.n;
    }

}
