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

    public void delete() {
        flecs_h.ecs_delete(this.world.nativeHandle(), this.id);
    }

    public boolean isValid() {
        return flecs_h.ecs_is_valid(this.world.nativeHandle(), this.id);
    }

    public boolean isAlive() {
        return flecs_h.ecs_is_alive(this.world.nativeHandle(), this.id);
    }

    public Entity addRelation(long relation, long target) {
        long pair = flecs_h.ecs_make_pair(relation, target);
        return this.add(pair);
    }

    public Entity addRelation(Entity relation, Entity target) {
        return addRelation(relation.id(), target.id());
    }

    public Entity removeRelation(long relation, long target) {
        long pair = flecs_h.ecs_make_pair(relation, target);
        return this.remove(pair);
    }

    public Entity removeRelation(Entity relation, Entity target) {
        return removeRelation(relation.id(), target.id());
    }

    public <T> Entity set(long componentId, Component<T> component, T data) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment dataSegment = tempArena.allocate(component.layout());

            component.write(dataSegment, data);

            flecs_h.ecs_set_id(this.world.nativeHandle(), this.id, componentId,
                component.size(), dataSegment);
        }
        return this;
    }

    public <T> T get(long componentId, Component<T> component) {
        MemorySegment dataPtr = flecs_h.ecs_get_id(this.world.nativeHandle(), this.id, componentId);

        if (dataPtr == null || dataPtr.address() == 0) {
            return null;
        }

        MemorySegment dataSegment = dataPtr.reinterpret(component.size());

        return component.read(dataSegment);
    }

    public <T> T getMut(long componentId, Component<T> component) {
        MemorySegment dataPtr = flecs_h.ecs_get_mut_id(this.world.nativeHandle(), this.id, componentId);

        if (dataPtr == null || dataPtr.address() == 0) {
            return null;
        }

        MemorySegment dataSegment = dataPtr.reinterpret(component.size());

        return component.read(dataSegment);
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

