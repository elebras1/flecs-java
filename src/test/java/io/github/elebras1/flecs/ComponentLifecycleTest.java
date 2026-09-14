package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.PositionView;
import io.github.elebras1.flecs.component.Velocity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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

    private static Position[] transformed(Position[] src) {
        Position[] dst = new Position[src.length];
        for (int i = 0; i < src.length; i++) {
            dst[i] = new Position(42, 43);
        }
        return dst;
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
    void onAddContextHook() {
        AtomicLong seen = new AtomicLong();
        this.world.component(Position.class, hooks ->
                hooks.onAdd((it, components) -> seen.set(it.entityId(0))));

        Entity entity = this.world.obtainEntity(this.world.entity()).add(Position.class);

        assertEquals(entity.id(), seen.get());
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

    @Test
    void onReplaceHook() {
        AtomicReference<Position> prev = new AtomicReference<>();
        AtomicReference<Position> next = new AtomicReference<>();
        this.world.component(Position.class, hooks ->
                hooks.onReplace((oldComponents, newComponents) -> {
                    prev.set(oldComponents[0]);
                    next.set(newComponents[0]);
                }));

        Entity entity = this.world.obtainEntity(this.world.entity());
        entity.set(new Position(10, 20));
        entity.set(new Position(30, 40));

        assertNotNull(prev.get());
        assertNotNull(next.get());
        assertEquals(10.0f, prev.get().x());
        assertEquals(20.0f, prev.get().y());
        assertEquals(30.0f, next.get().x());
        assertEquals(40.0f, next.get().y());
    }

    @Test
    void ctorValues() {
        this.world.component(Position.class, hooks -> hooks.ctor(count -> {
            Position[] positions = new Position[count];
            for (int i = 0; i < count; i++) {
                positions[i] = new Position(7, 8);
            }
            return positions;
        }));

        Entity entity = this.world.obtainEntity(this.world.entity()).add(Position.class);
        Position p = entity.get(Position.class);

        assertNotNull(p);
        assertEquals(7.0f, p.x());
        assertEquals(8.0f, p.y());
    }

    @Test
    void ctorDtorHooks() {
        AtomicInteger ctor = new AtomicInteger();
        AtomicInteger dtor = new AtomicInteger();
        this.world.component(Position.class, hooks -> {
            hooks.ctor(count -> {
                ctor.addAndGet(count);
                return null;
            });
            hooks.dtor(components -> dtor.addAndGet(components.length));
        });

        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        assertTrue(ctor.get() >= 1);

        entity.remove(Position.class);
        assertTrue(dtor.get() >= 1);
    }

    @Test
    void moveHooks() {
        AtomicInteger move = new AtomicInteger();
        AtomicInteger moveCtor = new AtomicInteger();
        this.world.component(Position.class, hooks -> {
            hooks.move(src -> {
                move.addAndGet(src.length);
                return null;
            });
            hooks.moveCtor(src -> {
                moveCtor.addAndGet(src.length);
                return null;
            });
        });

        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        entity.add(Velocity.class);

        assertTrue(move.get() + moveCtor.get() >= 1);
    }

    @Test
    void copyValues() {
        this.world.component(Position.class, hooks -> {
            hooks.copy(ComponentLifecycleTest::transformed);
            hooks.copyCtor(ComponentLifecycleTest::transformed);
        });

        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        Entity clone = this.world.obtainEntity(entity.clone(true));
        Position p = clone.get(Position.class);

        assertNotNull(p);
        assertEquals(42.0f, p.x());
        assertEquals(43.0f, p.y());
    }

    @Test
    void copyHooks() {
        AtomicInteger copy = new AtomicInteger();
        AtomicInteger copyCtor = new AtomicInteger();
        this.world.component(Position.class, hooks -> {
            hooks.copy(src -> {
                copy.addAndGet(src.length);
                return null;
            });
            hooks.copyCtor(src -> {
                copyCtor.addAndGet(src.length);
                return null;
            });
        });

        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        entity.clone(true);

        assertTrue(copy.get() + copyCtor.get() >= 1);
    }

    @Test
    void hookFailureIsReported() {
        this.world.component(Position.class, hooks -> hooks.onSet(components -> {
            throw new IllegalStateException("hook failure");
        }));

        Entity entity = this.world.obtainEntity(this.world.entity());

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> {
            entity.set(new Position(1, 2));
            entity.get(Position.class);
        });
        assertTrue(error.getMessage().contains("on_set"));
    }
}
