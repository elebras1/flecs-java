package io.github.elebras1.flecs;

import io.github.elebras1.flecs.util.Flecs;

import java.lang.foreign.MemorySegment;

public class FlecsSystem {

    private final World world;
    private final Entity entity;

    FlecsSystem(World world, long entityId) {
        this.world = world;
        this.entity = world.obtainEntity(entityId);
    }

    public void run() {
        flecs_h.ecs_run(this.world.worldSeg(), this.entity.id(), 0.0f, MemorySegment.NULL);
    }

    public void run(float deltaTime) {
        flecs_h.ecs_run(this.world.worldSeg(), this.entity.id(), deltaTime, MemorySegment.NULL);
    }

    public long id() {
        return this.entity.id();
    }

    public Entity entity() {
        return this.entity;
    }

    public void enable() {
        this.entity.remove(Flecs.Disabled);
    }

    public void disable() {
        this.entity.add(Flecs.Disabled);
    }

    public boolean isEnabled() {
        return !this.entity.has(Flecs.Disabled);
    }
}

