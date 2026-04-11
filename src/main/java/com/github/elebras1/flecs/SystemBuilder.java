package com.github.elebras1.flecs;

import com.github.elebras1.flecs.callback.*;
import com.github.elebras1.flecs.util.FlecsConstants;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static com.github.elebras1.flecs.util.FlecsConstants.*;

public class SystemBuilder extends SystemBuilderBase {

    protected final Arena arena;
    private final Iter[] iters;
    private int termCount = 0;
    private static final long TERM_SIZE = ecs_term_t.layout().byteSize();
    private IterCallback iterCallback;
    private RunCallback runCallback;
    private EntityCallback entityCallback;
    private long phase = 0;

    public SystemBuilder(World world) {
        Arena arena = Arena.ofConfined();
        super(world, ecs_system_desc_t.allocate(arena));
        this.arena = arena;
        this.iters = new Iter[world.getStageCount()];
        for(int i = 0; i < this.iters.length; i++) {
            World worldStage = world.getStage(i);
            this.iters[i] = new Iter(MemorySegment.NULL, worldStage);
        }
    }

    public SystemBuilder(World world, String name) {
        this(world);
        MemorySegment nameSegment = this.arena.allocateFrom(name);

        MemorySegment entityDescTemp = ecs_entity_desc_t.allocate(this.arena);
        ecs_entity_desc_t.name(entityDescTemp, nameSegment);
        ecs_system_desc_t.entity(this.desc, flecs_h.ecs_entity_init(world.worldSeg(), entityDescTemp));
    }

    public SystemBuilder kind(long phase) {
        this.phase = phase;
        return this;
    }

    public SystemBuilder interval(float interval) {
        ecs_system_desc_t.interval(this.desc, interval);
        return this;
    }

    public SystemBuilder rate(int rate) {
        ecs_system_desc_t.rate(this.desc, rate);
        return this;
    }

    public SystemBuilder tickSource(long tickSource) {
        ecs_system_desc_t.tick_source(this.desc, tickSource);
        return this;
    }

    public SystemBuilder multiThreaded() {
        ecs_system_desc_t.multi_threaded(this.desc, true);
        return this;
    }

    public SystemBuilder multiThreaded(boolean multiThreaded) {
        ecs_system_desc_t.multi_threaded(this.desc, multiThreaded);
        return this;
    }

    public SystemBuilder immediate() {
        ecs_system_desc_t.immediate(this.desc, true);
        return this;
    }

    public SystemBuilder immediate(boolean immediate) {
        ecs_system_desc_t.immediate(this.desc, immediate);
        return this;
    }

    public SystemBuilder with(long componentId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment term = queryDescSeg.asSlice(termOffset, TERM_SIZE);
        long idOffset = ecs_term_t.id$offset();

        term.set(ValueLayout.JAVA_LONG, idOffset, componentId);

        this.termCount++;
        return this;
    }

    public SystemBuilder with(Entity tagEntity) {
        return with(tagEntity.id());
    }

    public <T> SystemBuilder with(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(componentId);
    }

    public SystemBuilder with(long relationId, long componentId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        long pairId = flecs_h.ecs_make_pair(relationId, componentId);

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment termSeg = queryDescSeg.asSlice(termOffset, TERM_SIZE);
        long idOffset = ecs_term_t.id$offset();

        termSeg.set(ValueLayout.JAVA_LONG, idOffset, pairId);

        this.termCount++;
        return this;
    }

    public <T> SystemBuilder with(long relationId, Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(relationId, componentId);
    }

    public SystemBuilder without(long componentId) {
        return this.with(componentId).not();
    }

    public <T> SystemBuilder without(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.without(componentId);
    }

    public SystemBuilder without(long relationId, long componentId) {
        return this.with(relationId, componentId).not();
    }

    public SystemBuilder without(long relationId, Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.without(relationId, componentId);
    }

    public SystemBuilder without(Entity tagEntity) {
        return this.without(tagEntity.id());
    }

