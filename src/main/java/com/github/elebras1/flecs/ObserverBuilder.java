package com.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static com.github.elebras1.flecs.FlecsConstants.*;

public class ObserverBuilder {

    private final World world;
    private final Arena arena;
    private final MemorySegment desc;
    private int termCount;
    private int eventCount;
    private Query.IterCallback iterCallback;
    private Query.RunCallback runCallback;
    private Query.EntityCallback entityCallback;
    private Iter iter;
    private static final long TERM_SIZE = ecs_term_t.layout().byteSize();
    private static final int MAX_EVENTS = 8;

    public ObserverBuilder(World world) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_observer_desc_t.allocate(this.arena);
        this.termCount = 0;
        this.eventCount = 0;
        this.desc.fill((byte) 0);
        this.iter = null;
    }

    public ObserverBuilder(World world, String name) {
        this(world);
        MemorySegment nameSegment = this.arena.allocateFrom(name);

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment entityDescTemp = ecs_entity_desc_t.allocate(tempArena);
            ecs_entity_desc_t.name(entityDescTemp, nameSegment);
            ecs_observer_desc_t.entity(this.desc, flecs_h.ecs_entity_init(world.nativeHandle(), entityDescTemp));
        }
    }

    public ObserverBuilder event(long eventId) {
        if (this.eventCount >= MAX_EVENTS) {
            throw new IllegalStateException("Maximum number of events (" + MAX_EVENTS + ") reached");
        }

        MemorySegment events = ecs_observer_desc_t.events(this.desc);
        events.setAtIndex(ValueLayout.JAVA_LONG, this.eventCount, eventId);
        this.eventCount++;

        return this;
    }

    public ObserverBuilder event(Entity event) {
        return this.event(event.id());
    }

    public ObserverBuilder with(long componentId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        MemorySegment queryDesc = ecs_observer_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
        long idOffset = ecs_term_t.id$offset();

        term.set(ValueLayout.JAVA_LONG, idOffset, componentId);

        this.termCount++;
        return this;
    }

    public ObserverBuilder with(Entity tagEntity) {
        return with(tagEntity.id());
    }

    public <T> ObserverBuilder with(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(componentId);
    }

    public ObserverBuilder with(long relationId, long objectId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        long pairId = flecs_h.ecs_make_pair(relationId, objectId);

        MemorySegment queryDesc = ecs_observer_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
        long idOffset = ecs_term_t.id$offset();

        term.set(ValueLayout.JAVA_LONG, idOffset, pairId);

        this.termCount++;
        return this;
    }

    public ObserverBuilder without(long componentId) {
        this.with(componentId);

        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'without' modifier to");
        }

        MemorySegment queryDesc = ecs_observer_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
        long operOffset = ecs_term_t.oper$offset();

        term.set(ValueLayout.JAVA_SHORT, operOffset, (short) EcsNot);

        return this;
    }

    public <T> ObserverBuilder without(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.without(componentId);
    }

    public ObserverBuilder filter() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'filter' modifier to");
        }

        MemorySegment queryDesc = ecs_observer_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        term.set(ValueLayout.JAVA_SHORT, inoutOffset, (short) EcsIn);

        return this;
    }

    public ObserverBuilder yieldExisting() {
        ecs_observer_desc_t.yield_existing(this.desc, true);
        return this;
    }

    public ObserverBuilder yieldExisting(boolean yieldExisting) {
        ecs_observer_desc_t.yield_existing(this.desc, yieldExisting);
        return this;
    }

    public ObserverBuilder observerFlags(int flags) {
        ecs_observer_desc_t.flags_(this.desc, flags);
        return this;
    }

    public FlecsObserver iter(Query.IterCallback callback) {
        this.iterCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(it -> {
            if (this.iter == null) {
                this.iter = new Iter(it, this.world);
            } else {
                iter.setNativeIter(it);
            }
            callback.accept(this.iter);
        }, this.world.arena());

        ecs_observer_desc_t.callback(this.desc, callbackStub);

        return build();
    }

    public FlecsObserver run(Query.RunCallback callback) {
        this.runCallback = callback;

        MemorySegment callbackStub = ecs_run_action_t.allocate(it -> {
            if (this.iter == null) {
                this.iter = new Iter(it, this.world);
            } else {
                iter.setNativeIter(it);
            }
            callback.accept(this.iter);
        }, this.world.arena());

        ecs_observer_desc_t.run(this.desc, callbackStub);

        return build();
    }

    public FlecsObserver each(Query.EntityCallback callback) {
        this.entityCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(it -> {
            int count = ecs_iter_t.count(it);
            MemorySegment entities = ecs_iter_t.entities(it);

            for (int i = 0; i < count; i++) {
                long entityId = entities.getAtIndex(ValueLayout.JAVA_LONG, i);
                callback.accept(entityId);
            }
        }, this.world.arena());

        ecs_observer_desc_t.callback(this.desc, callbackStub);

        return build();
    }

    private FlecsObserver build() {
        long observerId = flecs_h.ecs_observer_init(this.world.nativeHandle(), this.desc);

        if (observerId == 0) {
            throw new IllegalStateException("Failed to create observer");
        }

        this.world.registerObserverCallbacks(observerId, this.iterCallback, this.runCallback, this.entityCallback);

        return new FlecsObserver(this.world, observerId);
    }
}

