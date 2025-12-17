package com.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class Field<T> {
    private final MemorySegment memorySegment;
    private final int count;
    private final Component<T> component;

    Field(MemorySegment memorySegment, int count, World world, Class<T> componentClass) {
        this.memorySegment = memorySegment;
        this.count = count;
        this.component = world.componentRegistry().getComponent(componentClass);
    }

    public boolean isSet() {
        return this.memorySegment != null && this.memorySegment.address() != 0;
    }

    public int count() {
        return this.count;
    }

    public T get(int i) {
        if (!this.isSet()) {
            throw new IllegalStateException("Field is not set.");
        }
        if (i < 0 || i >= this.count) {
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for count " + this.count);
        }

        long elementOffset = i * this.component.size();
        MemorySegment elementSegment = this.memorySegment.asSlice(elementOffset, this.component.size());

        return this.component.read(elementSegment);
    }

    public void set(int i, T componentData) {
        if (!this.isSet()) {
            throw new IllegalStateException("Field is not set.");
        }
        if (i < 0 || i >= this.count) {
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for count " + this.count);
        }

        try (Arena tempArena = Arena.ofConfined()) {
            long elementOffset = i * this.component.size();
            MemorySegment elementSegment = this.memorySegment.asSlice(elementOffset, this.component.size());

            this.component.write(elementSegment, componentData, tempArena);
        }
    }
}
