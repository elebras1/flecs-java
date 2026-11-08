package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Mass;
import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.PositionMutView;
import io.github.elebras1.flecs.component.PositionView;
import io.github.elebras1.flecs.component.Tag;
import io.github.elebras1.flecs.component.Velocity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SingletonTest {

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
    void setGetSingleton() {
        this.world.set(new Position(10, 20));

        Position p = this.world.get(Position.class);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void setIsStoredOnComponentEntity() {
        this.world.set(new Position(10, 20));

        Entity singleton = this.world.singleton(Position.class);
        assertEquals(this.world.id(Position.class), singleton.id());
        assertEquals(new Position(10, 20), singleton.get(Position.class));
        assertFalse(this.world.obtainEntity(Flecs.World).has(Position.class));
    }

    @Test
    void setWithSingletonTrait() {
        this.world.singleton(Mass.class).add(Flecs.Singleton);

        this.world.set(new Mass(1.5f));

        assertEquals(1.5f, this.world.get(Mass.class).value());
    }

    @Test
    void tryGetSingleton() {
        assertNull(this.world.tryGet(Position.class));
        assertThrows(IllegalStateException.class, () -> this.world.get(Position.class));

        this.world.set(new Position(10, 20));
        assertEquals(new Position(10, 20), this.world.tryGet(Position.class));
    }

    @Test
    void viewSingleton() {
        this.world.set(new Position(10, 20));

        PositionView view = this.world.getView(Position.class);
        assertEquals(10.0f, view.x());

        PositionMutView mutView = this.world.getMutView(Position.class);
        mutView.x(30);
        assertEquals(30.0f, this.world.get(Position.class).x());

        assertNull(this.world.tryGetView(Velocity.class));
        assertNull(this.world.tryGetMutView(Velocity.class));
    }

    @Test
    void ensureAndModifiedSingleton() {
        AtomicInteger onSet = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .each(entityId -> onSet.incrementAndGet());

        PositionMutView view = this.world.ensure(Position.class);
        view.x(1);
        view.y(2);
        assertEquals(0, onSet.get());

        this.world.modified(Position.class);

        assertEquals(1, onSet.get());
        assertEquals(new Position(1, 2), this.world.get(Position.class));
    }

    @Test
    void insertSingleton() {
        this.world.insert(Position.class, (PositionMutView view) -> {
            view.x(1);
            view.y(2);
        });

        assertEquals(new Position(1, 2), this.world.get(Position.class));
    }

    @Test
    void refSingleton() {
        this.world.set(new Position(10, 20));

        Ref<Position> ref = this.world.getRef(Position.class);
        assertEquals(10.0f, ref.get().x());

        this.world.set(new Position(30, 40));
        assertEquals(30.0f, ref.get().x());
    }

    @Test
    void addHasRemoveSingleton() {
        assertFalse(this.world.has(Position.class));

        this.world.add(Position.class);
        assertTrue(this.world.has(Position.class));

        this.world.remove(Position.class);
        assertFalse(this.world.has(Position.class));
    }

    @Test
    void addHasRemoveSingletonById() {
        long tag = this.world.entity();
        assertFalse(this.world.has(tag));

        this.world.add(tag);
        assertTrue(this.world.has(tag));
        assertTrue(this.world.obtainEntity(tag).has(tag));

        this.world.remove(tag);
        assertFalse(this.world.has(tag));
    }

    @Test
    void setGetSingletonPair() {
        long target = this.world.entity();

        this.world.set(new Position(10, 20), target);

        assertTrue(this.world.has(Position.class, target));
        assertEquals(new Position(10, 20), this.world.get(Position.class, target));
        assertEquals(new Position(10, 20), this.world.tryGet(Position.class, target));
        assertTrue(this.world.singleton(Position.class).has(Position.class, target));
    }

    @Test
    void setGetSingletonPairWithClass() {
        this.world.set(new Position(10, 20), Velocity.class);

        assertTrue(this.world.has(Position.class, Velocity.class));
        assertEquals(new Position(10, 20), this.world.get(Position.class, Velocity.class));
    }

    @Test
    void addRemoveSingletonPair() {
        long first = this.world.entity();
        long second = this.world.entity();

        this.world.add(first, second);
        assertTrue(this.world.has(first, second));
        assertTrue(this.world.obtainEntity(first).has(first, second));

        this.world.remove(first, second);
        assertFalse(this.world.has(first, second));

        this.world.add(Position.class, second);
        assertTrue(this.world.has(Position.class, second));
        this.world.remove(Position.class, second);
        assertFalse(this.world.has(Position.class, second));

        this.world.add(Position.class, Velocity.class);
        assertTrue(this.world.has(Position.class, Velocity.class));
        this.world.remove(Position.class, Velocity.class);
        assertFalse(this.world.has(Position.class, Velocity.class));
    }

    @Test
    void getTarget() {
        long relation = this.world.component(Tag.class);
        long obj1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        long obj2 = this.world.obtainEntity(this.world.entity()).add(Velocity.class).id();
        long obj3 = this.world.obtainEntity(this.world.entity()).add(Mass.class).id();

        this.world.add(Tag.class, obj1);
        this.world.add(Tag.class, obj2);
        this.world.add(relation, obj3);

        assertEquals(obj1, this.world.target(Tag.class));
        assertEquals(obj2, this.world.target(Tag.class, 1));
        assertEquals(obj3, this.world.target(relation, 2));
        assertEquals(obj1, this.world.target(relation));
        assertEquals(obj2, this.world.target(Tag.class, relation, 1));
    }

    @Test
    void setOnStageIsDeferred() {
        World stage = this.world.getStage(0);

        stage.set(new Position(10, 20));
        assertFalse(this.world.has(Position.class));

        stage.merge();
        assertEquals(new Position(10, 20), this.world.get(Position.class));
    }

    @Test
    void singletonQuery() {
        this.world.set(new Position(10, 20));
        this.world.obtainEntity(this.world.entity()).set(new Velocity(1, 2));
        this.world.obtainEntity(this.world.entity()).set(new Velocity(3, 4));

        Query query = this.world.query()
                .with(Velocity.class)
                .with(Position.class)
                .src(Position.class)
                .build();

        List<Float> values = new ArrayList<>();
        query.iter(it -> {
            Field<Position> positions = it.field(Position.class, 1);
            assertEquals(1, positions.count());
            for (int i = 0; i < it.count(); i++) {
                values.add(positions.get(0).x());
                values.add(positions.get(0).y());
            }
        });

        assertEquals(List.of(10.0f, 20.0f, 10.0f, 20.0f), values);
    }

    private void setupMassSingleton(float value) {
        this.world.singleton(Mass.class).add(Flecs.Singleton);
        this.world.set(new Mass(value));
    }

    @Test
    void singletonSystemEach() {
        this.setupMassSingleton(1.5f);

        List<Float> values = new ArrayList<>();
        this.world.system()
                .with(Mass.class)
                .each(Mass.class, m -> values.add(m.value()));

        this.world.progress(1.0f);

        assertEquals(List.of(1.5f), values);
    }

    @Test
    void singletonSystemEachWithEntity() {
        this.setupMassSingleton(1.5f);

        List<Long> entities = new ArrayList<>();
        this.world.system()
                .with(Mass.class)
                .each(Mass.class, (entityId, m) -> entities.add(entityId));

        this.world.progress(1.0f);

        assertTrue(entities.isEmpty());
    }

    @Test
    void singletonSystemEachMixed() {
        this.setupMassSingleton(1.5f);
        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        List<Float> values = new ArrayList<>();
        this.world.system()
                .with(Position.class)
                .with(Mass.class)
                .each(Position.class, Mass.class, (p, m) -> values.add(m.value()));

        this.world.progress(1.0f);

        assertEquals(List.of(1.5f, 1.5f), values);
    }

    @Test
    void singletonSystemIter() {
        this.setupMassSingleton(1.5f);

        List<Float> values = new ArrayList<>();
        this.world.system()
                .with(Mass.class)
                .iter(it -> {
                    assertEquals(0, it.count());
                    Field<Mass> masses = it.field(Mass.class, 0);
                    assertEquals(1, masses.count());
                    values.add(masses.get(0).value());
                });

        this.world.progress(1.0f);

        assertEquals(List.of(1.5f), values);
    }

    @Test
    void singletonQueryEach() {
        this.setupMassSingleton(1.5f);

        List<Float> values = new ArrayList<>();
        Query query = this.world.query().with(Mass.class).build();
        query.each(Mass.class, m -> values.add(m.value()));

        assertEquals(List.of(1.5f), values);
    }

    @Test
    void singletonQueryEachWithEntity() {
        this.setupMassSingleton(1.5f);

        List<Long> entities = new ArrayList<>();
        Query query = this.world.query().with(Mass.class).build();
        query.each(Mass.class, (entityId, m) -> entities.add(entityId));

        assertTrue(entities.isEmpty());
    }

    @Test
    void singletonQueryEachMixed() {
        this.setupMassSingleton(1.5f);
        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        List<Float> values = new ArrayList<>();
        Query query = this.world.query().with(Position.class).with(Mass.class).build();
        query.each(Position.class, Mass.class, (p, m) -> values.add(m.value()));

        assertEquals(List.of(1.5f, 1.5f), values);
    }

    @Test
    void singletonQueryIter() {
        this.setupMassSingleton(1.5f);

        List<Float> values = new ArrayList<>();
        Query query = this.world.query().with(Mass.class).build();
        query.iter(it -> {
            Field<Mass> masses = it.field(Mass.class, 0);
            values.add(masses.get(0).value());
        });

        assertEquals(List.of(1.5f), values);
    }

    @Test
    void singletonQueryRun() {
        this.setupMassSingleton(1.5f);

        List<Float> values = new ArrayList<>();
        Query query = this.world.query().with(Mass.class).build();
        query.run(it -> {
            while (it.next()) {
                Field<Mass> masses = it.field(Mass.class, 0);
                values.add(masses.get(0).value());
            }
        });

        assertEquals(List.of(1.5f), values);
    }

    @Test
    void singletonObserverEachMixed() {
        this.setupMassSingleton(1.5f);

        List<Float> values = new ArrayList<>();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .with(Mass.class)
                .each(Position.class, Mass.class, (p, m) -> values.add(m.value()));

        this.world.obtainEntity(this.world.entity()).add(Position.class);

        assertEquals(List.of(1.5f), values);
    }

    @Test
    void getWithId() {
        long positionId = this.world.id(Position.class);
        this.world.set(new Position(10, 20));

        Position p = this.world.singleton(Position.class).get(positionId);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
    }

    @Test
    void tagSingletonDataOperationsAreInvalid() {
        this.world.component(Tag.class);
        assertThrows(AssertionError.class, () -> this.world.set(new Tag()));
        assertThrows(AssertionError.class, () -> this.world.get(Tag.class));
        assertThrows(AssertionError.class, () -> this.world.ensure(Tag.class));
    }
}
