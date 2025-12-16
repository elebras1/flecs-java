package com.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static com.github.elebras1.flecs.FlecsConstants.EcsIn;
import static com.github.elebras1.flecs.FlecsConstants.EcsQueryCacheAuto;

public class QueryBuilder {

    private final World world;
    private final Arena arena;
    private final MemorySegment desc;
    private int termCount = 0;
    private static final long TERM_SIZE = ecs_term_t.layout().byteSize();

    public QueryBuilder(World world) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_query_desc_t.allocate(this.arena);
    }

    public QueryBuilder expr(String expr) {
        MemorySegment exprSegment = this.arena.allocateFrom(expr);
        ecs_query_desc_t.expr(this.desc, exprSegment);
        return this;
    }

    public QueryBuilder with(long componentId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment term = this.desc.asSlice(termOffset, TERM_SIZE);
        long idOffset = ecs_term_t.id$offset();

        term.set(ValueLayout.JAVA_LONG, idOffset, componentId);

        this.termCount++;
        return this;
    }

    public QueryBuilder with(Entity tagEntity) {
        return with(tagEntity.id());
    }

    public <T> QueryBuilder with(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(componentId);
    }

    public QueryBuilder with(long relationId, long objectId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        long pairId = flecs_h.ecs_make_pair(relationId, objectId);

        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment term = this.desc.asSlice(termOffset, TERM_SIZE);
        long idOffset = ecs_term_t.id$offset();

        term.set(ValueLayout.JAVA_LONG, idOffset, pairId);

        this.termCount++;
        return this;
    }

    public <T> QueryBuilder with(Class<T> relationClass, Class<T> objectClass) {
        long relationId = this.world.componentRegistry().getComponentId(relationClass);
        long objectId = this.world.componentRegistry().getComponentId(objectClass);
        return this.with(relationId, objectId);
    }

    public QueryBuilder cached() {
        ecs_query_desc_t.cache_kind(this.desc, EcsQueryCacheAuto);
        return this;
    }

    public QueryBuilder queryFlag(int flag) {
        ecs_query_desc_t.flags(this.desc, flag);
        return this;
    }

    public QueryBuilder in() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'in' modifier to");
        }

        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = this.desc.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        term.set(ValueLayout.JAVA_INT, inoutOffset, EcsIn);

        return this;
    }

    public QueryBuilder out() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'out' modifier to");
        }

        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = this.desc.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        term.set(ValueLayout.JAVA_INT, inoutOffset, FlecsConstants.EcsOut);

        return this;
    }

    public QueryBuilder inout() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'inout' modifier to");
        }

        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = this.desc.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        term.set(ValueLayout.JAVA_INT, inoutOffset, FlecsConstants.EcsInOut);

        return this;
    }

    public QueryBuilder operator(int operator) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'operator' modifier to");
        }

        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = this.desc.asSlice(termOffset, TERM_SIZE);
        ecs_term_t.oper(term, (short) operator);
        return this;
    }

    public QueryBuilder and() {
        return this.operator(FlecsConstants.EcsAnd);
    }

    public QueryBuilder or() {
        return this.operator(FlecsConstants.EcsOr);
    }

    public QueryBuilder not() {
        return this.operator(FlecsConstants.EcsNot);
    }

    public QueryBuilder optional() {
        return this.operator(FlecsConstants.EcsOptional);
    }

    public QueryBuilder andFrom() {
        return this.operator(FlecsConstants.EcsAndFrom);
    }

    public QueryBuilder orFrom() {
        return this.operator(FlecsConstants.EcsOrFrom);
    }

    public QueryBuilder notFrom() {
        return this.operator(FlecsConstants.EcsNotFrom);
    }

    public QueryBuilder src(long entityId) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'src' modifier to");
        }

        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = this.desc.asSlice(termOffset, TERM_SIZE);
        MemorySegment src = ecs_term_t.src(term);

        src.set(ValueLayout.JAVA_LONG, 0, entityId);
        return this;
    }

    public QueryBuilder src(Entity entity) {
        return this.src(entity.id());
    }

    public Query build() {
        try {
            MemorySegment queryPtr = flecs_h.ecs_query_init(this.world.nativeHandle(), this.desc);

            if (queryPtr == null || queryPtr.address() == 0) {
                String errorMsg = "Query creation failed. Structural alignment/size problem (TERM_SIZE=" + TERM_SIZE + ").";
                throw new IllegalStateException(errorMsg);
            }

            return new Query(this.world, queryPtr);
        } finally {
            this.close();
        }
    }

    public void close() {
        this.arena.close();
    }
}