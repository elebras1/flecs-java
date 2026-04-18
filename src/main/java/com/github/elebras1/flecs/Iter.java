package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Iter {

    private MemorySegment iterSeg;
    private final World world;
    private final Field<?>[] fields;
    private final Table table;
    private int count;

    Iter(MemorySegment iterSeg, World world) {
        this.iterSeg = iterSeg;
        this.world = world;
        this.fields = new Field[32];
        this.table = new Table(world, MemorySegment.NULL);
        this.count = -1;
    }

    void setIterSeg(MemorySegment iterSeg) {
        this.iterSeg = iterSeg;
        this.count = ecs_iter_t.count(iterSeg);
    }

    public World world() {
        return this.world;
    }

    public boolean next() {
        boolean hasNext = flecs_h.ecs_iter_next(this.iterSeg);
        this.count = hasNext ? ecs_iter_t.count(this.iterSeg) : 0;
        return hasNext;
    }

    public int count() {
        return this.count;
    }

    public long entityId(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";

        MemorySegment entities = ecs_iter_t.entities(this.iterSeg);
        if (entities.address() == 0) {
            throw new IllegalStateException("Entities array is null");
        }
        return entities.getAtIndex(ValueLayout.JAVA_LONG, index);
    }

    public long entity(int index) {
        return this.entityId(index);
    }

    public float deltaTime() {
        return ecs_iter_t.delta_time(this.iterSeg);
    }

    public float deltaSystemTime() {
        return ecs_iter_t.delta_system_time(this.iterSeg);
    }

    @SuppressWarnings("unchecked")
    public <T> Field<T> field(Class<T> componentClass, int index) {
        assert (index >= 0 && index < 32) : "The field index must be between 0 and 31.";

        Field<T> field = (Field<T>) this.fields[index];

        if (field == null) {
            Component<T> component = this.world.componentRegistry().getComponent(componentClass);
            MemorySegment columnSeg = flecs_h.ecs_field_w_size(this.iterSeg, component.size(), (byte) index);
            field = new Field<>(columnSeg, this.count(), this.world, componentClass);
            this.fields[index] = field;
        } else {
            MemorySegment columnSeg = flecs_h.ecs_field_w_size(this.iterSeg, field.componentSize(), (byte) index);
            field.reset(columnSeg, this.count());
        }

        return field;
    }

    public boolean isFieldSet(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";

        int setFields = ecs_iter_t.set_fields(this.iterSeg);
        return (setFields & (1 << index)) != 0;
    }

    public long termId(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";

        MemorySegment ids = ecs_iter_t.ids(this.iterSeg);
        if (ids.address() == 0) {
            return 0;
        }

        return ids.getAtIndex(ValueLayout.JAVA_LONG, index);
    }

    public int fieldSize(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";

        MemorySegment sizes = ecs_iter_t.sizes(this.iterSeg);
        if (sizes.address() == 0) {
            return 0;
        }

        return sizes.getAtIndex(ValueLayout.JAVA_INT, index);
    }

    public int fieldCount() {
        return Byte.toUnsignedInt(ecs_iter_t.field_count(this.iterSeg));
    }

    public long event() {
        return ecs_iter_t.event(this.iterSeg);
    }

    public Table table() {
        MemorySegment tableSeg = ecs_iter_t.table(this.iterSeg);
        if (tableSeg.address() == 0) {
            return null;
        }
        this.table.reset(tableSeg);
        return this.table;
    }

    public void fini() {
        int flags = ecs_iter_t.flags(this.iterSeg);
        MemorySegment tableSeg = ecs_iter_t.table(this.iterSeg);
        if((flags & flecs_h.EcsIterIsValid()) != 0 && tableSeg.address() != 0) {
            flecs_h.ecs_table_unlock(this.world.worldSeg(), tableSeg);
        }
        flecs_h.ecs_iter_fini(this.iterSeg);
    }
}