package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.ComparatorComponent;
import io.github.elebras1.flecs.callback.ComparatorComponentView;
import io.github.elebras1.flecs.callback.ComparatorId;
import io.github.elebras1.flecs.callback.GroupByCallback;
import io.github.elebras1.flecs.callback.GroupByContextCallback;
import io.github.elebras1.flecs.callback.GroupCreateCallback;
import io.github.elebras1.flecs.callback.GroupDeleteCallback;
import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.function.Consumer;

public abstract class QueryTermBuilder<S extends QueryTermBuilder<S>> {

    protected final World world;
    private final Arena arena;
    private final MemorySegment queryDesc;
    private int termCount;
    private int selectedTerm = -1;
    private int selectedRef = TermRef.SRC;
    private boolean exprSet;

    protected QueryTermBuilder(World world, Arena arena, MemorySegment queryDesc) {
        this.world = world;
        this.arena = arena;
        this.queryDesc = queryDesc;
    }

    @SuppressWarnings("unchecked")
    private S builder() {
        return (S) this;
    }

    public S queryFlags(int flag) {
        ecs_query_desc_t.flags(this.queryDesc, ecs_query_desc_t.flags(this.queryDesc) | flag);
        return builder();
    }

    public S cacheKind(int kind) {
        ecs_query_desc_t.cache_kind(this.queryDesc, kind);
        return builder();
    }

    public S cached() {
        return cacheKind(Flecs.QueryCacheAuto);
    }

    public S detectChanges() {
        return queryFlags(Flecs.QueryDetectChanges);
    }

    public S expr(String expr) {
        if (this.exprSet) {
            throw new IllegalStateException("expr() called more than once");
        }
        ecs_query_desc_t.expr(this.queryDesc, this.arena.allocateFrom(expr));
        this.exprSet = true;
        return builder();
    }

    public S with(long componentId) {
        MemorySegment termSeg = newTerm();

        if ((componentId & flecs_h.ECS_ID_FLAGS_MASK()) != 0) {
            ecs_term_t.id(termSeg, componentId);
        } else {
            MemorySegment firstRefSeg = ecs_term_ref_t.allocate(this.arena);
            ecs_term_ref_t.id(firstRefSeg, componentId);
            ecs_term_t.first(termSeg, firstRefSeg);
        }

        return builder();
    }

    public S with(String componentName) {
        MemorySegment termSeg = newTerm();
        MemorySegment firstRefSeg = ecs_term_ref_t.allocate(this.arena);
        applyNameOrVar(firstRefSeg, componentName);
        ecs_term_t.first(termSeg, firstRefSeg);

        return builder();
    }

    public S with(Entity entity) {
        return with(entity.id());
    }

    public <T> S with(Class<T> componentClass) {
        return with(this.world.componentRegistry().getComponentId(componentClass));
    }

    public S with(long first, long second) {
        MemorySegment termSeg = newTerm();

        MemorySegment firstRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(firstRefSeg, first);

        MemorySegment secondRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(secondRefSeg, second);

        ecs_term_t.first(termSeg, firstRefSeg);
        ecs_term_t.second(termSeg, secondRefSeg);

        return builder();
    }

    public S with(String first, String second) {
        MemorySegment termSeg = newTerm();

        MemorySegment firstRefSeg = ecs_term_ref_t.allocate(this.arena);
        applyNameOrVar(firstRefSeg, first);

        MemorySegment secondRefSeg = ecs_term_ref_t.allocate(this.arena);
        applyNameOrVar(secondRefSeg, second);

        ecs_term_t.first(termSeg, firstRefSeg);
        ecs_term_t.second(termSeg, secondRefSeg);

        return builder();
    }

    public <T> S with(Class<T> first, long second) {
        return with(this.world.componentRegistry().getComponentId(first), second);
    }

    public S with(String first, long second) {
        MemorySegment termSeg = newTerm();

        MemorySegment firstRefSeg = ecs_term_ref_t.allocate(this.arena);
        applyNameOrVar(firstRefSeg, first);

        MemorySegment secondRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(secondRefSeg, second);

        ecs_term_t.first(termSeg, firstRefSeg);
        ecs_term_t.second(termSeg, secondRefSeg);

        return builder();
    }

