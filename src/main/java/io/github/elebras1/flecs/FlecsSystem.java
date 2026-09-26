package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.IterCallback;
import io.github.elebras1.flecs.internal.ParamRegistry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class FlecsSystem extends Entity {

    FlecsSystem(World world, long entityId) {
        super(world, entityId);
    }

    public void run() {
        flecs_h.ecs_run(this.world.worldSeg(), this.id, 0.0f, MemorySegment.NULL);
    }

    public void run(float deltaTime) {
        flecs_h.ecs_run(this.world.worldSeg(), this.id, deltaTime, MemorySegment.NULL);
    }

    public <T> void run(T param) {
        long paramId = ParamRegistry.put(param);
        try {
            flecs_h.ecs_run(this.world.worldSeg(), this.id, 0.0f, MemorySegment.ofAddress(paramId));
        } finally {
            ParamRegistry.remove(paramId);
        }
    }

    public <T> void run(float deltaTime, T param) {
        long paramId = ParamRegistry.put(param);
        try {
            flecs_h.ecs_run(this.world.worldSeg(), this.id, deltaTime, MemorySegment.ofAddress(paramId));
        } finally {
            ParamRegistry.remove(paramId);
        }
    }

    public boolean isEnabled() {
        return !this.has(Flecs.Disabled);
    }

    public Object getCtx() {
        MemorySegment sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), this.id);
        if (sysSeg == null || sysSeg.address() == 0) {
            return null;
        }
        MemorySegment ctxSeg = ecs_system_t.ctx(sysSeg);
        if (ctxSeg == null || ctxSeg.address() == 0) {
            return null;
        }
        return ParamRegistry.get(ctxSeg.address());
    }

    public void setCtx(Object ctx) {
        MemorySegment sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), this.id);
        if (sysSeg != null && sysSeg.address() != 0) {
            MemorySegment oldCtx = ecs_system_t.ctx(sysSeg);
            if (oldCtx != null && oldCtx.address() != 0) {
                ParamRegistry.remove(oldCtx.address());
                this.world.untrackCtx(oldCtx.address());
            }
        }

        MemorySegment ctxPtr = MemorySegment.NULL;
        if (ctx != null) {
            long id = ParamRegistry.put(ctx);
            ctxPtr = MemorySegment.ofAddress(id);
            this.world.trackCtx(id);
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment desc = ecs_system_desc_t.allocate(arena);
            ecs_system_desc_t.ctx(desc, ctxPtr);
            flecs_h.ecs_system_update(this.world.worldSeg(), this.id, desc);
        }
    }

    public FlecsSystem interval(float interval) {
        flecs_h.ecs_set_interval(this.world.worldSeg(), this.id, interval);
        return this;
    }

    public float interval() {
        return flecs_h.ecs_get_interval(this.world.worldSeg(), this.id);
    }

    public FlecsSystem timeout(float timeout) {
        flecs_h.ecs_set_timeout(this.world.worldSeg(), this.id, timeout);
        return this;
    }

    public float timeout() {
        return flecs_h.ecs_get_timeout(this.world.worldSeg(), this.id);
    }

    public FlecsSystem rate(int rate) {
        flecs_h.ecs_set_rate(this.world.worldSeg(), this.id, rate, 0);
        return this;
    }

    public FlecsSystem tickSource(long tickSourceId) {
        flecs_h.ecs_set_tick_source(this.world.worldSeg(), this.id, tickSourceId);
        return this;
    }

    public FlecsSystem tickSource(Entity tickSource) {
        return this.tickSource(tickSource.id());
    }

    public void start() {
        flecs_h.ecs_start_timer(this.world.worldSeg(), this.id);
    }

    public void stop() {
        flecs_h.ecs_stop_timer(this.world.worldSeg(), this.id);
    }

    public void runWorker(int stageCurrent, int stageCount) {
        flecs_h.ecs_run_worker(this.world.worldSeg(), this.id, stageCurrent, stageCount, 0.0f, MemorySegment.NULL);
    }

    public void runWorker(int stageCurrent, int stageCount, float deltaTime) {
        flecs_h.ecs_run_worker(this.world.worldSeg(), this.id, stageCurrent, stageCount, deltaTime, MemorySegment.NULL);
    }

    public Query query() {
        MemorySegment sysSeg = flecs_h.ecs_system_get(this.world.worldSeg(), this.id);
        if (sysSeg == null || sysSeg.address() == 0) {
            throw new IllegalStateException("Entity is not a system: " + this.id);
        }
        return new Query(this.world, ecs_system_t.query(sysSeg));
    }

    public FlecsSystem runEach(IterCallback callback) {
        MemorySegment callbackStub = ecs_run_action_t.allocate(iterSeg -> {
            while (flecs_h.ecs_iter_next(iterSeg)) {
                Iter iter = new Iter(iterSeg, this.world);
                iter.setIterSeg(iterSeg);
                callback.accept(iter);
            }
        }, this.world.arena());
        this.updateRunCallback(callbackStub);
        return this;
    }

    private void updateRunCallback(MemorySegment callbackStub) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment desc = ecs_system_desc_t.allocate(arena);
            ecs_system_desc_t.run(desc, callbackStub);
            flecs_h.ecs_system_update(this.world.worldSeg(), this.id, desc);
        }
    }

    public void setGroup(long groupId) {
        flecs_h.ecs_system_set_group(this.world.worldSeg(), this.id, groupId);
    }
}
