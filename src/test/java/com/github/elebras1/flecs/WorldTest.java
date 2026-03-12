package com.github.elebras1.flecs;

import com.github.elebras1.flecs.component.Health;
import com.github.elebras1.flecs.component.Ideology;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    private World world;

    @BeforeEach
    void init() {
        this.world = new World();
        this.world.component(Health.class);
        this.world.component(Ideology.class);
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
        assertEquals("test", entity.getName());
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
        for(long entityId : entityIds) {
            EntityView entityView = this.world.obtainEntityView(entityId);
            assertTrue(entityView.has(Health.class));
            assertTrue(entityView.has(Ideology.class));
        }
    }

    @Test
    void makeAliveTest() {
        long entityId = 1000;
        long entityIdAlive = this.world.makeAlive(entityId);
        assertEquals(entityId, entityIdAlive);
        Entity entity = this.world.obtainEntity(entityId);
        assertTrue(entity.isAlive());
    }

    @Test
    void setVersionTest() {
        long entityId = this.world.makeAlive(500);

        this.world.setVersion(entityId);

        assertTrue(this.world.obtainEntityView(entityId).isAlive());
    }

    @Test
    void getVersionTest() {
        long entityId = this.world.makeAlive(500);
        int version = this.world.getVersion(entityId);
        assertTrue(version >= 0);
    }

    @AfterEach
    void tearDown() {
        this.world.close();
    }
}