    public S with(long first, String second) {
        MemorySegment termSeg = newTerm();

        MemorySegment firstRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(firstRefSeg, first);

        MemorySegment secondRefSeg = ecs_term_ref_t.allocate(this.arena);
        applyNameOrVar(secondRefSeg, second);

        ecs_term_t.first(termSeg, firstRefSeg);
        ecs_term_t.second(termSeg, secondRefSeg);

        return builder();
    }

    public <T> S with(Class<T> first, Entity second) {
        return with(this.world.componentRegistry().getComponentId(first), second.id());
    }

    public <T> S with(Class<T> first, String second) {
        MemorySegment termSeg = newTerm();

        MemorySegment firstRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(firstRefSeg, this.world.componentRegistry().getComponentId(first));

        MemorySegment secondRefSeg = ecs_term_ref_t.allocate(this.arena);
        applyNameOrVar(secondRefSeg, second);

        ecs_term_t.first(termSeg, firstRefSeg);
        ecs_term_t.second(termSeg, secondRefSeg);

        return builder();
    }

    public <A, B> S with(Class<A> first, Class<B> second) {
        return with(
                this.world.componentRegistry().getComponentId(first),
                this.world.componentRegistry().getComponentId(second));
    }

    public S without(long componentId) {
        return with(componentId).not();
    }

    public S without(String componentName) {
        return with(componentName).not();
    }

    public S without(Entity entity) {
        return with(entity).not();
    }

    public <T> S without(Class<T> componentClass) {
        return with(componentClass).not();
    }

    public S without(long first, long second) {
        return with(first, second).not();
    }

    public S without(String first, String second) {
        return with(first, second).not();
    }

    public <T> S without(Class<T> first, long second) {
        return with(first, second).not();
    }

    public <T> S without(Class<T> first, Entity second) {
        return with(first, second).not();
    }

    public <A, B> S without(Class<A> first, Class<B> second) {
        return with(first, second).not();
    }

    public <T> S without(Class<T> first, String second) {
        return with(first, second).not();
    }

    public S termAt(int index) {
        if (index < 0 || index >= this.termCount) {
            throw new IndexOutOfBoundsException("Invalid term index: " + index);
        }
        this.selectedTerm = index;
        this.selectedRef = TermRef.SRC;
        return builder();
    }

    public <T> S termAt(Class<T> componentClass) {
        long termId = this.world.componentRegistry().getComponentId(componentClass);
        for (int i = 0; i < this.termCount; i++) {
            MemorySegment termSeg = term(i);
            long curTermId = ecs_term_t.id(termSeg);
            long curTermPair = flecs_h.ecs_make_pair(
                    ecs_term_ref_t.id(ecs_term_t.first(termSeg)),
                    ecs_term_ref_t.id(ecs_term_t.second(termSeg)));

            if ((termId == curTermId || (curTermId != 0 && termId == flecs_h.ecs_get_typeid(this.world.worldSeg(), curTermId))) ||
                (termId == curTermPair || (curTermPair != 0 && termId == flecs_h.ecs_get_typeid(this.world.worldSeg(), curTermPair)))) {
                return termAt(i);
            }
        }
        throw new IllegalArgumentException("term not found");
    }

    public <T> S termAt(int index, Class<T> componentClass) {
        termAt(index);

        long termId = this.world.componentRegistry().getComponentId(componentClass);
        MemorySegment termSeg = termForConfiguration();
        long curTermId = ecs_term_t.id(termSeg);
        long curTermPair = flecs_h.ecs_make_pair(
                ecs_term_ref_t.id(ecs_term_t.first(termSeg)),
                ecs_term_ref_t.id(ecs_term_t.second(termSeg)));

        if (!((termId == curTermId || (curTermId != 0 && termId == flecs_h.ecs_get_typeid(this.world.worldSeg(), curTermId))) ||
              (termId == curTermPair || (curTermPair != 0 && termId == flecs_h.ecs_get_typeid(this.world.worldSeg(), curTermPair))))) {
            throw new IllegalArgumentException("term type mismatch");
        }
        return builder();
    }

    public S term() {
        if (this.termCount > 0 && !flecs_h.ecs_term_is_initialized(term(this.termCount - 1))) {
            throw new IllegalStateException("term() called without initializing the previous term");
        }
        newTerm();
        return builder();
    }

    public S in() {
        return inout(Flecs.In);
    }

    public S out() {
        return inout(Flecs.Out);
    }

    public S inout() {
        return inout(Flecs.InOut);
    }

