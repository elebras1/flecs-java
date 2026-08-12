package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Velocity;
import io.github.elebras1.flecs.util.Flecs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldFactoryTest {

    private World world;

    @BeforeEach
    void init() {
        this.world = new World();
        this.world.component(Position.class);
        this.world.component(Velocity.class);
    }

    @AfterEach
    void tearDown() {
        this.world.destroy();
    }

    @Test
    void entity() {
        long e = this.world.entity();
        assertTrue(e != 0);
    }

    @Test
    void entityWithName() {
        long e = this.world.entity("MyName");
        assertTrue(e != 0);
        assertEquals("MyName", this.world.obtainEntity(e).name());
    }

    @Test
    void prefab() {
        long e = this.world.prefab();
        assertTrue(e != 0);
        assertTrue(this.world.obtainEntity(e).has(Flecs.Prefab));
    }

    @Test
    void system() {
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
    }

    @Test
    void systemWithName() {
        FlecsSystem sys = this.world.system("MySystem")
                .with(Position.class)
                .with(Velocity.class)
                .each(entityId -> {
                });

        assertTrue(sys.id() != 0);
        assertEquals("MySystem", sys.entity().name());
    }

    @Test
    void query() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        Query query = this.world.query().with(Position.class).with(Velocity.class).build();

        query.iter(it -> {
            Field<Position> positions = it.field(Position.class, 0);
            Field<Velocity> velocities = it.field(Velocity.class, 1);
            for (int i = 0; i < it.count(); i++) {
                Position p = positions.get(i);
                Velocity v = velocities.get(i);
                positions.set(i, new Position(p.x() + v.x(), p.y() + v.y()));
            }
        });

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(11.0f, p.x());
        assertEquals(22.0f, p.y());

        query.destroy();
    }

    @Test
    void queryWithExpr() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        Query query = this.world.query("Position, [in] Velocity");

        query.iter(it -> {
            Field<Position> positions = it.field(Position.class, 0);
            Field<Velocity> velocities = it.field(Velocity.class, 1);
            for (int i = 0; i < it.count(); i++) {
                Position p = positions.get(i);
                Velocity v = velocities.get(i);
                positions.set(i, new Position(p.x() + v.x(), p.y() + v.y()));
            }
        });

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(11.0f, p.x());
        assertEquals(22.0f, p.y());

        query.destroy();
    }
}
