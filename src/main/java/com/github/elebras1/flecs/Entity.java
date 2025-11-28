package com.github.elebras1.flecs;

import com.github.elebras1.flecs.generated.flecs_h;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class Entity {
    
    private final Flecs world;
    private final long id;

    Entity(Flecs world, long id) {
        this.world = world;
        this.id = id;
    }

    public long id() {
        return id;
    }

    public Flecs world() {
        return world;
    }

    public Entity add(long componentId) {
        flecs_h.ecs_add_id(this.world.nativeHandle(), this.id, componentId);
        return this;
    }

    public Entity add(Entity component) {
        return this.add(component.id());
    }

    public Entity remove(long componentId) {
        flecs_h.ecs_remove_id(world.nativeHandle(), this.id, componentId);
        return this;
    }

    public Entity remove(Entity component) {
        return this.remove(component.id());
    }

    public Entity remove(Class<? extends EcsComponent> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.remove(componentId);
    }

    public boolean has(long componentId) {
        return flecs_h.ecs_has_id(this.world.nativeHandle(), this.id, componentId);
    }

    public boolean has(Entity component) {
        return this.has(component.id());
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

    private Entity addRelation(long relation, long target) {
        long pair = flecs_h.ecs_make_pair(relation, target);
        return this.add(pair);
    }

    public Entity childOf(long parentId) {
        return this.addRelation(FlecsConstants.EcsChildOf, parentId);
    }

    public Entity childOf(Entity parent) {
        return this.childOf(parent.id());
    }

    public Entity isA(long entityId) {
        return this.addRelation(FlecsConstants.EcsIsA, entityId);
    }

    public Entity removeRelation(long relation, long target) {
        long pair = flecs_h.ecs_make_pair(relation, target);
        return this.remove(pair);
    }

    public <T extends EcsComponent<T>> Entity set(T data) {
        Class<? extends EcsComponent> componentClass = data.getClass();
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        Component<T> component = this.world.componentRegistry().getComponent(componentClass);
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment dataSegment = tempArena.allocate(component.layout());

            component.write(dataSegment, data);

            flecs_h.ecs_set_id(this.world.nativeHandle(), this.id, componentId, component.size(), dataSegment);
        }
        return this;
    }

    public <T extends EcsComponent<T>> T get(long componentId) {
        Component<T> component = this.world.componentRegistry().getComponentById(componentId);
        MemorySegment dataPtr = flecs_h.ecs_get_id(this.world.nativeHandle(), this.id, componentId);

        if (dataPtr == null || dataPtr.address() == 0) {
            return null;
        }

        MemorySegment dataSegment = dataPtr.reinterpret(component.size());

        return component.read(dataSegment);
    }

    public <T extends EcsComponent<T>> T get(Class<? extends EcsComponent> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.get(componentId);
    }

    public <T extends EcsComponent<T>> T getMut(EcsComponent<T> component) {
        long componentId = this.world.componentRegistry().getComponentId(component.getClass());
        MemorySegment dataPtr = flecs_h.ecs_get_mut_id(this.world.nativeHandle(), this.id, componentId);

        if (dataPtr == null || dataPtr.address() == 0) {
            return null;
        }

        MemorySegment dataSegment = dataPtr.reinterpret(component.size());

        return component.read(dataSegment);
    }

    public void enable() {
        flecs_h.ecs_enable(this.world.nativeHandle(), this.id, true);
    }

    public void disable() {
        flecs_h.ecs_enable(this.world.nativeHandle(), this.id, false);
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