    public S inout(int inout) {
        ecs_term_t.inout(configuredTerm("inout"), (short) inout);
        return builder();
    }

    public S inoutNone() {
        return inout(Flecs.InOutNone);
    }

    public S inoutStage(int inout) {
        MemorySegment termSeg = configuredTerm("inoutStage");
        ecs_term_t.inout(termSeg, (short) inout);
        if (ecs_term_t.oper(termSeg) != Flecs.Not) {
            this.selectedRef = TermRef.SRC;
            ecs_term_ref_t.id(ecs_term_t.src(termSeg), Flecs.IsEntity);
        }
        return builder();
    }

    public S read() {
        return inoutStage(Flecs.In);
    }

    public S write() {
        return inoutStage(Flecs.Out);
    }

    public S readWrite() {
        return inoutStage(Flecs.InOut);
    }

    public S filter() {
        return inout(Flecs.InOutFilter);
    }

    public S oper(int operator) {
        ecs_term_t.oper(configuredTerm("operator"), (short) operator);
        return builder();
    }

    public S and() {
        return oper(Flecs.And);
    }

    public S or() {
        return oper(Flecs.Or);
    }

    public S not() {
        return oper(Flecs.Not);
    }

    public S optional() {
        return oper(Flecs.Optional);
    }

    public S andFrom() {
        return oper(Flecs.AndFrom);
    }

    public S orFrom() {
        return oper(Flecs.OrFrom);
    }

    public S notFrom() {
        return oper(Flecs.NotFrom);
    }

    public S idFlags(long flags) {
        MemorySegment termSeg = configuredTerm("idFlags");
        ecs_term_t.id(termSeg, ecs_term_t.id(termSeg) | flags);
        return builder();
    }

    public S src() {
        termForConfiguration();
        this.selectedRef = TermRef.SRC;
        return builder();
    }

    public S src(long entityId) {
        src();
        ecs_term_ref_t.id(currentTermRef("src"), entityId);
        return builder();
    }

    public S src(Entity entity) {
        return src(entity.id());
    }

    public <T> S src(Class<T> componentClass) {
        return src(this.world.componentRegistry().getComponentId(componentClass));
    }

    public S src(String name) {
        src();
        applyNameOrVar(currentTermRef("src"), name);
        return builder();
    }

    public S first() {
        termForConfiguration();
        this.selectedRef = TermRef.FIRST;
        return builder();
    }

    public S first(long entityId) {
        first();
        ecs_term_ref_t.id(currentTermRef("first"), entityId);
        return builder();
    }

    public S first(Entity entity) {
        return first(entity.id());
    }

    public <T> S first(Class<T> componentClass) {
        return first(this.world.componentRegistry().getComponentId(componentClass));
    }

    public S first(String name) {
        first();
        applyNameOrVar(currentTermRef("first"), name);
        return builder();
    }

    public S second() {
        termForConfiguration();
        this.selectedRef = TermRef.SECOND;
        return builder();
    }

    public S second(long entityId) {
        second();
        ecs_term_ref_t.id(currentTermRef("second"), entityId);
        return builder();
    }

    public S second(Entity entity) {
        return second(entity.id());
    }

    public <T> S second(Class<T> componentClass) {
        return second(this.world.componentRegistry().getComponentId(componentClass));
    }

    public S second(String name) {
        second();
        applyNameOrVar(currentTermRef("second"), name);
        return builder();
    }

    public S id(long entityId) {
        ecs_term_ref_t.id(currentTermRef("id"), entityId);
        return builder();
    }

    public S entity(long entityId) {
        ecs_term_ref_t.id(currentTermRef("entity"), entityId | Flecs.IsEntity);
        return builder();
    }

    public S name(String name) {
        MemorySegment refSeg = currentTermRef("name");
        ecs_term_ref_t.id(refSeg, ecs_term_ref_t.id(refSeg) | Flecs.IsEntity);
        ecs_term_ref_t.name(refSeg, this.arena.allocateFrom(name));
        return builder();
    }

    public S flags(long flags) {
        ecs_term_ref_t.id(currentTermRef("flags"), flags);
        return builder();
    }

    public S self() {
        MemorySegment refSeg = currentTermRef("self");
        ecs_term_ref_t.id(refSeg, ecs_term_ref_t.id(refSeg) | flecs_h.EcsSelf());
        return builder();
    }

