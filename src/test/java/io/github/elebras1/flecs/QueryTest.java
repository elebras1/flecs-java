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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

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
    void termEachComponent() {
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.obtainEntity(this.world.entity()).set(new Position(3, 4));
        this.world.obtainEntity(this.world.entity()).set(new Position(5, 6));

        Query query = this.world.query().with(Position.class).build();

        AtomicInteger count = new AtomicInteger();
        query.each(Position.class, (entityId, p) -> count.incrementAndGet());
        assertEquals(3, count.get());

        query.destroy();
    }

    @Test
    void termEachId() {
        long foo = this.world.entity();
        this.world.obtainEntity(this.world.entity()).add(foo);
        this.world.obtainEntity(this.world.entity()).add(foo);
        this.world.obtainEntity(this.world.entity()).add(foo);

        Query query = this.world.query().with(foo).build();

        AtomicInteger count = new AtomicInteger();
        query.each(entityId -> count.incrementAndGet());
        assertEquals(3, count.get());

        query.destroy();
    }

    @Test
    void termEachPair() {
        long rel = this.world.entity();
        long obj = this.world.entity();
        this.world.obtainEntity(this.world.entity()).add(rel, obj);
        this.world.obtainEntity(this.world.entity()).add(rel, obj);

        Query query = this.world.query().with(rel, obj).build();
        assertEquals(2, query.count());
        query.destroy();
    }

    @Test
    void run() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        Query query = this.world.query().with(Position.class).with(Velocity.class).build();
        query.iter(it -> {
            Field<Position> positions = it.field(Position.class, 0);
            Field<Velocity> velocities = it.field(Velocity.class, 1);
            for (int i = 0; i < it.count(); i++) {
                Position p = positions.get(i);
                Velocity v = velocities.get(i);
                positions.set(i, new Position(p.x() + v.x(), p.y() + v.y()));
            }
        });

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(11.0f, p.x());
        assertEquals(22.0f, p.y());

        query.destroy();
    }

    @Test
    void eachWithEntity() {
        Entity e1 = this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        Entity e2 = this.world.obtainEntity(this.world.entity()).set(new Position(3, 4));

        Query query = this.world.query().with(Position.class).build();

        Map<Long, Float> positions = new HashMap<>();
        query.each(Position.class, (entityId, p) -> positions.put(entityId, p.x()));
        assertEquals(2, positions.size());
        assertEquals(1.0f, positions.get(e1.id()));
        assertEquals(3.0f, positions.get(e2.id()));

        query.destroy();
    }

    @Test
    void countAndFirst() {
        long e1 = this.world.obtainEntity(this.world.entity()).set(new Position(1, 0)).id();
        this.world.obtainEntity(this.world.entity()).set(new Position(2, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(3, 0));

        Query query = this.world.query().with(Position.class).build();
        assertEquals(3, query.count());
        assertEquals(e1, query.first());

        long[] entities = query.entities();
        assertEquals(3, entities.length);

        query.destroy();
    }

    @Test
    void find() {
        this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        long e2 = this.world.obtainEntity(this.world.entity()).set(new Position(20, 30)).id();

        Query query = this.world.query().with(Position.class).build();

        long found = query.find(Position.class, p -> p.x() == 20);
        assertEquals(e2, found);

        long notFound = query.find(Position.class, p -> p.x() == 30);
        assertEquals(0, notFound);

        query.destroy();
    }

    @Test
    void findWithEntity() {
        this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20)).set(new Velocity(20, 30));
        long e2 = this.world.obtainEntity(this.world.entity())
                .set(new Position(20, 30)).set(new Velocity(20, 30)).id();

        Query query = this.world.query().with(Position.class).build();

        AtomicReference<Long> found = new AtomicReference<>(0L);
        query.each(Position.class, (entityId, p) -> {
            Velocity v = this.world.obtainEntityView(entityId).get(Velocity.class);
            if (v != null && p.x() == v.x() && p.y() == v.y()) {
                found.set(entityId);
            }
        });
        assertEquals(e2, found.get());

        query.destroy();
    }

    @Test
    void findWithMatchEmptyTables() {
        long e1 = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20)).add(Velocity.class).id();
        this.world.obtainEntity(e1).destruct();
        long e2 = this.world.obtainEntity(this.world.entity()).set(new Position(20, 30)).id();

        Query query = this.world.query()
                .with(Position.class)
                .queryFlags(Flecs.QueryMatchEmptyTables)
                .build();

        long found = query.find(Position.class, p -> p.x() == 20);
        assertEquals(e2, found);

        query.destroy();
    }

    @Test
    void sortBy() {
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(6, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(2, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(5, 0));
        this.world.obtainEntity(this.world.entity()).set(new Position(4, 0));

        Query query = this.world.query()
                .with(Position.class)
                .orderBy(Position.class, (ComparatorComponent<Position>) (a, b) -> Float.compare(a.x(), b.x()))
                .build();

        List<Float> actual = new ArrayList<>();
        query.iter(it -> {
            Field<Position> positions = it.field(Position.class, 0);
            for (int i = 0; i < it.count(); i++) {
                actual.add(positions.get(i).x());
            }
        });
        assertEquals(List.of(1.0f, 2.0f, 4.0f, 5.0f, 6.0f), actual);

        query.destroy();
    }

    @Test
    void signature() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        Query query = this.world.query("Position, [in] Velocity");
        AtomicInteger count = new AtomicInteger();
        query.each(Position.class, (entityId, p) -> count.incrementAndGet());
        assertEquals(1, count.get());

        assertNotNull(query.toStringExpr());

        query.destroy();
    }

    @Test
    void optionalTerm() {
        this.world.obtainEntity(this.world.entity()).set(new Position(10, 20)).set(new Velocity(1, 2));
        this.world.obtainEntity(this.world.entity()).set(new Position(30, 40));

        Query query = this.world.query()
                .with(Position.class)
                .with(Velocity.class).optional()
                .build();

        AtomicInteger withVelocity = new AtomicInteger();
        AtomicInteger withoutVelocity = new AtomicInteger();
        query.iter(it -> {
            boolean velocitySet = it.isFieldSet(1);
            Field<Position> positions = it.field(Position.class, 0);
            for (int i = 0; i < it.count(); i++) {
                if (velocitySet) {
                    withVelocity.incrementAndGet();
                } else {
                    withoutVelocity.incrementAndGet();
                }
            }
        });

        assertEquals(1, withVelocity.get());
        assertEquals(1, withoutVelocity.get());

        query.destroy();
    }

    @Test
    void without() {
        this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        this.world.obtainEntity(this.world.entity()).set(new Position(30, 40)).set(new Velocity(1, 2));

        Query query = this.world.query()
                .with(Position.class)
                .without(Velocity.class)
                .build();

        assertEquals(1, query.count());
        query.destroy();
    }

    @Test
    void firstWithTag() {
        long a = this.world.entity();
        long e1 = this.world.obtainEntity(this.world.entity()).add(a).id();
        this.world.obtainEntity(this.world.entity()).add(a);

        Query query = this.world.query().with(a).build();
        assertEquals(e1, query.first());
        assertEquals(2, query.count());
        query.destroy();
    }

    @Test
    void iterEntities() {
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        this.world.obtainEntity(this.world.entity()).set(new Position(3, 4));

        Query query = this.world.query().with(Position.class).build();

        List<Long> ids = new ArrayList<>();
        query.iter(it -> {
            Type type = it.type();
            assertTrue(type.count() >= 1);
            assertTrue(type.str().contains("Position"));
            for (int i = 0; i < it.count(); i++) {
                ids.add(it.entityId(i));
            }
        });
        assertEquals(2, ids.size());
        assertTrue(ids.stream().allMatch(id -> id != 0));

        query.destroy();
    }

    @Test
    void queryCreatedFromExpr() {
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));

        Query query = this.world.query().expr("Position").build();
        assertEquals(1, query.count());
        query.destroy();
    }
}
