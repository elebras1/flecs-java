package com.github.elebras1.flecs;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;

public class Iter {

    private MemorySegment nativeIter;
    private final World world;
    private int count;
    private final MemorySegment[] cachedColumns;

    Iter(MemorySegment nativeIter, World world) {
        this.nativeIter = nativeIter;
        this.world = world;
        this.count = -1;
        this.cachedColumns = new MemorySegment[32];
    }

    void setNativeIter(MemorySegment nativeIter) {
        this.nativeIter = nativeIter;
        this.count = -1;
        Arrays.fill(cachedColumns, null);
    }

    public boolean next() {
        this.count = -1;
        Arrays.fill(cachedColumns, null);
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

    private <T> long fieldPtr(Class<T> componentClass, int index, String fieldName, int i) {
        if (index < 0 || index > 127) {
            throw new IndexOutOfBoundsException("The field index must be between 0 and 127.");
        }
        if (i < 0 || i >= this.count()) {
            throw new IndexOutOfBoundsException("Entity index " + i + " out of bounds for count " + this.count());
        }

        Component<T> component = this.world.componentRegistry().getComponent(componentClass);

        MemorySegment columnPtr = this.cachedColumns[index];
        if (columnPtr == null) {
            columnPtr = flecs_h.ecs_field_w_size(this.nativeIter, component.size(), (byte) index);
            if (columnPtr == null || columnPtr.address() == 0) {
                throw new IllegalStateException("Field " + index + " is not available");
            }
            this.cachedColumns[index] = columnPtr;
        }

        return i * component.size() + component.offsetOf(fieldName);
    }

    public <T> int fieldInt(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        return this.cachedColumns[index].get(ValueLayout.JAVA_INT, offset);
    }

    public <T> float fieldFloat(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        return this.cachedColumns[index].get(ValueLayout.JAVA_FLOAT, offset);
    }

    public <T> double fieldDouble(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        return this.cachedColumns[index].get(ValueLayout.JAVA_DOUBLE, offset);
    }

    public <T> long fieldLong(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        return this.cachedColumns[index].get(ValueLayout.JAVA_LONG, offset);
    }

    public <T> short fieldShort(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        return this.cachedColumns[index].get(ValueLayout.JAVA_SHORT, offset);
    }

    public <T> boolean fieldBoolean(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        return this.cachedColumns[index].get(ValueLayout.JAVA_BOOLEAN, offset);
    }

    public <T> byte fieldByte(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        return this.cachedColumns[index].get(ValueLayout.JAVA_BYTE, offset);
    }

    public <T> String fieldString(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long capacity = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        return this.cachedColumns[index].asSlice(offset, capacity).getString(0);
    }

    public <T> int[] fieldIntArray(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        return this.cachedColumns[index].asSlice(offset, arrayByteSize).toArray(ValueLayout.JAVA_INT);
    }

    public <T> long[] fieldLongArray(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        return this.cachedColumns[index].asSlice(offset, arrayByteSize).toArray(ValueLayout.JAVA_LONG);
    }

    public <T> float[] fieldFloatArray(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        return this.cachedColumns[index].asSlice(offset, arrayByteSize).toArray(ValueLayout.JAVA_FLOAT);
    }

    public <T> double[] fieldDoubleArray(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        return this.cachedColumns[index].asSlice(offset, arrayByteSize).toArray(ValueLayout.JAVA_DOUBLE);
    }

    public <T> byte[] fieldByteArray(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        return this.cachedColumns[index].asSlice(offset, arrayByteSize).toArray(ValueLayout.JAVA_BYTE);
    }

    public <T> short[] fieldShortArray(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        return this.cachedColumns[index].asSlice(offset, arrayByteSize).toArray(ValueLayout.JAVA_SHORT);
    }

    public <T> boolean[] fieldBooleanArray(Class<T> componentClass, int index, String fieldName, int i) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();

        byte[] bytes = this.cachedColumns[index].asSlice(offset, arrayByteSize).toArray(ValueLayout.JAVA_BYTE);
        boolean[] result = new boolean[bytes.length];
        for (int j = 0; j < bytes.length; j++) {
            result[j] = bytes[j] != 0;
        }
        return result;
    }

    public <T> void setFieldInt(Class<T> componentClass, int index, String fieldName, int i, int value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        this.cachedColumns[index].set(ValueLayout.JAVA_INT, offset, value);
    }

    public <T> void setFieldFloat(Class<T> componentClass, int index, String fieldName, int i, float value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        this.cachedColumns[index].set(ValueLayout.JAVA_FLOAT, offset, value);
    }

