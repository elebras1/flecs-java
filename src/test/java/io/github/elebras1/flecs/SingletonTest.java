package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Mass;
import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Tag;
import io.github.elebras1.flecs.component.Velocity;
import io.github.elebras1.flecs.util.Flecs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
        this.world.destroy();
    }

    @Test
    void setGetSingleton() {
        this.world.obtainEntity(Flecs.World).set(new Position(10, 20));

        Position p = this.world.obtainEntity(Flecs.World).get(Position.class);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void hasSingleton() {
        assertFalse(this.world.obtainEntity(Flecs.World).has(Position.class));

        this.world.obtainEntity(Flecs.World).set(new Position(10, 20));
        assertTrue(this.world.obtainEntity(Flecs.World).has(Position.class));
    }

    @Test
    void addRemoveSingleton() {
        Entity worldEntity = this.world.obtainEntity(Flecs.World);

        worldEntity.add(Position.class);
        assertTrue(worldEntity.has(Position.class));

        worldEntity.remove(Position.class);
        assertFalse(worldEntity.has(Position.class));
    }

    @Test
    void singletonQuery() {
        this.world.obtainEntity(Flecs.World).set(new Position(10, 20));
        this.world.obtainEntity(this.world.entity()).set(new Velocity(1, 2));
        this.world.obtainEntity(this.world.entity()).set(new Velocity(3, 4));

        Query query = this.world.query()
                .with(Velocity.class)
                .with(Position.class)
                .src(Flecs.World)
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
        query.destroy();
    }

    @Test
    void getTarget() {
        long relation = this.world.component(Tag.class);
        this.world.obtainEntity(relation).add(Flecs.Singleton);
        long obj1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();
        long obj2 = this.world.obtainEntity(this.world.entity()).add(Velocity.class).id();
        long obj3 = this.world.obtainEntity(this.world.entity()).add(Mass.class).id();

        Entity singleton = this.world.obtainEntity(Flecs.World);
        singleton.add(relation, obj1);
        singleton.add(relation, obj2);
        singleton.add(relation, obj3);

        assertEquals(obj1, singleton.target(relation, 0));
        assertEquals(obj2, singleton.target(relation, 1));
        assertEquals(obj3, singleton.target(relation, 2));
    }

    @Test
    void getWithId() {
        long positionId = this.world.getComponentId(Position.class);
        this.world.obtainEntity(Flecs.World).set(new Position(10, 20));

        Position p = this.world.obtainEntity(Flecs.World).get(positionId);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
    }
}
