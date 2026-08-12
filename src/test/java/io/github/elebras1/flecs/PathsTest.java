package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathsTest {

    private World world;

    @BeforeEach
    void init() {
        this.world = new World();
    }

    @AfterEach
    void tearDown() {
        this.world.destroy();
    }

    @Test
    void name() {
        Entity e = this.world.obtainEntity(this.world.entity("foo"));
        assertEquals("foo", e.name());

        long eWorld = this.world.lookup("foo");
        assertEquals(e.id(), eWorld);

        eWorld = this.world.lookup("::foo");
        assertEquals(e.id(), eWorld);
    }

    @Test
    void pathDepth1() {
        Entity e = this.world.obtainEntity(this.world.entity("foo::bar"));
        assertEquals("bar", e.name());

        assertEquals(0, this.world.lookup("bar"));
        assertEquals(e.id(), this.world.lookup("foo::bar"));
        assertEquals(e.id(), this.world.lookup("::foo::bar"));
    }

    @Test
    void pathDepth2() {
        Entity e = this.world.obtainEntity(this.world.entity("foo::bar::hello"));
        assertEquals("hello", e.name());

        assertEquals(0, this.world.lookup("hello"));
        assertEquals(e.id(), this.world.lookup("foo::bar::hello"));
        assertEquals(e.id(), this.world.lookup("::foo::bar::hello"));
    }

    @Test
    void entityLookupName() {
        Entity parent = this.world.obtainEntity(this.world.entity("foo"));
        Entity e = this.world.obtainEntity(this.world.entity("foo::bar"));
        assertEquals("bar", e.name());

        assertEquals(e.id(), parent.lookup("bar"));
        assertEquals(e.id(), parent.lookup("::foo::bar"));
    }

    @Test
    void entityLookupDepth1() {
        Entity parent = this.world.obtainEntity(this.world.entity("foo"));
        Entity e = this.world.obtainEntity(this.world.entity("foo::bar::hello"));
        assertEquals("hello", e.name());

        assertEquals(e.id(), parent.lookup("bar::hello"));
        assertEquals(e.id(), parent.lookup("::foo::bar::hello"));
    }

    @Test
    void entityLookupDepth2() {
        Entity parent = this.world.obtainEntity(this.world.entity("foo"));
        Entity e = this.world.obtainEntity(this.world.entity("foo::bar::hello::world"));
        assertEquals("world", e.name());

        assertEquals(e.id(), parent.lookup("bar::hello::world"));
        assertEquals(e.id(), parent.lookup("::foo::bar::hello::world"));
    }

    @Test
    void lookupComponentByName() {
        long componentId = this.world.component(Position.class);
        long byName = this.world.lookup("Position");
        assertEquals(componentId, byName);
    }
}
