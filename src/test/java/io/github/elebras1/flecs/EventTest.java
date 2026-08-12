package io.github.elebras1.flecs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

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
    void evt1IdEntity() {
        long evt = this.world.entity();
        long id = this.world.entity();
        long e1 = this.world.obtainEntity(this.world.entity()).add(id).id();

        AtomicInteger count = new AtomicInteger();
        List<Long> ids = new ArrayList<>();
        this.world.observer()
                .event(evt)
                .with(id)
                .each(entityId -> {
                    ids.add(entityId);
                    count.incrementAndGet();
                });

        this.world.obtainEntity(e1).emit(evt, id);
        assertEquals(1, count.get());
        assertEquals(List.of(e1), ids);
    }

    @Test
    void evt2IdsEntity() {
        long evt = this.world.entity();
        long idA = this.world.entity();
        long idB = this.world.entity();
        long e1 = this.world.obtainEntity(this.world.entity()).add(idA).add(idB).id();

        AtomicInteger count = new AtomicInteger();
        this.world.observer().event(evt).with(idA).each(entityId -> count.incrementAndGet());
        this.world.observer().event(evt).with(idB).each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(e1).emit(evt, idA);
        this.world.obtainEntity(e1).emit(evt, idB);
        assertEquals(2, count.get());
    }

    @Test
    void evt1IdPair() {
        long evt = this.world.entity();
        long rel = this.world.entity();
        long obj = this.world.entity();
        long e1 = this.world.obtainEntity(this.world.entity()).add(rel, obj).id();

        AtomicInteger count = new AtomicInteger();
        List<Long> ids = new ArrayList<>();
        this.world.observer()
                .event(evt)
                .with(rel, obj)
                .each(entityId -> {
                    ids.add(entityId);
                    count.incrementAndGet();
                });

        this.world.obtainEntity(e1).emit(evt, this.world.pair(rel, obj).id());
        assertEquals(1, count.get());
        assertEquals(List.of(e1), ids);
    }

    @Test
    void entityEmitEventId() {
        long evt = this.world.entity();
        Entity e = this.world.obtainEntity(this.world.entity());

        AtomicInteger count = new AtomicInteger();
        e.observe(evt, count::incrementAndGet);

        e.emit(evt);
        assertEquals(1, count.get());
    }

    @Test
    void entityEmitEventNoSrc() {
        long evt = this.world.entity();
        long id = this.world.entity();
        Entity e = this.world.obtainEntity(this.world.entity()).add(id);

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(evt)
                .with(id)
                .each(entityId -> count.incrementAndGet());

        e.emit(evt, id);
        assertEquals(1, count.get());
    }

    @Test
    void enqueueEvent() {
        long evt = this.world.entity();
        long id = this.world.entity();
        long e1 = this.world.obtainEntity(this.world.entity()).add(id).id();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(evt)
                .with(id)
                .each(entityId -> count.incrementAndGet());

        this.world.deferBegin();
        this.world.obtainEntity(e1).emit(evt, id);
        this.world.deferEnd();

        assertEquals(1, count.get());
    }

    @Test
    void evtType() {
        long evt = this.world.entity("Evt");
        long id = this.world.entity();
        long e1 = this.world.obtainEntity(this.world.entity()).add(id).id();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(evt)
                .with(id)
                .each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(e1).emit(evt, id);
        assertEquals(1, count.get());
    }
}
