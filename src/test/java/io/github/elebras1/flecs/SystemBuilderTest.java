package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Mass;
import io.github.elebras1.flecs.component.PositionView;
import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Velocity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SystemBuilderTest {

    private World world;

    @BeforeEach
    void init() {
        this.world = new World();
        this.world.component(Position.class);
        this.world.component(Velocity.class);
        this.world.component(Mass.class);
    }

    @AfterEach
    void tearDown() {
        this.world.destroy();
    }

    @Test
    void oneType() {
        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        this.world.obtainEntity(this.world.entity()).add(Velocity.class);

        List<Long> ids = new ArrayList<>();
        FlecsSystem sys = this.world.system()
                .with(Position.class)
                .each(ids::add);

        assertEquals(0, ids.size());
        sys.run();
        assertEquals(List.of(e1), ids);
    }

    @Test
    void addTwoTypes() {
        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).add(Velocity.class).id();
        this.world.obtainEntity(this.world.entity()).add(Velocity.class);

        List<Long> ids = new ArrayList<>();
        FlecsSystem sys = this.world.system()
                .with(Position.class)
                .with(Velocity.class)
                .each(ids::add);

        sys.run();
        assertEquals(List.of(e1), ids);
    }

    @Test
    void addPair() {
        long likes = this.world.entity();
        long bob = this.world.entity();
        long alice = this.world.entity();

        long e1 = this.world.obtainEntity(this.world.entity()).add(likes, bob).id();
        this.world.obtainEntity(this.world.entity()).add(likes, alice);

        List<Long> ids = new ArrayList<>();
        FlecsSystem sys = this.world.system()
                .with(likes, bob)
                .each(ids::add);

        sys.run();
        assertEquals(List.of(e1), ids);
    }

    @Test
    void addNot() {
        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        this.world.obtainEntity(this.world.entity()).add(Position.class).add(Velocity.class);

        List<Long> ids = new ArrayList<>();
        FlecsSystem sys = this.world.system()
                .with(Position.class)
                .with(Velocity.class).not()
                .each(ids::add);

        sys.run();
        assertEquals(List.of(e1), ids);
    }

    @Test
    void addOr() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.obtainEntity(this.world.entity()).add(Velocity.class);
        this.world.obtainEntity(this.world.entity()).add(Mass.class);

        AtomicInteger count = new AtomicInteger();
        FlecsSystem sys = this.world.system()
                .with(Position.class).or()
                .with(Velocity.class)
                .each(entityId -> count.incrementAndGet());

        sys.run();
        assertEquals(2, count.get());
    }

    @Test
    void nameArg() {
        FlecsSystem sys = this.world.system("MySystem")
                .with(Position.class)
                .each(entityId -> {
                });

        assertTrue(sys.id() != 0);
        assertEquals("MySystem", sys.entity().name());
    }

    @Test
    void iter() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        FlecsSystem sys = this.world.system()
                .with(Position.class)
                .with(Velocity.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    for (int i = 0; i < it.count(); i++) {
                        Position p = positions.get(i);
                        Velocity v = velocities.get(i);
                        positions.set(i, new Position(p.x() + v.x(), p.y() + v.y()));
                    }
                });

        sys.run();

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(11.0f, p.x());
        assertEquals(22.0f, p.y());
    }

    @Test
    void eachWithComponent() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20));

        FlecsSystem sys = this.world.system()
                .with(Position.class)
                .each(Position.class, (entityId, p) -> {
                    this.world.obtainEntity(entityId).insert(Position.class, (PositionView view) -> {
                        view.x(p.x() + 1);
                        view.y(p.y() + 1);
                    });
                });

        sys.run();

        Position p = entity.get(Position.class);
        assertEquals(11.0f, p.x());
        assertEquals(21.0f, p.y());
    }

    @Test
    void enableDisable() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicInteger count = new AtomicInteger();
        FlecsSystem sys = this.world.system()
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        sys.disable();
        this.world.progress();
        assertEquals(0, count.get());

        sys.enable();
        this.world.progress();
        assertEquals(1, count.get());
    }
}
