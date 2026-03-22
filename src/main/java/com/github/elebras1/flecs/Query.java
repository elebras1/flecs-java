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

    public void each(EntityCallback callback) {
        this.checkClosed();
        MemorySegment iter = flecs_h.ecs_query_iter(this.arena, this.world.worldSeg(), this.querySeg);
        if (iter.address() == 0) {
            throw new IllegalStateException("ecs_query_iter returned a null iterator");
        }

        while (flecs_h.ecs_iter_next(iter)) {
            int count = ecs_iter_t.count(iter);
            MemorySegment entities = ecs_iter_t.entities(iter);

            for (int i = 0; i < count; i++) {
                long entityId = entities.getAtIndex(ValueLayout.JAVA_LONG, i);
                callback.accept(entityId);
            }
        }
    }

    public void iter(IterCallback callback) {
        this.checkClosed();
        MemorySegment iterSegment = flecs_h.ecs_query_iter(this.arena, this.world.worldSeg(), this.querySeg);
        if (iterSegment.address() == 0) {
            throw new IllegalStateException("ecs_query_iter returned a null iterator");
        }

        this.world.viewCache().resetCursors();
        while (flecs_h.ecs_iter_next(iterSegment)) {
            this.iter.setIterSeg(iterSegment);
            callback.accept(this.iter);
        }
    }

    public void run(RunCallback callback) {
        this.checkClosed();
        MemorySegment iterSegment = flecs_h.ecs_query_iter(this.arena, this.world.worldSeg(), this.querySeg);

        if (iterSegment.address() == 0) {
            throw new IllegalStateException("ecs_query_iter returned a null iterator");
        }
        this.iter.setIterSeg(iterSegment);
        this.world.viewCache().resetCursors();
        callback.accept(this.iter);
    }

    public int count() {
        this.checkClosed();
        MemorySegment iter = flecs_h.ecs_query_iter(this.arena, this.world.worldSeg(), this.querySeg);

        int total = 0;
        while (flecs_h.ecs_iter_next(iter)) {
            total += ecs_iter_t.count(iter);
        }

        return total;
    }

    public long[] entities() {
        this.checkClosed();
        int[] index = {0};
        long[] result = new long[this.count()];
        MemorySegment iter = flecs_h.ecs_query_iter(this.arena, this.world.worldSeg(), this.querySeg);
        while (flecs_h.ecs_iter_next(iter)) {
            int count = ecs_iter_t.count(iter);
            MemorySegment entities = ecs_iter_t.entities(iter);
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