    public SystemBuilder in() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'in' modifier to");
        }

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment termSeg = queryDescSeg.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        termSeg.set(ValueLayout.JAVA_INT, inoutOffset, EcsIn);

        return this;
    }

    public SystemBuilder out() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'out' modifier to");
        }

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment termSeg = queryDescSeg.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        termSeg.set(ValueLayout.JAVA_INT, inoutOffset, EcsOut);

        return this;
    }

    public SystemBuilder inOut() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'inout' modifier to");
        }

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment termSeg = queryDescSeg.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        termSeg.set(ValueLayout.JAVA_INT, inoutOffset, EcsInOut);

        return this;
    }

    public SystemBuilder operator(int operator) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'operator' modifier to");
        }

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment termSeg = queryDescSeg.asSlice(termOffset, TERM_SIZE);
        ecs_term_t.oper(termSeg, (short) operator);
        return this;
    }

    public SystemBuilder and() {
        return this.operator(FlecsConstants.EcsAnd);
    }

    public SystemBuilder or() {
        return this.operator(FlecsConstants.EcsOr);
    }

    public SystemBuilder not() {
        return this.operator(FlecsConstants.EcsNot);
    }

    public SystemBuilder optional() {
        return this.operator(FlecsConstants.EcsOptional);
    }

    public SystemBuilder andFrom() {
        return this.operator(FlecsConstants.EcsAndFrom);
    }

    public SystemBuilder orFrom() {
        return this.operator(FlecsConstants.EcsOrFrom);
    }

    public SystemBuilder notFrom() {
        return this.operator(FlecsConstants.EcsNotFrom);
    }

    public <T> SystemBuilder write(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(componentId).out();
    }

    public <T> SystemBuilder read(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(componentId).in();
    }

    public SystemBuilder orderBy(long componentId) {
        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        ecs_query_desc_t.order_by(queryDescSeg, componentId);
        return this;
    }

    public SystemBuilder orderBy(long componentId, ComparatorId comparator) {
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((idA, _, idB, _) ->
                comparator.compare(idA, idB), this.world.arena());

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        ecs_query_desc_t.order_by_callback(queryDescSeg, callbackStub);

        return this.orderBy(componentId);
    }

    public <T> SystemBuilder orderBy(long componentId, ComparatorComponent<T> comparator) {
        Component<T> component = this.world.componentRegistry().getComponentById(componentId);
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((_, componentASeg, _, componentBSeg) ->
                comparator.compare(component.read(componentASeg, 0), component.read(componentBSeg, 0)), this.world.arena());

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        ecs_query_desc_t.order_by_callback(queryDescSeg, callbackStub);

        return this.orderBy(componentId);
    }

    @SuppressWarnings("unchecked")
    public <V extends ComponentView> SystemBuilder orderBy(long componentId, ComparatorComponentView<V> comparator) {
        Class<?> componentClass = this.world.componentRegistry().getComponentClassById(componentId);
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((_, componentASeg, _, componentBSeg) -> {
            V componentViewA = (V) this.world.viewCache().getComponentView(componentClass);
            componentViewA.setBaseAddress(componentASeg.address());
            V componentViewB = (V) this.world.viewCache().getComponentView(componentClass);
            componentViewB.setBaseAddress(componentBSeg.address());
            return comparator.compare(componentViewA, componentViewB);
        }, this.world.arena());

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        ecs_query_desc_t.order_by_callback(queryDescSeg, callbackStub);

        return this.orderBy(componentId);
    }

    public SystemBuilder orderBy(Entity entity) {
        return this.orderBy(entity.id());
    }

    public SystemBuilder orderBy(Entity entity, ComparatorId comparator) {
        return this.orderBy(entity.id(), comparator);
    }

    public <T> SystemBuilder orderBy(Entity entity, ComparatorComponent<T> comparator) {
        return this.orderBy(entity.id(), comparator);
    }

    public <V extends ComponentView> SystemBuilder orderBy(Entity entity, ComparatorComponentView<V> comparator) {
        return this.orderBy(entity.id(), comparator);
    }

    public SystemBuilder orderBy(Class<?> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.orderBy(componentId);
    }

    public SystemBuilder orderBy(Class<?> componentClass, ComparatorId comparator) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.orderBy(componentId, comparator);
    }

    public <T> SystemBuilder orderBy(Class<T> componentClass, ComparatorComponent<T> comparator) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.orderBy(componentId, comparator);
    }

    public <V extends ComponentView> SystemBuilder orderBy(Class<?> componentClass, ComparatorComponentView<V> comparator) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.orderBy(componentId, comparator);
    }

    public SystemBuilder groupBy(long groupId) {
        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        ecs_query_desc_t.group_by(queryDescSeg, groupId);
        return this;
    }

    public SystemBuilder groupBy(long groupId, GroupByCallback groupByCallback) {
        MemorySegment callbackStub = ecs_group_by_action_t.allocate((_, tableSeg, id, _) -> {
            Table table = tableSeg.address() == 0 ? null : new Table(this.world, tableSeg);
            return groupByCallback.accept(this.world, table, id);
        }, this.world.arena());

        MemorySegment queryDescSeg = ecs_system_desc_t.query(this.desc);
        ecs_query_desc_t.group_by_callback(queryDescSeg, callbackStub);
        return this.groupBy(groupId);
    }

    public SystemBuilder groupBy(Class<?> componentClass) {
        long groupId = this.world.componentRegistry().getComponentId(componentClass);
        return this.groupBy(groupId);
    }

    public SystemBuilder groupBy(Class<?> componentClass, GroupByCallback groupByCallback) {
        long groupId = this.world.componentRegistry().getComponentId(componentClass);
        return this.groupBy(groupId, groupByCallback);
    }

    public SystemBuilder groupBy(Entity entity) {
        return this.groupBy(entity.id());
    }

    public SystemBuilder groupBy(Entity entity, GroupByCallback groupByCallback) {
        return this.groupBy(entity.id(), groupByCallback);
    }

    public FlecsSystem iter(IterCallback callback) {
        this.iterCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(iterSegment -> {
            MemorySegment stageSeg = ecs_iter_t.world(iterSegment);
            int stageId = flecs_h.ecs_stage_get_id(stageSeg);
            Iter iter = this.iters[stageId];
            iter.setIterSeg(iterSegment);
            iter.world().viewCache().resetCursors();
            callback.accept(iter);
        }, this.world.arena());

        ecs_system_desc_t.callback(this.desc, callbackStub);

        return build();
    }

    public FlecsSystem run(RunCallback callback) {
        this.runCallback = callback;

        MemorySegment callbackStub = ecs_run_action_t.allocate(iterSegment -> {
            MemorySegment stageSeg = ecs_iter_t.world(iterSegment);
            int stageId = flecs_h.ecs_stage_get_id(stageSeg);
            Iter iter = this.iters[stageId];
            iter.setIterSeg(iterSegment);
            iter.world().viewCache().resetCursors();
            callback.accept(this.iters[stageId]);
        }, this.world.arena());

        ecs_system_desc_t.run(this.desc, callbackStub);

        return build();
    }

    public FlecsSystem each(EntityCallback callback) {
        this.entityCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(iterSegment -> {
            int count = ecs_iter_t.count(iterSegment);
            MemorySegment entitiesSeg = ecs_iter_t.entities(iterSegment);

            for (int i = 0; i < count; i++) {
                long entityId = entitiesSeg.getAtIndex(ValueLayout.JAVA_LONG, i);
                callback.accept(entityId);
            }
        }, this.world.arena());

        ecs_system_desc_t.callback(this.desc, callbackStub);

        return build();
    }

    @Override
    protected FlecsSystem build() {
        long systemId = flecs_h.ecs_system_init(this.world.worldSeg(), this.desc);

        if (systemId == 0) {
            throw new IllegalStateException("Failed to create system");
        }

        if (this.phase != 0) {
            flecs_h.ecs_add_id(this.world.worldSeg(), systemId, this.phase);

            long dependsOnPair = flecs_h.ecs_make_pair(EcsDependsOn, this.phase);
            flecs_h.ecs_add_id(this.world.worldSeg(), systemId, dependsOnPair);
        }

        this.world.registerSystemCallbacks(systemId, this.iterCallback, this.runCallback, this.entityCallback);

        this.arena.close();

        return new FlecsSystem(this.world, systemId);
    }
}

