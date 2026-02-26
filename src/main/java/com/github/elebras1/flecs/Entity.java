package com.github.elebras1.flecs;

import com.github.elebras1.flecs.util.FlecsConstants;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.function.Consumer;

public class Entity {

    private final World world;
    private long id;

    Entity(World world, long id) {
        this.world = world;
        this.id = id;
    }

    protected void setId(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }

    public World world() {
        return world;
    }

    public Entity add(long entityId) {
        flecs_h.ecs_add_id(this.world.nativeHandle(), this.id, entityId);
        return this;
    }

    public Entity add(Entity entity) {
        return this.add(entity.id());
    }

    public Entity add(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.add(componentId);
    }

    public Entity remove(long entityId) {
        flecs_h.ecs_remove_id(world.nativeHandle(), this.id, entityId);
        return this;
    }

    public Entity remove(Entity entity) {
        return this.remove(entity.id());
    }

    public <T> Entity remove(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.remove(componentId);
    }

    public boolean has(long componentId) {
        return flecs_h.ecs_has_id(this.world.nativeHandle(), this.id, componentId);
    }

    public boolean has(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.has(componentId);
    }

    public boolean has(Entity entity) {
        return this.has(entity.id());
    }

    public Entity setName(String name) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment nameSegment = tempArena.allocateFrom(name);
            flecs_h.ecs_set_name(this.world.nativeHandle(), this.id, nameSegment);
        }
        return this;
    }

    public String getName() {
        MemorySegment nameSegment = flecs_h.ecs_get_name(this.world.nativeHandle(), this.id);
        if (nameSegment == null || nameSegment.address() == 0) {
            return null;
        }
        return nameSegment.getString(0);
    }

    public void destruct() {
        flecs_h.ecs_delete(this.world.nativeHandle(), this.id);
    }

    public boolean isValid() {
        return flecs_h.ecs_is_valid(this.world.nativeHandle(), this.id);
    }

    public boolean isAlive() {
        return flecs_h.ecs_is_alive(this.world.nativeHandle(), this.id);
    }

    public void clear() {
        flecs_h.ecs_clear(this.world.nativeHandle(), this.id);
    }

    public Entity addRelation(long relation, long target) {
        long pair = flecs_h.ecs_make_pair(relation, target);
        return this.add(pair);
    }

    public Entity childOf(long parentId) {
        return this.addRelation(FlecsConstants.EcsChildOf, parentId);
    }

    public Entity childOf(Entity parent) {
        return this.childOf(parent.id());
    }

    public Entity parent() {
        long parentId = this.target(FlecsConstants.EcsChildOf, 0);
        if (parentId != 0) {
            return new Entity(this.world, parentId);
        }
        return null;
    }

    public Entity isA(long entityId) {
        return this.addRelation(FlecsConstants.EcsIsA, entityId);
    }

    public Entity removeRelation(long relation, long target) {
        long pair = flecs_h.ecs_make_pair(relation, target);
        return this.remove(pair);
    }

    public boolean hasRelation(long relation, long target) {
        long pair = flecs_h.ecs_make_pair(relation, target);
        return this.has(pair);
    }

    public Entity removeRelation(long relation) {
        return this.removeRelation(relation, FlecsConstants.EcsWildcard);
    }

    public boolean hasRelation(long relation) {
        return this.hasRelation(relation, FlecsConstants.EcsWildcard);
    }

    @SuppressWarnings("unchecked")
    public <T> Entity set(T data) {
        Class<T> componentClass = (Class<T>) data.getClass();
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);

        MemorySegment dataSegment = this.world.getComponentBuffer(component.size());
        component.write(dataSegment, 0, data);
        flecs_h.ecs_set_id(this.world.nativeHandle(), this.id, componentId, component.size(), dataSegment);

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentView> Entity set(Class<?> componentClass, Consumer<T> consumer) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        flecs_h.ecs_add_id(this.world.nativeHandle(), this.id, componentId);
        MemorySegment ptr = flecs_h.ecs_get_mut_id(this.world.nativeHandle(), this.id, componentId);

        T view = (T) FlecsContext.CURRENT_CACHE.get().getComponentView(componentClass);
        view.setResource(ptr.address(), 0);
        consumer.accept(view);

        flecs_h.ecs_modified_id(this.world.nativeHandle(), this.id, componentId);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Entity set(T data, long target) {
        Class<T> componentClass = (Class<T>) data.getClass();
        long componentId = this.world.componentRegistry().getComponentId(componentClass);

        long pairId = flecs_h.ecs_make_pair(componentId, target);

        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        MemorySegment dataSegment = this.world.getComponentBuffer(component.size());
        component.write(dataSegment, 0, data);

        flecs_h.ecs_set_id(this.world.nativeHandle(), this.id, pairId, component.size(), dataSegment);

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentView> Entity set(Class<?> componentClass, long target, Consumer<T> consumer) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        long pairId = flecs_h.ecs_make_pair(componentId, target);
        flecs_h.ecs_add_id(this.world.nativeHandle(), this.id, pairId);
        MemorySegment ptr = flecs_h.ecs_get_mut_id(this.world.nativeHandle(), this.id, componentId);

        T view = (T) FlecsContext.CURRENT_CACHE.get().getComponentView(componentClass);
        view.setResource(ptr.address(), 0);
        consumer.accept(view);

        flecs_h.ecs_modified_id(this.world.nativeHandle(), this.id, pairId);
        return this;
    }

    public <T> T get(long componentId) {
        Component<T> component = this.world.componentRegistry().getComponentById(componentId);
        long address = flecs_h.ecs_get_id(this.world.nativeHandle(), this.id, componentId);

        if (address == 0) {
            return null;
        }

        MemorySegment dataSegment = MemorySegment.ofAddress(address).reinterpret(component.size());

        return component.read(dataSegment, 0);
    }

    public <T> T get(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.get(componentId);
    }

    public <T> T get(Class<T> componentClass, long target) {
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        long componentId = this.world.componentRegistry().getComponentId(componentClass);

        long pairId = flecs_h.ecs_make_pair(componentId, target);

        long address = flecs_h.ecs_get_id(this.world.nativeHandle(), this.id, pairId);

        if (address == 0) {
            return null;
        }

        MemorySegment dataSegment = MemorySegment.ofAddress(address).reinterpret(component.size());
        return component.read(dataSegment, 0);
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentView> T getMutView(Class<?> componentClass) {
        ComponentView view = FlecsContext.CURRENT_CACHE.get().getComponentView(componentClass);
        long componentId = this.world.componentRegistry().getComponentId(componentClass);

        long address = flecs_h.ecs_get_id(this.world.nativeHandle(), this.id, componentId);

        if (address == 0) {
            return null;
        }

        view.setResource(address, 0);

        return (T) view;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMutView(Class<?> componentClass, long target) {
        ComponentView view = FlecsContext.CURRENT_CACHE.get().getComponentView(componentClass);

        long componentId = this.world.componentRegistry().getComponentId(componentClass);

        long pairId = flecs_h.ecs_make_pair(componentId, target);

        long address = flecs_h.ecs_get_id(this.world.nativeHandle(), this.id, pairId);

        if (address == 0) {
            return null;
        }

        view.setResource(address, 0);

        return (T) view;
    }

    public void enable() {
        flecs_h.ecs_enable(this.world.nativeHandle(), this.id, true);
    }

    public void disable() {
        flecs_h.ecs_enable(this.world.nativeHandle(), this.id, false);
    }

    public FlecsObserver observe(long eventId, Runnable callback) {
        return this.world.observer()
                .event(eventId)
                .with(FlecsConstants.EcsAny)
                .each((entityId) -> {
                    if (entityId == this.id) {
                        callback.run();
                    }
                });
    }

    public <T> FlecsObserver observe(Class<T> eventClass, java.util.function.Consumer<T> callback) {
        long eventId = this.world.componentRegistry().getComponentId(eventClass);
        return this.world.observer()
                .event(eventId)
                .with(FlecsConstants.EcsAny)
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
            MemorySegment eventDesc = ecs_event_desc_t.allocate(tempArena);

            ecs_event_desc_t.event(eventDesc, eventId);
            ecs_event_desc_t.entity(eventDesc, this.id);

            flecs_h.ecs_emit(this.world.nativeHandle(), eventDesc);
        }
    }

    public void emit(long eventId, long componentId) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment eventDesc = ecs_event_desc_t.allocate(tempArena);

            MemorySegment typeSegment = ecs_type_t.allocate(tempArena);
            MemorySegment idsArray = tempArena.allocate(ValueLayout.JAVA_LONG);
            idsArray.set(ValueLayout.JAVA_LONG, 0, componentId);

            ecs_type_t.array(typeSegment, idsArray);
            ecs_type_t.count(typeSegment, 1);

            ecs_event_desc_t.event(eventDesc, eventId);
            ecs_event_desc_t.entity(eventDesc, this.id);
            ecs_event_desc_t.ids(eventDesc, typeSegment);

            flecs_h.ecs_emit(this.world.nativeHandle(), eventDesc);
        }
    }

    public <T> void emit(long eventId, Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        this.emit(eventId, componentId);
    }

    public long target(long relationId) {
        return this.target(relationId, 0);
    }

    public long target(long relationId, int index) {
        return flecs_h.ecs_get_target(this.world.nativeHandle(), this.id, relationId, index);
    }

    public int depth(long relationId) {
        return flecs_h.ecs_get_depth(this.world.nativeHandle(), this.id, relationId);
    }

    public boolean owns(long componentId) {
        return flecs_h.ecs_owns_id(this.world.nativeHandle(), this.id, componentId);
    }

    public boolean owns(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.owns(componentId);
    }

    public boolean enabled(long componentId) {
        return flecs_h.ecs_is_enabled_id(this.world.nativeHandle(), this.id, componentId);
    }

    public <T> boolean enabled(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.enabled(componentId);
    }

    public Entity enable(long componentId) {
        flecs_h.ecs_enable_id(this.world.nativeHandle(), this.id, componentId, true);
        return this;
    }

    public <T> Entity enable(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.enable(componentId);
    }

    public Entity disable(long componentId) {
        flecs_h.ecs_enable_id(this.world.nativeHandle(), this.id, componentId, false);
        return this;
    }

    public <T> Entity disable(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.disable(componentId);
    }

    public Entity clone(boolean cloneValues) {
        long cloneId = flecs_h.ecs_clone(this.world.nativeHandle(), 0, this.id, cloneValues);
        return new Entity(this.world, cloneId);
    }

    public Entity lookup(String path) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment pathSegment = tempArena.allocateFrom(path);
            long childId = flecs_h.ecs_lookup_child(this.world.nativeHandle(), this.id, pathSegment);
            if (childId == 0) {
                return null;
            }
            return new Entity(this.world, childId);
        }
    }

    public void children(Consumer<Entity> callback) {
        this.children(FlecsConstants.EcsChildOf, callback);
    }

    public void children(long relationId, Consumer<Entity> callback) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment iter = flecs_h.ecs_children(tempArena, this.world.nativeHandle(), this.id);

            while (flecs_h.ecs_children_next(iter)) {
                int count = ecs_iter_t.count(iter);
                MemorySegment entities = ecs_iter_t.entities(iter);

                for (int i = 0; i < count; i++) {
                    long entityId = entities.getAtIndex(ValueLayout.JAVA_LONG, i);
                    callback.accept(new Entity(this.world, entityId));
                }
            }
        }
    }

    public Table table() {
        MemorySegment tablePtr = flecs_h.ecs_get_table(this.world.nativeHandle(), this.id);
        if (tablePtr == null || tablePtr.address() == 0) {
            return null;
        }
        return new Table(this.world, tablePtr);
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
        String name = this.getName();
        if (name != null) {
            return String.format("Entity[%d, \"%s\"]", this.id, name);
        }
        return String.format("Entity[%d]", this.id);
    }
}