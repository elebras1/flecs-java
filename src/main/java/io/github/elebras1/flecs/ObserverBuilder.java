package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.EntityCallback;
import io.github.elebras1.flecs.callback.IterCallback;
import io.github.elebras1.flecs.callback.RunCallback;
import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ObserverBuilder extends ObserverBuilderBase {

    private static final int MAX_EVENTS = 8;

    private final Arena arena;
    private final Iter iter;
    private int eventCount;
    private IterCallback iterCallback;
    private RunCallback runCallback;
    private EntityCallback entityCallback;

    private ObserverBuilder(World world, Arena arena) {
        super(world, arena, ecs_observer_desc_t.allocate(arena));
        this.arena = arena;
        this.iter = new Iter(MemorySegment.NULL, this.world);
        this.eventCount = 0;
    }

    public ObserverBuilder(World world) {
        this(world, Arena.ofConfined());
    }

    public ObserverBuilder(World world, String name) {
        this(world, Arena.ofConfined());
        MemorySegment nameSegment = this.arena.allocateFrom(name);

        MemorySegment entityDescTemp = ecs_entity_desc_t.allocate(this.arena);
        ecs_entity_desc_t.name(entityDescTemp, nameSegment);
        ecs_entity_desc_t.sep(entityDescTemp, this.arena.allocateFrom("::"));
        ecs_entity_desc_t.root_sep(entityDescTemp, this.arena.allocateFrom("::"));
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

        if (ctx == null) {
            ecs_observer_desc_t.ctx(this.desc, MemorySegment.NULL);
            return this;
        }
        long id = ParamRegistry.put(ctx);
        ecs_observer_desc_t.ctx(this.desc, MemorySegment.ofAddress(id));
        this.world.trackCtx(id);
        return this;
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
