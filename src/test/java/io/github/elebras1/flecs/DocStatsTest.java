package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocStatsTest {

    private World world;

    @BeforeEach
    void init() {
        this.world = new World();
    }

    @AfterEach
    void tearDown() {
        this.world.destroy();
    }

    @Test
    void docSettersAndGetters() {
        Entity e = this.world.obtainEntity(this.world.entity("MyEntity"));

        e.setDocName("Nice Name");
        e.setDocBrief("A brief description");
        e.setDocDetail("A longer detail");
        e.setDocLink("https://example.com");
        e.setDocColor("#ff0000");
        e.setDocUuid("abc-123");

        assertEquals("Nice Name", e.docName());
        assertEquals("A brief description", e.docBrief());
        assertEquals("A longer detail", e.docDetail());
        assertEquals("https://example.com", e.docLink());
        assertEquals("#ff0000", e.docColor());
        assertEquals("abc-123", e.docUuid());
    }

    @Test
    void docUnsetReturnsNull() {
        Entity e = this.world.obtainEntity(this.world.entity());
        assertNull(e.docName());
        assertNull(e.docBrief());
    }

    @Test
    void worldStatsSnapshot() {
        // Create some entities and components so metrics are non-trivial.
        this.world.component(Position.class);
        for (int i = 0; i < 5; i++) {
            this.world.obtainEntity(this.world.entity()).set(new Position(i, i));
        }

        WorldStats stats = this.world.stats();
        assertNotNull(stats);
        assertTrue(stats.entityCount() > 0);
        assertTrue(stats.tableCount() >= 1);
    }
}