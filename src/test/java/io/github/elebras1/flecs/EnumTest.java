package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

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
    void registerAndToEntity() {
        long enumId = this.world.enumeration(Color.class);
        assertTrue(enumId != 0);

        Entity green = this.world.toEntity(Color.class, Color.GREEN);
        assertTrue(green.id() != 0);
    }

    @Test
    void constantToValueRoundTrip() {
        this.world.enumeration(Color.class);

        Entity blue = this.world.toEntity(Color.class, Color.BLUE);
        assertEquals(Color.BLUE, blue.toConstant(Color.class));
        assertEquals(Color.GREEN, this.world.toEntity(Color.class, Color.GREEN).toConstant(Color.class));
    }

    @Test
    void entityAddEnumConstant() {
        this.world.enumeration(Color.class);
        long redId = this.world.toEntity(Color.class, Color.RED).id();

        Entity e = this.world.obtainEntity(this.world.entity()).add(redId);
        assertTrue(e.has(redId));
    }
}