    public <T> void setFieldDouble(Class<T> componentClass, int index, String fieldName, int i, double value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        this.cachedColumns[index].set(ValueLayout.JAVA_DOUBLE, offset, value);
    }

    public <T> void setFieldLong(Class<T> componentClass, int index, String fieldName, int i, long value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        this.cachedColumns[index].set(ValueLayout.JAVA_LONG, offset, value);
    }

    public <T> void setFieldShort(Class<T> componentClass, int index, String fieldName, int i, short value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        this.cachedColumns[index].set(ValueLayout.JAVA_SHORT, offset, value);
    }

    public <T> void setFieldBoolean(Class<T> componentClass, int index, String fieldName, int i, boolean value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        this.cachedColumns[index].set(ValueLayout.JAVA_BOOLEAN, offset, value);
    }

    public <T> void setFieldByte(Class<T> componentClass, int index, String fieldName, int i, byte value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        this.cachedColumns[index].set(ValueLayout.JAVA_BYTE, offset, value);
    }

    public <T> void setFieldString(Class<T> componentClass, int index, String fieldName, int i, String value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long capacity = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        MemorySegment slice = this.cachedColumns[index].asSlice(offset, capacity);
        if (value == null || value.isEmpty()) {
            slice.set(ValueLayout.JAVA_BYTE, 0, (byte) 0);
            return;
        }
        byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int len = Math.min(bytes.length, (int) capacity - 1);
        MemorySegment.copy(bytes, 0, slice, ValueLayout.JAVA_BYTE, 0, len);
        slice.set(ValueLayout.JAVA_BYTE, len, (byte) 0);
    }

    public <T> void setFieldIntArray(Class<T> componentClass, int index, String fieldName, int i, int[] value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        int capacity = (int) (arrayByteSize / ValueLayout.JAVA_INT.byteSize());
        int len = Math.min(value.length, capacity);
        MemorySegment.copy(value, 0, this.cachedColumns[index], ValueLayout.JAVA_INT, offset, len);
    }

    public <T> void setFieldLongArray(Class<T> componentClass, int index, String fieldName, int i, long[] value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        int capacity = (int) (arrayByteSize / ValueLayout.JAVA_LONG.byteSize());
        int len = Math.min(value.length, capacity);
        MemorySegment.copy(value, 0, this.cachedColumns[index], ValueLayout.JAVA_LONG, offset, len);
    }

    public <T> void setFieldFloatArray(Class<T> componentClass, int index, String fieldName, int i, float[] value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        int capacity = (int) (arrayByteSize / ValueLayout.JAVA_FLOAT.byteSize());
        int len = Math.min(value.length, capacity);
        MemorySegment.copy(value, 0, this.cachedColumns[index], ValueLayout.JAVA_FLOAT, offset, len);
    }

    public <T> void setFieldDoubleArray(Class<T> componentClass, int index, String fieldName, int i, double[] value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        int capacity = (int) (arrayByteSize / ValueLayout.JAVA_DOUBLE.byteSize());
        int len = Math.min(value.length, capacity);
        MemorySegment.copy(value, 0, this.cachedColumns[index], ValueLayout.JAVA_DOUBLE, offset, len);
    }

    public <T> void setFieldByteArray(Class<T> componentClass, int index, String fieldName, int i, byte[] value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        int capacity = (int) (arrayByteSize / ValueLayout.JAVA_BYTE.byteSize());
        int len = Math.min(value.length, capacity);
        MemorySegment.copy(value, 0, this.cachedColumns[index], ValueLayout.JAVA_BYTE, offset, len);
    }

    public <T> void setFieldShortArray(Class<T> componentClass, int index, String fieldName, int i, short[] value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        int capacity = (int) (arrayByteSize / ValueLayout.JAVA_SHORT.byteSize());
        int len = Math.min(value.length, capacity);
        MemorySegment.copy(value, 0, this.cachedColumns[index], ValueLayout.JAVA_SHORT, offset, len);
    }

    public <T> void setFieldBooleanArray(Class<T> componentClass, int index, String fieldName, int i, boolean[] value) {
        long offset = fieldPtr(componentClass, index, fieldName, i);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long arrayByteSize = component.layout().select(MemoryLayout.PathElement.groupElement(fieldName)).byteSize();
        int capacity = (int) arrayByteSize;
        int len = Math.min(value.length, capacity);
        for (int j = 0; j < len; j++) {
            this.cachedColumns[index].set(ValueLayout.JAVA_BYTE, offset + j, (byte) (value[j] ? 1 : 0));
        }
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