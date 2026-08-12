package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.PositionView;
import io.github.elebras1.flecs.component.Velocity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ComponentLifecycleTest {

    private World world;
    private final AtomicInteger onAdd = new AtomicInteger();
    private final AtomicInteger onSet = new AtomicInteger();
    private final AtomicInteger onRemove = new AtomicInteger();

    @BeforeEach
    void init() {
        this.world = new World();
        this.world.component(Position.class, hooks -> {
            hooks.onAdd(components -> this.onAdd.addAndGet(components.length));
            hooks.onSet(components -> this.onSet.addAndGet(components.length));
            hooks.onRemove(components -> this.onRemove.addAndGet(components.length));
        });
        this.world.component(Velocity.class);
    }

    @AfterEach
    void tearDown() {
        this.world.destroy();
    }

    @Test
    void onAddHook() {
        Entity entity = this.world.obtainEntity(this.world.entity()).add(Position.class);

        assertTrue(entity.id() != 0);
        assertTrue(entity.has(Position.class));
        assertEquals(1, this.onAdd.get());
        assertEquals(0, this.onSet.get());
        assertEquals(0, this.onRemove.get());
    }

    @Test
    void onRemoveHook() {
        Entity entity = this.world.obtainEntity(this.world.entity()).add(Position.class);
        assertEquals(1, this.onAdd.get());

        entity.remove(Position.class);
        assertFalse(entity.has(Position.class));
        assertEquals(1, this.onAdd.get());
        assertEquals(1, this.onRemove.get());
    }

    @Test
    void onSetHook() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        assertEquals(1, this.onSet.get());
        assertEquals(1, this.onAdd.get());

        entity.set(new Position(30, 40));
        assertEquals(2, this.onSet.get());
        assertEquals(1, this.onAdd.get());
    }

    @Test
    void onAddHookMultiple() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class);
        assertEquals(2, this.onAdd.get());
    }

    @Test
    void chainedHooks() {
        Entity entity = this.world.obtainEntity(this.world.entity());

        entity.set(new Position(1, 2)).set(new Velocity(3, 4));
        assertEquals(1, this.onSet.get());

        entity.remove(Position.class);
        assertEquals(1, this.onRemove.get());
    }

    @Test
    void hooksIndependentPerWorld() {
        World world2 = new World();
        AtomicInteger onAdd2 = new AtomicInteger();
        world2.component(Position.class, hooks ->
                hooks.onAdd(components -> onAdd2.addAndGet(components.length)));
        world2.obtainEntity(world2.entity()).add(Position.class);
        assertEquals(1, onAdd2.get());
        world2.destroy();

        assertEquals(0, this.onAdd.get());
    }

    @Test
    void insertWithModified() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        entity.insert(Position.class, (PositionView view) -> {
            view.x(10);
            view.y(20);
        });

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
        assertEquals(1, this.onSet.get());
    }

    @Test
    void deferSet() {
        Entity entity = this.world.obtainEntity(this.world.entity());

        this.world.deferBegin();
        entity.set(new Position(10, 20));
        assertFalse(entity.has(Position.class));
        this.world.deferEnd();

        assertTrue(entity.has(Position.class));
        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        assertEquals(1, this.onAdd.get());
        assertEquals(1, this.onSet.get());
    }
}
