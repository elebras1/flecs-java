package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.HashMap;
import java.util.Map;
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
        this.world.set(new Rest().port(27750));
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

    @Test
    void scriptFromFilename() throws Exception {
        Path file = Files.createTempFile("flecs-script", ".flecs");
        Files.writeString(file, "FilenameEntityA {}");
        try {
            this.world.script(null).filename(file.toString()).run();
            assertNotEquals(0, this.world.lookup("FilenameEntityA"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void ctxFreeOnWorldDestroy() {
        World world2 = new World();
        long rel = world2.entity();
        long tgt = world2.entity();
        world2.obtainEntity(world2.entity()).add(rel, tgt);

        AtomicReference<Object> received = new AtomicReference<>();
        Object ctx = new Object();

        Query query = world2.query()
                .with(rel, Flecs.Wildcard)
                .groupBy(rel)
                .groupByCtx(ctx, received::set)
                .cached()
                .build();
        assertEquals(1, query.count());

        world2.destroy();

        assertSame(ctx, received.get());
    }

    @Test
    void appRunFrames() {
        this.world.app()
                .targetFps(60)
                .deltaTime(0.016f)
                .threads(1)
                .frames(1)
                .run();
    }

    @Test
    void setRest() {
        this.world.set(new Rest().port(27754));

        assertNotNull(Flecs.Stats);
    }

    @Test
    void deferRunnable() {
        AtomicInteger count = new AtomicInteger();
        this.world.defer(() -> {
            this.world.obtainEntity(this.world.entity()).add(Position.class);
            count.incrementAndGet();
        });
        assertEquals(1, count.get());
        assertEquals(1, this.world.count(Position.class));
    }

    @Test
    void typedPrefab() {
        long positionId = this.world.prefab(Position.class);
        this.world.obtainEntity(positionId).set(new Position(1, 2));

        Entity inst = this.world.obtainEntity(this.world.entity()).isA(Position.class);

        assertTrue(inst.has(Flecs.IsA, positionId));
    }

    @Test
    void prefabWithName() {
        long prefabId = this.world.prefab("SpaceShip");

        Entity prefab = this.world.obtainEntity(prefabId);
        assertEquals("SpaceShip", prefab.name());
        assertTrue(prefab.has(Flecs.Prefab));
    }

    @Test
    void prefabWithParentStorage() {
        Entity parent = this.world.obtainEntity(this.world.prefab("SpaceShip"));
        Entity child = this.world.obtainEntity(this.world.prefab())
                .set(new FlecsParent(parent.id()))
                .name("Cockpit");

        assertNotNull(child.get(FlecsParent.class));
        assertEquals(parent.id(), child.get(FlecsParent.class).value());
        assertTrue(child.has(Flecs.Prefab));
    }

    @Test
    void builtinPipeline() {
        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(Flecs.Phase).cascade(Flecs.DependsOn)
                .without(Flecs.Disabled).up(Flecs.DependsOn)
                .without(Flecs.Disabled).up(Flecs.ChildOf)
                .build();

        assertNotEquals(0, pipeline.id());
    }

    @Test
    void customPipeline() {
        long foo = this.world.entity("Foo");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(foo)
                .build();

        assertNotEquals(0, pipeline.id());

        this.world.setPipeline(pipeline);
        assertEquals(pipeline, this.world.getPipeline());
    }

    @Test
    void builtinPipelineWithPairTerm() {
        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(Flecs.Phase).cascade(Flecs.DependsOn)
                .with(Flecs.DependsOn, Flecs.OnStart).trav(Flecs.DependsOn)
                .without(Flecs.Disabled).up(Flecs.DependsOn)
                .without(Flecs.Disabled).up(Flecs.ChildOf)
                .build();

        assertNotEquals(0, pipeline.id());
    }

    @Test
    void pipelineWithPairMatchesSystems() {
        long rel = this.world.entity("PairRel");
        long target = this.world.entity("PairTgt");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(rel, target)
                .build();

        assertNotEquals(0, pipeline.id());

        this.world.setPipeline(pipeline);

        AtomicInteger count = new AtomicInteger();
        FlecsSystem system = this.world.system("PairSystem")
                .with(Position.class)
                .iter(it -> count.incrementAndGet());
        system.add(rel, target);

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.progress(0.016f);

        assertEquals(1, count.get());
    }

    @Test
    void pipelineWithSecondTerm() {
        long rel = this.world.entity("SecondRel");
        long target = this.world.entity("SecondTgt");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(rel)
                .second(target)
                .build();

        assertNotEquals(0, pipeline.id());

        this.world.setPipeline(pipeline);

        AtomicInteger count = new AtomicInteger();
        FlecsSystem system = this.world.system("SecondSystem")
                .with(Position.class)
                .iter(it -> count.incrementAndGet());
        system.add(rel, target);

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.progress(0.016f);

        assertEquals(1, count.get());
    }

    @Test
    void pipelineWithoutTermSkipsSystems() {
        long skip = this.world.entity("Skip");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .without(skip)
                .build();

        assertNotEquals(0, pipeline.id());

        this.world.setPipeline(pipeline);

        AtomicInteger count = new AtomicInteger();
        this.world.system("SkippedSystem")
                .with(Position.class)
                .iter(it -> count.addAndGet(100))
                .add(skip);
        this.world.system("KeptSystem")
                .with(Position.class)
                .iter(it -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.progress(0.016f);

        assertEquals(1, count.get());
    }

    @Test
    void pipelineTermSelection() {
        long phase = this.world.entity("SelectedPhase");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(phase)
                .termAt(1)
                .self()
                .inout(Flecs.InOutFilter)
                .build();

        assertNotEquals(0, pipeline.id());

        this.world.setPipeline(pipeline);

        AtomicInteger count = new AtomicInteger();
        this.world.system("SelectedSystem")
                .kind(phase)
                .with(Position.class)
                .iter(it -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.progress(0.016f);

        assertEquals(1, count.get());
    }

    @Test
    void pipelineWithNameTermOverloads() {
        long tag = this.world.entity("PipelineTag");
        long rel = this.world.entity("PipelineRel");
        long target = this.world.entity("PipelineTarget");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with("PipelineTag")
                .with("PipelineRel", "PipelineTarget")
                .with("PipelineTarget", target)
                .build();

        assertNotEquals(0, pipeline.id());

        this.world.setPipeline(pipeline);

        AtomicInteger count = new AtomicInteger();
        FlecsSystem matching = this.world.system("NameTermSystem")
                .with(Position.class)
                .iter(it -> count.addAndGet(100));
        matching.add(tag).add(rel, target).add(target, target);

        this.world.system("NameTermSystemPartial")
                .with(Position.class)
                .iter(it -> count.addAndGet(1000))
                .add(tag);

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.progress(0.016f);

        assertEquals(100, count.get());
    }

    @Test
    void pipelineWithClassTermOverloads() {
        long target = this.world.entity("ClassPairTarget");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class, target)
                .with(Velocity.class, this.world.obtainEntity(target))
                .with(Position.class, Velocity.class)
                .build();

        assertNotEquals(0, pipeline.id());

        this.world.setPipeline(pipeline);

        AtomicInteger count = new AtomicInteger();
        FlecsSystem matching = this.world.system("ClassTermSystem")
                .with(Position.class)
                .iter(it -> count.incrementAndGet());
        matching.add(Position.class, target);
        matching.add(Velocity.class, this.world.obtainEntity(target));
        matching.add(this.world.id(Position.class), this.world.id(Velocity.class));

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.progress(0.016f);

        assertEquals(1, count.get());
    }

    @Test
    void pipelineWithoutTermOverloads() {
        long tag = this.world.entity("SkipTag");
        long rel = this.world.entity("SkipRel");
        long target = this.world.entity("SkipTarget");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .without("SkipTag")
                .without(rel, target)
                .without(Position.class, target)
                .without(Velocity.class, this.world.obtainEntity(target))
                .without(Position.class, Velocity.class)
                .without(Position.class, "SkipTarget")
                .build();

        assertNotEquals(0, pipeline.id());

        this.world.setPipeline(pipeline);

        AtomicInteger count = new AtomicInteger();
        this.world.system("SkippedSystem")
                .with(Position.class)
                .iter(it -> count.addAndGet(1000))
                .add(tag);
        this.world.system("KeptSystem")
                .with(Position.class)
                .iter(it -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.progress(0.016f);

        assertEquals(1, count.get());
    }

    @Test
    void pipelineTermTraversalModifiers() {
        long rel = this.world.entity("TraversalRel");
        long target = this.world.entity("TraversalTarget");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class).self()
                .with(rel).second(target)
                .termAt(1).cascade(Flecs.DependsOn).desc()
                .with(Velocity.class).term().up().trav(Flecs.DependsOn)
                .without(Mass.class).parent()
                .build();

        assertNotEquals(0, pipeline.id());
    }

    @Test
    void pipelineTermVariable() {
        long rel = this.world.entity("VarRel");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(rel).var("PipelineTarget")
                .build();

        assertNotEquals(0, pipeline.id());
    }

    @Test
    void pipelineTermSourceAndFlags() {
        long source = this.world.entity("TermSource");

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class).src(source)
                .with(Velocity.class).in()
                .with(Mass.class).out()
                .with(Position.class).inout()
                .with(Velocity.class).inout(Flecs.InOutFilter)
                .with(Mass.class).filter()
                .build();

        assertNotEquals(0, pipeline.id());

        Pipeline flags = this.world.pipeline()
                .with(Flecs.System)
                .with(Flecs.PredEq).second("Position").flags(Flecs.IsName)
                .build();

        assertNotEquals(0, flags.id());

        Pipeline idFlags = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class).idFlags(Flecs.Toggle)
                .build();

        assertNotEquals(0, idFlags.id());
    }

    @Test
    void pipelineOperatorsAndScopes() {
        long prefab = this.world.prefab("OperatorPrefab");
        this.world.obtainEntity(prefab).set(new Position(1, 2));

        Pipeline andFrom = this.world.pipeline()
                .with(Flecs.System)
                .with(prefab).andFrom()
                .build();

        assertNotEquals(0, andFrom.id());

        Pipeline orFrom = this.world.pipeline()
                .with(Flecs.System)
                .with(prefab).orFrom()
                .build();

        assertNotEquals(0, orFrom.id());

        Pipeline notFrom = this.world.pipeline()
                .with(Flecs.System)
                .with(prefab).notFrom()
                .build();

        assertNotEquals(0, notFrom.id());

        Pipeline scoped = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class)
                .scopeOpen().not()
                .with(Velocity.class).or()
                .with(Mass.class)
                .scopeClose()
                .build();

        assertNotEquals(0, scoped.id());

        Pipeline optional = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class).and()
                .with(Velocity.class).optional()
                .build();

        assertNotEquals(0, optional.id());
    }

    @Test
    void pipelineQueryOptions() {
        long region = this.world.entity("PipelineRegion");
        Object ctx = new Object();

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class)
                .cached()
                .queryFlags(Flecs.QueryMatchEmptyTables)
                .groupBy(region)
                .ctx(ctx)
                .build();

        assertNotEquals(0, pipeline.id());

        Pipeline ordered = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class)
                .orderBy(Position.class)
                .build();

        assertNotEquals(0, ordered.id());
    }

    @Test
    void pipelineRejectsChangeDetectionAndExpr() {
        assertThrows(IllegalStateException.class, () -> this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class)
                .detectChanges()
                .build());

        assertThrows(IllegalStateException.class, () -> this.world.pipeline()
                .expr("System")
                .build());
    }

    @Test
    void setPipelineWithId() {
        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .build();

        this.world.setPipeline(pipeline.id());

        assertEquals(pipeline.id(), this.world.getPipeline().id());
    }

    @Test
    void getPipelineDefault() {
        assertNotNull(this.world.getPipeline());
    }

    @Test
    void deleteEmptyTables() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        entity.destruct();

        long tablesBefore = this.world.stats().tableCount();
        assertTrue(tablesBefore > 0);

        for (int i = 0; i < 12; i++) {
            this.world.deleteEmptyTables(0, 10, 0.0, 0);
        }

        assertTrue(this.world.stats().tableCount() < tablesBefore);
    }

    @Test
    void pipelineTermStringVariables() {
        long rel = this.world.entity("PipelineStringVarRel");
        long target = this.world.entity("PipelineStringVarTarget");

        Pipeline variable = this.world.pipeline()
                .with(Flecs.System)
                .with("$PipelineVar")
                .build();
        assertNotEquals(0, variable.id());

        Pipeline pair = this.world.pipeline()
                .with(Flecs.System)
                .with("$PipelineRel", target)
                .build();
        assertNotEquals(0, pair.id());

        Pipeline secondVar = this.world.pipeline()
                .with(Flecs.System)
                .with(rel).second("$PipelineTarget")
                .build();
        assertNotEquals(0, secondVar.id());

        Pipeline firstVar = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class).with("$PipelineFirst")
                .build();
        assertNotEquals(0, firstVar.id());

        Pipeline secondString = this.world.pipeline()
                .with(Flecs.System)
                .with(rel, "$PipelineTarget")
                .build();
        assertNotEquals(0, secondString.id());
    }

    @Test
    void pipelineGroupByCtx() {
        long region = this.world.entity("PipelineGroupRegion");
        long region1 = this.world.entity("PipelineGroupRegion1");
        this.world.obtainEntity(this.world.entity()).add(region, region1).set(new Position(1, 2));

        Object ctx = new Object();

        Pipeline pipeline = this.world.pipeline()
                .with(Flecs.System)
                .with(Position.class)
                .groupBy(region)
                .groupByCtx(ctx)
                .onGroupCreate((world, groupId, received) -> {
                    assertSame(ctx, received);
                    return null;
                })
                .onGroupDelete((world, groupId, groupCtx, received) -> assertSame(ctx, received))
                .cached()
                .build();

        assertNotEquals(0, pipeline.id());
    }

    @Test
    void queryGroupByCtx() {
        long rel = this.world.entity("WorldGroupRel");
        long tgtA = this.world.entity("WorldGroupTgtA");
        long tgtB = this.world.entity("WorldGroupTgtB");
        long tgtC = this.world.entity("WorldGroupTgtC");

        this.world.obtainEntity(this.world.entity()).add(rel, tgtA);
        this.world.obtainEntity(this.world.entity()).add(rel, tgtB);
        this.world.obtainEntity(this.world.entity()).add(rel, tgtC);

        Object ctx = new Object();
        Map<Long, Object> groupCtxs = new HashMap<>();
        AtomicInteger created = new AtomicInteger();
        AtomicInteger deleted = new AtomicInteger();

        Query query = this.world.query()
                .with(rel, Flecs.Wildcard)
                .groupBy(rel)
                .groupByCtx(ctx)
                .onGroupCreate((world, groupId, received) -> {
                    assertSame(ctx, received);
                    created.incrementAndGet();
                    Object groupCtx = new Object();
                    groupCtxs.put(groupId, groupCtx);
                    return groupCtx;
                })
                .onGroupDelete((world, groupId, groupCtx, received) -> {
                    assertSame(ctx, received);
                    assertSame(groupCtxs.get(groupId), groupCtx);
                    deleted.incrementAndGet();
                })
                .cached()
                .build();

        assertEquals(3, query.count());
        assertTrue(created.get() >= 3);

        query.destroy();
        assertEquals(created.get(), deleted.get());
    }

    @Test
    void queryGroupByActionReceivesCtx() {
        long rel = this.world.entity("WorldGroupActionRel");
        long tgtA = this.world.entity("WorldGroupActionTgtA");

        this.world.obtainEntity(this.world.entity()).add(rel, tgtA);

        Object ctx = new Object();
        AtomicInteger groupByCalls = new AtomicInteger();

        Query query = this.world.query()
                .with(rel, Flecs.Wildcard)
                .groupBy(rel, (world, table, id, received) -> {
                    assertSame(ctx, received);
                    groupByCalls.incrementAndGet();
                    return id;
                })
                .groupByCtx(ctx)
                .cached()
                .build();

        assertEquals(1, query.count());
        assertTrue(groupByCalls.get() >= 1);
        query.destroy();
    }

    @Test
    void queryGroupByCtxFree() {
        long rel = this.world.entity("WorldGroupFreeRel");
        long tgt = this.world.entity("WorldGroupFreeTgt");
        this.world.obtainEntity(this.world.entity()).add(rel, tgt);

        Object ctx = new Object();
        AtomicInteger freed = new AtomicInteger();

        Query query = this.world.query()
                .with(rel, Flecs.Wildcard)
                .groupBy(rel)
                .groupByCtx(ctx, received -> {
                    assertSame(ctx, received);
                    freed.incrementAndGet();
                })
                .cached()
                .build();

        assertEquals(1, query.count());
        query.destroy();
        assertEquals(1, freed.get());
    }
}