    public S up() {
        assertSrcRef("up traversal can only be applied to term source");
        MemorySegment refSeg = currentTermRef("up");
        ecs_term_ref_t.id(refSeg, ecs_term_ref_t.id(refSeg) | flecs_h.EcsUp());
        return builder();
    }

    public S up(long trav) {
        up();
        if (trav != 0) {
            ecs_term_t.trav(configuredTerm("up"), trav);
        }
        return builder();
    }

    public S up(Entity trav) {
        return up(trav.id());
    }

    public <T> S up(Class<T> trav) {
        return up(this.world.componentRegistry().getComponentId(trav));
    }

    public S parent() {
        return up();
    }

    public S cascade() {
        up();
        MemorySegment refSeg = currentTermRef("cascade");
        ecs_term_ref_t.id(refSeg, ecs_term_ref_t.id(refSeg) | flecs_h.EcsCascade());
        return builder();
    }

    public S cascade(long trav) {
        cascade();
        if (trav != 0) {
            ecs_term_t.trav(configuredTerm("cascade"), trav);
        }
        return builder();
    }

    public S cascade(Entity trav) {
        return cascade(trav.id());
    }

    public <T> S cascade(Class<T> trav) {
        return cascade(this.world.componentRegistry().getComponentId(trav));
    }

    public S desc() {
        MemorySegment refSeg = currentTermRef("desc");
        ecs_term_ref_t.id(refSeg, ecs_term_ref_t.id(refSeg) | flecs_h.EcsDesc());
        return builder();
    }

    public S trav(long trav) {
        return trav(trav, 0);
    }

    public S trav(long trav, long flags) {
        ecs_term_t.trav(configuredTerm("trav"), trav);
        MemorySegment refSeg = currentTermRef("trav");
        ecs_term_ref_t.id(refSeg, ecs_term_ref_t.id(refSeg) | flags);
        return builder();
    }

    public S var(String varName) {
        MemorySegment refSeg = currentTermRef("var");
        ecs_term_ref_t.id(refSeg, ecs_term_ref_t.id(refSeg) | flecs_h.EcsIsVariable());
        ecs_term_ref_t.name(refSeg, this.arena.allocateFrom(varName));
        return builder();
    }

    public S scopeOpen() {
        MemorySegment termSeg = newTerm();
        MemorySegment firstRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(firstRefSeg, Flecs.ScopeOpen);
        ecs_term_t.first(termSeg, firstRefSeg);
        ecs_term_ref_t.id(ecs_term_t.src(termSeg), Flecs.IsEntity);
        return builder();
    }

    public S scopeClose() {
        MemorySegment termSeg = newTerm();
        MemorySegment firstRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(firstRefSeg, Flecs.ScopeClose);
        ecs_term_t.first(termSeg, firstRefSeg);
        ecs_term_ref_t.id(ecs_term_t.src(termSeg), Flecs.IsEntity);
        return builder();
    }

    public S orderBy(long componentId, ComparatorId comparator) {
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((idA, _, idB, _) ->
                comparator.compare(idA, idB), this.world.arena());

        ecs_query_desc_t.order_by(this.queryDesc, componentId);
        ecs_query_desc_t.order_by_callback(this.queryDesc, callbackStub);
        return builder();
    }

    public <T> S orderBy(long componentId, ComparatorComponent<T> comparator) {
        Component<T> component = this.world.componentRegistry().getComponentById(componentId);
        long componentSize = component.size();
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((_, componentAdressA, _, componentAdressB) -> {
            if (componentAdressA == 0 || componentAdressB == 0) {
                return 0;
            }
            MemorySegment segmentA = MemorySegment.ofAddress(componentAdressA).reinterpret(componentSize);
            MemorySegment segmentB = MemorySegment.ofAddress(componentAdressB).reinterpret(componentSize);
            return comparator.compare(component.read(segmentA, 0), component.read(segmentB, 0));
        }, this.world.arena());

        ecs_query_desc_t.order_by(this.queryDesc, componentId);
        ecs_query_desc_t.order_by_callback(this.queryDesc, callbackStub);
        return builder();
    }

