package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.EntityCallback;
import io.github.elebras1.flecs.callback.IterCallback;
import io.github.elebras1.flecs.callback.RunCallback;
import io.github.elebras1.flecs.internal.FlecsAllocator;
import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Query extends QueryBase {

    private Iter iter;
    private boolean destructed;

    Query(World world, MemorySegment querySeg) {
        super(world, querySeg);
        this.destructed = false;
        this.world.trackQuery(this);
    }

    private Iter iter() {
        Iter local = this.iter;
        if (local == null) {
            local = new Iter(MemorySegment.NULL, this.world);
            this.iter = local;
        }
        return local;
    }

    private MemorySegment createIterSeg(Arena arena) {
        return flecs_h.ecs_query_iter(arena, this.world.worldSeg(), this.querySeg);
    }

    public void each(EntityCallback callback) {
        this.checkDestroyed();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment iterSeg = this.createIterSeg(arena);
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
    }

    public void iter(IterCallback callback) {
        this.checkDestroyed();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment iterSeg = this.createIterSeg(arena);
            if (iterSeg.address() == 0) {
                throw new IllegalStateException("ecs_query_iter returned a null iterator");
            }

            Iter iter = this.iter();
            this.world.viewCache().resetCursors();
            while (flecs_h.ecs_iter_next(iterSeg)) {
                iter.setIterSeg(iterSeg);
                callback.accept(iter);
            }
        }
    }

    public void run(RunCallback callback) {
        this.checkDestroyed();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment iterSeg = this.createIterSeg(arena);

            if (iterSeg.address() == 0) {
                throw new IllegalStateException("ecs_query_iter returned a null iterator");
            }
            Iter iter = this.iter();
            iter.setIterSeg(iterSeg);
            this.world.viewCache().resetCursors();
            callback.accept(iter);
        }
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
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment iterSeg = this.createIterSeg(arena);

            int total = 0;
            while (flecs_h.ecs_iter_next(iterSeg)) {
                total += ecs_iter_t.count(iterSeg);
            }

            return total;
        }
    }

    public long[] entities() {
        this.checkDestroyed();
        int[] index = {0};
        long[] result = new long[this.count()];
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment iterSeg = this.createIterSeg(arena);
            while (flecs_h.ecs_iter_next(iterSeg)) {
                int count = ecs_iter_t.count(iterSeg);
                MemorySegment entities = ecs_iter_t.entities(iterSeg);
                for (int i = 0; i < count; i++) {
                    result[index[0]++] = entities.getAtIndex(ValueLayout.JAVA_LONG, i);
                }
            }
            return result;
        }
    }

    public long first() {
        this.checkDestroyed();
        long[] result = { 0L };
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment iterSeg = this.createIterSeg(arena);
            while (flecs_h.ecs_iter_next(iterSeg)) {
                int count = ecs_iter_t.count(iterSeg);
                MemorySegment entities = ecs_iter_t.entities(iterSeg);
                if (count > 0) {
                    result[0] = entities.getAtIndex(ValueLayout.JAVA_LONG, 0);
                    flecs_h.ecs_iter_fini(iterSeg);
                    return result[0];
                }
            }
            return result[0];
        }
    }

    @Override
    protected void checkDestroyed() {
        if (this.destructed || this.world.isDestroyed()) {
            throw new IllegalStateException("The query has already been destroyed.");
        }
    }

    public String toStringExpr() {
        this.checkDestroyed();
        MemorySegment strSeg = flecs_h.ecs_query_str(this.querySeg);
        if (strSeg.address() == 0) {
            return "Invalid/empty query";
        }
        String expr = strSeg.getString(0);
        FlecsAllocator.free(strSeg);
        return expr;
    }

    public String toJson() {
        return this.toJson(null);
    }

    public String toJson(IterToJsonDesc desc) {
        this.checkDestroyed();
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment iterSeg = this.createIterSeg(tempArena);
            MemorySegment descSeg = MemorySegment.NULL;
            if (desc != null) {
                desc.query(this.querySeg);
                descSeg = desc.allocate(tempArena);
            }
            MemorySegment jsonSeg = flecs_h.ecs_iter_to_json(iterSeg, descSeg);
            if (jsonSeg.address() == 0) {
                return null;
            }

            String json = jsonSeg.reinterpret(Long.MAX_VALUE).getString(0);
            FlecsAllocator.free(jsonSeg);
            return json;
        }
    }

    public Object getCtx() {
        this.checkDestroyed();
        MemorySegment ctxSeg = ecs_query_t.ctx(this.querySeg);
        if (ctxSeg == null || ctxSeg.address() == 0) {
            return null;
        }
        return ParamRegistry.get(ctxSeg.address());
    }

    public void destruct() {
        if (!this.destructed) {
            this.destructed = true;
            this.world.untrackQuery(this);
            if (!this.world.isDestroyed() && this.querySeg != null && this.querySeg.address() != 0) {
                MemorySegment ctxSeg = ecs_query_t.ctx(this.querySeg);
                if (ctxSeg != null && ctxSeg.address() != 0) {
                    ParamRegistry.remove(ctxSeg.address());
                    this.world.untrackCtx(ctxSeg.address());
                }
                flecs_h.ecs_query_fini(this.querySeg);
            }
        }
    }

    @Override
    public String toString() {
        this.checkDestroyed();
        MemorySegment strSeg = flecs_h.ecs_query_str(this.querySeg);
        String str = strSeg.reinterpret(Long.MAX_VALUE).getString(0);
        FlecsAllocator.free(strSeg);
        return str;
    }
}
