package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Pair;
import io.github.elebras1.flecs.component.PairView;
import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.PositionView;
import io.github.elebras1.flecs.component.Tag;
import io.github.elebras1.flecs.component.Velocity;
import io.github.elebras1.flecs.util.Flecs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PairsTest {

    private World world;
    private long pairId;
    private long positionId;
    private long velocityId;
    private long tagId;

    @BeforeEach
    void init() {
        world = new World();
        pairId = world.component(Pair.class);
        positionId = world.component(Position.class);
        velocityId = world.component(Velocity.class);
        tagId = world.component(Tag.class);
    }

    @AfterEach
    void tearDown() {
        world.destroy();
    }

    @Test
    void addComponentPair() {
        Entity entity = world.obtainEntity(world.entity())
                .add(Pair.class, Position.class);

        assertNotEquals(0, entity.id());
        assertTrue(entity.has(Pair.class, Position.class));
        assertFalse(entity.has(Position.class, Pair.class));

    }

    @Test
    void addTagPair() {
        long pairTagId = world.entity("PairRelation");
        Entity entity = world.obtainEntity(world.entity())
                .add(pairTagId, positionId);

        assertNotEquals(0, entity.id());
        assertTrue(entity.has(pairTagId, positionId));
        assertFalse(entity.has(positionId, pairTagId));

    }

    @Test
    void addTagPairToTag() {
        long tag1 = world.entity("Tag");
        long pair1 = world.entity("PairRelation");

        Entity entity = world.obtainEntity(world.entity())
                .add(pair1, tag1);

        assertNotEquals(0, entity.id());
        assertTrue(entity.has(pair1, tag1));

    }

    @Test
    void removeComponentPair() {
        Entity entity = world.obtainEntity(world.entity())
                .add(Pair.class, Position.class);
        assertTrue(entity.has(Pair.class, Position.class));

        entity.remove(Pair.class, Position.class);
        assertFalse(entity.has(Pair.class, Position.class));
    }

    @Test
    void removeTagPair() {
        long pairTagId = world.entity("PairRelation");
        Entity entity = world.obtainEntity(world.entity())
                .add(pairTagId, positionId);

        assertTrue(entity.has(pairTagId, positionId));
        entity.remove(pairTagId, positionId);
        assertFalse(entity.has(pairTagId, positionId));
    }

    @Test
    void removeTagPairToTag() {
        long tag1 = world.entity("Tag");
        long pair1 = world.entity("PairRelation");
        Entity entity = world.obtainEntity(world.entity())
                .add(pair1, tag1);

        assertTrue(entity.has(pair1, tag1));
        entity.remove(tag1, pair1);
        assertFalse(entity.has(tag1, pair1));
    }

    @Test
    void setComponentPair() {
        Entity entity = world.obtainEntity(world.entity())
                .set(new Pair(10), positionId);

        assertTrue(entity.has(Pair.class, Position.class));
        assertFalse(entity.has(Position.class, Pair.class));
        Pair p = entity.get(Pair.class, Position.class);
        assertNotNull(p);
        assertEquals(10.0f, p.value());
    }

    @Test
    void setTagPair() {
        long pairTagId = world.entity("PairRelation");
        Entity entity = world.obtainEntity(world.entity())
                .setSecond(Position.class, pairTagId, (PositionView view) -> {
                    view.x(10);
                    view.y(20);
                });

        assertTrue(entity.has(pairTagId, positionId));
        Position p = entity.getSecond(Position.class, pairTagId);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void system1PairInstance() {
        world.obtainEntity(world.entity())
                .set(new Pair(10), positionId);

        AtomicInteger invokeCount = new AtomicInteger();
        AtomicInteger entityCount = new AtomicInteger();
        AtomicReference<Float> traitValue = new AtomicReference<>(0f);

        world.system()
                .expr("(Pair, *)")
                .iter(it -> {
                    Field<Pair> tr = it.field(Pair.class, 0);
                    invokeCount.incrementAndGet();
                    for (int i = 0; i < it.count(); i++) {
                        entityCount.incrementAndGet();
                        traitValue.set(traitValue.get() + tr.get(i).value());
                    }
                });

        world.progress();
        assertEquals(1, invokeCount.get());
        assertEquals(1, entityCount.get());
        assertEquals(10.0f, traitValue.get(), 0.001f);
    }

    @Test
    void system2PairInstances() {
        world.obtainEntity(world.entity())
                .set(new Pair(10), positionId)
                .set(new Pair(20), velocityId);

        AtomicInteger invokeCount = new AtomicInteger();
        AtomicInteger entityCount = new AtomicInteger();
        AtomicReference<Float> traitValue = new AtomicReference<>(0f);

        world.system()
                .expr("(Pair, *)")
                .iter(it -> {
                    Field<Pair> tr = it.field(Pair.class, 0);
                    invokeCount.incrementAndGet();
                    for (int i = 0; i < it.count(); i++) {
                        entityCount.incrementAndGet();
                        traitValue.set(traitValue.get() + tr.get(i).value());
                    }
                });

        world.progress();
        assertEquals(2, invokeCount.get());
        assertEquals(2, entityCount.get());
        assertEquals(30.0f, traitValue.get(), 0.001f);
    }

    @Test
    void overridePair() {

        world.obtainEntity(pairId).add(Flecs.OnInstantiate, Flecs.Inherit);

        Entity base = world.obtainEntity(world.entity())
                .set(new Pair(10), positionId);
        Entity instance = world.obtainEntity(world.entity())
                .isA(base.id());
        long pair = world.pair(pairId, positionId).id();

        assertTrue(instance.has(pairId, positionId));
        assertFalse(instance.owns(pair));
        Pair t = instance.get(Pair.class, Position.class);
        assertNotNull(t);
        assertEquals(10.0f, t.value());
        assertEquals(t, base.get(Pair.class, Position.class));

        instance.add(pairId, positionId);
        assertTrue(instance.owns(pair));
        t = instance.get(Pair.class, Position.class);
        assertEquals(10.0f, t.value());
        assertEquals(t, base.get(Pair.class, Position.class));

        instance.remove(pairId, positionId);
        assertFalse(instance.owns(pair));
        t = instance.get(Pair.class, Position.class);
        assertEquals(10.0f, t.value());
        assertEquals(t, base.get(Pair.class, Position.class));
    }

    @Test
    void overrideTagPair() {
        long pairTagId = world.entity("PairRelation");
        world.obtainEntity(pairTagId).add(Flecs.OnInstantiate, Flecs.Inherit);

        Entity base = world.obtainEntity(world.entity())
                .setSecond(Position.class, pairTagId, (PositionView view) -> {
                    view.x(10);
                    view.y(20);
                });
        Entity instance = world.obtainEntity(world.entity())
                .isA(base.id());
        long pair = world.pair(pairTagId, positionId).id();

        assertTrue(instance.has(pairTagId, positionId));
        assertFalse(instance.owns(pair));
        Position p = instance.getSecond(Position.class, pairTagId);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
        assertEquals(p, base.getSecond(Position.class, pairTagId));

        instance.add(pairTagId, positionId);
        assertTrue(instance.owns(pair));
        p = instance.getSecond(Position.class, pairTagId);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
        assertEquals(p, base.getSecond(Position.class, pairTagId));

        instance.remove(pairTagId, positionId);
        assertFalse(instance.owns(pair));
        p = instance.getSecond(Position.class, pairTagId);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
        assertEquals(p, base.getSecond(Position.class, pairTagId));
    }

    @Test
    void ensurePair() {
        Entity e = world.obtainEntity(world.entity());

        e.set(Pair.class, positionId, (PairView view) -> view.value(10));
        Pair t = e.get(Pair.class, Position.class);
        assertNotNull(t);
        assertEquals(10.0f, t.value());
    }

    @Test
    void ensurePairExisting() {
        Entity e = world.obtainEntity(world.entity())
                .set(new Pair(20), positionId);
        e.set(Pair.class, positionId, (PairView view) -> {
            assertEquals(20.0f, view.value());
            view.value(10);
        });
        Pair t = e.get(Pair.class, Position.class);
        assertEquals(10.0f, t.value());
    }

    @Test
    void ensurePairTag() {
        long pairTagId = world.entity("PairRelation");
        Entity e = world.obtainEntity(world.entity());
        e.setSecond(Position.class, pairTagId, (PositionView view) -> {
            view.x(10);
            view.y(20);
        });
        Position p = e.getSecond(Position.class, pairTagId);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void ensurePairTagExisting() {
        long pairTagId = world.entity("PairRelation");
        Entity e = world.obtainEntity(world.entity())
                .setSecond(Position.class, pairTagId, (PositionView view) -> {
                    view.x(10);
                    view.y(20);
                });
        e.setSecond(Position.class, pairTagId, (PositionView view) -> {
            assertEquals(10.0f, view.x());
            assertEquals(20.0f, view.y());
            view.x(30);
            view.y(40);
        });
        Position p = e.getSecond(Position.class, pairTagId);
        assertEquals(30.0f, p.x());
        assertEquals(40.0f, p.y());
    }

    @Test
    void ensureRTagO() {
        Entity e = world.obtainEntity(world.entity())
                .setSecond(Position.class, tagId, (PositionView view) -> {
                    view.x(10);
                    view.y(20);
                });
        e.setSecond(Position.class, tagId, (PositionView view) -> {
            assertEquals(10.0f, view.x());
            assertEquals(20.0f, view.y());
            view.x(30);
            view.y(40);
        });
        Position p = e.getSecond(Position.class, tagId);
        assertEquals(30.0f, p.x());
        assertEquals(40.0f, p.y());
    }

    @Test
    void getRelationFromId() {
        long rel = world.entity();
        long obj = world.entity();
        Id pair = world.pair(rel, obj);
        assertTrue(pair.isPair());
        assertEquals(rel, pair.first());
        assertNotEquals(rel, pair.second());
        assertTrue(world.obtainEntity(pair.first()).isAlive());
    }

    @Test
    void getSecondFromId() {
        long rel = world.entity();
        long obj = world.entity();
        Id pair = world.pair(rel, obj);
        assertNotEquals(obj, pair.first());
        assertEquals(obj, pair.second());
        assertTrue(world.obtainEntity(pair.second()).isAlive());
    }

    @Test
    void getRecycledRelationFromId() {
        long rel = world.entity();
        long obj = world.entity();
        world.obtainEntity(rel).destruct();
        world.obtainEntity(obj).destruct();
        rel = world.entity();
        obj = world.entity();

        assertNotEquals((int) rel, rel);
        assertNotEquals((int) obj, obj);
        Id pair = world.pair(rel, obj);
        assertEquals(rel, pair.first());
        assertTrue(world.obtainEntity(pair.first()).isAlive());
    }

    @Test
    void getRecycledObjectFromId() {
        long rel = world.entity();
        long obj = world.entity();
        world.obtainEntity(rel).destruct();
        world.obtainEntity(obj).destruct();
        rel = world.entity();
        obj = world.entity();
        assertNotEquals((int) rel, rel);
        assertNotEquals((int) obj, obj);
        Id pair = world.pair(rel, obj);
        assertEquals(obj, pair.second());
        assertTrue(world.obtainEntity(pair.second()).isAlive());
    }

    @Test
    void obtainNegativePairId() {
        long relation = world.entity();
        long object = world.entity();
        long pairId = world.pair(relation, object).id();

        assertTrue(pairId < 0);
        Id pair = world.obtainId(pairId);
        assertTrue(pair.isPair());
        assertEquals(relation, pair.first());
        assertEquals(object, pair.second());
    }

    @Test
    void getRelObj() {
        long obj = world.entity();
        Entity e = world.obtainEntity(world.entity())
                .set(new Position(10, 20), obj);
        assertTrue(e.has(positionId, obj));
        Position p = e.get(Position.class, obj);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void getRObj() {
        long obj = world.entity();
        Entity e = world.obtainEntity(world.entity())
                .set(new Position(10, 20), obj);
        assertTrue(e.has(Position.class, obj));
        Position p = e.get(Position.class, obj);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void getRO() {
        Entity e = world.obtainEntity(world.entity())
                .set(new Position(10, 20), tagId);
        assertTrue(e.has(Position.class, Tag.class));
        Position p = e.get(Position.class, Tag.class);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void getRTagO() {
        Entity e = world.obtainEntity(world.entity())
                .setSecond(Position.class, tagId, (PositionView view) -> {
                    view.x(10);
                    view.y(20);
                });
        assertTrue(e.has(Tag.class, Position.class));
        Position p = e.getSecond(Position.class, tagId);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void getSecond() {
        long rel = world.entity();
        Entity e = world.obtainEntity(world.entity())
                .setSecond(Position.class, rel, (PositionView view) -> {
                    view.x(10);
                    view.y(20);
                });
        assertTrue(e.has(rel, positionId));
        Position p = e.getSecond(Position.class, rel);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void each() {
        long p1 = world.entity();
        long p2 = world.entity();
        Entity e = world.obtainEntity(world.entity())
                .add(p1).add(p2);
        List<Long> ids = new ArrayList<>();
        e.each(ids::add);
        assertEquals(2, ids.size());
        assertTrue(ids.contains(p1));
        assertTrue(ids.contains(p2));
    }

    @Test
    void eachPair() {
        long pos = positionId;
        long vel = velocityId;
        Entity e = world.obtainEntity(world.entity())
                .add(pairId, pos)
                .add(pairId, vel);
        List<Long> objects = new ArrayList<>();
        e.each(id -> {
            Id idObj = world.obtainId(id);
            if (idObj.isPair() && idObj.first() == pairId) {
                objects.add(idObj.second());
            }
        });
        assertEquals(2, objects.size());
        assertTrue(objects.contains(pos));
        assertTrue(objects.contains(vel));
    }

    @Test
    void eachPairByType() {
        long pos = positionId;
        long vel = velocityId;
        Entity e = world.obtainEntity(world.entity())
                .add(Pair.class, Position.class)
                .add(Pair.class, Velocity.class);
        List<Long> objects = new ArrayList<>();
        e.each(id -> {
            Id idObj = world.obtainId(id);
            if (idObj.isPair() && idObj.first() == pairId) {
                objects.add(idObj.second());
            }
        });
        assertEquals(2, objects.size());
        assertTrue(objects.contains(pos));
        assertTrue(objects.contains(vel));
    }

    @Test
    void eachPairWithIsa() {
        long p1 = world.entity();
        long p2 = world.entity();
        Entity e = world.obtainEntity(world.entity())
                .isA(p1).isA(p2);
        List<Long> objects = new ArrayList<>();
        e.each(id -> {
            Id idObj = world.obtainId(id);
            if (idObj.isPair() && idObj.first() == Flecs.IsA) {
                objects.add(idObj.second());
            }
        });
        assertEquals(2, objects.size());
        assertTrue(objects.contains(p1));
        assertTrue(objects.contains(p2));
    }

    @Test
    void eachPairWithRecycledRel() {
        long e1 = world.entity();
        long e2 = world.entity();
        world.obtainEntity(world.entity()).destruct();
        long pairTag = world.entity();
        assertNotEquals((int) pairTag, pairTag);
        Entity e = world.obtainEntity(world.entity())
                .add(pairTag, e1)
                .add(pairTag, e2);
        List<Long> objects = new ArrayList<>();
        e.each(id -> {
            Id idObj = world.obtainId(id);
            if (idObj.isPair() && idObj.first() == pairTag) {
                objects.add(idObj.second());
            }
        });
        assertEquals(2, objects.size());
        assertTrue(objects.contains(e1));
        assertTrue(objects.contains(e2));
    }

    @Test
    void eachPairWithRecycledObj() {
        long pair = pairId;
        world.obtainEntity(world.entity()).destruct();
        long e1 = world.entity();
        assertNotEquals((int) e1, e1);
        world.obtainEntity(world.entity()).destruct();
        long e2 = world.entity();
        assertNotEquals((int) e2, e2);
        Entity e = world.obtainEntity(world.entity())
                .add(pair, e1)
                .add(pair, e2);
        List<Long> objects = new ArrayList<>();
        e.each(id -> {
            Id idObj = world.obtainId(id);
            if (idObj.isPair() && idObj.first() == pair) {
                objects.add(idObj.second());
            }
        });
        assertEquals(2, objects.size());
        assertTrue(objects.contains(e1));
        assertTrue(objects.contains(e2));
    }

    @Test
    void matchPair() {
        long eats = world.entity();
        long dislikes = world.entity();
        long apples = world.entity();
        long pears = world.entity();
        long bananas = world.entity();

        Entity e = world.obtainEntity(world.entity())
                .set(new Position(10, 20))
                .add(eats, apples)
                .add(eats, pears)
                .add(dislikes, bananas);

        AtomicInteger count = new AtomicInteger();
        e.each(id -> {
            Id idObj = world.obtainId(id);
            if (idObj.isPair() && idObj.first() == eats && idObj.second() == apples) {
                count.incrementAndGet();
            }
        });
        assertEquals(1, count.get());
    }

    @Test
    void matchPairObjWildcard() {
        long eats = world.entity();
        long dislikes = world.entity();
        long apples = world.entity();
        long pears = world.entity();
        long bananas = world.entity();

        Entity e = world.obtainEntity(world.entity())
                .set(new Position(10, 20))
                .add(eats, apples)
                .add(eats, pears)
                .add(dislikes, bananas);

        AtomicInteger count = new AtomicInteger();
        e.each(id -> {
            Id idObj = world.obtainId(id);
            if (idObj.isPair() && idObj.first() == eats) {
                count.incrementAndGet();
            }
        });
        assertEquals(2, count.get());
    }

    @Test
    void matchPairRelWildcard() {
        long eats = world.entity();
        long dislikes = world.entity();
        long apples = world.entity();
        long pears = world.entity();
        long bananas = world.entity();

        Entity e = world.obtainEntity(world.entity())
                .set(new Position(10, 20))
                .add(eats, apples)
                .add(eats, pears)
                .add(dislikes, bananas);

        AtomicInteger count = new AtomicInteger();
        e.each(id -> {
            Id idObj = world.obtainId(id);
            if (idObj.isPair() && idObj.second() == pears) {
                count.incrementAndGet();
            }
        });
        assertEquals(1, count.get());
    }

    @Test
    void matchPairBothWildcard() {
        long eats = world.entity();
        long dislikes = world.entity();
        long apples = world.entity();
        long pears = world.entity();
        long bananas = world.entity();

        Entity e = world.obtainEntity(world.entity())
                .set(new Position(10, 20))
                .add(eats, apples)
                .add(eats, pears)
                .add(dislikes, bananas);

        AtomicInteger count = new AtomicInteger();
        e.each(id -> {
            Id idObj = world.obtainId(id);
            if (idObj.isPair()) {
                count.incrementAndGet();
            }
        });
        assertEquals(3, count.get());
    }

    @Test
    void hasTagWithObject() {
        long likes = world.entity();
        long bob = world.entity();
        Entity e = world.obtainEntity(world.entity()).add(likes, bob);
        assertTrue(e.has(likes, bob));
    }

    @Test
    void hasSecondTag() {
        long likes = world.entity();
        long bob = world.entity();
        Entity e = world.obtainEntity(world.entity()).add(likes, bob);
        assertTrue(e.has(likes, bob));
    }

    @Test
    void symmetricWithChildOf() {
        long likes = world.entity("Likes");
        world.obtainEntity(likes).add(Flecs.Symmetric);

        long idk = world.entity("Idk");
        Entity bob = world.obtainEntity(world.entity("Bob")).childOf(idk);
        Entity alice = world.obtainEntity(world.entity("Alice")).childOf(idk)
                .add(likes, bob.id());

        assertTrue(bob.has(likes, alice.id()));
    }

    @Test
    void modifiedTagSecond() {
        AtomicInteger count = new AtomicInteger();
        world.observer(Position.class)
                .termAt(0).second(Tag.class)
                .event(Flecs.OnSet)
                .iter(it -> {
                    Position p = it.field(Position.class, 0).get(0);
                    assertEquals(10.0f, p.x());
                    assertEquals(20.0f, p.y());
                    count.incrementAndGet();
                });

        Entity e = world.obtainEntity(world.entity());
        e.set(Position.class, tagId, (PositionView view) -> {
            view.x(10);
            view.y(20);
        });
        e.modified(Position.class, Tag.class);
        assertEquals(1, count.get());
    }

    @Test
    void modifiedTagFirst() {
        AtomicInteger count = new AtomicInteger();
        world.observer()
                .with(Tag.class, Position.class)
                .event(Flecs.OnSet)
                .iter(iter -> {
                    Position p = iter.field(Position.class, 0).get(0);
                    assertEquals(10.0f, p.x());
                    assertEquals(20.0f, p.y());
                    count.incrementAndGet();
                });

        Entity e = world.obtainEntity(world.entity());
        e.setSecond(Position.class, tagId, (PositionView view) -> {
            view.x(10);
            view.y(20);
        });
        e.modified(Tag.class, Position.class);
        assertEquals(1, count.get());
    }

}