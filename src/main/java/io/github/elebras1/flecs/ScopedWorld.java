package io.github.elebras1.flecs;

public final class ScopedWorld extends World implements AutoCloseable {

    private final long scope;
    private boolean closed;

    ScopedWorld(World world, long scopeEntityId) {
        super(world.worldSeg(), world.componentRegistry());
        this.scope = this.setScope(scopeEntityId);
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.setScope(this.scope);
            this.closed = true;
        }
        this.destroy();
    }
}
