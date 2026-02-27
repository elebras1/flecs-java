package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

public class Field<T> {
    private MemorySegment memorySegment;
    private int count;
    private final long componentSize;
    private final Component<T> component;
    private final ComponentView componentView;

    Field(MemorySegment memorySegment, int count, World world, Class<T> componentClass) {
        this.memorySegment = memorySegment;
        this.count = count;
        this.component = world.componentRegistry().getComponent(componentClass);
        this.componentSize = this.component.size();
        this.componentView = world.viewCache().getComponentView(componentClass);
    }

    public void reset(MemorySegment memorySegment, int count) {
        this.memorySegment = memorySegment;
        this.count = count;
    }

    public int count() {
        return this.count;
    }

    public T get(int i) {
        assert i >= 0 && i < this.count : "Index " + i + " out of bounds";

        long elementOffset = i * this.componentSize;
        return this.component.read(this.memorySegment, elementOffset);
    }

    @SuppressWarnings("unchecked")
    public <V extends ComponentView> V getMutView(int i) {
        assert i >= 0 && i < this.count : "Index " + i + " out of bounds";

        long elementOffset = i * this.componentSize;
        this.componentView.setBaseAddress(this.memorySegment.address() + elementOffset);

        return (V) this.componentView;
    }

    public void set(int i, T componentData) {
        assert i >= 0 && i < this.count : "Index " + i + " out of bounds";

        long elementOffset = i * this.componentSize;
        this.component.write(this.memorySegment, elementOffset, componentData);
    }
}
