package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertTest {

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
    void createAlert() {
        this.world.alerts();
        this.world.component(Position.class);

        Entity alert = this.world.alert("low_health_alert")
                .with(Position.class)
                .message("Entity has Position")
                .severity(Flecs.AlertError)
                .brief("A test alert")
                .build();

        assertTrue(alert.id() != 0);
        assertEquals("low_health_alert", alert.name());
    }

    @Test
    void alertWithoutComponent() {
        this.world.alerts();
        this.world.component(Position.class);

        Entity alert = this.world.alert("no_position_alert")
                .without(Position.class)
                .message("Entity has no Position")
                .build();

        assertTrue(alert.id() != 0);
    }
}