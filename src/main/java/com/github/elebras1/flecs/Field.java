package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

public class Field<T extends FlecsComponent<T>> {
    private final MemorySegment memorySegment;
    private final int count;
    private final Flecs world;
    private final Class<T> componentClass;

    Field(MemorySegment memorySegment, int count, Flecs world, Class<T> componentClass) {
        this.memorySegment = memorySegment;
        this.count = count;
        this.world = world;
        this.componentClass = componentClass;
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

        Component<T> component = this.world.componentRegistry().getComponent(this.componentClass);

        long elementOffset = i * component.size();
        MemorySegment elementSegment = this.memorySegment.asSlice(elementOffset, component.size());

        return component.read(elementSegment);
    }
}
