package io.github.elebras1.flecs;

import java.lang.foreign.Arena;

public final class ScopedWorld extends World implements AutoCloseable {

    private final long scope;
    private boolean closed;

    ScopedWorld(World world, long scopeEntityId) {
        super(world.worldSeg(), world.componentRegistry(), world.realWorld());
        this.scope = this.setScope(scopeEntityId);
    }

    @Override
    public Arena arena() {
        return this.realWorld().arena();
    }

    @Override
    public World getStage(int stageId) {
        return this.realWorld().getStage(stageId);
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.setScope(this.scope);
            this.closed = true;
        }
        super.close();
    }
}
