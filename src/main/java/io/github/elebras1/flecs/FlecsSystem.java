package io.github.elebras1.flecs;

import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
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

    public <T> void run(T param) {
        long id = ParamRegistry.put(param);
        try {
            flecs_h.ecs_run(this.world.worldSeg(), this.entity.id(), 0.0f, MemorySegment.ofAddress(id));
        } finally {
            ParamRegistry.remove(id);
        }
    }

    public <T> void run(float deltaTime, T param) {
        long id = ParamRegistry.put(param);
        try {
            flecs_h.ecs_run(this.world.worldSeg(), this.entity.id(), deltaTime, MemorySegment.ofAddress(id));
        } finally {
            ParamRegistry.remove(id);
        }
    }

    public long id() {
        return this.entity.id();
    }

    public Entity entity() {
        return this.entity;
    }

    public void enable() {
        flecs_h.ecs_enable(this.world.worldSeg(), this.entity.id(), true);
    }

    public void disable() {
        flecs_h.ecs_enable(this.world.worldSeg(), this.entity.id(), false);
    }

    public boolean isEnabled() {
        return !this.entity.has(Flecs.Disabled);
    }

    public Object getCtx() {
        MemorySegment sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), this.entity.id());
        if (sysSeg == null || sysSeg.address() == 0) {
            return null;
        }
        MemorySegment ctxSeg = ecs_system_t.ctx(sysSeg);
        if (ctxSeg == null || ctxSeg.address() == 0) {
            return null;
        }
        return ParamRegistry.get(ctxSeg.address());
    }

    public void setCtx(Object ctx) {
        MemorySegment sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), this.entity.id());
        if (sysSeg != null && sysSeg.address() != 0) {
            MemorySegment oldCtx = ecs_system_t.ctx(sysSeg);
            if (oldCtx != null && oldCtx.address() != 0) {
                ParamRegistry.remove(oldCtx.address());
                this.world.untrackCtx(oldCtx.address());
            }
        }

        MemorySegment ctxPtr = MemorySegment.NULL;
        if (ctx != null) {
            long id = ParamRegistry.put(ctx);
            ctxPtr = MemorySegment.ofAddress(id);
            this.world.trackCtx(id);
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment desc = ecs_system_desc_t.allocate(arena);
            ecs_system_desc_t.ctx(desc, ctxPtr);
            flecs_h.ecs_system_update(this.world.worldSeg(), this.entity.id(), desc);
        }
    }

    public void setGroup(long groupId) {
        flecs_h.ecs_system_set_group(this.world.worldSeg(), this.entity.id(), groupId);
    }
}

