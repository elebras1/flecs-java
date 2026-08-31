package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.EntityCallback;
import io.github.elebras1.flecs.util.Flecs;
import io.github.elebras1.flecs.util.internal.FlecsAllocator;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class Entity extends EntityBase<Entity> {

    Entity(World world, long id) {
        super(world, id);
    }

    public World world() {
        return this.world;
    }

    public Entity add(long entityId) {
        flecs_h.ecs_add_id(this.world.worldSeg(), this.id, entityId);
        return this;
    }

    public Entity add(Entity entity) {
        return this.add(entity.id());
    }

    public Entity add(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.add(componentId);
    }

    public Entity add(long first, long second) {
        long pair = flecs_h.ecs_make_pair(first, second);
        return this.add(pair);
    }

    public <T> Entity add(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.add(firstId, second);
    }

    public <T> Entity add(Class<T> first, Entity second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.add(firstId, second.id());
    }

    public <A, B> Entity add(Class<A> first, Class<B> second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        long secondId = this.world.componentRegistry().getComponentId(second);
        return this.add(firstId, secondId);
    }

    public Entity remove(long entityId) {
        flecs_h.ecs_remove_id(world.worldSeg(), this.id, entityId);
        return this;
    }

    public Entity remove(Entity entity) {
        return this.remove(entity.id());
    }

    public <T> Entity remove(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.remove(componentId);
    }

    public Entity remove(long first, long second) {
        long pair = flecs_h.ecs_make_pair(first, second);
        return this.remove(pair);
    }

    public <T> Entity remove(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.remove(firstId, second);
    }

    public <T> Entity remove(Class<T> first, Entity second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.remove(firstId, second.id());
    }

    public <A, B> Entity remove(Class<A> first, Class<B> second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        long secondId = this.world.componentRegistry().getComponentId(second);
        return this.remove(firstId, secondId);
    }

    public boolean has(long componentId) {
        return flecs_h.ecs_has_id(this.world.worldSeg(), this.id, componentId);
    }

    public boolean has(Entity entity) {
        return this.has(entity.id());
    }

    public boolean has(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.has(componentId);
    }

    public boolean has(long first, long second) {
        long pair = flecs_h.ecs_make_pair(first, second);
        return this.has(pair);
    }

    public <T> boolean has(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.has(firstId, second);
    }

    public <T> boolean has(Class<T> first, Entity second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.has(firstId, second.id());
    }

    public <A, B> boolean has(Class<A> first, Class<B> second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        long secondId = this.world.componentRegistry().getComponentId(second);
        return this.has(firstId, secondId);
    }

    public Entity name(String name) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment nameSeg = tempArena.allocateFrom(name);
            flecs_h.ecs_set_name(this.world.worldSeg(), this.id, nameSeg);
        }
        return this;
    }

    public String name() {
        MemorySegment nameSeg = flecs_h.ecs_get_name(this.world.worldSeg(), this.id);
        if (nameSeg.address() == 0) {
            return null;
        }
        return nameSeg.getString(0);
    }

    public String symbol() {
        MemorySegment symbolSeg = flecs_h.ecs_get_symbol(this.world.worldSeg(), this.id);
        if (symbolSeg.address() == 0) {
            return null;
        }
        return symbolSeg.getString(0);
    }

    public void destruct() {
        flecs_h.ecs_delete(this.world.worldSeg(), this.id);
    }

    public void setChildOrder(long... childrenIds) {
        try(Arena arena = Arena.ofConfined()) {
            MemorySegment childrenSeg = arena.allocate(ValueLayout.JAVA_LONG, childrenIds.length);

            MemorySegment.copy(childrenIds, 0, childrenSeg, ValueLayout.JAVA_LONG, 0, childrenIds.length);

            flecs_h.ecs_set_child_order(this.world.worldSeg(), this.id, childrenSeg, childrenIds.length);
        }
    }

    public void setChildOrder(Entity... children) {
        long[] childrenIds = new long[children.length];
        for (int i = 0; i < children.length; i++) {
            childrenIds[i] = children[i].id();
        }

        setChildOrder(childrenIds);
    }

    public boolean isValid() {
        return flecs_h.ecs_is_valid(this.world.worldSeg(), this.id);
    }

    public boolean isAlive() {
        return flecs_h.ecs_is_alive(this.world.worldSeg(), this.id);
    }

    public void clear() {
        flecs_h.ecs_clear(this.world.worldSeg(), this.id);
    }

    public Entity childOf(long parentId) {
        return this.add(Flecs.ChildOf, parentId);
    }

    public Entity childOf(Entity parent) {
        return this.childOf(parent.id());
    }

    public long parent() {
        return this.target(Flecs.ChildOf, 0);
    }

    public Entity isA(long entityId) {
        return this.add(Flecs.IsA, entityId);
    }

    public Entity isA(Entity entity) {
        return this.add(Flecs.IsA, entity.id());
    }

    public Entity slotOf(long targetId) {
        return this.add(Flecs.SlotOf, targetId);
    }

    public Entity slotOf(Entity target) {
        return this.slotOf(target.id());
    }

    public <T> Entity slotOf(Class<T> targetClass) {
        long targetId = this.world.componentRegistry().getComponentId(targetClass);
        return this.slotOf(targetId);
    }

    public Entity slot() {
        long target = this.target(Flecs.ChildOf, 0);
        if (target == 0) {
            throw new IllegalStateException("add ChildOf pair before using slot()");
        }
        return this.slotOf(target);
    }

    public Entity autoOverride(long componentId) {
        flecs_h.ecs_auto_override_id(this.world.worldSeg(), this.id, componentId);
        return this;
    }

    public <T> Entity autoOverride(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.autoOverride(componentId);
    }

    @SuppressWarnings("unchecked")
    public <T> Entity set(T data) {
        Class<T> componentClass = (Class<T>) data.getClass();
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);

        MemorySegment dataSeg = this.world.getComponentBuffer(component.size());
        component.write(dataSeg, 0, data);
        flecs_h.ecs_set_id(this.world.worldSeg(), this.id, componentId, component.size(), dataSeg);

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Entity set(T data, long target) {
        Class<T> componentClass = (Class<T>) data.getClass();
        long componentId = this.world.componentRegistry().getComponentId(componentClass);

        long pairId = flecs_h.ecs_make_pair(componentId, target);

        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment dataSeg = this.world.getComponentBuffer(component.size());
        component.write(dataSeg, 0, data);

        flecs_h.ecs_set_id(this.world.worldSeg(), this.id, pairId, component.size(), dataSeg);

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentView> Entity set(Class<?> componentClass, long target, Consumer<T> consumer) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        long size = this.world.componentRegistry().getComponent(componentClass).size();
        long pairId = flecs_h.ecs_make_pair(componentId, target);

        MemorySegment dataSeg = flecs_h.ecs_ensure_id(this.world.worldSeg(), this.id, pairId, size);

        T view = (T) this.world.viewCache().getComponentView(componentClass);
        view.setBaseAddress(dataSeg.address());
        consumer.accept(view);

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentView> Entity setSecond(Class<?> componentClass, long relationId, Consumer<T> consumer) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        long size = this.world.componentRegistry().getComponent(componentClass).size();
        long pairId = flecs_h.ecs_make_pair(relationId, componentId);

        MemorySegment dataSeg = flecs_h.ecs_ensure_id(this.world.worldSeg(), this.id, pairId, size);

        T view = (T) this.world.viewCache().getComponentView(componentClass);
        view.setBaseAddress(dataSeg.address());
        consumer.accept(view);

        return this;
    }

    public <T> T get(long componentId) {
        Component<T> component = this.world.componentRegistry().getComponentById(componentId);
        long address = flecs_h.ecs_get_id(this.world.worldSeg(), this.id, componentId);

        if (address == 0) {
            return null;
        }

        MemorySegment dataSeg = MemorySegment.ofAddress(address).reinterpret(component.size());

        return component.read(dataSeg, 0);
    }

    public <T> T get(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.get(componentId);
    }

    public <T> T get(Class<T> componentClass, long target) {
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        long pairId = flecs_h.ecs_make_pair(componentId, target);

        long address = flecs_h.ecs_get_id(this.world.worldSeg(), this.id, pairId);
        if (address == 0) {
            return null;
        }

        MemorySegment dataSeg = MemorySegment.ofAddress(address).reinterpret(component.size());
        return component.read(dataSeg, 0);
    }

    public <T> T get(Class<T> componentClass, Class<?> targetClass) {
        long targetId = this.world.componentRegistry().getComponentId(targetClass);
        return this.get(componentClass, targetId);
    }

    public <T> T getSecond(Class<T> componentClass, long relationId) {
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        long pairId = flecs_h.ecs_make_pair(relationId, componentId);

        long address = flecs_h.ecs_get_id(this.world.worldSeg(), this.id, pairId);
        if (address == 0) {
            return null;
        }

        MemorySegment dataSeg = MemorySegment.ofAddress(address).reinterpret(component.size());
        return component.read(dataSeg, 0);
    }

    public <T> T getSecond(Class<T> componentClass, Class<?> relationClass) {
        long relationId = this.world.componentRegistry().getComponentId(relationClass);
        return this.getSecond(componentClass, relationId);
    }

    public Entity modified(long componentId) {
        flecs_h.ecs_modified_id(this.world.worldSeg(), this.id, componentId);
        return this;
    }

    public <T> Entity modified(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.modified(componentId);
    }

    public Entity modified(long first, long second) {
        long pairId = flecs_h.ecs_make_pair(first, second);
        flecs_h.ecs_modified_id(this.world.worldSeg(), this.id, pairId);
        return this;
    }

    public <T> Entity modified(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.modified(firstId, second);
    }

    public <A, B> Entity modified(Class<A> first, Class<B> second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        long secondId = this.world.componentRegistry().getComponentId(second);
        return this.modified(firstId, secondId);
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentView> T getMutView(Class<?> componentClass) {
        ComponentView view = this.world.viewCache().getComponentView(componentClass);
        long componentId = this.world.componentRegistry().getComponentId(componentClass);

        long address = flecs_h.ecs_get_mut_id(this.world.worldSeg(), this.id, componentId);

        if (address == 0) {
            return null;
        }

        view.setBaseAddress(address);

        return (T) view;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMutView(Class<?> componentClass, long target) {
        ComponentView view = this.world.viewCache().getComponentView(componentClass);

        long componentId = this.world.componentRegistry().getComponentId(componentClass);

        long pairId = flecs_h.ecs_make_pair(componentId, target);

        long address = flecs_h.ecs_get_mut_id(this.world.worldSeg(), this.id, pairId);

        if (address == 0) {
            return null;
        }

        view.setBaseAddress(address);

        return (T) view;
    }

    public <T> Ref<T> getRef(Class<T> componentClass) {
        return new Ref<>(this.world, this.id, componentClass);
    }

    public <T> Ref<T> getRef(Class<T> componentClass, long target) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        long pairId = flecs_h.ecs_make_pair(componentId, target);
        return new Ref<>(this.world, this.id, pairId, componentClass);
    }

    public <T> Ref<T> getRef(Class<T> componentClass, Entity target) {
        return this.getRef(componentClass, target.id());
    }

    public void enable() {
        flecs_h.ecs_enable(this.world.worldSeg(), this.id, true);
    }

    public void disable() {
        flecs_h.ecs_enable(this.world.worldSeg(), this.id, false);
    }

    public FlecsObserver observe(long eventId, Runnable callback) {
        return this.world.observer()
                .event(eventId)
                .with(Flecs.Any)
                .each((entityId) -> {
                    if (entityId == this.id) {
                        callback.run();
                    }
                });
    }

    public <T> FlecsObserver observe(Class<T> eventClass, Consumer<T> callback) {
        long eventId = this.world.componentRegistry().getComponentId(eventClass);
        return this.world.observer()
                .event(eventId)
                .with(Flecs.Any)
                .iter((it) -> {
                    for (int i = 0; i < it.count(); i++) {
                        if (it.entityId(i) == this.id) {
                            T eventData = this.get(eventClass);
                            if (eventData != null) {
                                callback.accept(eventData);
                            }
                        }
                    }
                });
    }

    public void emit(long eventId) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment eventDescSeg = ecs_event_desc_t.allocate(tempArena);

            ecs_event_desc_t.event(eventDescSeg, eventId);
            ecs_event_desc_t.entity(eventDescSeg, this.id);

            flecs_h.ecs_emit(this.world.worldSeg(), eventDescSeg);
        }
    }

    public void emit(long eventId, long componentId) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment eventDescSeg = ecs_event_desc_t.allocate(tempArena);

            MemorySegment typeSeg = ecs_type_t.allocate(tempArena);
            MemorySegment idsArraySeg = tempArena.allocate(ValueLayout.JAVA_LONG);
            idsArraySeg.set(ValueLayout.JAVA_LONG, 0, componentId);

            ecs_type_t.array(typeSeg, idsArraySeg);
            ecs_type_t.count(typeSeg, 1);

            ecs_event_desc_t.event(eventDescSeg, eventId);
            ecs_event_desc_t.entity(eventDescSeg, this.id);
            ecs_event_desc_t.ids(eventDescSeg, typeSeg);

            flecs_h.ecs_emit(this.world.worldSeg(), eventDescSeg);
        }
    }

    public <T> void emit(long eventId, Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        this.emit(eventId, componentId);
    }

    public <E> void emit(Class<E> payloadClass, E payload) {
        this.world.event(payloadClass)
                .entity(this)
                .payload(payload)
                .emit();
    }

    public void enqueue(long eventId) {
        this.world.event(eventId)
                .entity(this)
                .enqueue();
    }

    public void enqueue(long eventId, long componentId) {
        this.world.event(eventId)
                .id(componentId)
                .entity(this)
                .enqueue();
    }

    public <T> void enqueue(long eventId, Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        this.enqueue(eventId, componentId);
    }

    public <E> void enqueue(Class<E> payloadClass, E payload) {
        this.world.event(payloadClass)
                .entity(this)
                .payload(payload)
                .enqueue();
    }

    public long target(long relationId, int index) {
        return flecs_h.ecs_get_target(this.world.worldSeg(), this.id, relationId, index);
    }

    public long target(Entity relation, int index) {
        return this.target(relation.id(), index);
    }

    public long target(long relationId) {
        return this.target(relationId, 0);
    }

    public long target(Entity relation) {
        return this.target(relation.id(), 0);
    }

    public int depth(long relationId) {
        return flecs_h.ecs_get_depth(this.world.worldSeg(), this.id, relationId);
    }

    public int depth(Entity relation) {
        return this.depth(relation.id());
    }

    public boolean owns(long componentId) {
        return flecs_h.ecs_owns_id(this.world.worldSeg(), this.id, componentId);
    }

    public boolean owns(Entity component) {
        return this.owns(component.id());
    }

    public boolean owns(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.owns(componentId);
    }

    public boolean enabled(long componentId) {
        return flecs_h.ecs_is_enabled_id(this.world.worldSeg(), this.id, componentId);
    }

    public <T> boolean enabled(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.enabled(componentId);
    }

    public Entity enable(long componentId) {
        flecs_h.ecs_enable_id(this.world.worldSeg(), this.id, componentId, true);
        return this;
    }

    public <T> Entity enable(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.enable(componentId);
    }

    public Entity disable(long componentId) {
        flecs_h.ecs_enable_id(this.world.worldSeg(), this.id, componentId, false);
        return this;
    }

    public <T> Entity disable(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.disable(componentId);
    }

    public long clone(boolean cloneValues) {
        return flecs_h.ecs_clone(this.world.worldSeg(), 0, this.id, cloneValues);
    }

    public ScopedWorld scope() {
        return new ScopedWorld(this.world, this.id);
    }

    public long lookup(String path) {
        return this.lookup(path, false);
    }

    public long lookup(String path, boolean recursive) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment pathSeg = tempArena.allocateFrom(path);
            MemorySegment sepSeg = tempArena.allocateFrom("::");
            MemorySegment rootSepSeg = tempArena.allocateFrom("::");
            return flecs_h.ecs_lookup_path_w_sep(this.world.worldSeg(), this.id, pathSeg, sepSeg, rootSepSeg, recursive);
        }
    }

    public String path() {
        return this.path("::", "::");
    }

    public String path(String sep, String initSep) {
        return this.pathFrom(0, sep, initSep);
    }

    public String pathFrom(Entity parent) {
        return this.pathFrom(parent.id());
    }

    public String pathFrom(Class<?> parentClass) {
        long parentId = this.world.componentRegistry().getComponentId(parentClass);
        return this.pathFrom(parentId);
    }

    public String pathFrom(long parentId) {
        return this.pathFrom(parentId, "::", "::");
    }

    public String pathFrom(long parentId, String sep, String initSep) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment sepSeg = tempArena.allocateFrom(sep);
            MemorySegment initSepSeg = tempArena.allocateFrom(initSep);

            MemorySegment pathSeg = flecs_h.ecs_get_path_w_sep(this.world.worldSeg(), parentId, this.id, sepSeg, initSepSeg);
            if (pathSeg.address() == 0) {
                return null;
            }

            String path = pathSeg.reinterpret(Long.MAX_VALUE).getString(0);
            FlecsAllocator.free(pathSeg);
            return path;
        }
    }

    public void children(EntityCallback callback) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment iterSeg = flecs_h.ecs_children(tempArena, this.world.worldSeg(), this.id);

            while (flecs_h.ecs_children_next(iterSeg)) {
                int count = ecs_iter_t.count(iterSeg);
                MemorySegment entitiesSeg = ecs_iter_t.entities(iterSeg);
                for (int i = 0; i < count; i++) {
                    long entityId = entitiesSeg.getAtIndex(ValueLayout.JAVA_LONG, i);
                    callback.accept(entityId);
                }
            }
        }
    }

    public void children(long relationId, EntityCallback callback) {
        if (this.id == Flecs.Wildcard || this.id == Flecs.Any) {
            return;
        }

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment iterSeg = flecs_h.ecs_children_w_rel(tempArena, this.world.worldSeg(), relationId, this.id);

            while (flecs_h.ecs_each_next(iterSeg)) {
                int count = ecs_iter_t.count(iterSeg);
                MemorySegment entitiesSeg = ecs_iter_t.entities(iterSeg);

                for (int i = 0; i < count; i++) {
                    long entityId = entitiesSeg.getAtIndex(ValueLayout.JAVA_LONG, i);
                    callback.accept(entityId);
                }
            }
        }
    }

    public Type type() {
        MemorySegment typeSeg = flecs_h.ecs_get_type(this.world.worldSeg(), this.id);
        return new Type(this.world, typeSeg);
    }

    public Table table() {
        MemorySegment tableSeg = flecs_h.ecs_get_table(this.world.worldSeg(), this.id);
        if (tableSeg.address() == 0) {
            return null;
        }
        return new Table(this.world, tableSeg);
    }

    public String toJson() {
        return this.toJsonDesc(MemorySegment.NULL);
    }

    public String toJson(boolean serializeValues) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment descSeg = ecs_entity_to_json_desc_t.allocate(tempArena);
            ecs_entity_to_json_desc_t.serialize_values(descSeg, serializeValues);
            return this.toJsonDesc(descSeg);
        }
    }

    private String toJsonDesc(MemorySegment desc) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment jsonSeg = flecs_h.ecs_entity_to_json(this.world.worldSeg(), this.id, desc);
            if (jsonSeg.address() == 0) {
                return null;
            }

            String json = jsonSeg.reinterpret(Long.MAX_VALUE).getString(0);
            FlecsAllocator.free(jsonSeg);
            return json;
        }
    }

    public void fromJson(String json) {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("JSON cannot be null or empty");
        }

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment jsonSeg = tempArena.allocateFrom(json);
            MemorySegment resultSeg = flecs_h.ecs_entity_from_json(this.world.worldSeg(), this.id, jsonSeg, MemorySegment.NULL);
            if (resultSeg.address() == 0) {
                throw new RuntimeException("Failed to parse JSON into entity");
            }
        }
    }

    public void each(LongConsumer callback) {
        MemorySegment typeSeg = flecs_h.ecs_get_type(this.world.worldSeg(), this.id);
        if (typeSeg.address() == 0) {
            return;
        }

        int count = ecs_type_t.count(typeSeg);
        MemorySegment ids = ecs_type_t.array(typeSeg);

        for (int i = 0; i < count; i++) {
            callback.accept(ids.getAtIndex(ValueLayout.JAVA_LONG, i));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Entity other)) {
            return false;
        }
        return this.id == other.id && this.world == other.world;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }

    @Override
    public String toString() {
        String name = this.name();
        if (name != null) {
            return String.format("Entity[%d, \"%s\"]", this.id, name);
        }
        return String.format("Entity[%d]", this.id);
    }
}