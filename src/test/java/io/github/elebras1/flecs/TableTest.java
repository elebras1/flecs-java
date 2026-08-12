package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Mass;
import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Velocity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

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
    void count() {
        long e = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20)).id();
        this.world.obtainEntity(this.world.entity()).set(new Position(20, 30));
        this.world.obtainEntity(this.world.entity()).set(new Position(30, 40));

        Table table = this.world.obtainEntity(e).table();
        assertNotNull(table);
        assertEquals(3, table.count());
    }

    @Test
    void has() {
        long t1 = this.world.entity();
        long t2 = this.world.entity();
        long t3 = this.world.entity();

        long e = this.world.obtainEntity(this.world.entity())
                .add(t1).add(t2).set(new Position(10, 20)).set(new Velocity(1, 2)).id();
        this.world.obtainEntity(this.world.entity()).add(t1).add(t2).set(new Position(10, 20)).set(new Velocity(1, 2));
        this.world.obtainEntity(this.world.entity()).add(t1).add(t2).set(new Position(10, 20)).set(new Velocity(1, 2));

        Table table = this.world.obtainEntity(e).table();
        assertTrue(table.has(t1));
        assertTrue(table.has(t2));
        assertFalse(table.has(t3));
        assertTrue(table.has(Position.class));
        assertTrue(table.has(Velocity.class));
        assertFalse(table.has(Mass.class));
    }

    @Test
    void hasPair() {
        long r = this.world.entity();
        long t1 = this.world.entity();
        long t2 = this.world.entity();
        long t3 = this.world.entity();

        long e = this.world.obtainEntity(this.world.entity())
                .add(r, t1).add(r, t2).id();
        this.world.obtainEntity(this.world.entity()).add(r, t1).add(r, t2);
        this.world.obtainEntity(this.world.entity()).add(r, t1).add(r, t2);

        Table table = this.world.obtainEntity(e).table();
        assertTrue(table.has(r, t1));
        assertTrue(table.has(r, t2));
        assertFalse(table.has(r, t3));
    }

    @Test
    void get() {
        long e = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20)).id();
        this.world.obtainEntity(this.world.entity()).set(new Position(20, 30));
        this.world.obtainEntity(this.world.entity()).set(new Position(30, 40));

        Table table = this.world.obtainEntity(e).table();

        Position p0 = table.get(Position.class, 0);
        assertEquals(10.0f, p0.x());
        assertEquals(20.0f, p0.y());

        Position p2 = table.get(Position.class, 2);
        assertEquals(30.0f, p2.x());
        assertEquals(40.0f, p2.y());
    }

    @Test
    void tryGet() {
        long e = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20)).id();

        Table table = this.world.obtainEntity(e).table();
        assertNull(table.tryGet(Velocity.class, 0));
        assertNotNull(table.tryGet(Position.class, 0));
    }

    @Test
    void typeAndStr() {
        long e = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2)).id();

        Table table = this.world.obtainEntity(e).table();

        Type type = table.type();
        assertTrue(type.count() >= 2);
        assertEquals(type.count(), type.array().length);
        assertTrue(type.get(0).id() != 0);

        String str = table.str();
        assertNotNull(str);
        assertTrue(str.contains("Position"));
        assertTrue(str.contains("Velocity"));
    }

    @Test
    void entities() {
        long e1 = this.world.obtainEntity(this.world.entity()).set(new Position(1, 2)).id();
        long e2 = this.world.obtainEntity(this.world.entity()).set(new Position(3, 4)).id();

        Table table = this.world.obtainEntity(e1).table();
        long[] entities = table.entities();
        assertEquals(2, entities.length);
        assertEquals(e1, entities[0]);
        assertEquals(e2, entities[1]);
    }

    @Test
    void lockUnlock() {
        long e = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20)).id();

        Table table = this.world.obtainEntity(e).table();
        table.lock();
        table.unlock();
        assertTrue(true);
    }

    @Test
    void clearEntities() {
        long e = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20)).id();

        Table table = this.world.obtainEntity(e).table();
        assertEquals(1, table.count());

        table.clearEntities();
        assertEquals(0, table.count());
        assertFalse(this.world.obtainEntity(e).isAlive());
    }

    @Test
    void typeAndColumnIndex() {
        long e = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2)).id();

        Table table = this.world.obtainEntity(e).table();
        assertTrue(table.typeIndex(Position.class) >= 0);
        assertTrue(table.columnIndex(Position.class) >= 0);
        assertEquals(-1, table.typeIndex(Mass.class));
    }

    @Test
    void size() {
        long e = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20)).id();

        Table table = this.world.obtainEntity(e).table();
        assertTrue(table.size() >= 1);
    }
}
