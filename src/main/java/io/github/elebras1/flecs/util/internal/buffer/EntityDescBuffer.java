package io.github.elebras1.flecs.util.internal.buffer;

import io.github.elebras1.flecs.ecs_entity_desc_t;
import io.github.elebras1.flecs.util.internal.FlecsAllocator;

import java.lang.foreign.MemorySegment;

public final class EntityDescBuffer implements AutoCloseable {
    private final MemorySegment segment;

    public EntityDescBuffer() {
        this.segment = FlecsAllocator.malloc(ecs_entity_desc_t.sizeof());
    }

    public MemorySegment get() {
        this.segment.fill((byte) 0);
        return this.segment;
    }

    @Override
    public void close() {
        FlecsAllocator.free(this.segment);
    }
}