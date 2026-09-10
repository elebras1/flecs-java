package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    private World world;

    @BeforeEach
    void init() {
        this.world = new World();
        this.world.component(Health.class);
        this.world.component(Ideology.class);
        this.world.component(Position.class);
        this.world.component(Velocity.class);
        this.world.component(Mass.class);
    }

    @AfterEach
    void tearDown() {
        this.world.destroy();
    }

    @Test
    void entityTest() {
        long entityId = this.world.entity();
        assertTrue(entityId > 0);
    }

    @Test
    void entityWithNameIdTest() {
        long entityId = this.world.entity("test");
        assertTrue(entityId > 0);
        Entity entity = this.world.obtainEntity(entityId);
        assertEquals("test", entity.name());
    }

    @Test
    void obtainEntityTest() {
        long entityId = this.world.entity();
        Entity entity = this.world.obtainEntity(entityId);
        assertEquals(entity.id(), entityId);
    }

    @Test
    void obtainEntityViewTest() {
        long entityId = this.world.entity();
        EntityView entityView = this.world.obtainEntityView(entityId);
        assertEquals(entityView.id(), entityId);
    }

    @Test
    void entityBulkTest() {
        long[] entityIds = this.world.entityBulk(10);
        assertEquals(10, entityIds.length);
    }

    @Test
    void entityBulkWithComponentClassesTest() {
        long[] entityIds = this.world.entityBulk(10, Health.class, Ideology.class);
        assertEquals(10, entityIds.length);
        for (long entityId : entityIds) {
            EntityView entityView = this.world.obtainEntityView(entityId);
            assertTrue(entityView.has(Health.class));
            assertTrue(entityView.has(Ideology.class));
        }
    }

    @Test
    void makeAliveTest() {
        long entityId = 1000;
        this.world.makeAlive(entityId);
        Entity entity = this.world.obtainEntity(entityId);
        assertTrue(entity.isAlive());
    }

    @Test
    void setVersionTest() {
        this.world.makeAlive(500);
        this.world.setVersion(500);
        assertTrue(this.world.getVersion(500) >= 0);
    }

    @Test
    void getVersionTest() {
        long entityId = 500;
        this.world.makeAlive(entityId);
        int version = this.world.getVersion(entityId);
        assertTrue(version >= 0);
    }

    @Test
    void testLookup() {
        this.world.entity("test_entity");
        long entityId = this.world.lookup("test_entity");
        assertTrue(entityId > 0);
    }

    @Test
    void testLookupWithSystemCreateBefore() {
        this.world.enableRest((short) 27750);
        this.world.component(Health.class);

        AtomicLong found = new AtomicLong(-1);
        AtomicInteger iterCount = new AtomicInteger(-1);
        this.world.system("system_test").kind(Flecs.OnUpdate).with(Health.class).iter(iter -> {
            found.set(iter.world().lookup("test_entity"));
            iterCount.set(iter.count());
        });

        for (int i = 0; i < 10; i++) {
            long entityId = this.world.entity();
            EntityView entity = this.world.obtainEntityView(entityId);
            entity.set(new Health(100));
        }

        this.world.entity("test_entity");
        this.world.progress(1);

        assertTrue(found.get() > 0);
        assertEquals(10, iterCount.get());
    }

    @Test
    void count() {
        assertEquals(0, this.world.count(Position.class));

        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class).add(Mass.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class).add(Mass.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class).add(Velocity.class);

        assertEquals(6, this.world.count(Position.class));
    }

    @Test
    void countId() {
        long tag = this.world.entity();
        assertEquals(0, this.world.count(tag));

        this.world.obtainEntity(this.world.entity()).add(tag);
        this.world.obtainEntity(this.world.entity()).add(tag);
        this.world.obtainEntity(this.world.entity()).add(tag);
        this.world.obtainEntity(this.world.entity()).add(tag).add(Mass.class);
        this.world.obtainEntity(this.world.entity()).add(tag).add(Mass.class);
        this.world.obtainEntity(this.world.entity()).add(tag).add(Velocity.class);

        assertEquals(6, this.world.count(tag));
    }

    @Test
    void countPair() {
        long parent = this.world.entity();
        long childOfPair = this.world.pair(Flecs.ChildOf, parent).id();

        this.world.obtainEntity(this.world.entity()).childOf(parent);
        this.world.obtainEntity(this.world.entity()).childOf(parent);
        this.world.obtainEntity(this.world.entity()).childOf(parent);

        assertEquals(3, this.world.count(childOfPair));
    }

    @Test
    void deleteWithType() {
        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        long e2 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        long e3 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();

        this.world.deleteWith(Position.class);

        assertFalse(this.world.obtainEntity(e1).isAlive());
        assertFalse(this.world.obtainEntity(e2).isAlive());
        assertFalse(this.world.obtainEntity(e3).isAlive());
    }

    @Test
    void removeAll() {
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.obtainEntity(this.world.entity()).set(new Position(3, 4));
        this.world.obtainEntity(this.world.entity()).set(new Position(5, 6));
        assertEquals(3, this.world.count(Position.class));

        this.world.removeAll(Position.class);
        assertEquals(0, this.world.count(Position.class));
    }

    @Test
    void setWith() {
        this.world.setWith(Position.class);
        try {
            long entityId = this.world.entity();
            assertTrue(this.world.obtainEntity(entityId).has(Position.class));
        } finally {
            this.world.setWith(0);
        }
    }

    @Test
    void runPostFrame() {
        AtomicBoolean ran = new AtomicBoolean(false);
        this.world.runPostFrame(() -> ran.set(true));

        this.world.progress();
        this.world.progress();

        assertTrue(ran.get());
    }

    @Test
    void atfini() {
        AtomicBoolean ran = new AtomicBoolean(false);
        this.world.atfini(() -> ran.set(true));
        this.world.destroy();
        assertTrue(ran.get());

        this.world = new World();
    }

    @Test
    void rangeSet() {
        EntityRange range = this.world.rangeNew(100, 200);
        this.world.rangeSet(range);

        assertEquals(100, range.min());
        assertEquals(200, range.max());

        long e1 = this.world.entity();
        assertEquals(100, e1);

        this.world.obtainEntity(e1).destruct();

        long e2 = this.world.entity();
        assertEquals(100, this.world.stripGeneration(e2));

        EntityRange active = this.world.rangeGet();
        assertEquals(100, active.min());
        assertEquals(200, active.max());
    }

    @Test
    void stripGeneration() {
        long stripped = this.world.stripGeneration(0x1_0000_0010L);
        assertEquals(0x10, stripped);
    }

    @Test
    void typeInfo() {
        TypeInfo info = this.world.typeInfo(Position.class);
        assertNotNull(info);
        assertEquals(Float.BYTES * 2, info.size());
        assertNotNull(info.name());
    }

    @Test
    void singleton() {
        Entity singleton = this.world.singleton(Position.class);
        assertNotNull(singleton);
        assertEquals(this.world.id(Position.class), singleton.id());
    }

    @Test
    void getScope() {
        long scope = this.world.entity("scope");
        this.world.setScope(scope);
        assertEquals(scope, this.world.getScope());
        this.world.setScope(0);
    }

    @Test
    void isAlive() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        assertTrue(entity.isAlive());
        assertTrue(entity.isValid());
        assertFalse(this.world.obtainEntity(1000L).isAlive());
        assertFalse(this.world.obtainEntity(1000L).isValid());

        entity.destruct();
        assertFalse(entity.isAlive());
        assertFalse(entity.isValid());

        this.world.makeAlive(1000);
        assertTrue(this.world.obtainEntity(1000L).isAlive());
        assertTrue(this.world.obtainEntity(1000L).isValid());
    }

    @Test
    void makeAlive() {
        long e1 = this.world.entity();
        this.world.obtainEntity(e1).destruct();
        assertFalse(this.world.obtainEntity(e1).isAlive());

        long e2 = this.world.entity();
        this.world.makeAlive(e2);
        assertTrue(this.world.obtainEntity(e2).isAlive());
    }

    @Test
    void getTick() {
        assertEquals(0, this.world.getInfo().frameCountTotal());
        this.world.progress();
        assertEquals(1, this.world.getInfo().frameCountTotal());
        this.world.progress();
        assertEquals(2, this.world.getInfo().frameCountTotal());
    }

    @Test
    void multiWorld() {
        World world2 = new World();
        world2.component(Position.class);
        world2.component(Velocity.class);

        long p1 = this.world.id(Position.class);
        long p2 = world2.id(Position.class);
        assertTrue(p1 != 0);
        assertTrue(p2 != 0);

        Entity e1 = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        Entity e2 = world2.obtainEntity(world2.entity()).set(new Position(30, 40));

        Position pos1 = e1.get(Position.class);
        assertEquals(10.0f, pos1.x());
        Position pos2 = e2.get(Position.class);
        assertEquals(30.0f, pos2.x());

        world2.destroy();
    }

    @Test
    void getRef() {
        long componentId = this.world.id(Position.class);
        Ref<Position> ref = this.world.getRef(Position.class);
        assertNotNull(ref);
        assertEquals(componentId, ref.component());
        ref.destroy();
    }

    @Test
    void setGetContext() {
        Object ctx = new Object();
        this.world.setCtx(ctx);
        assertSame(ctx, this.world.getCtx());
    }

    @Test
    void makePair() {
        long r = this.world.entity();
        long t = this.world.entity();
        Id id = this.world.pair(r, t);

        assertTrue(id.isPair());
        assertEquals(r, id.first());
        assertEquals(t, id.second());
    }

    @Test
    void deltaTimeFromIterator() {
        AtomicInteger dt = new AtomicInteger();
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        this.world.system()
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .iter(it -> dt.set((int) it.deltaTime()));

        this.world.progress(2.0f);
        assertEquals(2, dt.get());
    }

    @Test
    void defer() {
        assertFalse(this.world.isDeferred());
        this.world.deferBegin();
        assertTrue(this.world.isDeferred());
        this.world.deferEnd();
        assertFalse(this.world.isDeferred());
    }

    @Test
    void toJsonAndFromJson() {
        this.world.obtainEntity(this.world.entity("json_entity")).set(new Position(10, 20));

        String json = this.world.toJson();
        assertNotNull(json);
        assertFalse(json.isEmpty());

        World world2 = new World();
        world2.component(Position.class);
        world2.fromJson(json);
        long entityId = world2.lookup("json_entity");
        assertTrue(entityId != 0);
        Entity restored = world2.obtainEntity(entityId);
        assertTrue(restored.has(Position.class));
        Position p = restored.get(Position.class);
        assertNotNull(p);

        world2.destroy();
    }

    @Test
    void scopeWithName() {
        long parent = this.world.entity("parent");
        try (ScopedWorld scope = this.world.obtainEntity(parent).scope()) {
            long child = scope.entity();
            assertTrue(this.world.obtainEntity(child).has(Flecs.ChildOf, parent));
        }
    }

    @Test
    void maxIdAndEntities() {
        this.world.entity();
        long[] entities = this.world.entities();
        assertTrue(entities.length > 0);
    }

    @Test
    void existsWithEntityIdTest() {
        long id = this.world.entity();
        assertTrue(this.world.exists(id));

        this.world.obtainEntity(id).destruct();
        assertTrue(this.world.exists(id));

        assertFalse(this.world.exists(999999L));
    }

    @Test
    void existsWithEntityTest() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        assertTrue(this.world.exists(entity));

        entity.destruct();
        assertTrue(this.world.exists(entity));

        Entity invalid = new Entity(this.world, 0);
        assertFalse(this.world.exists(invalid));
    }

    @Test
    void existsWithComponentClassTest() {
        assertTrue(this.world.exists(Position.class));
        assertTrue(this.world.exists(Health.class));
    }

    @Test
    void isAliveWithEntityIdTest() {
        long id = this.world.entity();
        assertTrue(this.world.isAlive(id));
        this.world.obtainEntity(id).destruct();
        assertFalse(this.world.isAlive(id));
        assertFalse(this.world.isAlive(999999L));
    }

    @Test
    void isAliveWithEntityTest() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        assertTrue(this.world.isAlive(entity));
        entity.destruct();
        assertFalse(this.world.isAlive(entity));
        Entity invalid = new Entity(this.world, 0);
        assertFalse(this.world.isAlive(invalid));
    }

    @Test
    void isAliveWithComponentClassTest() {
        assertTrue(this.world.isAlive(Position.class));
        assertTrue(this.world.isAlive(Health.class));
    }

    @Test
    void getAliveWithEntityIdTest() {
        long id = this.world.entity();
        assertEquals(id, this.world.getAlive(id));

        this.world.obtainEntity(id).destruct();
        long alive = this.world.getAlive(id);
        if (alive != 0) {
            assertTrue(this.world.isAlive(alive));
        }
        assertEquals(0, this.world.getAlive(999999L));
    }

    @Test
    void getAliveWithEntityTest() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        assertEquals(entity.id(), this.world.getAlive(entity));

        entity.destruct();
        long alive = this.world.getAlive(entity);
        if (alive != 0) {
            assertTrue(this.world.isAlive(alive));
        }
        Entity invalid = new Entity(this.world, 0);
        assertEquals(0, this.world.getAlive(invalid));
    }

    @Test
    void getAliveWithComponentClassTest() {
        long posId = this.world.id(Position.class);
        assertEquals(posId, this.world.getAlive(Position.class));
    }

    @Test
    void eachEntities() {
        int aliveCount = 5;
        for (int i = 0; i < aliveCount; i++) {
            this.world.entity();
        }

        long deadId = this.world.entity();
        this.world.obtainEntity(deadId).destruct();

        AtomicInteger aliveIterCount = new AtomicInteger(0);
        AtomicInteger deadIterCount = new AtomicInteger(0);
        this.world.each(entityId -> {
            if (this.world.isAlive(entityId)) {
                aliveIterCount.incrementAndGet();
            } else {
                deadIterCount.incrementAndGet();
            }
        });

        assertEquals(0, deadIterCount.get());
        assertTrue(aliveIterCount.get() >= aliveCount);
    }

    @Test
    void deltaTime() {
        assertEquals(0.0f, this.world.deltaTime(), 0.0001f);

        this.world.progress(0.5f);
        assertEquals(0.5f, this.world.deltaTime(), 0.0001f);

        this.world.progress(1.0f);
        assertEquals(1.0f, this.world.deltaTime(), 0.0001f);
    }

    @Test
    void getWith() {
        long positionId = this.world.id(Position.class);
        assertEquals(0, this.world.getWith());

        this.world.setWith(positionId);
        assertEquals(positionId, this.world.getWith());

        this.world.setWith(0);
        assertEquals(0, this.world.getWith());
    }

    @Test
    void worldCtx() {
        assertNull(this.world.getCtx());

        Object ctx = new Object();
        this.world.setCtx(ctx);
        assertEquals(ctx, this.world.getCtx());

        this.world.setCtx(null);
        assertNull(this.world.getCtx());
    }

    @Test
    void scriptRunAndUpdate() {
        Entity script = this.world.obtainEntity(this.world.script("ScriptEntityA {}").name("managed_script").run());
        assertNotEquals(0, this.world.lookup("ScriptEntityA"));

        this.world.script("ScriptEntityB {}").update(script);
        assertNotEquals(0, this.world.lookup("ScriptEntityB"));
        assertEquals(0, this.world.lookup("ScriptEntityA"));
    }
}
