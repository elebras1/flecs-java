package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.MassMutView;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import io.github.elebras1.flecs.component.Tag;
import io.github.elebras1.flecs.callback.ComparatorComponent;
import io.github.elebras1.flecs.component.Mass;
import io.github.elebras1.flecs.internal.ParamRegistry;
import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Velocity;
import io.github.elebras1.flecs.component.VelocityView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import io.github.elebras1.flecs.component.VelocityMutView;
import io.github.elebras1.flecs.component.PositionMutView;

class SystemTest {

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
        this.world.close();
    }

    @Test
    void timerApi() {
        FlecsSystem system = this.world.system("Timed", Position.class)
                .each(Position.class, p -> { });

        system.interval(1.0f);
        assertEquals(1.0f, system.interval());

        system.timeout(2.0f);
        assertEquals(2.0f, system.timeout());

        Timer timer = this.world.timer();
        assertDoesNotThrow(() -> {
            system.rate(2);
            system.tickSource(timer);
            system.start();
            system.stop();
        });
    }

    @Test
    void updateCallbacks() {
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        AtomicInteger count = new AtomicInteger();

        FlecsSystem system = this.world.system("UpdateRunEach", Position.class)
                .each(Position.class, p -> count.addAndGet(100));
        this.world.progress();
        assertEquals(100, count.get());

        assertNotNull(system.query());
        assertEquals(1, system.query().count());

        system.runEach(it -> count.set(1));
        this.world.progress();
        assertEquals(1, count.get());

    }

    @Test
    void systemQueryNotFreedOnWorldClose() {
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        FlecsSystem system = this.world.system("SystemQueryOwnership", Position.class)
                .each(Position.class, p -> {});

        assertEquals(1, system.query().count());
        system.destruct();
    }

    @Test
    void runWorker() {
        AtomicInteger runs = new AtomicInteger();
        FlecsSystem system = this.world.system("Worker", Position.class)
                .each(Position.class, p -> runs.incrementAndGet());
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        system.runWorker(0, 1);
        assertEquals(1, runs.get());
    }

    @Test
    void iter() {
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

        Velocity v = entity.get(Velocity.class);
        assertEquals(1.0f, v.x());
        assertEquals(2.0f, v.y());
    }

    @Test
    void each() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        this.world.system()
                .with(Position.class)
                .with(Velocity.class)
                .each(Position.class, (Iter it, int index, Position p) -> {
                    EntityView e = it.stage().obtainEntityView(it.entity(index));
                    Velocity v = e.get(Velocity.class);
                    e.insert(Position.class, (PositionMutView view) -> {
                        view.x(p.x() + v.x());
                        view.y(p.y() + v.y());
                    });
                });

        this.world.progress();

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(11.0f, p.x());
        assertEquals(22.0f, p.y());
    }

    @Test
    void signature() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        this.world.system()
                .with(Position.class)
                .with(Velocity.class).in()
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
    void copyNameOnCreate() {
        FlecsSystem sys = this.world.system("MySystem")
                .with(Position.class)
                .each(entityId -> {
                });

        assertTrue(sys.id() != 0);
        assertEquals("MySystem", sys.name());
    }

    @Test
    void kindAndProgress() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicInteger count = new AtomicInteger();
        this.world.system("KindSystem")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        assertEquals(0, count.get());
        this.world.progress();
        assertEquals(1, count.get());
        this.world.progress();
        assertEquals(2, count.get());
    }

    @Test
    void addFromEach() {
        Entity e2 = this.world.obtainEntity(this.world.entity());

        this.world.system()
                .with(Position.class)
                .iter(it -> {
                    e2.stage(it).add(Mass.class);
                    assertFalse(e2.has(Mass.class));
                });

        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.progress();

        assertTrue(e2.has(Mass.class));
    }

    @Test
    void newFromEach() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicInteger count = new AtomicInteger();
        this.world.system()
                .with(Position.class)
                .iter(it -> {
                    it.stage().entity();
                    count.incrementAndGet();
                });

        this.world.progress();
        assertEquals(1, count.get());
    }

    @Test
    void orderBy() {
        this.world.obtainEntity(this.world.entity()).set(new Position(3, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(5, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(2, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(4, 0));

        List<Float> values = new ArrayList<>();
        this.world.system()
                .with(Position.class)
                .orderBy(Position.class, (ComparatorComponent<Position>) (a, b) -> Float.compare(a.x(), b.x()))
                .each(Position.class, (entityId, p) -> values.add(p.x()));

        this.world.progress();

        assertEquals(5, values.size());
        assertEquals(1.0f, values.get(0));
        assertEquals(2.0f, values.get(1));
        assertEquals(3.0f, values.get(2));
        assertEquals(4.0f, values.get(3));
        assertEquals(5.0f, values.get(4));
    }

    @Test
    void deltaTime() {
        AtomicInteger dt = new AtomicInteger();
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        this.world.system()
                .with(Position.class)
                .iter(it -> dt.set((int) it.deltaTime()));

        this.world.progress(2.0f);
        assertEquals(2, dt.get());
    }

    @Test
    void runOnDemand() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicInteger count = new AtomicInteger();
        FlecsSystem sys = this.world.system("RunSystem")
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        sys.run();
        assertEquals(1, count.get());
        sys.run();
        assertEquals(2, count.get());
    }

    @Test
    void runWithParam() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicReference<String> received = new AtomicReference<>();
        FlecsSystem sys = this.world.system("ParamSystem")
                .with(Position.class)
                .iter(it -> received.set(it.<String>param()));

        sys.run("hello");
        assertEquals("hello", received.get());

        sys.run(0.5f, "world");
        assertEquals("world", received.get());
    }

    @Test
    void runWithoutParam() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicReference<String> received = new AtomicReference<>("unset");
        FlecsSystem sys = this.world.system("NoParamSystem")
                .with(Position.class)
                .iter(it -> received.set(it.param()));

        sys.run();
        assertNull(received.get());
    }

    @Test
    void timerStartStop() {
        Timer timer = this.world.timer().interval(1.0f);

        timer.start();
        assertTrue(timer.isAlive());
        timer.stop();
        assertTrue(timer.isAlive());
    }

    @Test
    void destructRemovesSystemCtxEntry() {
        FlecsSystem system = this.world.system("CtxSystem", Position.class)
                .each(Position.class, p -> { });

        system.setCtx(new Object());
        MemorySegment sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), system.id());
        long ctxId = ecs_system_t.ctx(sysSeg).address();
        assertNotNull(ParamRegistry.get(ctxId));

        system.destruct();
        assertNull(ParamRegistry.get(ctxId));
    }

    @Test
    void setCtxNullClearsSystemCtx() {
        FlecsSystem system = this.world.system("CtxSystem", Position.class)
                .each(Position.class, p -> { });

        system.setCtx(new Object());
        MemorySegment sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), system.id());
        long oldCtxId = ecs_system_t.ctx(sysSeg).address();
        assertNotNull(ParamRegistry.get(oldCtxId));

        system.setCtx(null);

        assertNull(system.getCtx());
        assertNull(ParamRegistry.get(oldCtxId));
        sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), system.id());
        assertEquals(ParamRegistry.NO_CTX, ecs_system_t.ctx(sysSeg).address());
    }

    @Test
    void multiThreadedEachViewWritesEveryEntity() {
        int n = 200_000;
        int steps = 10;
        this.world.setThreads(4);

        long[] ids = this.world.entityBulk(n, Position.class, Velocity.class);
        for (long id : ids) {
            VelocityMutView velocity = this.world.obtainEntityView(id).getMutView(Velocity.class);
            velocity.x(1.0f);
            velocity.y(1.0f);
        }

        this.world.system("MoveAll", Position.class, Velocity.class)
                .kind(Flecs.OnUpdate)
                .multiThreaded(true)
                .eachView(Position.class, Velocity.class, (PositionMutView position, VelocityView velocity) -> {
                    position.x(position.x() + velocity.x());
                    position.y(position.y() + velocity.y());
                });

        for (int step = 0; step < steps; step++) {
            this.world.progress();
        }

        for (long id : ids) {
            Position position = this.world.obtainEntity(id).get(Position.class);
            assertEquals(steps, position.x(), 0.0f, "entity " + id + " x");
            assertEquals(steps, position.y(), 0.0f, "entity " + id + " y");
        }
    }

    @Test
    void runEachVisitsEveryTableWithReusedIter() {
        for (int i = 0; i < 3; i++) {
            this.world.obtainEntity(this.world.entity()).set(new Position(i, i));
        }
        for (int i = 0; i < 2; i++) {
            this.world.obtainEntity(this.world.entity()).set(new Position(i, i)).set(new Mass(i));
        }

        FlecsSystem system = this.world.system("Visit", Position.class).each(Position.class, p -> { });
        List<Integer> counts = new ArrayList<>();
        List<Iter> iters = new ArrayList<>();
        system.runEach(it -> {
            counts.add(it.count());
            iters.add(it);
        });

        this.world.progress();

        counts.sort(Integer::compare);
        assertEquals(List.of(2, 3), counts);
        assertSame(iters.get(0), iters.get(1));
    }

    @Test
    void stageHandleIsDeferredInSystem() {
        for (int i = 0; i < 4; i++) {
            this.world.obtainEntity(this.world.entity()).set(new Position(i, 0));
        }

        List<Integer> counts = new ArrayList<>();
        this.world.system()
                .with(Position.class)
                .iter(it -> {
                    for (int i = 0; i < it.count(); i++) {
                        it.stage().obtainEntity(it.entity(i)).destruct();
                        counts.add(this.world.count(Position.class));
                    }
                });

        this.world.progress();

        assertEquals(List.of(4, 4, 4, 4), counts);
        assertEquals(0, this.world.count(Position.class));
    }

    @Test
    void entityStageFromIterIsDeferredInSystem() {
        Entity other = this.world.obtainEntity(this.world.entity());
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        this.world.system()
                .with(Position.class)
                .iter(it -> {
                    other.stage(it).add(Mass.class);
                    assertFalse(other.has(Mass.class));
                });

        this.world.progress();

        assertTrue(other.has(Mass.class));
    }

    @Test
    void scopedWorldBuildsSystem() {
        long parent = this.world.entity("ScopedParent");
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        AtomicInteger count = new AtomicInteger();
        try (ScopedWorld scoped = this.world.obtainEntity(parent).scope()) {
            scoped.system("ScopedSystem").with(Position.class).iter(it -> count.addAndGet(it.count()));
        }

        this.world.progress();

        assertEquals(1, count.get());
    }

    @Test
    void multiThreadedFieldViewsArePerStage() {
        int n = 200_000;
        int frames = 10;
        this.world.setThreads(8);
        long[] ids = new long[n];
        for (int i = 0; i < n; i++) {
            ids[i] = this.world.obtainEntity(this.world.entity()).set(new Position(0, 0)).id();
        }

        Set<Integer> views = ConcurrentHashMap.newKeySet();
        Set<Long> threads = ConcurrentHashMap.newKeySet();
        this.world.system()
                .with(Position.class)
                .multiThreaded()
                .iter(it -> {
                    Field<Position> field = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        PositionMutView view = field.getMutView(i);
                        if (i == 0) {
                            views.add(System.identityHashCode(view));
                            threads.add(Thread.currentThread().threadId());
                        }
                        view.x(view.x() + 1);
                    }
                });

        for (int frame = 0; frame < frames; frame++) {
            this.world.progress();
        }

        assertEquals(threads.size(), views.size());
        for (long id : ids) {
            assertEquals(frames, this.world.obtainEntity(id).get(Position.class).x());
        }
    }

    @Test
    void multiThreadedEntityViewsOnStageAreNotShared() {
        int n = 100_000;
        this.world.setThreads(8);
        for (int i = 0; i < n; i++) {
            this.world.obtainEntity(this.world.entity()).add(Position.class);
        }

        LongAdder wrong = new LongAdder();
        this.world.system()
                .with(Position.class)
                .multiThreaded()
                .iter(it -> {
                    World stage = it.stage();
                    for (int i = 0; i < it.count(); i++) {
                        long id = it.entity(i);
                        EntityView view = stage.obtainEntityView(id);
                        Thread.onSpinWait();
                        if (view.id() != id) {
                            wrong.increment();
                        }
                    }
                });

        for (int frame = 0; frame < 5; frame++) {
            this.world.progress();
        }

        assertEquals(0, wrong.sum());
    }

    @Test
    void systemAndObserverEachOnTag() {
        this.world.component(Tag.class);
        AtomicInteger systemCount = new AtomicInteger();
        AtomicInteger observerCount = new AtomicInteger();
        this.world.system().with(Tag.class).each(Tag.class, (Tag tag) -> systemCount.incrementAndGet());
        this.world.observer().event(Flecs.OnAdd).with(Tag.class).each(Tag.class, (Tag tag) -> observerCount.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).add(Tag.class);
        this.world.progress();

        assertEquals(1, systemCount.get());
        assertEquals(1, observerCount.get());
    }

    @Test
    void setThreadsAfterSystemIsBuilt() {
        int n = 1000;
        for (int i = 0; i < n; i++) {
            this.world.obtainEntity(this.world.entity()).add(Position.class);
        }

        AtomicInteger iterCount = new AtomicInteger();
        AtomicInteger runCount = new AtomicInteger();
        AtomicInteger eachCount = new AtomicInteger();
        this.world.system("IterSystem").with(Position.class).multiThreaded()
                .iter(it -> iterCount.addAndGet(it.count()));
        this.world.system("RunSystem").with(Position.class).multiThreaded()
                .run(it -> {
                    while (it.next()) {
                        runCount.addAndGet(it.count());
                    }
                });
        this.world.system("EachSystem").with(Position.class).multiThreaded()
                .eachView(Position.class, (PositionMutView view) -> eachCount.incrementAndGet());

        this.world.setThreads(4);
        this.world.progress();

        assertEquals(n, iterCount.get());
        assertEquals(n, runCount.get());
        assertEquals(n, eachCount.get());
    }

    @Test
    void iterStageFollowsThreadCountChanges() {
        int n = 1000;
        long[] ids = new long[n];
        for (int i = 0; i < n; i++) {
            ids[i] = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        }

        this.world.setThreads(2);
        this.world.system()
                .with(Position.class)
                .without(Mass.class)
                .multiThreaded()
                .iter(it -> {
                    World stage = it.stage();
                    assertTrue(stage.isStage());
                    for (int i = 0; i < it.count(); i++) {
                        stage.obtainEntity(it.entity(i)).set(new Mass(1));
                    }
                });

        this.world.setThreads(4);
        this.world.progress();

        for (long id : ids) {
            assertEquals(1.0f, this.world.obtainEntity(id).get(Mass.class).value());
        }
    }

    @Test
    void sparseComponentSystemEach() {
        this.world.obtainEntity(this.world.id(Position.class)).add(Flecs.Sparse);
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));

        List<Float> values = new ArrayList<>();
        this.world.system().with(Position.class).each(Position.class, (Position position) -> values.add(position.x()));
        this.world.system().with(Position.class).eachView(Position.class, (PositionMutView position) -> position.x(position.x() + 10));
        this.world.progress();

        assertEquals(List.of(1.0f), values);
        assertEquals(11.0f, entity.get(Position.class).x());
    }

    @Test
    void componentInheritanceSystemEachView() {
        this.world.obtainEntity(this.world.id(Position.class)).isA(this.world.id(Mass.class));
        Entity e1 = this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        Entity e2 = this.world.obtainEntity(this.world.entity()).set(new Position(3, 4));

        this.world.system().with(Mass.class).eachView(Mass.class, (MassMutView mass) -> mass.value(mass.value() + 10));
        this.world.progress();

        assertEquals(new Position(11, 2), e1.get(Position.class));
        assertEquals(new Position(13, 4), e2.get(Position.class));
    }

    @Test
    void sparseComponentMultiThreadedSystem() {
        this.world.obtainEntity(this.world.id(Position.class)).add(Flecs.Sparse);
        this.world.setThreads(4);
        int n = 1000;
        long[] ids = new long[n];
        for (int i = 0; i < n; i++) {
            ids[i] = this.world.obtainEntity(this.world.entity()).set(new Position(0, 0)).id();
        }

        this.world.system()
                .with(Position.class)
                .multiThreaded()
                .eachView(Position.class, (PositionMutView position) -> position.x(position.x() + 1));
        this.world.progress();

        for (long id : ids) {
            assertEquals(1.0f, this.world.obtainEntity(id).get(Position.class).x());
        }
    }
}
