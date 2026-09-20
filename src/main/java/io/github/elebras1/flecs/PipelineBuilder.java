package io.github.elebras1.flecs;

import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class PipelineBuilder extends QueryTermBuilder<PipelineBuilder> {

    private final World world;
    private final Arena arena;
    private final MemorySegment desc;

    public PipelineBuilder(World world) {
        this(world, Arena.ofConfined());
    }

    public PipelineBuilder(World world, String name) {
        this(world, Arena.ofConfined());
        MemorySegment nameSegment = this.arena.allocateFrom(name);

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment entityDescTemp = ecs_entity_desc_t.allocate(tempArena);
            ecs_entity_desc_t.name(entityDescTemp, nameSegment);
            ecs_pipeline_desc_t.entity(this.desc, flecs_h.ecs_entity_init(world.worldSeg(), entityDescTemp));
        }
    }

    private PipelineBuilder(World world, Arena arena) {
        this(world, arena, ecs_pipeline_desc_t.allocate(arena));
    }

    private PipelineBuilder(World world, Arena arena, MemorySegment desc) {
        super(world, arena, ecs_pipeline_desc_t.query(desc));
        this.world = world;
        this.arena = arena;
        this.desc = desc;
    }

    public PipelineBuilder ctx(Object ctx) {
        long offset = ecs_query_desc_t.ctx$offset();
        MemorySegment queryDesc = ecs_pipeline_desc_t.query(this.desc);
        MemorySegment prev = queryDesc.get(ValueLayout.ADDRESS, offset);
        if (prev != null && prev.address() != 0) {
            ParamRegistry.remove(prev.address());
            this.world.untrackCtx(prev.address());
        }

        if (ctx == null) {
            queryDesc.set(ValueLayout.ADDRESS, offset, MemorySegment.NULL);
            return this;
        }
        long id = ParamRegistry.put(ctx);
        queryDesc.set(ValueLayout.ADDRESS, offset, MemorySegment.ofAddress(id));
        this.world.trackCtx(id);
        return this;
    }

    public Pipeline build() {
        long pipelineId = flecs_h.ecs_pipeline_init(this.world.worldSeg(), this.desc);

        if (pipelineId == 0) {
            this.arena.close();
            throw new IllegalStateException("Failed to create pipeline");
        }

        this.arena.close();
        return new Pipeline(this.world, pipelineId);
    }
}
