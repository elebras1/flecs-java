package com.github.elebras1.flecs;

import com.github.elebras1.flecs.util.FlecsConstants;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static com.github.elebras1.flecs.util.FlecsConstants.*;

public class SystemBuilder {

    private final World world;
    private final MemorySegment desc;
    private final Arena arena;
    private int termCount = 0;
    private static final long TERM_SIZE = ecs_term_t.layout().byteSize();
    private Query.IterCallback iterCallback;
    private Query.RunCallback runCallback;
    private Query.EntityCallback entityCallback;
    private long phase = 0;

    public SystemBuilder(World world) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_system_desc_t.allocate(this.arena);
    }

    public SystemBuilder(World world, String name) {
        this(world);
        MemorySegment nameSegment = this.arena.allocateFrom(name);

        MemorySegment entityDescTemp = ecs_entity_desc_t.allocate(this.arena);
        ecs_entity_desc_t.name(entityDescTemp, nameSegment);
        ecs_system_desc_t.entity(this.desc, flecs_h.ecs_entity_init(world.nativeHandle(), entityDescTemp));
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
        
        MemorySegment queryDesc = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
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

    public SystemBuilder with(long relationId, long objectId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }

        long pairId = flecs_h.ecs_make_pair(relationId, objectId);

        MemorySegment queryDesc = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + (this.termCount * TERM_SIZE);

        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
        long idOffset = ecs_term_t.id$offset();

        term.set(ValueLayout.JAVA_LONG, idOffset, pairId);

        this.termCount++;
        return this;
    }

    public SystemBuilder in() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'in' modifier to");
        }

        MemorySegment queryDesc = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        term.set(ValueLayout.JAVA_INT, inoutOffset, EcsIn);

        return this;
    }

    public SystemBuilder out() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'out' modifier to");
        }

        MemorySegment queryDesc = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        term.set(ValueLayout.JAVA_INT, inoutOffset, EcsOut);

        return this;
    }

    public SystemBuilder inOut() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'inout' modifier to");
        }

        MemorySegment queryDesc = ecs_system_desc_t.query(this.desc);
        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = queryDesc.asSlice(termOffset, TERM_SIZE);
        long inoutOffset = ecs_term_t.inout$offset();

        term.set(ValueLayout.JAVA_INT, inoutOffset, EcsInOut);

        return this;
    }

    public SystemBuilder operator(int operator) {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'operator' modifier to");
        }

        long termsOffset = ecs_query_desc_t.terms$offset();
        long termOffset = termsOffset + ((this.termCount - 1) * TERM_SIZE);

        MemorySegment term = this.desc.asSlice(termOffset, TERM_SIZE);
        ecs_term_t.oper(term, (short) operator);
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

    public FlecsSystem iter(Query.IterCallback callback) {
        this.iterCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(it -> {
            Iter iter = new Iter(it, this.world);
            var cache = new FlecsContext.ViewCache(this.world);
            ScopedValue.where(FlecsContext.CURRENT_CACHE, cache).run(() -> {
                callback.accept(iter);
            });
        }, this.world.arena());

        ecs_system_desc_t.callback(this.desc, callbackStub);

        return build();
    }

    public FlecsSystem run(Query.RunCallback callback) {
        this.runCallback = callback;

        MemorySegment callbackStub = ecs_run_action_t.allocate(it -> {
            Iter iter = new Iter(it, this.world);
            var cache = new FlecsContext.ViewCache(this.world);
            ScopedValue.where(FlecsContext.CURRENT_CACHE, cache).run(() -> {
                callback.accept(iter);
            });
        }, this.world.arena());

        ecs_system_desc_t.run(this.desc, callbackStub);

        return build();
    }

    public FlecsSystem each(Query.EntityCallback callback) {
        this.entityCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(it -> {
            var cache = new FlecsContext.ViewCache(this.world);
            ScopedValue.where(FlecsContext.CURRENT_CACHE, cache).run(() -> {
                int count = ecs_iter_t.count(it);
                MemorySegment entities = ecs_iter_t.entities(it);

                for (int i = 0; i < count; i++) {
                    long entityId = entities.getAtIndex(ValueLayout.JAVA_LONG, i);
                    callback.accept(entityId);
                }
            });
        }, this.world.arena());

        ecs_system_desc_t.callback(this.desc, callbackStub);

        return build();
    }

    private FlecsSystem build() {
        long systemId = flecs_h.ecs_system_init(this.world.nativeHandle(), this.desc);

        if (systemId == 0) {
            throw new IllegalStateException("Failed to create system");
        }

        if (this.phase != 0) {
            flecs_h.ecs_add_id(this.world.nativeHandle(), systemId, this.phase);

            long dependsOnPair = flecs_h.ecs_make_pair(EcsDependsOn, this.phase);
            flecs_h.ecs_add_id(this.world.nativeHandle(), systemId, dependsOnPair);
        }

        this.world.registerSystemCallbacks(systemId, this.iterCallback, this.runCallback, this.entityCallback);

        this.arena.close();

        return new FlecsSystem(this.world, systemId);
    }
}

