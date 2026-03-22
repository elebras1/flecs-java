package com.github.elebras1.flecs;

import com.github.elebras1.flecs.callback.EntityCallback;
import com.github.elebras1.flecs.callback.IterCallback;
import com.github.elebras1.flecs.callback.RunCallback;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Query extends QueryBase implements AutoCloseable {

    private final Arena arena;
    private final Iter iter;
    private boolean closed;

    Query(World world, MemorySegment querySeg) {
        super(world, querySeg);
        this.arena = Arena.ofShared();
        this.iter = new Iter(MemorySegment.NULL, this.world);
        this.closed = false;
    }

    private MemorySegment createIterSeg() {
        return flecs_h.ecs_query_iter(this.arena, this.world.worldSeg(), this.querySeg);
    }

    public void each(EntityCallback callback) {
        this.checkClosed();
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
        this.checkClosed();
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
        this.checkClosed();
        MemorySegment iterSeg = this.createIterSeg();

        if (iterSeg.address() == 0) {
            throw new IllegalStateException("ecs_query_iter returned a null iterator");
        }
        this.iter.setIterSeg(iterSeg);
        this.world.viewCache().resetCursors();
        callback.accept(this.iter);
    }

    public int count() {
        this.checkClosed();
        MemorySegment iterSeg = this.createIterSeg();

        int total = 0;
        while (flecs_h.ecs_iter_next(iterSeg)) {
            total += ecs_iter_t.count(iterSeg);
        }

        return total;
    }

    public long[] entities() {
        this.checkClosed();
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

    @Override
    protected void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("The query has already been closed.");
        }
    }

    public String toStringExpr() {
        MemorySegment strSeg = flecs_h.ecs_query_str(this.querySeg);
        if (strSeg.address() == 0) {
            return "Invalid/empty query";
        }
        return strSeg.getString(0);
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            if (this.querySeg != null && this.querySeg.address() != 0) {
                flecs_h.ecs_query_fini(this.querySeg);
            }
            this.arena.close();
        }
    }
}
