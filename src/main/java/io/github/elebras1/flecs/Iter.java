package io.github.elebras1.flecs;

import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
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
        boolean shared = !flecs_h.ecs_field_is_self(this.iterSeg, (byte) index);
        int fieldCount = shared ? 1 : this.count();

        if (field == null) {
            Component<T> component = this.world.componentRegistry().getComponent(componentClass);
            MemorySegment columnSeg = flecs_h.ecs_field_w_size(this.iterSeg, component.size(), (byte) index);
            field = new Field<>(columnSeg, fieldCount, this.world, componentClass, shared);
            this.fields[index] = field;
        } else {
            MemorySegment columnSeg = flecs_h.ecs_field_w_size(this.iterSeg, field.componentSize(), (byte) index);
            field.reset(columnSeg, fieldCount, shared);
        }

        return field;
    }

    public long fieldSource(int index) {
        assert (index >= 0 && index < 32) : "The field index must be between 0 and 31.";
        return flecs_h.ecs_field_src(this.iterSeg, (byte) index);
    }

    public boolean isFieldSet(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";

        int setFields = ecs_iter_t.set_fields(this.iterSeg);
        return (setFields & (1 << index)) != 0;
    }

    public boolean isSelf(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";
        return flecs_h.ecs_field_is_self(this.iterSeg, (byte) index);
    }

    public boolean isReadonly(int index) {
        assert (index >= 0 && index < 32) : "The index must be between 0 and 31.";
        return flecs_h.ecs_field_is_readonly(this.iterSeg, (byte) index);
    }

    public boolean changed() {
        return flecs_h.ecs_iter_changed(this.iterSeg);
    }

    public void skip() {
        flecs_h.ecs_iter_skip(this.iterSeg);
    }

    public void setGroup(long groupId) {
        flecs_h.ecs_iter_set_group(this.iterSeg, groupId);
    }

    public String toJson() {
        MemorySegment jsonSeg = flecs_h.ecs_iter_to_json(this.iterSeg, MemorySegment.NULL);
        if (jsonSeg.address() == 0) {
            return null;
        }
        return jsonSeg.getString(0);
    }

    public Table otherTable() {
        MemorySegment tableSeg = ecs_iter_t.other_table(this.iterSeg);
        if (tableSeg == null || tableSeg.address() == 0) {
            return null;
        }
        return new Table(this.world, tableSeg);
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

    public Type type() {
        MemorySegment tableSeg = ecs_iter_t.table(this.iterSeg);
        if (tableSeg.address() == 0) {
            return new Type(this.world, MemorySegment.NULL);
        }
        MemorySegment typeSeg = flecs_h.ecs_table_get_type(tableSeg);
        return new Type(this.world, typeSeg);
    }

    public Table table() {
        MemorySegment tableSeg = ecs_iter_t.table(this.iterSeg);
        if (tableSeg.address() == 0) {
            return null;
        }
        this.table.reset(tableSeg);
        return this.table;
    }

    @SuppressWarnings("unchecked")
    public <T> T param() {
        long id = ecs_iter_t.param(this.iterSeg).address();
        return (T) ParamRegistry.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T> T payload(Class<T> payloadClass) {
        MemorySegment paramSeg = ecs_iter_t.param(this.iterSeg);
        if (paramSeg == null || paramSeg.address() == 0) {
            return null;
        }

        Component<T> component = this.world.componentRegistry().getComponent(payloadClass);
        MemorySegment dataSeg = paramSeg.reinterpret(component.size());
        return component.read(dataSeg, 0);
    }

    public long getVar(int varId) {
        return flecs_h.ecs_iter_get_var(this.iterSeg, varId);
    }

    public long getVar(String name) {
        int varId = this.findVar(name);
        if (varId < 0) {
            return 0;
        }
        return this.getVar(varId);
    }

    public void setVar(int varId, long entityId) {
        flecs_h.ecs_iter_set_var(this.iterSeg, varId, entityId);
    }

    public void setVar(String name, long entityId) {
        int varId = this.findVar(name);
        if (varId < 0) {
            throw new IllegalArgumentException("Unknown query variable: " + name);
        }
        this.setVar(varId, entityId);
    }

    public int findVar(String name) {
        MemorySegment querySeg = ecs_iter_t.query(this.iterSeg);
        if (querySeg == null || querySeg.address() == 0) {
            return -1;
        }
        try (Arena tempArena = Arena.ofConfined()) {
            return flecs_h.ecs_query_find_var(querySeg, tempArena.allocateFrom(name));
        }
    }

    public void destroy() {
        int flags = ecs_iter_t.flags(this.iterSeg);
        MemorySegment tableSeg = ecs_iter_t.table(this.iterSeg);
        if((flags & flecs_h.EcsIterIsValid()) != 0 && tableSeg.address() != 0) {
            flecs_h.ecs_table_unlock(this.world.worldSeg(), tableSeg);
        }
        flecs_h.ecs_iter_fini(this.iterSeg);
    }
}