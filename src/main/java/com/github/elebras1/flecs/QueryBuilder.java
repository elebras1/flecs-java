package com.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class QueryBuilder {

    private final Flecs world;
    private final Arena arena;
    private final MemorySegment desc;
    private int termCount = 0;
    private static final long TERM_SIZE = ecs_term_t.layout().byteSize();

    public QueryBuilder(Flecs world) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_query_desc_t.allocate(this.arena);
        this.desc.fill((byte) 0);
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

    // Not sure about that one
    public QueryBuilder with(Entity component) {
        return with(component.id());
    }

    public QueryBuilder with(FlecsComponent<?> component) {
        long componentId = this.world.componentRegistry().getComponentId(component.getClass());
        return with(componentId);
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