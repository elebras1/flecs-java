package io.github.elebras1.flecs;

import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

/**
 * An observer is a callback that is invoked when one or more events match the
 * observer query. Observers are entities (mirrors {@code flecs::observer : entity}),
 * so an observer inherits the regular entity API (enable, disable, destruct, ...).
 */
public class Observer extends Entity {

    Observer(World world, long observerId) {
        super(world, observerId);
    }

    @Override
    public void destruct() {
        this.removeCtxEntry();
        super.destruct();
    }

    private void removeCtxEntry() {
        MemorySegment obsSeg = flecs_h.ecs_observer_get(this.world.worldSeg(), this.id);
        if (obsSeg == null || obsSeg.address() == 0) {
            return;
        }
        MemorySegment ctxSeg = ecs_observer_t.ctx(obsSeg);
        if (ctxSeg != null && ctxSeg.address() != 0) {
            ParamRegistry.remove(ctxSeg.address());
            this.world.untrackCtx(ctxSeg.address());
        }
    }

    public Object getCtx() {
        MemorySegment obsSeg = flecs_h.ecs_observer_get(this.world.worldSeg(), this.id);
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
            flecs_h.ecs_observer_update(this.world.worldSeg(), this.id, desc);
        }
    }
}
