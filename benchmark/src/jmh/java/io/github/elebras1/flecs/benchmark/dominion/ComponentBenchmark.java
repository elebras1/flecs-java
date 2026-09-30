package io.github.elebras1.flecs.benchmark.dominion;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
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
public class ComponentBenchmark {

    static {
        System.setProperty("dominion.show-banner", "false");
    }

    @Param({"1000", "10000", "100000"})
    public int n;

    private Dominion dominion;
    private Entity[] positionEntities;
    private Entity[] positionVelocityEntities;
    private int[] order;

    @Setup(Level.Trial)
    public void setup() {
        this.dominion = Dominion.create();

        this.positionEntities = new Entity[this.n];
        for (int i = 0; i < this.n; i++) {
            this.positionEntities[i] = this.dominion.createEntity(new Position(0.0f, 0.0f));
        }

        this.positionVelocityEntities = new Entity[this.n];
        for (int i = 0; i < this.n; i++) {
            this.positionVelocityEntities[i] =
                    this.dominion.createEntity(new Position(0.0f, 0.0f), new Velocity(0.0f, 0.0f));
        }

        this.order = BenchUtils.shuffledIndices(this.n);
    }

    @TearDown(Level.Invocation)
    public void restoreState() {
        for (int i = 0; i < this.n; i++) {
            this.positionEntities[i].removeType(Velocity.class);
            if (!this.positionVelocityEntities[i].has(Velocity.class)) {
                this.positionVelocityEntities[i].add(new Velocity(0.0f, 0.0f));
            }
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        this.dominion.close();
    }

    @Benchmark
    public double addComponent() {
        long checksum = 0L;
        int[] order = this.order;
        Entity[] entities = this.positionEntities;
        for (int i = 0; i < this.n; i++) {
            Entity entity = entities[order[i]].add(new Velocity(0.0f, 0.0f));
            checksum += entity.isDeleted() ? 0 : 1;
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double removeComponent() {
        long checksum = 0L;
        int[] order = this.order;
        Entity[] entities = this.positionVelocityEntities;
        for (int i = 0; i < this.n; i++) {
            if (entities[order[i]].removeType(Velocity.class)) {
                checksum++;
            }
        }
        return (double) checksum / this.n;
    }

    @Benchmark
    public double get() {
        double checksum = 0.0;
        int[] order = this.order;
        Entity[] entities = this.positionEntities;
        for (int i = 0; i < this.n; i++) {
            Position position = entities[order[i]].get(Position.class);
            checksum += position.x + position.y;
        }
        return checksum / this.n;
    }

    @Benchmark
    public double getSet() {
        double checksum = 0.0;
        int[] order = this.order;
        Entity[] entities = this.positionEntities;
        for (int i = 0; i < this.n; i++) {
            Position position = entities[order[i]].get(Position.class);
            float x = position.x;
            float y = position.y;
            position.x = x + 1.0f;
            position.y = y + 1.0f;
            checksum += x;
        }
        return checksum / this.n;
    }

    @Benchmark
    public double has() {
        long checksum = 0L;
        int[] order = this.order;
        Entity[] entities = this.positionEntities;
        for (int i = 0; i < this.n; i++) {
            if (entities[order[i]].has(Position.class)) {
                checksum++;
            }
        }
        return (double) checksum / this.n;
    }
}
