package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.EntityCallback;
import io.github.elebras1.flecs.callback.IterCallback;
import io.github.elebras1.flecs.callback.RunCallback;
import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class SystemBuilder extends SystemBuilderBase {

    private final Arena arena;
    private final Iter[] iters;
    private IterCallback iterCallback;
    private RunCallback runCallback;
    private EntityCallback entityCallback;
    private long phase;

    public SystemBuilder(World world) {
        this(world, Arena.ofConfined());
    }

    public SystemBuilder(World world, String name) {
        this(world);
        MemorySegment nameSegment = this.arena.allocateFrom(name);

        MemorySegment entityDescTemp = ecs_entity_desc_t.allocate(this.arena);
        ecs_entity_desc_t.name(entityDescTemp, nameSegment);
        ecs_system_desc_t.entity(this.desc, flecs_h.ecs_entity_init(world.worldSeg(), entityDescTemp));
    }

    private SystemBuilder(World world, Arena arena) {
        super(world, arena, ecs_system_desc_t.allocate(arena));
        this.arena = arena;
        this.iters = new Iter[world.getStageCount()];
        for(int i = 0; i < this.iters.length; i++) {
            World worldStage = world.getStage(i);
            this.iters[i] = new Iter(MemorySegment.NULL, worldStage);
        }
        this.phase = Flecs.OnUpdate;
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

    public SystemBuilder tickSource(Timer tickSource) {
        return this.tickSource(tickSource.id());
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

    public SystemBuilder ctx(Object ctx) {
        MemorySegment prev = ecs_system_desc_t.ctx(this.desc);
        if (prev != null && prev.address() != 0) {
            ParamRegistry.remove(prev.address());
            this.world.untrackCtx(prev.address());
        }

        long id = ParamRegistry.put(ctx);
        ecs_system_desc_t.ctx(this.desc, MemorySegment.ofAddress(id));
        this.world.trackCtx(id);
        return this;
    }

    public SystemBuilder readWrite(long componentId) {
        return this.with(componentId).inout();
    }

    public SystemBuilder readWrite(Entity entity) {
        return this.with(entity.id()).inout();
    }

    public <T> SystemBuilder readWrite(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.readWrite(componentId);
    }

    public SystemBuilder write(long componentId) {
        return this.with(componentId).out();
    }

    public SystemBuilder write(Entity entity) {
        return this.with(entity.id()).out();
    }

    public <T> SystemBuilder write(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.write(componentId);
    }

    public SystemBuilder read(long componentId) {
        return this.with(componentId).in();
    }

    public SystemBuilder read(Entity entity) {
        return this.read(entity.id());
    }

    public <T> SystemBuilder read(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.read(componentId);
    }

    @Override
    protected Iter iterFor(MemorySegment iterSegment) {
        MemorySegment stageSeg = ecs_iter_t.world(iterSegment);
        int stageId = flecs_h.ecs_stage_get_id(stageSeg);
        Iter iter = this.iters[stageId];
        iter.setIterSeg(iterSegment);
        return iter;
    }

    public FlecsSystem iter(IterCallback callback) {
        this.iterCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(iterSegment -> {
            Iter iter = this.iterFor(iterSegment);
            iter.world().viewCache().resetCursors();
            callback.accept(iter);
        }, this.world.arena());

        ecs_system_desc_t.callback(this.desc, callbackStub);

        return build();
    }

    public FlecsSystem run(RunCallback callback) {
        this.runCallback = callback;

        MemorySegment callbackStub = ecs_run_action_t.allocate(iterSeg -> {
            MemorySegment stageSeg = ecs_iter_t.world(iterSeg);
            int stageId = flecs_h.ecs_stage_get_id(stageSeg);
            Iter iter = this.iters[stageId];
            iter.setIterSeg(iterSeg);
            iter.world().viewCache().resetCursors();
            callback.accept(this.iters[stageId]);
        }, this.world.arena());

        ecs_system_desc_t.run(this.desc, callbackStub);

        return build();
    }

    public FlecsSystem each(EntityCallback callback) {
        this.entityCallback = callback;

        MemorySegment callbackStub = ecs_iter_action_t.allocate(iterSeg -> {
            int count = ecs_iter_t.count(iterSeg);
            MemorySegment entitiesSeg = ecs_iter_t.entities(iterSeg);

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

            long dependsOnPair = flecs_h.ecs_make_pair(Flecs.DependsOn, this.phase);
            flecs_h.ecs_add_id(this.world.worldSeg(), systemId, dependsOnPair);
        }

        this.world.registerSystemCallbacks(systemId, this.iterCallback, this.runCallback, this.entityCallback);

        this.arena.close();

        return new FlecsSystem(this.world, systemId);
    }
}
