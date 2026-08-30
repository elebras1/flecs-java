package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.PositionView;
import io.github.elebras1.flecs.component.Velocity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefTest {

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
    void creation() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        Ref<Position> ref = entity.getRef(Position.class);
        assertNotNull(ref);
        assertEquals(entity.id(), ref.entity());
        assertEquals(this.world.getComponentId(Position.class), ref.component());
        ref.destroy();
    }

    @Test
    void getReadsComponent() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        Ref<Position> ref = entity.getRef(Position.class);

        assertTrue(ref.has());
        Position p = ref.get();
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());

        ref.destroy();
    }

    @Test
    void tryGetReturnsNullWhenComponentAbsent() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        Ref<Position> ref = entity.getRef(Position.class);

        assertNull(ref.tryGet());
        assertFalse(ref.has());

        entity.set(new Position(5, 6));
        assertNotNull(ref.tryGet());
        assertEquals(5.0f, ref.get().x());

        ref.destroy();
    }

    @Test
    void creationWithTarget() {
        long target = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Velocity(1, 2), target);

        Ref<Velocity> ref = entity.getRef(Velocity.class, target);
        assertNotNull(ref);
        assertEquals(entity.id(), ref.entity());
        assertEquals(this.world.pair(this.world.getComponentId(Velocity.class), target).id(), ref.component());

        assertTrue(ref.has());
        Velocity v = ref.get();
        assertNotNull(v);
        assertEquals(1.0f, v.x());
        assertEquals(2.0f, v.y());
        ref.destroy();
    }

    @Test
    void creationWithEntityTarget() {
        Entity target = this.world.obtainEntity(this.world.entity());
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Velocity(1, 2), target.id());

        Ref<Velocity> ref = entity.getRef(Velocity.class, target);
        assertNotNull(ref);
        assertTrue(ref.has());
        assertEquals(1.0f, ref.get().x());
        ref.destroy();
    }

    @Test
    void reflectTargetUpdates() {
        Entity target = this.world.obtainEntity(this.world.entity());
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Velocity(1, 2), target.id());
        Ref<Velocity> ref = entity.getRef(Velocity.class, target);

        assertEquals(1.0f, ref.get().x());

        entity.set(new Velocity(7, 8), target.id());
        assertEquals(7.0f, ref.get().x());
        assertEquals(8.0f, ref.get().y());

        ref.destroy();
    }

    @Test
    void getMutViewOnPair() {
        long target = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20), target);
        Ref<Position> ref = entity.getRef(Position.class, target);

        // getMutView returns a live view into the paired (Position, target) data.
        PositionView view = ref.getMutView();
        assertNotNull(view);
        assertEquals(10.0f, view.x());
        assertEquals(20.0f, view.y());

        // Mutations through the view are visible through the ref.
        view.x(50);
        view.y(60);
        Position p = ref.get();
        assertNotNull(p);
        assertEquals(50.0f, p.x());
        assertEquals(60.0f, p.y());

        ref.destroy();
    }

    @Test
    void getMutViewOnPairReturnsNullWhenAbsent() {
        long target = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity());
        Ref<Position> ref = entity.getRef(Position.class, target);

        assertNull(ref.getMutView());
        ref.destroy();
    }

    @Test
    void reflectsEntityUpdates() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        Ref<Position> ref = entity.getRef(Position.class);
        assertEquals(10.0f, ref.get().x());

        entity.set(new Position(30, 40));
        assertEquals(30.0f, ref.get().x());
        assertEquals(40.0f, ref.get().y());

        ref.destroy();
    }
}