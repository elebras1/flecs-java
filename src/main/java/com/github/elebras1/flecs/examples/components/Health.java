package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.Component;
import com.github.elebras1.flecs.utils.LayoutField;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class Health implements Component<Health.Data> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            LayoutField.intLayout().withName("value")
    ).withName("Health");

    private static final long OFFSET_VALUE = LayoutField.offsetOf(LAYOUT, "value");

    public record Data(int value) {}

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, Data data) {
        LayoutField.set(segment, OFFSET_VALUE, data.value);
    }

    @Override
    public Data read(MemorySegment segment) {
        int value = LayoutField.getInt(segment, OFFSET_VALUE);
        return new Data(value);
    }
}

