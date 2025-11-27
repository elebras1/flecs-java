package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.EcsComponent;
import com.github.elebras1.flecs.util.LayoutField;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public record Position(float x, float y) implements EcsComponent<Position> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            LayoutField.floatLayout().withName("x"),
            LayoutField.floatLayout().withName("y")
    ).withName("Position");

    private static final long OFFSET_X = LayoutField.offsetOf(LAYOUT, "x");
    private static final long OFFSET_Y = LayoutField.offsetOf(LAYOUT, "y");

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, Position data) {
        LayoutField.set(segment, OFFSET_X, data.x);
        LayoutField.set(segment, OFFSET_Y, data.y);
    }

    @Override
    public Position read(MemorySegment segment) {
        float x = LayoutField.getFloat(segment, OFFSET_X);
        float y = LayoutField.getFloat(segment, OFFSET_Y);
        return new Position(x, y);
    }

    public static Position component() {
        return new Position(0.0f, 0.0f);
    }
}

