package com.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class PipelineBuilder {

    private final Flecs world;
    private final Arena arena;
    private final MemorySegment desc;
    private int termCount = 0;
    private static final long TERM_SIZE = ecs_term_t.layout().byteSize();

    public PipelineBuilder(Flecs world) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_pipeline_desc_t.allocate(this.arena);
        this.desc.fill((byte) 0);
    }

    public PipelineBuilder(Flecs world, String name) {
        this(world);
        MemorySegment nameSegment = this.arena.allocateFrom(name);

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment entityDescTemp = ecs_entity_desc_t.allocate(tempArena);
            ecs_entity_desc_t.name(entityDescTemp, nameSegment);
            ecs_pipeline_desc_t.entity(this.desc, flecs_h.ecs_entity_init(world.nativeHandle(), entityDescTemp));
        }
    }

    public PipelineBuilder with(long phaseId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        MemorySegment queryDesc = ecs_pipeline_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();

        if (this.termCount > 0) {
            long prevTermOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);
            MemorySegment prevTerm = queryDesc.asSlice(prevTermOffset, TERM_SIZE);
            ecs_term_t.oper(prevTerm, (short) FlecsConstants.EcsOr);
        }

        long termOffset = termsOffset + (this.termCount * TERM_SIZE);
        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
        ecs_term_t.id(term, phaseId);


        this.termCount++;
        return this;
    }

    public PipelineBuilder with(Entity phase) {
        return this.with(phase.id());
    }

    public PipelineBuilder expr(String expr) {
        MemorySegment exprSegment = this.arena.allocateFrom(expr);
        MemorySegment queryDesc = ecs_pipeline_desc_t.query(this.desc);
        ecs_query_desc_t.expr(queryDesc, exprSegment);
        return this;
    }

    public Pipeline build() {
        long pipelineId = flecs_h.ecs_pipeline_init(this.world.nativeHandle(), this.desc);

        if (pipelineId == 0) {
            this.arena.close();
            throw new IllegalStateException("Failed to create pipeline");
        }

        this.arena.close();
        return new Pipeline(this.world, pipelineId);
    }
}

