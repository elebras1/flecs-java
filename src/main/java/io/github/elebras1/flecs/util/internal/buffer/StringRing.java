package io.github.elebras1.flecs.util.internal.buffer;

import io.github.elebras1.flecs.util.internal.FlecsAllocator;

import java.lang.foreign.MemorySegment;

public final class StringRing implements AutoCloseable {
    private final MemorySegment[] slots;
    private final long[] capacities;
    private int cursor;

    StringRing(int slotCount, long initialCapacity) {
        this.slots = new MemorySegment[slotCount];
        this.capacities = new long[slotCount];
        this.cursor = 0;
        for (int i = 0; i < slotCount; i++) {
            this.slots[i] = FlecsAllocator.malloc(initialCapacity);
            this.capacities[i] = initialCapacity;
        }
    }

    public MemorySegment set(String value) {
        int i = this.cursor;
        this.cursor = (this.cursor + 1) % this.slots.length;

        long needed = value.length() + 1L;
        if (needed > this.capacities[i]) {
            FlecsAllocator.free(this.slots[i]);
            this.capacities[i] = Math.max(needed * 2, this.capacities[i] * 2);
            this.slots[i] = FlecsAllocator.malloc(this.capacities[i]);
        }

        MemorySegment seg = this.slots[i];
        seg.setString(0, value);
        return seg;
    }

    @Override
    public void close() {
        for (MemorySegment seg : this.slots) {
            if (seg != null && seg.address() != 0) {
                FlecsAllocator.free(seg);
            }
        }
    }
}