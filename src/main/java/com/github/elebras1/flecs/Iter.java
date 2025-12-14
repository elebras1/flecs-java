package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Iter {
    
    private MemorySegment nativeIter;
    private final World world;
    private int count;

    Iter(MemorySegment nativeIter, World world) {
        this.nativeIter = nativeIter;
        this.world = world;
        this.count = -1;
    }

    void setNativeIter(MemorySegment nativeIter) {
        this.nativeIter = nativeIter;
        this.count = -1;
    }

    public boolean next() {
        this.count = -1;
        return flecs_h.ecs_iter_next(this.nativeIter);
    }

    public int count() {
        if (this.count < 0) {
            this.count = ecs_iter_t.count(this.nativeIter);
        }
        return this.count;
    }

    public long entityId(int index) {
        if (index < 0 || index >= this.count()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for count " + this.count());
        }
        MemorySegment entities = ecs_iter_t.entities(this.nativeIter);
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

    public <T> Field<T> field(Class<T> componentClass, int index) {
        if (index < 0 || index > 127) {
            throw new IndexOutOfBoundsException("The field index must be between 0 and 127.");
        }

        byte flecsIndex = (byte) index;
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnPtr = flecs_h.ecs_field_w_size(this.nativeIter, component.size(), flecsIndex);

        if (columnPtr == null || columnPtr.address() == 0) {
            return new Field<>(null, this.count(), this.world, componentClass);
        }

        return new Field<>(columnPtr, this.count(), this.world, componentClass);
    }

    public boolean isFieldSet(int index) {
        if (index < 0 || index > 31) {
            throw new IndexOutOfBoundsException("The field index must be between 0 and 31.");
        }

        int setFields = ecs_iter_t.set_fields(this.nativeIter);
        return (setFields & (1 << index)) != 0;
    }

    public long termId(int index) {
        if (index < 0 || index > 127) {
            throw new IndexOutOfBoundsException("The term index must be between 0 and 127.");
        }

        MemorySegment ids = ecs_iter_t.ids(this.nativeIter);
        if (ids == null || ids.address() == 0) {
            return 0;
        }

        return ids.getAtIndex(ValueLayout.JAVA_LONG, index);
    }

    public int fieldSize(int index) {
        if (index < 0 || index > 127) {
            throw new IndexOutOfBoundsException("The field index must be between 0 and 127.");
        }

        MemorySegment sizes = ecs_iter_t.sizes(this.nativeIter);
        if (sizes == null || sizes.address() == 0) {
            return 0;
        }

        return sizes.getAtIndex(ValueLayout.JAVA_INT, index);
    }

    public int fieldCount() {
        return Byte.toUnsignedInt(ecs_iter_t.field_count(this.nativeIter));
    }

    private <T> MemorySegment fieldPtr(Class<T> componentClass, int index, String fieldName, int i, long[] outOffset) {
        if (index < 0 || index > 127) {
            throw new IndexOutOfBoundsException("The field index must be between 0 and 127.");
        }
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnPtr = flecs_h.ecs_field_w_size(this.nativeIter, component.size(), (byte) index);
        outOffset[0] = i * component.size() + component.offsetOf(fieldName);
        return columnPtr;
    }

    public <T> int fieldInt(Class<T> componentClass, int index, String fieldName, int i) {
        long[] offset = new long[1];
        return this.fieldPtr(componentClass, index, fieldName, i, offset).get(ValueLayout.JAVA_INT, offset[0]);
    }

    public <T> float fieldFloat(Class<T> componentClass, int index, String fieldName, int i) {
        long[] offset = new long[1];
        return this.fieldPtr(componentClass, index, fieldName, i, offset).get(ValueLayout.JAVA_FLOAT, offset[0]);
    }

    public <T> double fieldDouble(Class<T> componentClass, int index, String fieldName, int i) {
        long[] offset = new long[1];
        return this.fieldPtr(componentClass, index, fieldName, i, offset).get(ValueLayout.JAVA_DOUBLE, offset[0]);
    }

    public <T> long fieldLong(Class<T> componentClass, int index, String fieldName, int i) {
        long[] offset = new long[1];
        return this.fieldPtr(componentClass, index, fieldName, i, offset).get(ValueLayout.JAVA_LONG, offset[0]);
    }

    public <T> short fieldShort(Class<T> componentClass, int index, String fieldName, int i) {
        long[] offset = new long[1];
        return this.fieldPtr(componentClass, index, fieldName, i, offset).get(ValueLayout.JAVA_SHORT, offset[0]);
    }

    public <T> boolean fieldBoolean(Class<T> componentClass, int index, String fieldName, int i) {
        long[] offset = new long[1];
        return this.fieldPtr(componentClass, index, fieldName, i, offset).get(ValueLayout.JAVA_BOOLEAN, offset[0]);
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

