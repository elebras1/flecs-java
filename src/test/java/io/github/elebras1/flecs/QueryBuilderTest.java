package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.ComparatorComponent;
import io.github.elebras1.flecs.component.Mass;
import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Velocity;
import io.github.elebras1.flecs.util.Flecs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderTest {

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
        this.world.destroy();
    }

    @Test
    void oneType() {
        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        this.world.obtainEntity(this.world.entity()).add(Velocity.class);

        Query query = this.world.query().with(Position.class).build();
        assertEquals(1, query.count());
        assertEquals(e1, query.first());
        query.destroy();
    }

    @Test
    void twoTypes() {
        long e1 = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2)).id();
        this.world.obtainEntity(this.world.entity()).set(new Velocity(10, 20));

        Query query = this.world.query().with(Position.class).with(Velocity.class).build();
        assertEquals(1, query.count());
        assertEquals(e1, query.first());

        List<Float> xs = new ArrayList<>();
        List<Float> ys = new ArrayList<>();
        query.each(Position.class, (entityId, p) -> {
            xs.add(p.x());
            ys.add(p.y());
        });
        assertEquals(List.of(10.0f), xs);
        assertEquals(List.of(20.0f), ys);
        query.destroy();
    }

    @Test
    void idTerm() {
        long tag = this.world.entity();
        long e1 = this.world.obtainEntity(this.world.entity()).add(tag).id();
        this.world.obtainEntity(this.world.entity()).set(new Velocity(10, 20));

        Query query = this.world.query().with(tag).build();
        assertEquals(1, query.count());
        assertEquals(e1, query.first());
        query.destroy();
    }

    @Test
    void idPairTerm() {
        long likes = this.world.entity();
        long apples = this.world.entity();
        long pears = this.world.entity();

        long e1 = this.world.obtainEntity(this.world.entity()).add(likes, apples).id();
        this.world.obtainEntity(this.world.entity()).add(likes, pears);

        Query query = this.world.query().with(likes, apples).build();
        assertEquals(1, query.count());
        assertEquals(e1, query.first());
        query.destroy();
    }

    @Test
    void idPairWildcardTerm() {
        long likes = this.world.entity();
        long apples = this.world.entity();
        long pears = this.world.entity();

        this.world.obtainEntity(this.world.entity()).add(likes, apples);
        this.world.obtainEntity(this.world.entity()).add(likes, pears);

        Query query = this.world.query().with(likes, Flecs.Wildcard).build();
        assertEquals(2, query.count());
        query.destroy();
    }

    @Test
    void typePairTerm() {
        long target = this.world.entity();
        long e1 = this.world.obtainEntity(this.world.entity())
                .add(this.world.getComponentId(Position.class), target).id();

        Query query = this.world.query().with(Position.class, target).build();
        assertEquals(1, query.count());
        assertEquals(e1, query.first());
        query.destroy();
    }

    @Test
    void without() {
        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        this.world.obtainEntity(this.world.entity()).add(Position.class).add(Velocity.class);

        Query query = this.world.query()
                .with(Position.class)
                .without(Velocity.class)
                .build();
        assertEquals(1, query.count());
        assertEquals(e1, query.first());
        query.destroy();
    }

    @Test
    void or() {
        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        long e2 = this.world.obtainEntity(this.world.entity()).add(Velocity.class).id();
        this.world.obtainEntity(this.world.entity()).add(Mass.class);

        Query query = this.world.query()
                .with(Position.class).or()
                .with(Velocity.class)
                .build();
        assertEquals(2, query.count());
        assertTrue(query.entities()[0] == e1 || query.entities()[0] == e2);
        query.destroy();
    }

    @Test
    void optional() {
        this.world.obtainEntity(this.world.entity()).add(Position.class).add(Velocity.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        Query query = this.world.query()
                .with(Position.class)
                .with(Velocity.class).optional()
                .build();
        assertEquals(2, query.count());
        query.destroy();
    }

    @Test
    void expr() {
        long e1 = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2)).id();
        this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));

        Query query = this.world.query().expr("Position, [in] Velocity").build();
        assertEquals(1, query.count());
        assertEquals(e1, query.first());
        query.destroy();
    }

    @Test
    void stringTerm() {
        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();

        Query query = this.world.query().with("Position").build();
        assertEquals(1, query.count());
        assertEquals(e1, query.first());
        query.destroy();
    }

    @Test
    void singletonTerm() {
        this.world.obtainEntity(Flecs.World).set(new Position(10, 20));
        this.world.obtainEntity(this.world.entity()).set(new Velocity(1, 2));

        Query query = this.world.query()
                .with(Velocity.class)
                .with(Position.class)
                .src(Flecs.World)
                .build();

        assertEquals(1, query.count());
        query.destroy();
    }

    @Test
    void explicitSubject() {
        Entity source = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));

        Query query = this.world.query()
                .with(Position.class)
                .src(source)
                .build();

        AtomicInteger invocations = new AtomicInteger();
        query.iter(it -> {
            Position p = it.field(Position.class, 0).get(0);
            assertEquals(source.id(), it.fieldSource(0));
            assertEquals(10.0f, p.x());
            assertEquals(20.0f, p.y());
            invocations.incrementAndGet();
        });
        assertEquals(1, invocations.get());
        query.destroy();
    }

    @Test
    void cached() {
        this.world.obtainEntity(this.world.entity()).add(Position.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class);

        Query query = this.world.query().with(Position.class).cached().build();
        assertEquals(2, query.count());

        this.world.obtainEntity(this.world.entity()).add(Position.class);
        assertEquals(3, query.count());
        query.destroy();
    }

    @Test
    void orderBy() {
        this.world.obtainEntity(this.world.entity()).set(new Position(3, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(2, 0));

        Query query = this.world.query()
                .with(Position.class)
                .orderBy(Position.class, (ComparatorComponent<Position>) (a, b) -> Float.compare(a.x(), b.x()))
                .build();

        long[] entities = query.entities();
        assertEquals(3, entities.length);
        assertEquals(1.0f, this.world.obtainEntityView(entities[0]).get(Position.class).x());
        assertEquals(2.0f, this.world.obtainEntityView(entities[1]).get(Position.class).x());
        assertEquals(3.0f, this.world.obtainEntityView(entities[2]).get(Position.class).x());
        query.destroy();
    }

    @Test
    void inOutShortcuts() {
        this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));

        Query query = this.world.query()
                .with(Position.class).in()
                .build();
        assertEquals(1, query.count());
        query.destroy();
    }
}
