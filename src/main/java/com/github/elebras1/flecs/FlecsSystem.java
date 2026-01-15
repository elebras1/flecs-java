package com.github.elebras1.flecs;

import com.github.elebras1.flecs.util.FlecsConstants;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class FlecsSystem {

    private final World world;
    private final long entityId;
    private final Entity entity;

    FlecsSystem(World world, long entityId) {
        this.world = world;
        this.entityId = entityId;
        this.entity = world.obtainEntity(entityId);
    }

    public void run() {
        flecs_h.ecs_run(this.world.nativeHandle(), this.entityId, 0.0f, MemorySegment.NULL);
    }

    public void run(float deltaTime) {
        flecs_h.ecs_run(this.world.nativeHandle(), this.entityId, deltaTime, MemorySegment.NULL);
    }

    public long id() {
        return this.entityId;
    }

    public Entity entity() {
        return this.entity;
    }

    public void enable() {
        this.entity.remove(FlecsConstants.EcsDisabled);
    }

    public void disable() {
        this.entity.add(FlecsConstants.EcsDisabled);
    }

    public boolean isEnabled() {
        return !this.entity.has(FlecsConstants.EcsDisabled);
    }
}