    @SuppressWarnings("unchecked")
    public <V extends ComponentView> S orderBy(long componentId, ComparatorComponentView<V> comparator) {
        Class<?> componentClass = this.world.componentRegistry().getComponentClassById(componentId);
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((_, componentAdressA, _, componentAdressB) -> {
            V componentViewA = (V) this.world.viewCache().getComponentView(componentClass);
            componentViewA.setBaseAddress(componentAdressA);
            V componentViewB = (V) this.world.viewCache().getComponentView(componentClass);
            componentViewB.setBaseAddress(componentAdressB);
            return comparator.compare(componentViewA, componentViewB);
        }, this.world.arena());

        ecs_query_desc_t.order_by(this.queryDesc, componentId);
        ecs_query_desc_t.order_by_callback(this.queryDesc, callbackStub);
        return builder();
    }

    public S orderBy(Entity entity, ComparatorId comparator) {
        return orderBy(entity.id(), comparator);
    }

    public <T> S orderBy(Entity entity, ComparatorComponent<T> comparator) {
        return orderBy(entity.id(), comparator);
    }

    public <V extends ComponentView> S orderBy(Entity entity, ComparatorComponentView<V> comparator) {
        return orderBy(entity.id(), comparator);
    }

    public S orderBy(Class<?> componentClass, ComparatorId comparator) {
        return orderBy(this.world.componentRegistry().getComponentId(componentClass), comparator);
    }

    public <T> S orderBy(Class<T> componentClass, ComparatorComponent<T> comparator) {
        return orderBy(this.world.componentRegistry().getComponentId(componentClass), comparator);
    }

    public <V extends ComponentView> S orderBy(Class<?> componentClass, ComparatorComponentView<V> comparator) {
        return orderBy(this.world.componentRegistry().getComponentId(componentClass), comparator);
    }

    public S groupBy(long groupId) {
        ecs_query_desc_t.group_by(this.queryDesc, groupId);
        ecs_query_desc_t.group_by_callback(this.queryDesc, MemorySegment.NULL);
        return builder();
    }

    public S groupBy(long groupId, GroupByCallback groupByCallback) {
        MemorySegment callbackStub = ecs_group_by_action_t.allocate((_, tableSeg, id, _) -> {
            Table table = tableSeg.address() == 0 ? null : new Table(this.world, tableSeg);
            return groupByCallback.accept(this.world, table, id);
        }, this.world.arena());

        groupBy(groupId);
        ecs_query_desc_t.group_by_callback(this.queryDesc, callbackStub);
        return builder();
    }

    public S groupBy(long groupId, GroupByContextCallback groupByCallback) {
        MemorySegment callbackStub = ecs_group_by_action_t.allocate((_, tableSeg, id, ctxSeg) -> {
            Table table = tableSeg.address() == 0 ? null : new Table(this.world, tableSeg);
            Object ctx = ctxSeg.address() == 0 ? null : ParamRegistry.get(ctxSeg.address());
            return groupByCallback.accept(this.world, table, id, ctx);
        }, this.world.arena());

        groupBy(groupId);
        ecs_query_desc_t.group_by_callback(this.queryDesc, callbackStub);
        return builder();
    }

    public S groupBy(Class<?> componentClass) {
        return groupBy(this.world.componentRegistry().getComponentId(componentClass));
    }

    public S groupBy(Class<?> componentClass, GroupByCallback groupByCallback) {
        return groupBy(this.world.componentRegistry().getComponentId(componentClass), groupByCallback);
    }

    public S groupBy(Class<?> componentClass, GroupByContextCallback groupByCallback) {
        return groupBy(this.world.componentRegistry().getComponentId(componentClass), groupByCallback);
    }

    public S groupBy(Entity entity) {
        return groupBy(entity.id());
    }

    public S groupBy(Entity entity, GroupByCallback groupByCallback) {
        return groupBy(entity.id(), groupByCallback);
    }

    public S groupBy(Entity entity, GroupByContextCallback groupByCallback) {
        return groupBy(entity.id(), groupByCallback);
    }

    public S groupByCtx(Object ctx) {
        return groupByCtx(ctx, null);
    }

