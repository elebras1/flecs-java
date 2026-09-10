package io.github.elebras1.flecs;

import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class FlecsSystem extends Entity {

    FlecsSystem(World world, long entityId) {
        super(world, entityId);
    }

    public void run() {
        flecs_h.ecs_run(this.world.worldSeg(), this.id, 0.0f, MemorySegment.NULL);
    }

    public void run(float deltaTime) {
        flecs_h.ecs_run(this.world.worldSeg(), this.id, deltaTime, MemorySegment.NULL);
    }

    public <T> void run(T param) {
        long paramId = ParamRegistry.put(param);
        try {
            flecs_h.ecs_run(this.world.worldSeg(), this.id, 0.0f, MemorySegment.ofAddress(paramId));
        } finally {
            ParamRegistry.remove(paramId);
        }
    }

    public <T> void run(float deltaTime, T param) {
        long paramId = ParamRegistry.put(param);
        try {
            flecs_h.ecs_run(this.world.worldSeg(), this.id, deltaTime, MemorySegment.ofAddress(paramId));
        } finally {
            ParamRegistry.remove(paramId);
        }
    }

    public boolean isEnabled() {
        return !this.has(Flecs.Disabled);
    }

    public Object getCtx() {
        MemorySegment sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), this.id);
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
        MemorySegment sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), this.id);
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
            flecs_h.ecs_system_update(this.world.worldSeg(), this.id, desc);
        }
    }

    public void setGroup(long groupId) {
        flecs_h.ecs_system_set_group(this.world.worldSeg(), this.id, groupId);
    }
}
