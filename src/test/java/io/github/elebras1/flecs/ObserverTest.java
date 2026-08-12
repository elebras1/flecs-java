package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Velocity;
import io.github.elebras1.flecs.util.Flecs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {

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
    void onAdd() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        assertEquals(1, count.get());
    }

    @Test
    void onRemove() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnRemove)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity()).add(Position.class);
        assertEquals(0, count.get());

        e.remove(Position.class);
        assertEquals(1, count.get());

        e.remove(Position.class);
        assertEquals(1, count.get());
    }

    @Test
    void onSet() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity());
        e.set(new Position(10, 20));
        assertEquals(1, count.get());

        e.set(new Position(30, 40));
        assertEquals(2, count.get());
    }

    @Test
    void twoTermsOnAdd() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .with(Velocity.class)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity());
        assertEquals(0, count.get());

        e.set(new Position(10, 20));
        assertEquals(0, count.get());

        e.set(new Velocity(1, 2));
        assertEquals(1, count.get());
    }

    @Test
    void twoTermsOnSet() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .with(Velocity.class)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity());
        e.set(new Position(10, 20));
        assertEquals(0, count.get());

        e.set(new Velocity(1, 2));
        assertEquals(1, count.get());
    }

    @Test
    void twoEntitiesEach() {
        long e1 = this.world.entity();
        long e2 = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .each(entityId -> {
                    if (entityId == e1 || entityId == e2) {
                        count.incrementAndGet();
                    }
                });

        this.world.obtainEntity(e1).set(new Position(10, 20));
        assertEquals(1, count.get());

        this.world.obtainEntity(e2).set(new Position(30, 40));
        assertEquals(2, count.get());
    }

    @Test
    void twoEntitiesIter() {
        long e1 = this.world.entity();
        long e2 = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        Map<Long, Float> xs = new HashMap<>();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        xs.put(it.entity(i), positions.get(i).x());
                        count.incrementAndGet();
                    }
                });

        this.world.obtainEntity(e1).set(new Position(10, 20));
        assertEquals(1, count.get());

        this.world.obtainEntity(e2).set(new Position(30, 40));
        assertEquals(2, count.get());
        assertEquals(10.0f, xs.get(e1));
        assertEquals(30.0f, xs.get(e2));
    }

    @Test
    void createWithoutTypeArgs() {
        long e1 = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        List<Long> ids = new ArrayList<>();
        this.world.observer()
                .with(Position.class)
                .event(Flecs.OnAdd)
                .each(ids::add);

        this.world.obtainEntity(e1).set(new Position(10, 20));
        assertEquals(List.of(e1), ids);
        assertEquals(1, ids.size());
    }

    @Test
    void onAddTag() {
        long tag = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(tag)
                .each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).add(tag);
        assertEquals(1, count.get());
    }

    @Test
    void yieldExisting() {

        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .yieldExisting()
                .each(entityId -> count.incrementAndGet());

        assertEquals(1, count.get());

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        assertEquals(2, count.get());
    }

    @Test
    void onAddExpr() {
        long tag = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(tag)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity()).add(tag);
        assertEquals(1, count.get());

        e.remove(tag);
        assertEquals(1, count.get());
    }

    @Test
    void runCallback() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .iter(it -> {
                    for (int i = 0; i < it.count(); i++) {
                        count.incrementAndGet();
                    }
                });

        Entity e = this.world.obtainEntity(this.world.entity());
        assertEquals(0, count.get());

        e.set(new Position(10, 20));
        assertEquals(1, count.get());
    }

    @Test
    void onSetWithSet() {
        AtomicInteger count = new AtomicInteger();
        List<Float> xs = new ArrayList<>();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        xs.add(positions.get(i).x());
                        count.incrementAndGet();
                    }
                });

        Entity e = this.world.obtainEntity(this.world.entity());
        e.set(new Position(10, 20));
        assertEquals(1, count.get());
        assertEquals(List.of(10.0f), xs);
    }

    @Test
    void enableDisable() {
        AtomicInteger count = new AtomicInteger();
        FlecsObserver observer = this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        observer.disable();
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        assertEquals(0, count.get());

        observer.enable();
        this.world.obtainEntity(this.world.entity()).set(new Position(3, 4));
        assertEquals(1, count.get());
    }
}
