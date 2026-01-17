package com.github.elebras1.flecs;

import com.github.elebras1.flecs.collection.LongList;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Query implements AutoCloseable {

    private final World world;
    private final MemorySegment nativeQuery;
    private final Arena arena;
    private boolean closed = false;

    Query(World world, MemorySegment nativeQuery) {
        this.world = world;
        this.nativeQuery = nativeQuery;
        this.arena = Arena.ofConfined();
    }

    @FunctionalInterface
    public interface EntityCallback {
        void accept(long entityId);
    }

    @FunctionalInterface
    public interface IterCallback {
        void accept(Iter iter);
    }

    @FunctionalInterface
    public interface RunCallback {
        void accept(Iter iter);
    }

    public void each(EntityCallback callback) {
        this.checkClosed();
        try (Arena tmpArena = Arena.ofConfined()) {
            var cache = new FlecsContext.ViewCache();
            ScopedValue.where(FlecsContext.CURRENT_CACHE, cache).run(() -> {
                MemorySegment iter = flecs_h.ecs_query_iter(tmpArena, this.world.nativeHandle(), this.nativeQuery);
                if (iter == null || iter.address() == 0) {
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
            });
        }
    }

    public void iter(IterCallback callback) {
        this.checkClosed();
        try (Arena tmpArena = Arena.ofConfined()) {
            var cache = new FlecsContext.ViewCache();
            ScopedValue.where(FlecsContext.CURRENT_CACHE, cache).run(() -> {
                MemorySegment iter = flecs_h.ecs_query_iter(tmpArena, this.world.nativeHandle(), this.nativeQuery);
                if (iter == null || iter.address() == 0) {
                    throw new IllegalStateException("ecs_query_iter returned a null iterator");
                }

                Iter it = new Iter(iter, this.world);

                while (flecs_h.ecs_iter_next(iter)) {
                    it.setNativeIter(iter);
                    callback.accept(it);
                }
            });
        }
    }

    public void run(RunCallback callback) {
        this.checkClosed();
        try (Arena tmpArena = Arena.ofConfined()) {
            var cache = new FlecsContext.ViewCache();
            ScopedValue.where(FlecsContext.CURRENT_CACHE, cache).run(() -> {
                MemorySegment iter = flecs_h.ecs_query_iter(tmpArena, this.world.nativeHandle(), this.nativeQuery);

                if (iter == null || iter.address() == 0) {
                    throw new IllegalStateException("ecs_query_iter returned a null iterator");
                }

                Iter it = new Iter(iter, this.world);
                callback.accept(it);
            });
        }
    }

    public int count() {
        this.checkClosed();
        try (Arena tmpArena = Arena.ofConfined()) {
            MemorySegment iter = flecs_h.ecs_query_iter(tmpArena, this.world.nativeHandle(), this.nativeQuery);

            int total = 0;
            while (flecs_h.ecs_iter_next(iter)) {
                total += ecs_iter_t.count(iter);
            }

            return total;
        }
    }

    public LongList entities() {
        this.checkClosed();
        LongList result = new LongList();
        this.each(result::add);
        return result;
    }

    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("The query has already been closed.");
        }
    }

    public String toStringExpr() {
        MemorySegment strPtr = flecs_h.ecs_query_str(this.nativeQuery);
        if (strPtr.address() == 0) {
            return "Invalid/empty query";
        }
        return strPtr.getString(0);
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            if (this.nativeQuery != null && this.nativeQuery.address() != 0) {
                flecs_h.ecs_query_fini(this.nativeQuery);
            }
            this.arena.close();
        }
    }
}

