package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.EntityCallback;
import io.github.elebras1.flecs.callback.IterCallback;
import io.github.elebras1.flecs.callback.RunCallback;
import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Query extends QueryBase {

    private final Arena arena;
    private final Iter iter;
    private boolean destroyed;

    Query(World world, MemorySegment querySeg) {
        super(world, querySeg);
        this.arena = Arena.ofShared();
        this.iter = new Iter(MemorySegment.NULL, this.world);
        this.destroyed = false;
    }

    private MemorySegment createIterSeg() {
        return flecs_h.ecs_query_iter(this.arena, this.world.worldSeg(), this.querySeg);
    }

    public void each(EntityCallback callback) {
        this.checkDestroyed();
        MemorySegment iterSeg = this.createIterSeg();
        if (iterSeg.address() == 0) {
            throw new IllegalStateException("ecs_query_iter returned a null iterator");
        }

        while (flecs_h.ecs_iter_next(iterSeg)) {
            int count = ecs_iter_t.count(iterSeg);
            MemorySegment entities = ecs_iter_t.entities(iterSeg);

            for (int i = 0; i < count; i++) {
                long entityId = entities.getAtIndex(ValueLayout.JAVA_LONG, i);
                callback.accept(entityId);
            }
        }
    }

    public void iter(IterCallback callback) {
        this.checkDestroyed();
        MemorySegment iterSeg = this.createIterSeg();
        if (iterSeg.address() == 0) {
            throw new IllegalStateException("ecs_query_iter returned a null iterator");
        }

        this.world.viewCache().resetCursors();
        while (flecs_h.ecs_iter_next(iterSeg)) {
            this.iter.setIterSeg(iterSeg);
            callback.accept(this.iter);
        }
    }

    public void run(RunCallback callback) {
        this.checkDestroyed();
        MemorySegment iterSeg = this.createIterSeg();

        if (iterSeg.address() == 0) {
            throw new IllegalStateException("ecs_query_iter returned a null iterator");
        }
        this.iter.setIterSeg(iterSeg);
        this.world.viewCache().resetCursors();
        callback.accept(this.iter);
    }

    public boolean changed() {
        this.checkDestroyed();
        return flecs_h.ecs_query_changed(this.querySeg);
    }

    public int findVar(String name) {
        this.checkDestroyed();
        try (Arena tempArena = Arena.ofConfined()) {
            return flecs_h.ecs_query_find_var(this.querySeg, tempArena.allocateFrom(name));
        }
    }

    public int count() {
        this.checkDestroyed();
        MemorySegment iterSeg = this.createIterSeg();

        int total = 0;
        while (flecs_h.ecs_iter_next(iterSeg)) {
            total += ecs_iter_t.count(iterSeg);
        }

        return total;
    }

    public long[] entities() {
        this.checkDestroyed();
        int[] index = {0};
        long[] result = new long[this.count()];
        MemorySegment iterSeg = this.createIterSeg();
        while (flecs_h.ecs_iter_next(iterSeg)) {
            int count = ecs_iter_t.count(iterSeg);
            MemorySegment entities = ecs_iter_t.entities(iterSeg);
            for (int i = 0; i < count; i++) {
                result[index[0]++] = entities.getAtIndex(ValueLayout.JAVA_LONG, i);
            }
        }
        return result;
    }

    public long first() {
        this.checkDestroyed();
        long[] result = { 0L };
        MemorySegment iterSeg = this.createIterSeg();
        while (flecs_h.ecs_iter_next(iterSeg)) {
            int count = ecs_iter_t.count(iterSeg);
            MemorySegment entities = ecs_iter_t.entities(iterSeg);
            if (count > 0) {
                result[0] = entities.getAtIndex(ValueLayout.JAVA_LONG, 0);
                break;
            }
        }
        return result[0];
    }

    @Override
    protected void checkDestroyed() {
        if (this.destroyed) {
            throw new IllegalStateException("The query has already been destroyed.");
        }
    }

    public String toStringExpr() {
        MemorySegment strSeg = flecs_h.ecs_query_str(this.querySeg);
        if (strSeg.address() == 0) {
            return "Invalid/empty query";
        }
        return strSeg.getString(0);
    }

    public String toJson() {
        this.checkDestroyed();
        MemorySegment iterSeg = this.createIterSeg();
        MemorySegment jsonSeg = flecs_h.ecs_iter_to_json(iterSeg, MemorySegment.NULL);
        if (jsonSeg.address() == 0) {
            return null;
        }
        return jsonSeg.getString(0);
    }

    public Object getCtx() {
        this.checkDestroyed();
        MemorySegment ctxSeg = ecs_query_t.ctx(this.querySeg);
        if (ctxSeg == null || ctxSeg.address() == 0) {
            return null;
        }
        return ParamRegistry.get(ctxSeg.address());
    }

    public void destroy() {
        if (!this.destroyed) {
            this.destroyed = true;
            if (this.querySeg != null && this.querySeg.address() != 0) {
                MemorySegment ctxSeg = ecs_query_t.ctx(this.querySeg);
                if (ctxSeg != null && ctxSeg.address() != 0) {
                    ParamRegistry.remove(ctxSeg.address());
                    this.world.untrackCtx(ctxSeg.address());
                }
                flecs_h.ecs_query_fini(this.querySeg);
            }
            this.arena.close();
        }
    }
}
