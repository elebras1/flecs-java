package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.ComparatorComponent;
import io.github.elebras1.flecs.component.Mass;
import io.github.elebras1.flecs.component.PositionView;
import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Velocity;
import io.github.elebras1.flecs.util.Flecs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SystemTest {

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
    void iter() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        this.world.system()
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

        this.world.progress();

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(11.0f, p.x());
        assertEquals(22.0f, p.y());

        Velocity v = entity.get(Velocity.class);
        assertEquals(1.0f, v.x());
        assertEquals(2.0f, v.y());
    }

    @Test
    void each() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        this.world.system()
                .with(Position.class)
                .with(Velocity.class)
                .each(Position.class, (entityId, p) -> {
                    EntityView e = this.world.obtainEntityView(entityId);
                    Velocity v = e.get(Velocity.class);
                    e.insert(Position.class, (PositionView view) -> {
                        view.x(p.x() + v.x());
                        view.y(p.y() + v.y());
                    });
                });

        this.world.progress();

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(11.0f, p.x());
        assertEquals(22.0f, p.y());
    }

    @Test
    void signature() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        this.world.system()
                .with(Position.class)
                .with(Velocity.class).in()
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    for (int i = 0; i < it.count(); i++) {
                        Position p = positions.get(i);
                        Velocity v = velocities.get(i);
                        positions.set(i, new Position(p.x() + v.x(), p.y() + v.y()));
                    }
                });

        this.world.progress();

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(11.0f, p.x());
        assertEquals(22.0f, p.y());
    }

    @Test
    void copyNameOnCreate() {
        FlecsSystem sys = this.world.system("MySystem")
                .with(Position.class)
                .each(entityId -> {
                });

        assertTrue(sys.id() != 0);
        assertEquals("MySystem", sys.entity().name());
    }

    @Test
    void kindAndProgress() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicInteger count = new AtomicInteger();
        this.world.system("KindSystem")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        assertEquals(0, count.get());
        this.world.progress();
        assertEquals(1, count.get());
        this.world.progress();
        assertEquals(2, count.get());
    }

    @Test
    void addFromEach() {
        Entity e2 = this.world.obtainEntity(this.world.entity());

        this.world.system()
                .with(Position.class)
                .each(entityId -> e2.add(Mass.class));

        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.progress();

        assertTrue(e2.has(Mass.class));
    }

    @Test
    void newFromEach() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicInteger count = new AtomicInteger();
        this.world.system()
                .with(Position.class)
                .each(entityId -> {
                    this.world.entity();
                    count.incrementAndGet();
                });

        this.world.progress();
        assertEquals(1, count.get());
    }

    @Test
    void orderBy() {
        this.world.obtainEntity(this.world.entity()).set(new Position(3, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(5, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(2, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(4, 0));

        List<Float> values = new ArrayList<>();
        this.world.system()
                .with(Position.class)
                .orderBy(Position.class, (ComparatorComponent<Position>) (a, b) -> Float.compare(a.x(), b.x()))
                .each(Position.class, (entityId, p) -> values.add(p.x()));

        this.world.progress();

        assertEquals(5, values.size());
        assertEquals(1.0f, values.get(0));
        assertEquals(2.0f, values.get(1));
        assertEquals(3.0f, values.get(2));
        assertEquals(4.0f, values.get(3));
        assertEquals(5.0f, values.get(4));
    }

    @Test
    void deltaTime() {
        AtomicInteger dt = new AtomicInteger();
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        this.world.system()
                .with(Position.class)
                .iter(it -> dt.set((int) it.deltaTime()));

        this.world.progress(2.0f);
        assertEquals(2, dt.get());
    }

    @Test
    void runOnDemand() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicInteger count = new AtomicInteger();
        FlecsSystem sys = this.world.system("RunSystem")
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        sys.run();
        assertEquals(1, count.get());
        sys.run();
        assertEquals(2, count.get());
    }
}
