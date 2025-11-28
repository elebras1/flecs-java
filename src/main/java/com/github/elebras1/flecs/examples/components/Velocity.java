package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.FlecsComponent;
import com.github.elebras1.flecs.util.LayoutField;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public record Velocity(float dx, float dy) implements FlecsComponent<Velocity> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            LayoutField.floatLayout().withName("dx"),
            LayoutField.floatLayout().withName("dy")
    ).withName("Velocity");

    private static final long OFFSET_DX = LayoutField.offsetOf(LAYOUT, "dx");
    private static final long OFFSET_DY = LayoutField.offsetOf(LAYOUT, "dy");

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, Velocity data) {
        LayoutField.set(segment, OFFSET_DX, data.dx);
        LayoutField.set(segment, OFFSET_DY, data.dy);
    }

    @Override
    public Velocity read(MemorySegment segment) {
        float dx = LayoutField.getFloat(segment, OFFSET_DX);
        float dy = LayoutField.getFloat(segment, OFFSET_DY);

        return new Velocity(dx, dy);
    }

    public static Velocity component() {
        return new Velocity(0.0f, 0.0f);
    }
}

