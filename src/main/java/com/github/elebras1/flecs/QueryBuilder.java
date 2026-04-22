package com.github.elebras1.flecs;

import com.github.elebras1.flecs.callback.ComparatorComponent;
import com.github.elebras1.flecs.callback.ComparatorComponentView;
import com.github.elebras1.flecs.callback.ComparatorId;
import com.github.elebras1.flecs.callback.GroupByCallback;
import com.github.elebras1.flecs.util.FlecsConstants;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static com.github.elebras1.flecs.util.FlecsConstants.EcsIn;
import static com.github.elebras1.flecs.util.FlecsConstants.EcsQueryCacheAuto;

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
        MemorySegment exprSeg = this.arena.allocateFrom(expr);
        ecs_query_desc_t.expr(this.desc, exprSeg);
        return this;
    }

    public QueryBuilder with(long componentId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment termSeg = this.desc.asSlice(termOffset, TERM_SIZE);
        long idOffset = ecs_term_t.id$offset();

        termSeg.set(ValueLayout.JAVA_LONG, idOffset, componentId);

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

    public QueryBuilder with(long relationId, long componentId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        long pairId = flecs_h.ecs_make_pair(relationId, componentId);

        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment term = this.desc.asSlice(termOffset, TERM_SIZE);
        long idOffset = ecs_term_t.id$offset();

        term.set(ValueLayout.JAVA_LONG, idOffset, pairId);

        this.termCount++;
        return this;
    }

    public <T> QueryBuilder with(long relationId, Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(relationId, componentId);
    }

    public QueryBuilder without(long componentId) {
        return this.with(componentId).not();
    }

    public <T> QueryBuilder without(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.without(componentId);
    }

    public QueryBuilder without(long relationId, long componentId) {
        return this.with(relationId, componentId).not();
    }

    public QueryBuilder without(long relationId, Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.without(relationId, componentId);
    }

    public QueryBuilder without(Entity tagEntity) {
        return this.without(tagEntity.id());
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

        MemorySegment termSeg = this.desc.asSlice(termOffset, TERM_SIZE);
        MemorySegment srcSeg = ecs_term_t.src(termSeg);

        srcSeg.set(ValueLayout.JAVA_LONG, 0, entityId);
        return this;
    }

    public QueryBuilder src(Entity entity) {
        return this.src(entity.id());
    }

    public QueryBuilder orderBy(long componentId) {
        ecs_query_desc_t.order_by(this.desc, componentId);
        return this;
    }

    public QueryBuilder orderBy(long componentId, ComparatorId comparator) {
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((idA, _, idB, _) ->
                comparator.compare(idA, idB), this.world.arena());

        ecs_query_desc_t.order_by_callback(this.desc, callbackStub);
        return this.orderBy(componentId);
    }

    public <T> QueryBuilder orderBy(long componentId, ComparatorComponent<T> comparator) {
        Component<T> component = this.world.componentRegistry().getComponentById(componentId);
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((_, componentAdressA, _, componentAdressB) ->
                comparator.compare(component.read(MemorySegment.ofAddress(componentAdressA), 0), component.read(MemorySegment.ofAddress(componentAdressB), 0)), this.world.arena());
        ecs_query_desc_t.order_by_callback(this.desc, callbackStub);
        return this.orderBy(componentId);
    }

    @SuppressWarnings("unchecked")
    public <V extends ComponentView> QueryBuilder orderBy(long componentId, ComparatorComponentView<V> comparator) {
        Class<?> componentClass = this.world.componentRegistry().getComponentClassById(componentId);
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((_, componentAdressA, _, componentAdressB) -> {
            V componentViewA = (V) this.world.viewCache().getComponentView(componentClass);
            componentViewA.setBaseAddress(componentAdressA);
            V componentViewB = (V) this.world.viewCache().getComponentView(componentClass);
            componentViewB.setBaseAddress(componentAdressB);
            return comparator.compare(componentViewA, componentViewB);
        }, this.world.arena());
        ecs_query_desc_t.order_by_callback(this.desc, callbackStub);
        return this.orderBy(componentId);
    }

    public QueryBuilder orderBy(Entity entity) {
        return this.orderBy(entity.id());
    }

    public QueryBuilder orderBy(Entity entity, ComparatorId comparator) {
        return this.orderBy(entity.id(), comparator);
    }

    public <T> QueryBuilder orderBy(Entity entity, ComparatorComponent<T> comparator) {
        return this.orderBy(entity.id(), comparator);
    }

    public <V extends ComponentView> QueryBuilder orderBy(Entity entity, ComparatorComponentView<V> comparator) {
        return this.orderBy(entity.id(), comparator);
    }

    public QueryBuilder orderBy(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.orderBy(componentId);
    }

    public QueryBuilder orderBy(Class<?> componentClass, ComparatorId comparator) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.orderBy(componentId, comparator);
    }

    public <T> QueryBuilder orderBy(Class<T> componentClass, ComparatorComponent<T> comparator) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.orderBy(componentId, comparator);
    }

    public <V extends ComponentView> QueryBuilder orderBy(Class<?> componentClass, ComparatorComponentView<V> comparator) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.orderBy(componentId, comparator);
    }

    public QueryBuilder groupBy(long groupId) {
        ecs_query_desc_t.group_by(this.desc, groupId);
        return this;
    }

    public QueryBuilder groupBy(long groupId, GroupByCallback groupByCallback) {
        MemorySegment callbackStub = ecs_group_by_action_t.allocate((_, tableSeg, id, _) -> {
            Table table = tableSeg.address() == 0 ? null : new Table(this.world, tableSeg);
            return groupByCallback.accept(this.world, table, id);
        }, this.world.arena());

        ecs_query_desc_t.group_by_callback(this.desc, callbackStub);
        return this.groupBy(groupId);
    }

    public QueryBuilder groupBy(Class<?> componentClass) {
        long groupId = this.world.componentRegistry().getComponentId(componentClass);
        return this.groupBy(groupId);
    }

    public QueryBuilder groupBy(Class<?> componentClass, GroupByCallback groupByCallback) {
        long groupId = this.world.componentRegistry().getComponentId(componentClass);
        return this.groupBy(groupId, groupByCallback);
    }

    public QueryBuilder groupBy(Entity entity) {
        return this.groupBy(entity.id());
    }

    public QueryBuilder groupBy(Entity entity, GroupByCallback groupByCallback) {
        return this.groupBy(entity.id(), groupByCallback);
    }

    public Query build() {
        try {
            MemorySegment querySeg = flecs_h.ecs_query_init(this.world.worldSeg(), this.desc);

            if (querySeg.address() == 0) {
                String errorMsg = "Query creation failed. Structural alignment/size problem (TERM_SIZE=" + TERM_SIZE + ").";
                throw new IllegalStateException(errorMsg);
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