    public S groupByCtx(Object ctx, Consumer<Object> ctxFree) {
        long offset = ecs_query_desc_t.group_by_ctx$offset();
        MemorySegment prev = this.queryDesc.get(ValueLayout.ADDRESS, offset);
        if (prev != null && prev.address() != 0) {
            ParamRegistry.remove(prev.address());
            this.world.untrackCtx(prev.address());
        }

        if (ctx == null) {
            this.queryDesc.set(ValueLayout.ADDRESS, offset, MemorySegment.NULL);
        } else {
            long id = ParamRegistry.put(ctx);
            this.queryDesc.set(ValueLayout.ADDRESS, offset, MemorySegment.ofAddress(id));
            this.world.trackCtx(id);
        }

        MemorySegment freeStub = MemorySegment.NULL;
        if (ctxFree != null) {
            freeStub = ecs_ctx_free_t.allocate(ctxSeg -> {
                Object received = ctxSeg.address() == 0 ? null : ParamRegistry.get(ctxSeg.address());
                if (ctxSeg.address() != 0) {
                    ParamRegistry.remove(ctxSeg.address());
                    this.world.untrackCtx(ctxSeg.address());
                }
                ctxFree.accept(received);
            }, this.world.arena());
        }
        ecs_query_desc_t.group_by_ctx_free(this.queryDesc, freeStub);
        return builder();
    }

    public S onGroupCreate(GroupCreateCallback callback) {
        MemorySegment callbackStub = ecs_group_create_action_t.allocate((_, groupId, ctxSeg) -> {
            Object ctx = ctxSeg.address() == 0 ? null : ParamRegistry.get(ctxSeg.address());
            Object groupCtx = callback.accept(this.world, groupId, ctx);
            if (groupCtx == null) {
                return MemorySegment.NULL;
            }
            long id = ParamRegistry.put(groupCtx);
            this.world.trackCtx(id);
            return MemorySegment.ofAddress(id);
        }, this.world.arena());

        ecs_query_desc_t.on_group_create(this.queryDesc, callbackStub);
        return builder();
    }

    public S onGroupDelete(GroupDeleteCallback callback) {
        MemorySegment callbackStub = ecs_group_delete_action_t.allocate((_, groupId, groupCtxSeg, ctxSeg) -> {
            Object groupCtx = groupCtxSeg.address() == 0 ? null : ParamRegistry.get(groupCtxSeg.address());
            Object ctx = ctxSeg.address() == 0 ? null : ParamRegistry.get(ctxSeg.address());
            callback.accept(this.world, groupId, groupCtx, ctx);
            if (groupCtxSeg.address() != 0) {
                ParamRegistry.remove(groupCtxSeg.address());
                this.world.untrackCtx(groupCtxSeg.address());
            }
        }, this.world.arena());

        ecs_query_desc_t.on_group_delete(this.queryDesc, callbackStub);
        return builder();
    }

    private MemorySegment newTerm() {
        if (this.termCount >= flecs_h.FLECS_TERM_COUNT_MAX()) {
            throw new IllegalStateException("Maximum number of terms (" + flecs_h.FLECS_TERM_COUNT_MAX() + ") reached");
        }
        this.selectedTerm = -1;
        this.selectedRef = TermRef.SRC;
        return ecs_query_desc_t.terms(this.queryDesc, this.termCount++);
    }

    private MemorySegment term(int index) {
        return ecs_query_desc_t.terms(this.queryDesc, index);
    }

    private int selectedTermIndex() {
        return this.selectedTerm < 0 ? this.termCount - 1 : this.selectedTerm;
    }

    private MemorySegment configuredTerm(String modifier) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply '" + modifier + "' modifier to");
        }
        return term(selectedTermIndex());
    }

    private MemorySegment termForConfiguration() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to configure");
        }
        return term(selectedTermIndex());
    }

    private MemorySegment currentTermRef(String modifier) {
        MemorySegment termSeg = configuredTerm(modifier);
        return switch (this.selectedRef) {
            case TermRef.FIRST -> ecs_term_t.first(termSeg);
            case TermRef.SECOND -> ecs_term_t.second(termSeg);
            default -> ecs_term_t.src(termSeg);
        };
    }

    private void assertSrcRef(String message) {
        if (this.selectedRef != TermRef.SRC) {
            throw new IllegalStateException(message);
        }
    }

    private void applyNameOrVar(MemorySegment refSeg, String name) {
        if (name.startsWith("$")) {
            ecs_term_ref_t.id(refSeg, ecs_term_ref_t.id(refSeg) | Flecs.IsVariable);
            ecs_term_ref_t.name(refSeg, this.arena.allocateFrom(name.substring(1)));
        } else {
            ecs_term_ref_t.id(refSeg, ecs_term_ref_t.id(refSeg) | Flecs.IsEntity);
            ecs_term_ref_t.name(refSeg, this.arena.allocateFrom(name));
        }
    }
}
