package io.github.elebras1.flecs.util.internal.buffer;

import io.github.elebras1.flecs.util.internal.FlecsAllocator;

import java.lang.foreign.MemorySegment;

public final class ComponentBuffer implements AutoCloseable {
    private MemorySegment segment;
    private long capacity;

    public ComponentBuffer(long initialCapacity) {
        this.capacity = initialCapacity;
        this.segment = FlecsAllocator.malloc(initialCapacity);
    }

    public MemorySegment ensure(long needed) {
        if (needed > this.capacity) {
            this.capacity = Math.max(needed, this.capacity * 2);
            FlecsAllocator.free(segment);
            this.segment = FlecsAllocator.malloc(this.capacity);
        }
        return this.segment.fill((byte) 0);
    }

    @Override
    public void close() {
        if (this.segment != null && this.segment.address() != 0) {
            FlecsAllocator.free(this.segment);
        }
    }
}
