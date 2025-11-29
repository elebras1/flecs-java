package com.github.elebras1.flecs.examples.components;

import com.github.elebras1.flecs.FlecsComponent;
import com.github.elebras1.flecs.util.LayoutField;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public record Label(String label) implements FlecsComponent<Label> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            LayoutField.stringLayout().withName("label")
    ).withName("Label");

    private static final long OFFSET_VALUE = LayoutField.offsetOf(LAYOUT, "label");

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, Label data) {
        LayoutField.set(segment, OFFSET_VALUE, data.label);
    }

    @Override
    public Label read(MemorySegment segment) {
        String label = LayoutField.getString(segment, OFFSET_VALUE);
        return new Label(label);
    }

    public static Label component() {
        return new Label("");
    }
}
