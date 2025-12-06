package com.github.elebras1.flecs;

import com.github.elebras1.flecs.collection.EcsLongList;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Table {

    private final Flecs world;
    private final MemorySegment nativeTable;

    Table(Flecs world, MemorySegment nativeTable) {
        this.world = world;
        this.nativeTable = nativeTable;
    }

    public boolean isValid() {
        return this.nativeTable != null && this.nativeTable.address() != 0;
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

    public EcsLongList entities() {
        if (!this.isValid()) {
            return new EcsLongList();
        }

        int count = this.count();
        if (count == 0) {
            return new EcsLongList();
        }

        MemorySegment entitiesPtr = flecs_h.ecs_table_entities(this.nativeTable);
        if (entitiesPtr == null || entitiesPtr.address() == 0) {
            return new EcsLongList();
        }

        EcsLongList entities = new EcsLongList(count);
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

