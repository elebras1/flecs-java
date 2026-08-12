package io.github.elebras1.flecs;

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

class OrderedChildrenTest {

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
    void iterNoChildren() {
        Entity parent = this.world.obtainEntity(this.world.entity()).add(Flecs.OrderedChildren);

        AtomicInteger count = new AtomicInteger();
        parent.children(entityId -> count.incrementAndGet());
        assertEquals(0, count.get());
    }

    @Test
    void children1Table() {
        Entity parent = this.world.obtainEntity(this.world.entity()).add(Flecs.OrderedChildren);
        long childA = this.world.obtainEntity(this.world.entity()).childOf(parent).add(Position.class).id();
        long childB = this.world.obtainEntity(this.world.entity()).childOf(parent).add(Position.class).id();
        long childC = this.world.obtainEntity(this.world.entity()).childOf(parent).add(Position.class).id();

        List<Long> children = new ArrayList<>();
        parent.children(children::add);

        assertEquals(childA, children.get(0));
        assertEquals(childB, children.get(1));
        assertEquals(childC, children.get(2));
    }

    @Test
    void children2Tables() {
        Entity parent = this.world.obtainEntity(this.world.entity()).add(Flecs.OrderedChildren);
        long childA = this.world.obtainEntity(this.world.entity()).childOf(parent).add(Position.class).id();
        long childB = this.world.obtainEntity(this.world.entity()).childOf(parent).add(Velocity.class).id();
        long childC = this.world.obtainEntity(this.world.entity()).childOf(parent).add(Position.class).id();

        List<Long> children = new ArrayList<>();
        parent.children(children::add);

        assertEquals(childA, children.get(0));
        assertEquals(childB, children.get(1));
        assertEquals(childC, children.get(2));
    }

    @Test
    void setChildOrder() {
        Entity parent = this.world.obtainEntity(this.world.entity()).add(Flecs.OrderedChildren);
        Entity childA = this.world.obtainEntity(this.world.entity()).childOf(parent).add(Position.class);
        Entity childB = this.world.obtainEntity(this.world.entity()).childOf(parent).add(Position.class);
        Entity childC = this.world.obtainEntity(this.world.entity()).childOf(parent).add(Position.class);

        List<Long> children = new ArrayList<>();
        parent.children(children::add);
        assertEquals(childA.id(), children.get(0));
        assertEquals(childB.id(), children.get(1));
        assertEquals(childC.id(), children.get(2));

        parent.setChildOrder(childC, childA, childB);

        children.clear();
        parent.children(children::add);
        assertEquals(childC.id(), children.get(0));
        assertEquals(childA.id(), children.get(1));
        assertEquals(childB.id(), children.get(2));
    }
}
