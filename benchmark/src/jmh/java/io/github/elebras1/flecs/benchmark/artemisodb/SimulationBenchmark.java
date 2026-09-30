package io.github.elebras1.flecs.benchmark.artemisodb;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.utils.IntBag;
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

    private World ecsWorld;
    private ComponentMapper<Position> mPosition;
    private ComponentMapper<Velocity> mVelocity;
    private EntitySubscription movement;
    private int[] pool;
    private int[] created;
    private int cursor;

    @Setup(Level.Iteration)
    public void setup() {
        this.ecsWorld = new World(new WorldConfiguration());
        this.mPosition = this.ecsWorld.getMapper(Position.class);
        this.mVelocity = this.ecsWorld.getMapper(Velocity.class);
        this.pool = new int[this.n];
        for (int i = 0; i < this.n; i++) {
            int entityId = this.ecsWorld.create();
            this.mPosition.create(entityId);
            this.mVelocity.create(entityId);
            this.pool[i] = entityId;
        }
        this.ecsWorld.process();
        this.movement = this.ecsWorld.getAspectSubscriptionManager()
                .get(Aspect.all(Position.class, Velocity.class));
        this.created = new int[this.n / 10];
        this.cursor = 0;
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        this.ecsWorld = null;
    }

    @Benchmark
    public double mixedSimulation() {
        int tenth = this.n / 10;
        double checksum = 0.0;

        for (int i = 0; i < tenth; i++) {
            int entityId = this.ecsWorld.create();
            Position position = this.mPosition.create(entityId);
            position.x = 1.0f;
            position.y = 2.0f;
            Velocity velocity = this.mVelocity.create(entityId);
            velocity.dx = 0.5f;
            velocity.dy = 0.25f;
            this.created[i] = entityId;
        }
        this.ecsWorld.process();

        IntBag entities = this.movement.getEntities();
        int[] ids = entities.getData();
        for (int i = 0, s = entities.size(); i < s; i++) {
            Position position = this.mPosition.get(ids[i]);
            Velocity velocity = this.mVelocity.get(ids[i]);
            position.x += velocity.dx;
            position.y += velocity.dy;
            checksum += position.x + position.y;
        }

        for (int i = 0; i < tenth; i++) {
            int slot = (this.cursor + i) % this.n;
            this.ecsWorld.delete(this.pool[slot]);
            this.pool[slot] = this.created[i];
        }
        this.ecsWorld.process();
        this.cursor = (this.cursor + tenth) % this.n;

        return checksum / this.n;
    }
}
