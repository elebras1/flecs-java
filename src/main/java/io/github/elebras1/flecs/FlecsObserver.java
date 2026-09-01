package io.github.elebras1.flecs;

import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class FlecsObserver {

    private final World world;
    private final long observerId;

    FlecsObserver(World world, long observerId) {
        this.world = world;
        this.observerId = observerId;
    }

    public long id() {
        return this.observerId;
    }

    public void enable() {
        flecs_h.ecs_enable(this.world.worldSeg(), this.observerId, true);
    }

    public void disable() {
        flecs_h.ecs_enable(this.world.worldSeg(), this.observerId, false);
    }

    public void destruct() {
        this.removeCtxEntry();
        flecs_h.ecs_delete(this.world.worldSeg(), this.observerId);
    }

    private void removeCtxEntry() {
        MemorySegment obsSeg = flecs_h.ecs_observer_get(this.world.worldSeg(), this.observerId);
        if (obsSeg == null || obsSeg.address() == 0) {
            return;
        }
        MemorySegment ctxSeg = ecs_observer_t.ctx(obsSeg);
        if (ctxSeg != null && ctxSeg.address() != 0) {
            ParamRegistry.remove(ctxSeg.address());
            this.world.untrackCtx(ctxSeg.address());
        }
    }

    public boolean isValid() {
        return flecs_h.ecs_is_valid(this.world.worldSeg(), this.observerId);
    }

    public boolean isAlive() {
        return flecs_h.ecs_is_alive(this.world.worldSeg(), this.observerId);
    }

    public Object getCtx() {
        MemorySegment obsSeg = flecs_h.ecs_observer_get(this.world.worldSeg(), this.observerId);
        if (obsSeg == null || obsSeg.address() == 0) {
            return null;
        }
        MemorySegment ctxSeg = ecs_observer_t.ctx(obsSeg);
        if (ctxSeg == null || ctxSeg.address() == 0) {
            return null;
        }
        return ParamRegistry.get(ctxSeg.address());
    }

    public void setCtx(Object ctx) {
        this.removeCtxEntry();

        MemorySegment ctxPtr = MemorySegment.NULL;
        if (ctx != null) {
            long id = ParamRegistry.put(ctx);
            ctxPtr = MemorySegment.ofAddress(id);
            this.world.trackCtx(id);
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment desc = ecs_observer_desc_t.allocate(arena);
            ecs_observer_desc_t.ctx(desc, ctxPtr);
            flecs_h.ecs_observer_update(this.world.worldSeg(), this.observerId, desc);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FlecsObserver other)) {
            return false;
        }
        return this.observerId == other.observerId && this.world == other.world;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(this.observerId);
    }
    
    @Override
    public String toString() {
        return String.format("Observer[%d]", this.observerId);
    }
}