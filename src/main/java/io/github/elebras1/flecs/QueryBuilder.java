package io.github.elebras1.flecs;

import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class QueryBuilder extends QueryTermBuilder<QueryBuilder> {

    private final World world;
    private final Arena arena;
    private final MemorySegment desc;

    public QueryBuilder(World world) {
        this(world, Arena.ofConfined());
    }

    private QueryBuilder(World world, Arena arena) {
        this(world, arena, ecs_query_desc_t.allocate(arena));
    }

    private QueryBuilder(World world, Arena arena, MemorySegment desc) {
        super(world, arena, desc);
        this.world = world;
        this.arena = arena;
        this.desc = desc;
    }

    public QueryBuilder ctx(Object ctx) {
        long offset = ecs_query_desc_t.ctx$offset();
        MemorySegment prev = this.desc.get(ValueLayout.ADDRESS, offset);
        if (prev != null && prev.address() != 0) {
            ParamRegistry.remove(prev.address());
            this.world.untrackCtx(prev.address());
        }

        if (ctx == null) {
            this.desc.set(ValueLayout.ADDRESS, offset, MemorySegment.NULL);
            return this;
        }
        long id = ParamRegistry.put(ctx);
        this.desc.set(ValueLayout.ADDRESS, offset, MemorySegment.ofAddress(id));
        this.world.trackCtx(id);
        return this;
    }

    public Query build() {
        try {
            MemorySegment querySeg = flecs_h.ecs_query_init(this.world.worldSeg(), this.desc);

            if (querySeg.address() == 0) {
                throw new IllegalStateException("Query creation failed.");
            }

            return new Query(this.world, querySeg);
        } finally {
            this.close();
        }
    }

    public void close() {
        this.arena.close();
    }
}
