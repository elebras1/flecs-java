package com.github.elebras1.flecs;

import com.github.elebras1.flecs.util.FlecsConstants;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class FlecsSystem implements AutoCloseable {

    private final World world;
    private final long entityId;
    private final Entity entity;
    private final Arena arena;
    private boolean closed = false;

    FlecsSystem(World world, long entityId) {
        this.world = world;
        this.entityId = entityId;
        this.entity = world.obtainEntity(entityId);
        this.arena = Arena.ofConfined();
    }

    public void run() {
        this.checkClosed();
        flecs_h.ecs_run(this.world.nativeHandle(), this.entityId, 0.0f, MemorySegment.NULL);
    }

    public void run(float deltaTime) {
        this.checkClosed();
        flecs_h.ecs_run(this.world.nativeHandle(), this.entityId, deltaTime, MemorySegment.NULL);
    }

    public long id() {
        this.checkClosed();
        return this.entityId;
    }

    public Entity entity() {
        this.checkClosed();
        return this.entity;
    }

    public void enable() {
        this.checkClosed();
        this.entity.remove(FlecsConstants.EcsDisabled);
    }

    public void disable() {
        this.checkClosed();
        this.entity.add(FlecsConstants.EcsDisabled);
    }

    public boolean isEnabled() {
        this.checkClosed();
        return !this.entity.has(FlecsConstants.EcsDisabled);
    }

    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("System has been closed");
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.arena.close();
            this.closed = true;
        }
    }
}

