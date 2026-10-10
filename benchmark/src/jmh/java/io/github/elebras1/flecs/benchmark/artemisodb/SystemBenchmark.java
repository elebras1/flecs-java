package io.github.elebras1.flecs.benchmark.artemisodb;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.systems.IteratingSystem;
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

    @Param({"1000", "10000", "100000"})
    public int n;

    private World ecsWorld;
    private World ecsWorld5;
    private PositionVelocitySystem system;
    private PositionVelocityHealthMassAgeSystem system5;

    @Setup(Level.Trial)
    public void setup() {
        this.system = new PositionVelocitySystem();
        this.ecsWorld = new World(new WorldConfigurationBuilder().with(this.system).build());

        ComponentMapper<Position> mPosition = this.ecsWorld.getMapper(Position.class);
        ComponentMapper<Velocity> mVelocity = this.ecsWorld.getMapper(Velocity.class);
        for (int i = 0; i < this.n; i++) {
            int entityId = this.ecsWorld.create();
            Position position = mPosition.create(entityId);
            position.x = 1.0f;
            position.y = 2.0f;
            Velocity velocity = mVelocity.create(entityId);
            velocity.dx = 0.5f;
            velocity.dy = 0.25f;
        }
        this.ecsWorld.process();

        this.system5 = new PositionVelocityHealthMassAgeSystem();
        this.ecsWorld5 = new World(new WorldConfigurationBuilder().with(this.system5).build());

        ComponentMapper<Position> mPosition5 = this.ecsWorld5.getMapper(Position.class);
        ComponentMapper<Velocity> mVelocity5 = this.ecsWorld5.getMapper(Velocity.class);
        ComponentMapper<Health> mHealth5 = this.ecsWorld5.getMapper(Health.class);
        ComponentMapper<Mass> mMass5 = this.ecsWorld5.getMapper(Mass.class);
        ComponentMapper<Age> mAge5 = this.ecsWorld5.getMapper(Age.class);
        for (int i = 0; i < this.n; i++) {
            int entityId = this.ecsWorld5.create();
            Position position = mPosition5.create(entityId);
            position.x = 1.0f;
            position.y = 2.0f;
            Velocity velocity = mVelocity5.create(entityId);
            velocity.dx = 0.5f;
            velocity.dy = 0.25f;
            mHealth5.create(entityId).value = 100;
            mMass5.create(entityId).value = 1.0f;
            mAge5.create(entityId).value = 10;
        }
        this.ecsWorld5.process();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        this.ecsWorld = null;
        this.ecsWorld5 = null;
    }

    @Benchmark
    public double systemRun() {
        this.system.checksum = 0.0;
        this.ecsWorld.process();
        return this.system.checksum / this.n;
    }

    @Benchmark
    public double systemRun5() {
        this.system5.checksum = 0.0;
        this.ecsWorld5.process();
        return this.system5.checksum / this.n;
    }

    private static final class PositionVelocitySystem extends IteratingSystem {

        private ComponentMapper<Position> mPosition;
        private ComponentMapper<Velocity> mVelocity;
        private double checksum;

        private PositionVelocitySystem() {
            super(Aspect.all(Position.class, Velocity.class));
        }

        @Override
        protected void initialize() {
            this.mPosition = this.world.getMapper(Position.class);
            this.mVelocity = this.world.getMapper(Velocity.class);
        }

        @Override
        protected void process(int entityId) {
            Position position = this.mPosition.get(entityId);
            Velocity velocity = this.mVelocity.get(entityId);
            position.x += velocity.dx;
            position.y += velocity.dy;
            this.checksum += position.x;
        }
    }

    private static final class PositionVelocityHealthMassAgeSystem extends IteratingSystem {

        private ComponentMapper<Position> mPosition;
        private ComponentMapper<Velocity> mVelocity;
        private ComponentMapper<Health> mHealth;
        private ComponentMapper<Mass> mMass;
        private ComponentMapper<Age> mAge;
        private double checksum;

        private PositionVelocityHealthMassAgeSystem() {
            super(Aspect.all(Position.class, Velocity.class, Health.class, Mass.class, Age.class));
        }

        @Override
        protected void initialize() {
            this.mPosition = this.world.getMapper(Position.class);
            this.mVelocity = this.world.getMapper(Velocity.class);
            this.mHealth = this.world.getMapper(Health.class);
            this.mMass = this.world.getMapper(Mass.class);
            this.mAge = this.world.getMapper(Age.class);
        }

        @Override
        protected void process(int entityId) {
            Position position = this.mPosition.get(entityId);
            Velocity velocity = this.mVelocity.get(entityId);
            position.x += velocity.dx;
            position.y += velocity.dy;
            this.mHealth.get(entityId).value += 1;
            this.mMass.get(entityId).value += 0.5f;
            this.mAge.get(entityId).value += 1;
            this.checksum += position.x;
        }
    }
}
