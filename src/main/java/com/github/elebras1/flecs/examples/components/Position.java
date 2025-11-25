package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.Component;
import com.github.elebras1.flecs.utils.LayoutField;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class Position implements Component<Position.Data> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            LayoutField.floatLayout().withName("x"),
            LayoutField.floatLayout().withName("y")
    ).withName("Position");

    private static final long OFFSET_X = LayoutField.offsetOf(LAYOUT, "x");
    private static final long OFFSET_Y = LayoutField.offsetOf(LAYOUT, "y");

    public record Data(float x, float y) {

        @Override
        public String toString() {
            return String.format("Position(x=%.2f, y=%.2f)", this.x, this.y);
        }
    }

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, Data data) {
        LayoutField.set(segment, OFFSET_X, data.x);
        LayoutField.set(segment, OFFSET_Y, data.y);
    }

    @Override
    public Data read(MemorySegment segment) {
        float x = LayoutField.getFloat(segment, OFFSET_X);
        float y = LayoutField.getFloat(segment, OFFSET_Y);
        return new Data(x, y);
    }
}

