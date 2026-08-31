package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Health;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitsMetricsTest {

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
    void registerCustomUnit() {
        Entity unit = this.world.unit("MyMeters")
                .symbol("m")
                .buildEntity();
        assertTrue(unit.id() != 0);
        assertEquals("MyMeters", unit.name());
    }

    @Test
    void importUnitsModule() {
        this.world.units();
        long duration = this.world.lookup("::flecs::units::Duration", "::", "::", true);
        assertTrue(duration != 0, "expected Duration quantity to exist after units() import");
    }

    @Test
    void createCounterMetric() {
        this.world.metrics();
        long healthId = this.world.component(Health.class);
        this.world.obtainEntity(this.world.entity()).set(new Health(100));

        Entity metric = this.world.metric("health_metric")
                .id(healthId)
                .kind(Flecs.Counter)
                .brief("Health counter")
                .targets(false)
                .buildEntity();

        assertTrue(metric.id() != 0);
        assertEquals("health_metric", metric.name());
    }
}