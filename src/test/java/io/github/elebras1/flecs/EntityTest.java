package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Mass;
import io.github.elebras1.flecs.component.PositionView;
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

class EntityTest {

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
    void newEntity() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        assertNotNull(entity);
        assertTrue(entity.id() != 0);
    }

    @Test
    void newNamed() {
        Entity entity = this.world.obtainEntity(this.world.entity("Foo"));
        assertNotNull(entity);
        assertEquals("Foo", entity.name());
    }

    @Test
    void newAdd() {
        Entity entity = this.world.obtainEntity(this.world.entity()).add(Position.class);
        assertTrue(entity.id() != 0);
        assertTrue(entity.has(Position.class));
    }

    @Test
    void newAdd2() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .add(Position.class)
                .add(Velocity.class);
        assertTrue(entity.has(Position.class));
        assertTrue(entity.has(Velocity.class));
    }

    @Test
    void add() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        entity.add(Position.class);
        assertTrue(entity.has(Position.class));
    }

    @Test
    void add2() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .add(Position.class)
                .add(Velocity.class);
        assertTrue(entity.has(Position.class));
        assertTrue(entity.has(Velocity.class));
    }

    @Test
    void addEntityId() {
        long tag = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).add(tag);
        assertTrue(entity.has(tag));
    }

    @Test
    void addChildOf() {
        long parent = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).childOf(parent);
        assertTrue(entity.has(Flecs.ChildOf, parent));
        assertEquals(parent, entity.parent());
    }

    @Test
    void addInstanceOf() {
        long base = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).isA(base);
        assertTrue(entity.has(Flecs.IsA, base));
    }

    @Test
    void remove() {
        Entity entity = this.world.obtainEntity(this.world.entity()).add(Position.class);
        assertTrue(entity.has(Position.class));
        entity.remove(Position.class);
        assertFalse(entity.has(Position.class));
    }

    @Test
    void remove2() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .add(Position.class)
                .add(Velocity.class);
        assertTrue(entity.has(Position.class));
        assertTrue(entity.has(Velocity.class));
        entity.remove(Position.class).remove(Velocity.class);
        assertFalse(entity.has(Position.class));
        assertFalse(entity.has(Velocity.class));
    }

    @Test
    void removeEntityId() {
        long tag = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).add(tag);
        assertTrue(entity.has(tag));
        entity.remove(tag);
        assertFalse(entity.has(tag));
    }

    @Test
    void removeChildOf() {
        long parent = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).childOf(parent);
        assertTrue(entity.has(Flecs.ChildOf, parent));
        entity.remove(Flecs.ChildOf, parent);
        assertFalse(entity.has(Flecs.ChildOf, parent));
    }

    @Test
    void newSet() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        assertTrue(entity.has(Position.class));
        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void set() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        entity.set(new Position(10, 20));
        assertTrue(entity.has(Position.class));
        Position p = entity.get(Position.class);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void set2() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));
        assertTrue(entity.has(Position.class));
        assertTrue(entity.has(Velocity.class));
        Position p = entity.get(Position.class);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
        Velocity v = entity.get(Velocity.class);
        assertEquals(1.0f, v.x());
        assertEquals(2.0f, v.y());
    }

    @Test
    void getNotFound() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        assertNull(entity.get(Position.class));
    }

    @Test
    void setName() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        assertNull(entity.name());
        entity.name("Foo");
        assertEquals("Foo", entity.name());
    }

    @Test
    void changeName() {
        Entity entity = this.world.obtainEntity(this.world.entity("Foo"));
        entity.name("Bar");
        assertEquals("Bar", entity.name());
    }

    @Test
    void delete() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        assertTrue(entity.isAlive());
        entity.destruct();
        assertFalse(entity.isAlive());
        assertFalse(entity.isValid());
    }

    @Test
    void clear() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        assertTrue(entity.has(Position.class));
        entity.clear();
        assertFalse(entity.has(Position.class));
        assertTrue(entity.isAlive());
    }

    @Test
    void equals() {
        long id = this.world.entity();
        Entity a = this.world.obtainEntity(id);
        Entity b = this.world.obtainEntity(id);
        assertEquals(a, b);
    }

    @Test
    void isAlive() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        assertTrue(entity.isAlive());
        assertTrue(entity.isValid());
        assertFalse(this.world.obtainEntity(100000L).isAlive());
        assertFalse(this.world.obtainEntity(100000L).isValid());
    }

    @Test
    void getTarget() {
        long parent = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).childOf(parent);
        assertEquals(parent, entity.target(Flecs.ChildOf));
        assertEquals(parent, entity.parent());
    }

    @Test
    void getDepth() {
        Entity root = this.world.obtainEntity(this.world.entity());
        Entity mid = this.world.obtainEntity(this.world.entity()).childOf(root);
        Entity leaf = this.world.obtainEntity(this.world.entity()).childOf(mid);
        assertTrue(leaf.depth(Flecs.ChildOf) >= 2);
        assertTrue(mid.depth(Flecs.ChildOf) >= 1);
    }

    @Test
    void owns() {
        long compId = this.world.getComponentId(Position.class);
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        assertTrue(entity.owns(Position.class));
        assertTrue(entity.owns(compId));
        assertFalse(entity.owns(Velocity.class));
    }

    @Test
    void cloneWithValues() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        long cloneId = entity.clone(true);
        assertTrue(cloneId != 0);
        Entity clone = this.world.obtainEntity(cloneId);
        assertTrue(clone.has(Position.class));
        Position p = clone.get(Position.class);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void newNamedFromScope() {
        Entity parent = this.world.obtainEntity(this.world.entity("Foo"));
        long previousScope = this.world.setScope(parent.id());
        Entity child = this.world.obtainEntity(this.world.entity("Bar"));
        this.world.setScope(previousScope);

        assertEquals("Bar", child.name());
        assertTrue(child.has(Flecs.ChildOf, parent.id()));
        assertEquals(child.id(), this.world.lookup("Foo::Bar"));
    }

    @Test
    void scope() {
        Entity parent = this.world.obtainEntity(this.world.entity("parent"));
        try (ScopedWorld scope = parent.scope()) {
            Entity child = scope.obtainEntity(scope.entity("child"));
            assertNotNull(child);
            assertTrue(child.has(Flecs.ChildOf, parent.id()));
            assertEquals("child", child.name());
        }
    }

    @Test
    void children() {
        Entity parent = this.world.obtainEntity(this.world.entity());
        long c1 = this.world.obtainEntity(this.world.entity()).childOf(parent).id();
        long c2 = this.world.obtainEntity(this.world.entity()).childOf(parent).id();

        List<Long> children = new ArrayList<>();
        parent.children(children::add);
        assertEquals(2, children.size());
        assertTrue(children.contains(c1));
        assertTrue(children.contains(c2));
    }

    @Test
    void lookupChild() {
        Entity parent = this.world.obtainEntity(this.world.entity("parent"));
        try (ScopedWorld scope = parent.scope()) {
            Entity child = scope.obtainEntity(scope.entity("child"));
            long foo = scope.entity("foo");

            assertEquals(0, child.lookup("foo"));

            assertEquals(foo, child.lookup("foo", true));
        }
    }

    @Test
    void table() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        Table table = entity.table();
        assertNotNull(table);
        assertTrue(table.has(Position.class));
        assertEquals(1, table.count());
    }

    @Test
    void type() {
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        Type type = entity.type();
        assertEquals(2, type.count());
        assertEquals(type.count(), type.array().length);
        assertTrue(type.str().contains("Position"));
        assertTrue(type.str().contains("Velocity"));
        assertTrue(type.get(0).id() != 0);
    }

    @Test
    void eachType() {
        long tag1 = this.world.entity();
        long tag2 = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).add(tag1).add(tag2);

        List<Long> ids = new ArrayList<>();
        entity.each(ids::add);
        assertEquals(2, ids.size());
        assertTrue(ids.contains(tag1));
        assertTrue(ids.contains(tag2));
    }

    @Test
    void getFromReadonlyWorld() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));

        this.world.readonlyBegin();
        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        this.world.readonlyEnd();
    }

    @Test
    void deferAdd() {
        Entity entity = this.world.obtainEntity(this.world.entity());

        this.world.deferBegin();
        assertTrue(this.world.isDeferred());
        entity.add(Position.class);

        assertFalse(entity.has(Position.class));
        this.world.deferEnd();

        assertTrue(entity.has(Position.class));
    }

    @Test
    void deferSuspendResume() {
        Entity entity = this.world.obtainEntity(this.world.entity());

        this.world.deferBegin();
        this.world.deferSuspend();
        entity.add(Position.class);

        assertTrue(entity.has(Position.class));
        this.world.deferResume();
        this.world.deferEnd();
    }

    @Test
    void insert() {
        Entity entity = this.world.obtainEntity(this.world.entity());
        entity.insert(Position.class, (PositionView view) -> {
            view.x(10);
            view.y(20);
        });
        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(10.0f, p.x());
        assertEquals(20.0f, p.y());
    }

    @Test
    void setWithTarget() {
        long target = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Mass(10), target);
        assertTrue(entity.has(this.world.getComponentId(Mass.class), target));
        Mass m = entity.get(Mass.class, target);
        assertNotNull(m);
        assertEquals(10.0f, m.value());
    }

    @Test
    void hasWildcard() {
        long rel = this.world.entity();
        long obj = this.world.entity();
        Entity entity = this.world.obtainEntity(this.world.entity()).add(rel, obj);
        assertTrue(entity.has(rel, Flecs.Wildcard));
    }

    @Test
    void enableDisableComponent() {
        Entity entity = this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        assertTrue(entity.enabled(Position.class));
        entity.disable(Position.class);
        assertFalse(entity.enabled(Position.class));
        entity.enable(Position.class);
        assertTrue(entity.enabled(Position.class));
    }

    @Test
    void enableDisableEntity() {
        Entity entity = this.world.obtainEntity(this.world.entity()).add(Position.class);
        Query query = this.world.query().with(Position.class).build();

        AtomicInteger count = new AtomicInteger();
        query.each(entityId -> count.incrementAndGet());
        assertEquals(1, count.get());

        entity.disable();
        count.set(0);
        query.each(entityId -> count.incrementAndGet());
        assertEquals(0, count.get());

        entity.enable();
        count.set(0);
        query.each(entityId -> count.incrementAndGet());
        assertEquals(1, count.get());

        query.destroy();
    }
}
