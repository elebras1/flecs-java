package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.ComparatorComponent;
import io.github.elebras1.flecs.callback.ComparatorComponentView;
import io.github.elebras1.flecs.callback.ComparatorId;
import io.github.elebras1.flecs.callback.GroupByCallback;
import io.github.elebras1.flecs.util.Flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class QueryBuilder {

    private final World world;
    private final Arena arena;
    private final MemorySegment desc;
    private int termCount = 0;

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

        MemorySegment termSeg = ecs_query_desc_t.terms(this.desc, this.termCount);
        ecs_term_t.id(termSeg, componentId);

        this.termCount++;
        return this;
    }

    public QueryBuilder with(Entity entity) {
        return with(entity.id());
    }

    public <T> QueryBuilder with(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(componentId);
    }

    public QueryBuilder with(long first, long second) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        long pairId = flecs_h.ecs_make_pair(first, second);
        MemorySegment termSeg = ecs_query_desc_t.terms(this.desc, this.termCount);
        ecs_term_t.id(termSeg, pairId);

        this.termCount++;
        return this;
    }

    public <T> QueryBuilder with(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.with(firstId, second);
    }

    public <T> QueryBuilder with(Class<T> first, Entity second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.with(firstId, second.id());
    }

    public <A, B> QueryBuilder with(Class<A> first, Class<B> second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        long secondId = this.world.componentRegistry().getComponentId(second);
        return this.with(firstId, secondId);
    }

    public QueryBuilder without(long componentId) {
        return this.with(componentId).not();
    }

    public QueryBuilder without(Entity entity) {
        return this.without(entity.id());
    }

    public <T> QueryBuilder without(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.without(componentId);
    }

    public QueryBuilder without(long first, long second) {
        return this.with(first, second).not();
    }

    public <T> QueryBuilder without(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.without(firstId, second);
    }

    public <T> QueryBuilder without(Class<T> first, Entity second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.without(firstId, second.id());
    }

    public <A, B> QueryBuilder without(Class<A> first, Class<B> second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        long secondId = this.world.componentRegistry().getComponentId(second);
        return this.without(firstId, secondId);
    }

    public QueryBuilder cached() {
        ecs_query_desc_t.cache_kind(this.desc, Flecs.QueryCacheAuto);
        return this;
    }

    public QueryBuilder queryFlags(int flag) {
        ecs_query_desc_t.flags(this.desc, flag);
        return this;
    }

    public QueryBuilder in() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'in' modifier to");
        }

        MemorySegment termSeg = ecs_query_desc_t.terms(this.desc, this.termCount - 1);
        ecs_term_t.inout(termSeg, (short) Flecs.In);

        return this;
    }

    public QueryBuilder out() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'out' modifier to");
        }

        MemorySegment termSeg = ecs_query_desc_t.terms(this.desc, this.termCount - 1);
        ecs_term_t.inout(termSeg, (short) Flecs.Out);

        return this;
    }

    public QueryBuilder inout() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'inout' modifier to");
        }

        MemorySegment termSeg = ecs_query_desc_t.terms(this.desc, this.termCount - 1);
        ecs_term_t.inout(termSeg, (short) Flecs.InOut);

        return this;
    }

    public QueryBuilder operator(int operator) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'operator' modifier to");
        }

        MemorySegment termSeg = ecs_query_desc_t.terms(this.desc, this.termCount - 1);
        ecs_term_t.oper(termSeg, (short) operator);

        return this;
    }

    public QueryBuilder and() {
        return this.operator(Flecs.And);
    }

    public QueryBuilder or() {
        return this.operator(Flecs.Or);
    }

    public QueryBuilder not() {
        return this.operator(Flecs.Not);
    }

    public QueryBuilder optional() {
        return this.operator(Flecs.Optional);
    }

    public QueryBuilder andFrom() {
        return this.operator(Flecs.AndFrom);
    }

    public QueryBuilder orFrom() {
        return this.operator(Flecs.OrFrom);
    }

    public QueryBuilder notFrom() {
        return this.operator(Flecs.NotFrom);
    }

    public QueryBuilder src(long entityId) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'src' modifier to");
        }

        MemorySegment termSeg = ecs_query_desc_t.terms(this.desc, this.termCount - 1);
        MemorySegment srcRefSeg = ecs_term_t.src(termSeg);
        ecs_term_ref_t.id(srcRefSeg, entityId);

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
                String errorMsg = "Query creation failed.";
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