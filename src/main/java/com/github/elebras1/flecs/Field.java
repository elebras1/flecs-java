package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

public class Field<T> {
    private final MemorySegment memorySegment;
    private final int count;
    private final Component<T> component;
    private final ComponentView componentView;

    Field(MemorySegment memorySegment, int count, World world, Class<T> componentClass) {
        this.memorySegment = memorySegment;
        this.count = count;
        this.component = world.componentRegistry().getComponent(componentClass);
        this.componentView = world.viewCache().getComponentView(componentClass);
    }

    public int count() {
        return this.count;
    }

    public T get(int i) {
        if (i < 0 || i >= this.count) {
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for count " + this.count);
        }

        long elementOffset = i * this.component.size();
        return this.component.read(this.memorySegment, elementOffset);
    }

    @SuppressWarnings("unchecked")
    public <V extends ComponentView> V getMutView(int i) {
        if (i < 0 || i >= this.count) {
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for count " + this.count);
        }

        long elementOffset = i * this.component.size();
        this.componentView.setResource(this.memorySegment.address(), elementOffset);

        return (V) this.componentView;
    }

    public void set(int i, T componentData) {
        if (i < 0 || i >= this.count) {
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for count " + this.count);
        }

        long elementOffset = i * this.component.size();
        this.component.write(this.memorySegment, elementOffset, componentData);
    }
}
