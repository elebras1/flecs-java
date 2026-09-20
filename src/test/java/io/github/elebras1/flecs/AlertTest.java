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

    @Test
    void alertWithPairTerm() {
        this.world.alerts();
        this.world.component(Position.class);
        long target = this.world.entity("AlertTarget");

        Entity alert = this.world.alert("pair_alert")
                .with(Position.class)
                .with(Position.class, target)
                .with(target, target)
                .termAt(1).self()
                .message("Entity has Position")
                .build();

        assertTrue(alert.id() != 0);
        assertEquals("pair_alert", alert.name());
    }

    @Test
    void alertWithSeverityType() {
        this.world.alerts();
        this.world.component(Position.class);

        Entity alert = this.world.alert("severity_alert")
                .with(Position.class)
                .severity(Position.class)
                .message("Entity has Position")
                .build();

        assertNotEquals(0, alert.id());
    }

    @Test
    void alertMemberLookup() {
        this.world.alerts();
        this.world.component(Position.class);

        AlertBuilder bogus = this.world.alert("member_bogus").with(Position.class);
        assertThrows(IllegalArgumentException.class, () -> bogus.member(Position.class, "does_not_exist"));

        AlertBuilder builder = this.world.alert("member_alert")
                .with(Position.class)
                .severity(Flecs.AlertError)
                .member(Position.class, "x", "$this");
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void alertWithQueryFlags() {
        this.world.alerts();
        this.world.component(Position.class);

        Entity alert = this.world.alert("flagged_alert")
                .with(Position.class)
                .queryFlags(Flecs.QueryDetectChanges)
                .queryFlags(Flecs.QueryMatchEmptyTables)
                .cached()
                .message("Entity has Position")
                .build();

        assertTrue(alert.id() != 0);
    }
}