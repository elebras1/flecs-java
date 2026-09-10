package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.EntityCallback;
import io.github.elebras1.flecs.callback.IterCallback;
import io.github.elebras1.flecs.callback.RunCallback;
import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ObserverBuilder extends ObserverBuilderBase {

    private final Arena arena;
    private final Iter iter;
    private int termCount;
    private int eventCount;
    private IterCallback iterCallback;
    private RunCallback runCallback;
    private EntityCallback entityCallback;
    private static final int MAX_EVENTS = 8;
    private int selectedTerm;

    private ObserverBuilder(World world, Arena arena) {
        super(world, ecs_observer_desc_t.allocate(arena));
        this.arena = arena;
        this.iter = new Iter(MemorySegment.NULL, this.world);
        this.termCount = 0;
        this.eventCount = 0;
        this.selectedTerm = -1;
    }

    public ObserverBuilder(World world) {
        this(world, Arena.ofConfined());
    }

    public ObserverBuilder(World world, String name) {
        Arena arena = Arena.ofConfined();
        this(world, arena);
        MemorySegment nameSegment = arena.allocateFrom(name);

        MemorySegment entityDescTemp = ecs_entity_desc_t.allocate(arena);
        ecs_entity_desc_t.name(entityDescTemp, nameSegment);
        ecs_observer_desc_t.entity(this.desc, flecs_h.ecs_entity_init(world.worldSeg(), entityDescTemp));
    }

    public ObserverBuilder event(long eventId) {
        if (this.eventCount >= MAX_EVENTS) {
            throw new IllegalStateException("Maximum number of events (" + MAX_EVENTS + ") reached");
        }

        MemorySegment eventsSeg = ecs_observer_desc_t.events(this.desc);
        eventsSeg.setAtIndex(ValueLayout.JAVA_LONG, this.eventCount, eventId);
        this.eventCount++;

        return this;
    }

    public ObserverBuilder event(Entity event) {
        return this.event(event.id());
    }

    public ObserverBuilder ctx(Object ctx) {
        MemorySegment prev = ecs_observer_desc_t.ctx(this.desc);
        if (prev != null && prev.address() != 0) {
            ParamRegistry.remove(prev.address());
            this.world.untrackCtx(prev.address());
        }

        long id = ParamRegistry.put(ctx);
        ecs_observer_desc_t.ctx(this.desc, MemorySegment.ofAddress(id));
        this.world.trackCtx(id);
        return this;
    }

    public ObserverBuilder queryFlags(int flag) {
        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        ecs_query_desc_t.flags(queryDescSeg, ecs_query_desc_t.flags(queryDescSeg) | flag);
        return this;
    }

    public ObserverBuilder cached() {
        ecs_query_desc_t.cache_kind(ecs_observer_desc_t.query(this.desc), Flecs.QueryCacheAuto);
        return this;
    }

    public ObserverBuilder detectChanges() {
        return this.queryFlags(Flecs.QueryDetectChanges);
    }

    public ObserverBuilder with(long componentId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount);
        ecs_term_t.id(termSeg, componentId);

        this.termCount++;
        return this;
    }

    public ObserverBuilder with(String componentName) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount);
        MemorySegment termRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.name(termRefSeg, this.arena.allocateFrom(componentName));
        ecs_term_t.first(termSeg, termRefSeg);

        this.termCount++;
        return this;
    }

    public ObserverBuilder with(Entity entity) {
        return with(entity.id());
    }

    public <T> ObserverBuilder with(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(componentId);
    }

    public ObserverBuilder with(long first, long second) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount);

        MemorySegment firstTermRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(firstTermRefSeg, first);

        MemorySegment secondTermRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.id(secondTermRefSeg, second);

        ecs_term_t.first(termSeg, firstTermRefSeg);
        ecs_term_t.second(termSeg, secondTermRefSeg);

        this.termCount++;
        return this;
    }

    public ObserverBuilder termAt(int index) {
        if (index < 0 || index >= this.termCount) {
            throw new IndexOutOfBoundsException("Invalid observer term index: " + index);
        }
        this.selectedTerm = index;
        return this;
    }

    public ObserverBuilder second(long entityId) {
        MemorySegment termSeg = this.term(indexForConfiguration());
        MemorySegment secondSeg = ecs_term_t.second(termSeg);
        ecs_term_ref_t.id(secondSeg, entityId);

        long relationId = ecs_term_t.id(termSeg);
        if (relationId == 0) {
            relationId = ecs_term_ref_t.id(ecs_term_t.first(termSeg));
        }
        if (relationId != 0) {
            ecs_term_t.id(termSeg, flecs_h.ecs_make_pair(relationId, entityId));
        }
        return this;
    }

    public ObserverBuilder second(Entity entity) {
        return this.second(entity.id());
    }

    public <T> ObserverBuilder second(Class<T> componentClass) {
        return this.second(this.world.componentRegistry().getComponentId(componentClass));
    }

    public ObserverBuilder second(String componentName) {
        MemorySegment termSeg = this.term(indexForConfiguration());
        MemorySegment secondRef = ecs_term_t.second(termSeg);
        ecs_term_ref_t.name(secondRef, this.arena.allocateFrom(componentName));
        return this;
    }

    private int indexForConfiguration() {
        if (this.selectedTerm < 0) {
            throw new IllegalStateException("Call termAt(index) before configuring a term");
        }
        return this.selectedTerm;
    }

    private MemorySegment term(int index) {
        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        return ecs_query_desc_t.terms(queryDescSeg, index);
    }

    public ObserverBuilder with(String first, String second) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount);

        MemorySegment firstTermRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.name(firstTermRefSeg, this.arena.allocateFrom(first));

        MemorySegment secondTermRefSeg = ecs_term_ref_t.allocate(this.arena);
        ecs_term_ref_t.name(secondTermRefSeg, this.arena.allocateFrom(second));

        ecs_term_t.first(termSeg, firstTermRefSeg);
        ecs_term_t.second(termSeg, secondTermRefSeg);

        this.termCount++;
        return this;
    }

    public <T> ObserverBuilder with(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.with(firstId, second);
    }

    public <T> ObserverBuilder with(Class<T> first, Entity second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.with(firstId, second.id());
    }

    public <A, B> ObserverBuilder with(Class<A> first, Class<B> second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        long secondId = this.world.componentRegistry().getComponentId(second);
        return this.with(firstId, secondId);
    }


    public ObserverBuilder without(long componentId) {
        return this.with(componentId).not();
    }

    public ObserverBuilder without(Entity entity) {
        return this.without(entity.id());
    }

    public <T> ObserverBuilder without(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.without(componentId);
    }

    public ObserverBuilder without(long first, long second) {
        return this.with(first, second).not();
    }

    public <T> ObserverBuilder without(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.without(firstId, second);
    }

    public <T> ObserverBuilder without(Class<T> first, Entity second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.without(firstId, second.id());
    }

    public <A, B> ObserverBuilder without(Class<A> first, Class<B> second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        long secondId = this.world.componentRegistry().getComponentId(second);
        return this.without(firstId, secondId);
    }

    public ObserverBuilder in() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'in' modifier to");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        ecs_term_t.inout(termSeg, (short) Flecs.In);

        return this;
    }

    public ObserverBuilder out() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'out' modifier to");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        ecs_term_t.inout(termSeg, (short) Flecs.Out);

        return this;
    }

    public ObserverBuilder inOut() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'inout' modifier to");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        ecs_term_t.inout(termSeg, (short) Flecs.InOut);

        return this;
    }

    public ObserverBuilder oper(int operator) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'operator' modifier to");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        ecs_term_t.oper(termSeg, (short) operator);

        return this;
    }

    public ObserverBuilder and() {
        return this.oper(Flecs.And);
    }

    public ObserverBuilder or() {
        return this.oper(Flecs.Or);
    }

    public ObserverBuilder not() {
        return this.oper(Flecs.Not);
    }

    public ObserverBuilder optional() {
        return this.oper(Flecs.Optional);
    }

    public ObserverBuilder andFrom() {
        return this.oper(Flecs.AndFrom);
    }

    public ObserverBuilder orFrom() {
        return this.oper(Flecs.OrFrom);
    }

    public ObserverBuilder notFrom() {
        return this.oper(Flecs.NotFrom);
    }

    public ObserverBuilder up() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'up' modifier to");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        MemorySegment srcRefSeg = ecs_term_t.src(termSeg);
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsUp());

        return this;
    }

    public ObserverBuilder up(long trav) {
        this.up();

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        ecs_term_t.trav(termSeg, trav);

        return this;
    }

    public ObserverBuilder parent() {
        return this.up();
    }

    public ObserverBuilder cascade() {
        this.up();

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        MemorySegment srcRefSeg = ecs_term_t.src(termSeg);
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsCascade());

        return this;
    }

    public ObserverBuilder cascade(long trav) {
        this.cascade();

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        ecs_term_t.trav(termSeg, trav);

        return this;
    }

    public ObserverBuilder desc() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'desc' modifier to");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        MemorySegment srcRefSeg = ecs_term_t.src(termSeg);
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsDesc());

        return this;
    }

    public ObserverBuilder trav(long trav) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'trav' modifier to");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        ecs_term_t.trav(termSeg, trav);

        return this;
    }

    public ObserverBuilder self() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'self' modifier to");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        MemorySegment srcRefSeg = ecs_term_t.src(termSeg);
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsSelf());

        return this;
    }

    public ObserverBuilder var(String varName) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'var' modifier to");
        }

        MemorySegment queryDescSeg = ecs_observer_desc_t.query(this.desc);
        MemorySegment termSeg = ecs_query_desc_t.terms(queryDescSeg, this.termCount - 1);
        MemorySegment srcRefSeg = ecs_term_t.src(termSeg);
        ecs_term_ref_t.id(srcRefSeg, ecs_term_ref_t.id(srcRefSeg) | flecs_h.EcsIsVariable());
        ecs_term_ref_t.name(srcRefSeg, this.arena.allocateFrom(varName));

        return this;
    }

    public ObserverBuilder read() {
        return this.in();
    }

    public ObserverBuilder write() {
        return this.out();
    }

    public ObserverBuilder readWrite() {
        return this.inOut();
    }

    public ObserverBuilder yieldExisting() {
        return this.yieldExisting(true);
    }

    public ObserverBuilder yieldExisting(boolean yieldExisting) {
        if (yieldExisting && (this.currentObserverFlags() & (Flecs.ObserverYieldOnCreate | Flecs.ObserverYieldOnDelete)) != 0) {
            throw new IllegalStateException("yieldExisting() cannot be combined with ObserverYieldOnCreate/ObserverYieldOnDelete flags");
        }
        ecs_observer_desc_t.yield_existing(this.desc, yieldExisting);
        return this;
    }

    public ObserverBuilder observerFlags(int flags) {
        if (ecs_observer_desc_t.yield_existing(this.desc)
                && (flags & (Flecs.ObserverYieldOnCreate | Flecs.ObserverYieldOnDelete)) != 0) {
            throw new IllegalStateException("ObserverYieldOnCreate/ObserverYieldOnDelete flags cannot be combined with yieldExisting()");
        }
        ecs_observer_desc_t.flags_(this.desc, this.currentObserverFlags() | flags);
        return this;
    }

    private int currentObserverFlags() {
        return ecs_observer_desc_t.flags_(this.desc);
    }

    @Override
    protected Iter iterFor(MemorySegment iterSegment) {
        this.iter.setIterSeg(iterSegment);
        return this.iter;
    }

    public Observer iter(IterCallback callback) {
        this.iterCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(iterSeg -> {
            Iter iter = this.iterFor(iterSeg);
            this.world.viewCache().resetCursors();
            callback.accept(iter);
        }, this.world.arena());

        ecs_observer_desc_t.callback(this.desc, callbackStub);

        return build();
    }

    public Observer run(RunCallback callback) {
        this.runCallback = callback;

        MemorySegment callbackStub = ecs_run_action_t.allocate(iterSeg -> {
            this.iter.setIterSeg(iterSeg);
            this.world.viewCache().resetCursors();
            callback.accept(this.iter);
        }, this.world.arena());

        ecs_observer_desc_t.run(this.desc, callbackStub);

        return build();
    }

    public Observer each(EntityCallback callback) {
        this.entityCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(iterSeg -> {
            int count = ecs_iter_t.count(iterSeg);
            MemorySegment entities = ecs_iter_t.entities(iterSeg);
            for (int i = 0; i < count; i++) {
                long entityId = entities.getAtIndex(ValueLayout.JAVA_LONG, i);
                callback.accept(entityId);
            }
        }, this.world.arena());

        ecs_observer_desc_t.callback(this.desc, callbackStub);

        return build();
    }

    @Override
    protected Observer build() {
        long observerId = flecs_h.ecs_observer_init(this.world.worldSeg(), this.desc);

        if (observerId == 0) {
            throw new IllegalStateException("Failed to create observer");
        }

        this.world.registerObserverCallbacks(observerId, this.iterCallback, this.runCallback, this.entityCallback);

        this.arena.close();

        return new Observer(this.world, observerId);
    }
}
