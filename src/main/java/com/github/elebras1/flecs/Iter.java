package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Iter {

    private MemorySegment nativeIter;
    private final World world;
    private final Field<?>[] fields;
    private int count;

    Iter(MemorySegment nativeIter, World world) {
        this.nativeIter = nativeIter;
        this.world = world;
        this.fields = new Field[32];
        this.count = -1;
    }

    void setNativeIter(MemorySegment nativeIter) {
        this.nativeIter = nativeIter;
        this.count = ecs_iter_t.count(nativeIter);
    }

    World world() {
        return this.world;
    }

    public boolean next() {
        boolean hasNext = flecs_h.ecs_iter_next(this.nativeIter);
        this.count = hasNext ? ecs_iter_t.count(this.nativeIter) : 0;
        return hasNext;
    }

    public int count() {
        return this.count;
    }

    public long entityId(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";

        MemorySegment entities = ecs_iter_t.entities(this.nativeIter);
        if (entities == null || entities.address() == 0) {
            throw new IllegalStateException("Entities array is null");
        }
        return entities.getAtIndex(ValueLayout.JAVA_LONG, index);
    }

    public long entity(int index) {
        return this.entityId(index);
    }

    public float deltaTime() {
        return ecs_iter_t.delta_time(this.nativeIter);
    }

    public float deltaSystemTime() {
        return ecs_iter_t.delta_system_time(this.nativeIter);
    }

    @SuppressWarnings("unchecked")
    public <T> Field<T> field(Class<T> componentClass, int index) {
        assert (index >= 0 && index < 32) : "The field index must be between 0 and 31.";

        Field<T> field = (Field<T>) this.fields[index];

        if (field == null) {
            Component<T> component = this.world.componentRegistry().getComponent(componentClass);
            MemorySegment columnPtr = flecs_h.ecs_field_w_size(this.nativeIter, component.size(), (byte) index);
            field = new Field<>(columnPtr, this.count(), this.world, componentClass);
            this.fields[index] = field;
        } else {
            MemorySegment columnPtr = flecs_h.ecs_field_w_size(this.nativeIter, field.componentSize(), (byte) index);
            field.reset(columnPtr, this.count());
        }

        return field;
    }

    public boolean isFieldSet(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";

        int setFields = ecs_iter_t.set_fields(this.nativeIter);
        return (setFields & (1 << index)) != 0;
    }

    public long termId(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";

        MemorySegment ids = ecs_iter_t.ids(this.nativeIter);
        if (ids == null || ids.address() == 0) {
            return 0;
        }

        return ids.getAtIndex(ValueLayout.JAVA_LONG, index);
    }

    public int fieldSize(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";

        MemorySegment sizes = ecs_iter_t.sizes(this.nativeIter);
        if (sizes == null || sizes.address() == 0) {
            return 0;
        }

        return sizes.getAtIndex(ValueLayout.JAVA_INT, index);
    }

    public int fieldCount() {
        return Byte.toUnsignedInt(ecs_iter_t.field_count(this.nativeIter));
    }

    public long event() {
        return ecs_iter_t.event(this.nativeIter);
    }

    public Table table() {
        MemorySegment tablePtr = ecs_iter_t.table(this.nativeIter);
        if (tablePtr == null || tablePtr.address() == 0) {
            return null;
        }
        return new Table(this.world, tablePtr);
    }
}