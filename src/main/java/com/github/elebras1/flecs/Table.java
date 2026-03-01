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

    @FunctionalInterface
    public interface RowConsumer<V extends ComponentView> {
        void accept(V view, int row);
    }

    public String str() {
        MemorySegment nativeStr = flecs_h.ecs_table_str(this.world.nativeHandle(), this.nativeTable);
        String str = nativeStr.reinterpret(Long.MAX_VALUE).getString(0);
        MemorySegment freePtr = ecs_os_api_t.free_(flecs_h.ecs_os_api());
        ecs_os_api_free_t.invoke(freePtr, nativeStr);
        return str;
    }

    public LongList type() {
        MemorySegment nativeType = flecs_h.ecs_table_get_type(this.nativeTable);
        if (nativeType == null || nativeType.address() == 0) {
            return new LongList();
        }
        MemorySegment nativeArray = ecs_type_t.array(nativeType);
        int count = ecs_type_t.count(nativeType);
        if (nativeArray == null || nativeArray.address() == 0 || count == 0) {
            return new LongList();
        }
        LongList ids = new LongList(count);
        ids.addAll(nativeArray.reinterpret((long) count * Long.BYTES).toArray(ValueLayout.JAVA_LONG));
        return ids;
    }

    public int count() {
        return flecs_h.ecs_table_count(this.nativeTable);
    }

    public int size() {
        return flecs_h.ecs_table_size(this.nativeTable);
    }

    public LongList entities() {
        int count = this.count();
        if (count == 0) {
            return new LongList();
        }
        MemorySegment nativeEntities = flecs_h.ecs_table_entities(this.nativeTable);
        if (nativeEntities == null || nativeEntities.address() == 0) {
            return new LongList();
        }
        LongList entities = new LongList(count);
        entities.addAll(nativeEntities.reinterpret((long) count * Long.BYTES).toArray(ValueLayout.JAVA_LONG));
        return entities;
    }

    public void clearEntities() {
        flecs_h.ecs_table_clear_entities(this.world.nativeHandle(), this.nativeTable);
    }

    public int typeIndex(long id) {
        return flecs_h.ecs_table_get_type_index(this.world.nativeHandle(), this.nativeTable, id);
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
        return flecs_h.ecs_table_get_column_index(this.world.nativeHandle(), this.nativeTable, id);
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
        return flecs_h.ecs_table_has_id(this.world.nativeHandle(), this.nativeTable, id);
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
        MemorySegment columnPtr = this.getRawColumn(col);
        if (columnPtr == null || columnPtr.address() == 0) {
            return null;
        }
        V view = (V) this.world.viewCache().getComponentView(componentClass);
        view.setBaseAddress(columnPtr.address() + (long) row * component.size());
        return view;
    }

    @SuppressWarnings("unchecked")
    public <V extends ComponentView> void forEachRow(Class<?> componentClass, RowConsumer<V> consumer) {
        int col = this.columnIndex(componentClass);
        if (col == -1) {
            return;
        }
        Component<?> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnPtr = this.getRawColumn(col);
        if (columnPtr == null || columnPtr.address() == 0) {
            return;
        }
        V view = (V) this.world.viewCache().getComponentView(componentClass);
        long size  = component.size();
        int  count = this.count();
        for (int i = 0; i < count; i++) {
            view.setBaseAddress(columnPtr.address() + i * size);
            consumer.accept(view, i);
        }
    }

    public long columnSize(int columnIndex) {
        return flecs_h.ecs_table_get_column_size(this.nativeTable, columnIndex);
    }

    public int depth(long rel) {
        return flecs_h.ecs_table_get_depth(this.world.nativeHandle(), this.nativeTable, rel);
    }

    public int depth(Class<?> relClass) {
        long rel = this.world.componentRegistry().getComponentId(relClass);
        return this.depth(rel);
    }

    public long id() {
        return flecs_h.flecs_table_id(this.nativeTable);
    }

    public void lock() {
        flecs_h.ecs_table_lock(this.world.nativeHandle(), this.nativeTable);
    }

    public void unlock() {
        flecs_h.ecs_table_unlock(this.world.nativeHandle(), this.nativeTable);
    }

    public boolean hasFlags(int flags) {
        return flecs_h.ecs_table_has_flags(this.nativeTable, flags);
    }

    MemorySegment getRawColumn(int columnIndex) {
        return this.getRawColumn(columnIndex, 0);
    }

    MemorySegment getRawColumn(int columnIndex, int offset) {
        return flecs_h.ecs_table_get_column(this.nativeTable, columnIndex, offset);
    }

    private <T> T readAt(Class<T> componentClass, int columnIndex, int row) {
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnPtr = this.getRawColumn(columnIndex);
        if (columnPtr == null || columnPtr.address() == 0) {
            return null;
        }
        long size = component.size();
        long elementOffset = (long) row * size;
        MemorySegment slice = columnPtr.reinterpret(size * this.count()).asSlice(elementOffset, size);
        return component.read(slice, 0);
    }

    @SuppressWarnings("unchecked")
    private <V extends ComponentView> V readViewAt(Class<?> componentClass, int columnIndex, int row) {
        Component<?> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment columnPtr = this.getRawColumn(columnIndex);
        if (columnPtr == null || columnPtr.address() == 0) {
            return null;
        }
        long size = component.size();
        long elementOffset = (long) row * size;
        MemorySegment slice = columnPtr.reinterpret(size * this.count()).asSlice(elementOffset, size);
        V view = (V) this.world.viewCache().getComponentView(componentClass);
        view.setBaseAddress(slice.address());
        return view;
    }
}