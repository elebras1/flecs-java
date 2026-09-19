package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.ComparatorComponent;
import io.github.elebras1.flecs.callback.ComparatorComponentView;
import io.github.elebras1.flecs.callback.ComparatorId;
import io.github.elebras1.flecs.callback.GroupByCallback;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public abstract class QueryTermBuilder<S extends QueryTermBuilder<S>> {

    protected final World world;
    private final Arena arena;
    private final MemorySegment queryDesc;
    private int termCount;
    private int selectedTerm = -1;
    private int selectedRef;

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

    public S cached() {
        ecs_query_desc_t.cache_kind(this.queryDesc, Flecs.QueryCacheAuto);
        return builder();
    }

    public S detectChanges() {
        return queryFlags(Flecs.QueryDetectChanges);
    }

    public S expr(String expr) {
        ecs_query_desc_t.expr(this.queryDesc, this.arena.allocateFrom(expr));
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
        ecs_term_ref_t.name(firstRefSeg, this.arena.allocateFrom(componentName));
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
        ecs_term_ref_t.name(firstRefSeg, this.arena.allocateFrom(first));

        MemorySegment secondRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.name(secondRefSeg, this.arena.allocateFrom(second));

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
        ecs_term_ref_t.id(firstRefSeg, Flecs.IsEntity);
        ecs_term_ref_t.name(firstRefSeg, this.arena.allocateFrom(first));

        MemorySegment secondRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(secondRefSeg, second);

        ecs_term_t.first(termSeg, firstRefSeg);
        ecs_term_t.second(termSeg, secondRefSeg);

        this.selectedRef = TermRef.SECOND;

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

        this.selectedRef = TermRef.SECOND;

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
        return builder();
    }

    public S term() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No terms in builder");
        }
        this.selectedTerm = this.termCount - 1;
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

    public S read() {
        return in();
    }

    public S write() {
        return out();
    }

    public S readWrite() {
        return inout();
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

    public S src(long entityId) {
        MemorySegment srcRefSeg = ecs_term_t.src(termForConfiguration());
        ecs_term_ref_t.id(srcRefSeg, entityId);
        this.selectedRef = TermRef.SRC;
        return builder();
    }

    public S src(Entity entity) {
        return src(entity.id());
    }

    public <T> S src(Class<T> componentClass) {
        return src(this.world.componentRegistry().getComponentId(componentClass));
    }

    public S src(String name) {
        MemorySegment srcRefSeg = ecs_term_t.src(termForConfiguration());
        applyNameOrVar(srcRefSeg, name);
        this.selectedRef = TermRef.SRC;
        return builder();
    }

    public S second(long entityId) {
        MemorySegment secondRefSeg = ecs_term_t.second(secondTermForConfiguration());
        ecs_term_ref_t.id(secondRefSeg, entityId);
        this.selectedRef = TermRef.SECOND;
        return builder();
    }

    public S second(Entity entity) {
        return second(entity.id());
    }

    public <T> S second(Class<T> componentClass) {
        return second(this.world.componentRegistry().getComponentId(componentClass));
    }

    public S second(String name) {
        MemorySegment secondRefSeg = ecs_term_t.second(secondTermForConfiguration());
        applyNameOrVar(secondRefSeg, name);
        this.selectedRef = TermRef.SECOND;
        return builder();
    }

    public S flags(long flags) {
        MemorySegment termSeg = termForConfiguration();
        MemorySegment refSeg = switch (this.selectedRef) {
            case TermRef.FIRST -> ecs_term_t.first(termSeg);
            case TermRef.SECOND -> ecs_term_t.second(termSeg);
            default -> ecs_term_t.src(termSeg);
        };
        ecs_term_ref_t.id(refSeg, flags);
        return builder();
    }

    public S self() {
        MemorySegment srcRefSeg = ecs_term_t.src(configuredTerm("self"));
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsSelf());
        return builder();
    }

    public S up() {
        MemorySegment srcRefSeg = ecs_term_t.src(configuredTerm("up"));
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsUp());
        return builder();
    }

    public S up(long trav) {
        up();
        ecs_term_t.trav(configuredTerm("up"), trav);
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

        MemorySegment termSeg = configuredTerm("cascade");
        MemorySegment srcRefSeg = ecs_term_t.src(termSeg);
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsCascade());

        return builder();
    }

    public S cascade(long trav) {
        cascade();
        ecs_term_t.trav(configuredTerm("cascade"), trav);
        return builder();
    }

    public S cascade(Entity trav) {
        return cascade(trav.id());
    }

    public <T> S cascade(Class<T> trav) {
        return cascade(this.world.componentRegistry().getComponentId(trav));
    }

    public S desc() {
        MemorySegment srcRefSeg = ecs_term_t.src(configuredTerm("desc"));
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsDesc());
        return builder();
    }

    public S trav(long trav) {
        ecs_term_t.trav(configuredTerm("trav"), trav);
        return builder();
    }

    public S var(String varName) {
        MemorySegment srcRefSeg = ecs_term_t.src(configuredTerm("var"));
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsIsVariable());
        ecs_term_ref_t.name(srcRefSeg, this.arena.allocateFrom(varName));
        return builder();
    }

    public S scopeOpen() {
        MemorySegment termSeg = newTerm();
        ecs_term_t.id(termSeg, Flecs.ScopeOpen);
        ecs_term_ref_t.id(ecs_term_t.src(termSeg), Flecs.IsEntity);
        return builder();
    }

    public S scopeClose() {
        MemorySegment termSeg = newTerm();
        ecs_term_t.id(termSeg, Flecs.ScopeClose);
        ecs_term_ref_t.id(ecs_term_t.src(termSeg), Flecs.IsEntity);
        return builder();
    }

    public S orderBy(long componentId) {
        ecs_query_desc_t.order_by(this.queryDesc, componentId);
        return builder();
    }

    public S orderBy(long componentId, ComparatorId comparator) {
        MemorySegment callbackStub = ecs_order_by_action_t.allocate((idA, _, idB, _) ->
                comparator.compare(idA, idB), this.world.arena());

        ecs_query_desc_t.order_by_callback(this.queryDesc, callbackStub);
        return orderBy(componentId);
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

        ecs_query_desc_t.order_by_callback(this.queryDesc, callbackStub);
        return orderBy(componentId);
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

        ecs_query_desc_t.order_by_callback(this.queryDesc, callbackStub);
        return orderBy(componentId);
    }

    public S orderBy(Entity entity) {
        return orderBy(entity.id());
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

    public S orderBy(Class<?> componentClass) {
        return orderBy(this.world.componentRegistry().getComponentId(componentClass));
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
        return builder();
    }

    public S groupBy(long groupId, GroupByCallback groupByCallback) {
        MemorySegment callbackStub = ecs_group_by_action_t.allocate((_, tableSeg, id, _) -> {
            Table table = tableSeg.address() == 0 ? null : new Table(this.world, tableSeg);
            return groupByCallback.accept(this.world, table, id);
        }, this.world.arena());

        ecs_query_desc_t.group_by_callback(this.queryDesc, callbackStub);
        return groupBy(groupId);
    }

    public S groupBy(Class<?> componentClass) {
        return groupBy(this.world.componentRegistry().getComponentId(componentClass));
    }

    public S groupBy(Class<?> componentClass, GroupByCallback groupByCallback) {
        return groupBy(this.world.componentRegistry().getComponentId(componentClass), groupByCallback);
    }

    public S groupBy(Entity entity) {
        return groupBy(entity.id());
    }

    public S groupBy(Entity entity, GroupByCallback groupByCallback) {
        return groupBy(entity.id(), groupByCallback);
    }

    private MemorySegment newTerm() {
        if (this.termCount >= flecs_h.FLECS_TERM_COUNT_MAX()) {
            throw new IllegalStateException("Maximum number of terms (" + flecs_h.FLECS_TERM_COUNT_MAX() + ") reached");
        }
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

    private MemorySegment secondTermForConfiguration() {
        MemorySegment termSeg = termForConfiguration();
        long termId = ecs_term_t.id(termSeg);
        if (termId != 0 && ecs_term_ref_t.id(ecs_term_t.first(termSeg)) == 0) {
            ecs_term_ref_t.id(ecs_term_t.first(termSeg), termId);
            ecs_term_t.id(termSeg, 0);
        }
        return termSeg;
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
