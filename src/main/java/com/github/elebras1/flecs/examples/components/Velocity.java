package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.Component;
import com.github.elebras1.flecs.utils.LayoutField;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class Velocity implements Component<Velocity.Data> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            LayoutField.floatLayout().withName("dx"),
            LayoutField.floatLayout().withName("dy")
    ).withName("Velocity");

    private static final long OFFSET_DX = LayoutField.offsetOf(LAYOUT, "dx");
    private static final long OFFSET_DY = LayoutField.offsetOf(LAYOUT, "dy");

    public record Data(float dx, float dy) {

        @Override
        public String toString() {
            return String.format("Velocity(dx=%.2f, dy=%.2f)", this.dx, this.dy);
        }
    }

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, Data data) {
        LayoutField.set(segment, OFFSET_DX, data.dx);
        LayoutField.set(segment, OFFSET_DY, data.dy);
    }

    @Override
    public Data read(MemorySegment segment) {
        float dx = LayoutField.getFloat(segment, OFFSET_DX);
        float dy = LayoutField.getFloat(segment, OFFSET_DY);

        return new Data(dx, dy);
    }
}

