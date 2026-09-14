package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class PipelineBuilder {

    private final World world;
    private final Arena arena;
    private final MemorySegment desc;
    private int termCount = 0;

    public PipelineBuilder(World world) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_pipeline_desc_t.allocate(this.arena);
    }

    public PipelineBuilder(World world, String name) {
        this(world);
        MemorySegment nameSegment = this.arena.allocateFrom(name);

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment entityDescTemp = ecs_entity_desc_t.allocate(tempArena);
            ecs_entity_desc_t.name(entityDescTemp, nameSegment);
            ecs_pipeline_desc_t.entity(this.desc, flecs_h.ecs_entity_init(world.worldSeg(), entityDescTemp));
        }
    }

    public PipelineBuilder with(long componentId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        ecs_term_t.id(this.term(this.termCount), componentId);
        this.termCount++;
        return this;
    }

    public PipelineBuilder with(Entity entity) {
        return this.with(entity.id());
    }

    public <T> PipelineBuilder with(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(componentId);
    }

    public PipelineBuilder without(long componentId) {
        return this.with(componentId).not();
    }

    public PipelineBuilder without(Entity entity) {
        return this.with(entity).not();
    }

    public <T> PipelineBuilder without(Class<T> componentClass) {
        return this.with(componentClass).not();
    }

    public PipelineBuilder not() {
        this.checkTerm();
        ecs_term_t.oper(this.term(this.termCount - 1), (short) Flecs.Not);
        return this;
    }

    public PipelineBuilder cascade() {
        this.checkTerm();
        MemorySegment termSeg = this.term(this.termCount - 1);
        MemorySegment srcRefSeg = ecs_term_t.src(termSeg);
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | Flecs.Cascade);
        return this;
    }

    public PipelineBuilder cascade(long trav) {
        this.cascade();
        ecs_term_t.trav(this.term(this.termCount - 1), trav);
        return this;
    }

    public PipelineBuilder cascade(Entity trav) {
        return this.cascade(trav.id());
    }

    public <T> PipelineBuilder cascade(Class<T> trav) {
        long travId = this.world.componentRegistry().getComponentId(trav);
        return this.cascade(travId);
    }

    public PipelineBuilder up() {
        this.checkTerm();
        MemorySegment termSeg = this.term(this.termCount - 1);
        MemorySegment srcRefSeg = ecs_term_t.src(termSeg);
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsUp());
        return this;
    }

    public PipelineBuilder up(long trav) {
        this.up();
        ecs_term_t.trav(this.term(this.termCount - 1), trav);
        return this;
    }

    public PipelineBuilder up(Entity trav) {
        return this.up(trav.id());
    }

    public <T> PipelineBuilder up(Class<T> trav) {
        long travId = this.world.componentRegistry().getComponentId(trav);
        return this.up(travId);
    }

    public PipelineBuilder expr(String expr) {
        MemorySegment exprSegment = this.arena.allocateFrom(expr);
        MemorySegment queryDesc = ecs_pipeline_desc_t.query(this.desc);
        ecs_query_desc_t.expr(queryDesc, exprSegment);
        return this;
    }

    public PipelineBuilder queryFlags(int flag) {
        MemorySegment queryDesc = ecs_pipeline_desc_t.query(this.desc);
        ecs_query_desc_t.flags(queryDesc, ecs_query_desc_t.flags(queryDesc) | flag);
        return this;
    }

    public PipelineBuilder cached() {
        ecs_query_desc_t.cache_kind(ecs_pipeline_desc_t.query(this.desc), Flecs.QueryCacheAuto);
        return this;
    }

    public PipelineBuilder detectChanges() {
        return this.queryFlags(Flecs.QueryDetectChanges);
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

    private MemorySegment term(int index) {
        return ecs_query_desc_t.terms(ecs_pipeline_desc_t.query(this.desc), index);
    }

    private void checkTerm() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to configure");
        }
    }
}
