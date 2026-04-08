package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Table {
    private final World world;
    private final MemorySegment tableSeg;

    Table(World world, MemorySegment tableSeg) {
        this.world = world;
        this.tableSeg = tableSeg;
    }

    @FunctionalInterface
    public interface RowConsumer<V extends ComponentView> {
        void accept(V view, int row);
    }

    public String str() {
        MemorySegment strSeg = flecs_h.ecs_table_str(this.world.worldSeg(), this.tableSeg);
        String str = strSeg.reinterpret(Long.MAX_VALUE).getString(0);
        MemorySegment freeSeg = ecs_os_api_t.free_(flecs_h.ecs_os_api());
        ecs_os_api_free_t.invoke(freeSeg, strSeg);
        return str;
    }

    public long[] type() {
        MemorySegment typeSeg = flecs_h.ecs_table_get_type(this.tableSeg);
        if (typeSeg.address() == 0) {
            return new long[0];
        }
        MemorySegment arraySeg = ecs_type_t.array(typeSeg);
        int count = ecs_type_t.count(typeSeg);
        if (arraySeg.address() == 0 || count == 0) {
            return new long[0];
        }
        return arraySeg.reinterpret((long) count * Long.BYTES).toArray(ValueLayout.JAVA_LONG);
    }

    public int count() {
        return flecs_h.ecs_table_count(this.tableSeg);
    }

    public int size() {
        return flecs_h.ecs_table_size(this.tableSeg);
    }

    public long[] entities() {
        int count = this.count();
        if (count == 0) {
            return new long[0];
        }
        MemorySegment entitiesSeg = flecs_h.ecs_table_entities(this.tableSeg);
        if (entitiesSeg.address() == 0) {
            return new long[0];
        }
        return entitiesSeg.reinterpret((long) count * Long.BYTES).toArray(ValueLayout.JAVA_LONG);
    }

    public void clearEntities() {
        flecs_h.ecs_table_clear_entities(this.world.worldSeg(), this.tableSeg);
    }

    public int typeIndex(long id) {
        return flecs_h.ecs_table_get_type_index(this.world.worldSeg(), this.tableSeg, id);
    }

    public int typeIndex(Class<?> componentClass) {
        long id = this.world.componentRegistry().getComponentId(componentClass);
        return this.typeIndex(id);
    }

    public int typeIndex(long first, long second) {
        return this.typeIndex(flecs_h.ecs_make_pair(first, second));
    }

    public int typeIndex(Class<?> firstClass, Class<?> secondClass) {
        long first  = this.world.componentRegistry().getComponentId(firstClass);
        long second = this.world.componentRegistry().getComponentId(secondClass);
        return this.typeIndex(flecs_h.ecs_make_pair(first, second));
    }

    public int columnIndex(long id) {
        return flecs_h.ecs_table_get_column_index(this.world.worldSeg(), this.tableSeg, id);
    }

    public int columnIndex(Class<?> componentClass) {
        long id = this.world.componentRegistry().getComponentId(componentClass);
        return this.columnIndex(id);
    }

    public int columnIndex(long first, long second) {
        return this.columnIndex(flecs_h.ecs_make_pair(first, second));
    }

    public int columnIndex(Class<?> firstClass, Class<?> secondClass) {
        long first = this.world.componentRegistry().getComponentId(firstClass);
        long second = this.world.componentRegistry().getComponentId(secondClass);
        return this.columnIndex(flecs_h.ecs_make_pair(first, second));
    }

    public boolean has(long id) {
        return flecs_h.ecs_table_has_id(this.world.worldSeg(), this.tableSeg, id);
    }

    public boolean has(Class<?> componentClass) {
        long id = this.world.componentRegistry().getComponentId(componentClass);
        return this.has(id);
    }

    public boolean has(long first, long second) {
        return this.has(flecs_h.ecs_make_pair(first, second));
    }

    public boolean has(Class<?> firstClass, Class<?> secondClass) {
        long first  = this.world.componentRegistry().getComponentId(firstClass);
        long second = this.world.componentRegistry().getComponentId(secondClass);
        return this.has(flecs_h.ecs_make_pair(first, second));
    }

    public <T> T get(Class<T> componentClass, int row) {
        assert row >= 0 && row < this.count() : "row " + row + " out of bound (count=" + this.count() + ")";
        int col = this.columnIndex(componentClass);
        assert col != -1 : "Component missing from the table " + componentClass.getSimpleName();
        return this.readAt(componentClass, col, row);
    }

    public <T> T get(Class<T> firstClass, Class<?> secondClass, int row) {
        assert row >= 0 && row < this.count() : "row " + row + " out of bound (count=" + this.count() + ")";
        int col = this.columnIndex(firstClass, secondClass);
        assert col != -1 : "Pair missing from the table " + firstClass.getSimpleName() + " " + secondClass.getSimpleName();
        return this.readAt(firstClass, col, row);
    }

    public <V extends ComponentView> V getMutView(Class<?> componentClass, int row) {
        assert row >= 0 && row < this.count() : "row " + row + " out of bound (count=" + this.count() + ")";
        int col = this.columnIndex(componentClass);
        assert col != -1 : "Component missing from the table " + componentClass.getSimpleName();
        return this.readViewAt(componentClass, col, row);
    }

    public <V extends ComponentView> V getMutView(Class<?> firstClass, Class<?> secondClass, int row) {
        assert row >= 0 && row < this.count() : "row " + row + " out of bound (count=" + this.count() + ")";
        int col = this.columnIndex(firstClass, secondClass);
        assert col != -1 : "Pair missing from the table " + firstClass.getSimpleName() + " " + secondClass.getSimpleName();
        return this.readViewAt(firstClass, col, row);
    }


    public <T> T tryGet(Class<T> componentClass, int row) {
        assert row >= 0 && row < this.count() : "row " + row + " out of bound (count=" + this.count() + ")";
        int col = this.columnIndex(componentClass);
        if (col == -1) {
            return null;
        }
        return this.readAt(componentClass, col, row);
    }

    public <T> T tryGet(Class<T> firstClass, Class<?> secondClass, int row) {
        assert row >= 0 && row < this.count() : "row " + row + " out of bound (count=" + this.count() + ")";
        int col = this.columnIndex(firstClass, secondClass);
        if (col == -1) {
            return null;
        }
        return this.readAt(firstClass, col, row);
    }

    public <V extends ComponentView> V tryGetMutView(Class<?> componentClass, int row) {
        assert row >= 0 && row < this.count() : "row " + row + " out of bound (count=" + this.count() + ")";
        int col = this.columnIndex(componentClass);
        if (col == -1) {
            return null;
        }
        return this.readViewAt(componentClass, col, row);
    }

    public <V extends ComponentView> V tryGetMutView(Class<?> firstClass, Class<?> secondClass, int row) {
        assert row >= 0 && row < this.count() : "row " + row + " out of bound (count=" + this.count() + ")";
        int col = this.columnIndex(firstClass, secondClass);
        if (col == -1) {
            return null;
        }
        return this.readViewAt(firstClass, col, row);
    }

    @SuppressWarnings("unchecked")
    public <V extends ComponentView> V getColumnView(Class<?> componentClass, int row) {
        assert row >= 0 && row < this.count() : "row " + row + " out of bound (count=" + this.count() + ")";
        int col = this.columnIndex(componentClass);
        if (col == -1) {
            return null;
        }
        Component<?> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnSeg = this.getRawColumn(col);
        if (columnSeg.address() == 0) {
            return null;
        }
        V view = (V) this.world.viewCache().getComponentView(componentClass);
        view.setBaseAddress(columnSeg.address() + (long) row * component.size());
        return view;
    }

    @SuppressWarnings("unchecked")
    public <V extends ComponentView> void forEachRow(Class<?> componentClass, RowConsumer<V> consumer) {
        int col = this.columnIndex(componentClass);
        if (col == -1) {
            return;
        }
        Component<?> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnSeg = this.getRawColumn(col);
        if (columnSeg.address() == 0) {
            return;
        }
        V view = (V) this.world.viewCache().getComponentView(componentClass);
        long size  = component.size();
        int  count = this.count();
        for (int i = 0; i < count; i++) {
            view.setBaseAddress(columnSeg.address() + i * size);
            consumer.accept(view, i);
        }
    }

    public long columnSize(int columnIndex) {
        return flecs_h.ecs_table_get_column_size(this.tableSeg, columnIndex);
    }

    public int depth(long rel) {
        return flecs_h.ecs_table_get_depth(this.world.worldSeg(), this.tableSeg, rel);
    }

    public int depth(Class<?> relClass) {
        long rel = this.world.componentRegistry().getComponentId(relClass);
        return this.depth(rel);
    }

    public long id() {
        return flecs_h.flecs_table_id(this.tableSeg);
    }

    public void lock() {
        flecs_h.ecs_table_lock(this.world.worldSeg(), this.tableSeg);
    }

    public void unlock() {
        flecs_h.ecs_table_unlock(this.world.worldSeg(), this.tableSeg);
    }

    public boolean hasFlags(int flags) {
        return flecs_h.ecs_table_has_flags(this.tableSeg, flags);
    }

    public void resetColumn(Class<?> componentClass) {
        int col = this.columnIndex(componentClass);
        if (col != -1) {
            MemorySegment column = this.getRawColumn(col);
            if (column.address() == 0) {
                return;
            }

            Component<?> component = this.world.componentRegistry().getComponent(componentClass);
            column.reinterpret(component.size() * this.count()).fill((byte) 0);
        }
    }

    MemorySegment getRawColumn(int columnIndex) {
        return this.getRawColumn(columnIndex, 0);
    }

    MemorySegment getRawColumn(int columnIndex, int offset) {
        return flecs_h.ecs_table_get_column(this.tableSeg, columnIndex, offset);
    }

    private <T> T readAt(Class<T> componentClass, int columnIndex, int row) {
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnSeg = this.getRawColumn(columnIndex);
        if (columnSeg.address() == 0) {
            return null;
        }
        long size = component.size();
        long elementOffset = (long) row * size;
        MemorySegment slice = columnSeg.reinterpret(size * this.count()).asSlice(elementOffset, size);
        return component.read(slice, 0);
    }

    @SuppressWarnings("unchecked")
    private <V extends ComponentView> V readViewAt(Class<?> componentClass, int columnIndex, int row) {
        Component<?> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnSeg = this.getRawColumn(columnIndex);
        if (columnSeg.address() == 0) {
            return null;
        }
        long size = component.size();
        long elementOffset = (long) row * size;
        MemorySegment slice = columnSeg.reinterpret(size * this.count()).asSlice(elementOffset, size);
        V view = (V) this.world.viewCache().getComponentView(componentClass);
        view.setBaseAddress(slice.address());
        return view;
    }
}