package io.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

public class Field<T> {
    private MemorySegment memorySegment;
    private int count;
    private final long componentSize;
    private final Component<T> component;
    private final ComponentView componentView;
    private boolean shared;

    Field(MemorySegment memorySegment, int count, World world, Class<T> componentClass, boolean shared) {
        this.memorySegment = memorySegment;
        this.count = count;
        this.component = world.componentRegistry().getComponent(componentClass);
        this.componentSize = this.component.size();
        this.componentView = world.viewCache().getComponentView(componentClass);
        this.shared = shared;
    }

    long componentSize() {
        return this.componentSize;
    }

    void reset(MemorySegment memorySegment, int count, boolean shared) {
        this.memorySegment = memorySegment;
        this.count = count;
        this.shared = shared;
    }

    public int count() {
        return this.count;
    }

    public T get(int i) {
        assert this.memorySegment.address() != 0 : "Field is not set";
        assert i >= 0 && i < this.count : "Index " + i + " out of bounds";
        assert !this.shared || i == 0 : "Non-zero index invalid for shared field";

        long elementOffset = i * this.componentSize;
        return this.component.read(this.memorySegment, elementOffset);
    }

    @SuppressWarnings("unchecked")
    public <V extends ComponentView> V getMutView(int i) {
        assert this.memorySegment.address() != 0 : "Field is not set";
        assert i >= 0 && i < this.count : "Index " + i + " out of bounds";
        assert !this.shared || i == 0 : "Non-zero index invalid for shared field";

        long elementOffset = i * this.componentSize;
        this.componentView.setBaseAddress(this.memorySegment.address() + elementOffset);

        return (V) this.componentView;
    }

    public void set(int i, T componentData) {
        assert this.memorySegment.address() != 0 : "Field is not set";
        assert i >= 0 && i < this.count : "Index " + i + " out of bounds";
        assert !this.shared || i == 0 : "Non-zero index invalid for shared field";

        long elementOffset = i * this.componentSize;
        this.component.write(this.memorySegment, elementOffset, componentData);
    }

    public void reset() {
        this.memorySegment.reinterpret(this.count() * this.componentSize).fill((byte) 0);
    }
}
