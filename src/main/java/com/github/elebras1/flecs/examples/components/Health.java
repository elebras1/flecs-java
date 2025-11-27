package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.EcsComponent;
import com.github.elebras1.flecs.util.LayoutField;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public record Health(int value) implements EcsComponent<Health> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            LayoutField.intLayout().withName("value")
    ).withName("Health");

    private static final long OFFSET_VALUE = LayoutField.offsetOf(LAYOUT, "value");

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, Health data) {
        LayoutField.set(segment, OFFSET_VALUE, data.value);
    }

    @Override
    public Health read(MemorySegment segment) {
        int value = LayoutField.getInt(segment, OFFSET_VALUE);
        return new Health(value);
    }

    public static Health component() {
        return new Health(0);
    }
}

