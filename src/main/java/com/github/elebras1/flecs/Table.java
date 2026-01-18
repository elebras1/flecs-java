package com.github.elebras1.flecs;

import com.github.elebras1.flecs.collection.LongList;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Table {

    private final World world;
    private final MemorySegment nativeTable;

    Table(World world, MemorySegment nativeTable) {
        this.world = world;
        this.nativeTable = nativeTable;
    }

    public boolean isValid() {
        return this.nativeTable != null && this.nativeTable.address() != 0;
    }

    public String str() {
        if (!this.isValid()) {
            return "";
        }
        MemorySegment strPtr = flecs_h.ecs_table_str(this.world.nativeHandle(), this.nativeTable);
        if (strPtr == null || strPtr.address() == 0) {
            return "";
        }
        return strPtr.reinterpret(Long.MAX_VALUE).getString(0);
    }

    public LongList type() {
        if (!this.isValid()) {
            return new LongList();
        }
        MemorySegment typePtr = flecs_h.ecs_table_get_type(this.nativeTable);
        if (typePtr == null || typePtr.address() == 0) {
            return new LongList();
        }
        MemorySegment arrayPtr = ecs_type_t.array(typePtr);
        int count = ecs_type_t.count(typePtr);
        if (arrayPtr == null || arrayPtr.address() == 0 || count == 0) {
            return new LongList();
        }
        LongList ids = new LongList(count);
        MemorySegment idsArray = arrayPtr.reinterpret((long) count * Long.BYTES);
        ids.addAll(idsArray.toArray(ValueLayout.JAVA_LONG));
        return ids;
    }

    public int count() {
        if (!this.isValid()) {
            return 0;
        }
        return flecs_h.ecs_table_count(this.nativeTable);
    }

    public int size() {
        if (!this.isValid()) {
            return 0;
        }
        return flecs_h.ecs_table_size(this.nativeTable);
    }

    public int columnCount() {
        if (!this.isValid()) {
            return 0;
        }
        return flecs_h.ecs_table_column_count(this.nativeTable);
    }

    public boolean hasId(long id) {
        if (!this.isValid()) {
            return false;
        }
        return flecs_h.ecs_table_has_id(this.world.nativeHandle(), this.nativeTable, id);
    }

    public boolean has(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.hasId(componentId);
    }

    public int depth(long relationId) {
        if (!this.isValid()) {
            return 0;
        }
        return flecs_h.ecs_table_get_depth(this.world.nativeHandle(), this.nativeTable, relationId);
    }

    public int typeIndex(long id) {
        if (!this.isValid()) {
            return -1;
        }
        return flecs_h.ecs_table_get_type_index(this.world.nativeHandle(), this.nativeTable, id);
    }

    public int columnIndex(long id) {
        if (!this.isValid()) {
            return -1;
        }
        return flecs_h.ecs_table_get_column_index(this.world.nativeHandle(), this.nativeTable, id);
    }

    public int columnIndex(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.columnIndex(componentId);
    }

    public long getColumnSize(int columnIndex) {
        if (!this.isValid()) {
            return 0;
        }
        return flecs_h.ecs_table_get_column_size(this.nativeTable, columnIndex);
    }

    public MemorySegment getColumn(int columnIndex) {
        if (!this.isValid()) {
            return MemorySegment.NULL;
        }
        return flecs_h.ecs_table_get_column(this.nativeTable, columnIndex, 0);
    }

    public MemorySegment getColumn(int columnIndex, int offset) {
        if (!this.isValid()) {
            return MemorySegment.NULL;
        }
        return flecs_h.ecs_table_get_column(this.nativeTable, columnIndex, offset);
    }

    public <T> T get(Class<T> componentClass, int row) {
        int colIndex = this.columnIndex(componentClass);
        if (colIndex == -1) {
            throw new IllegalArgumentException("Component " + componentClass.getSimpleName() + " not found in table");
        }
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnPtr = this.getColumn(colIndex);
        if (columnPtr == null || columnPtr.address() == 0) {
            throw new IllegalStateException("Failed to get column data");
        }
        long elementOffset = (long) row * component.size();
        MemorySegment elementSegment = columnPtr.reinterpret(component.size() * this.count()).asSlice(elementOffset, component.size());
        return component.read(elementSegment, 0);
    }

    public <T> T tryGet(Class<T> componentClass, int row) {
        int colIndex = this.columnIndex(componentClass);
        if (colIndex == -1) {
            return null;
        }
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnPtr = this.getColumn(colIndex);
        if (columnPtr == null || columnPtr.address() == 0) {
            return null;
        }
        long elementOffset = (long) row * component.size();
        MemorySegment elementSegment = columnPtr.reinterpret(component.size() * this.count()).asSlice(elementOffset, component.size());
        return component.read(elementSegment, 0);
    }

    public LongList entities() {
        if (!this.isValid()) {
            return new LongList();
        }

        int count = this.count();
        if (count == 0) {
            return new LongList();
        }

        MemorySegment entitiesPtr = flecs_h.ecs_table_entities(this.nativeTable);
        if (entitiesPtr == null || entitiesPtr.address() == 0) {
            return new LongList();
        }

        LongList entities = new LongList(count);
        MemorySegment entitiesArray = entitiesPtr.reinterpret((long) count * Long.BYTES);
        entities.addAll(entitiesArray.toArray(ValueLayout.JAVA_LONG));

        return entities;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Table other)) {
            return false;
        }
        if (!this.isValid() || !other.isValid()) {
            return false;
        }
        return this.nativeTable.address() == other.nativeTable.address();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.nativeTable != null ? this.nativeTable.address() : 0);
    }

    @Override
    public String toString() {
        if (!this.isValid()) {
            return "Table[invalid]";
        }
        return String.format("Table[count=%d, columns=%d]", this.count(), this.columnCount());
    }
}

