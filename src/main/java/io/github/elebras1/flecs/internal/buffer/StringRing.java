package io.github.elebras1.flecs.internal.buffer;

import io.github.elebras1.flecs.internal.FlecsAllocator;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public final class StringRing implements AutoCloseable {
    private final MemorySegment[] slots;
    private final long[] capacities;
    private final MemorySegment separator;
    private int cursor;

    StringRing(int slotCount, long initialCapacity) {
        this.slots = new MemorySegment[slotCount];
        this.capacities = new long[slotCount];
        this.cursor = 0;
        for (int i = 0; i < slotCount; i++) {
            this.slots[i] = FlecsAllocator.malloc(initialCapacity);
            this.capacities[i] = initialCapacity;
        }
        this.separator = FlecsAllocator.malloc(3);
        this.separator.setString(0, "::");
    }

    public MemorySegment separator() {
        return this.separator;
    }

    public MemorySegment set(String value) {
        int i = this.cursor;
        this.cursor = (this.cursor + 1) % this.slots.length;

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        long needed = bytes.length + 1L;
        if (needed > this.capacities[i]) {
            FlecsAllocator.free(this.slots[i]);
            this.capacities[i] = Math.max(needed * 2, this.capacities[i] * 2);
            this.slots[i] = FlecsAllocator.malloc(this.capacities[i]);
        }

        MemorySegment seg = this.slots[i];
        MemorySegment.copy(bytes, 0, seg, ValueLayout.JAVA_BYTE, 0, bytes.length);
        seg.set(ValueLayout.JAVA_BYTE, bytes.length, (byte) 0);
        return seg;
    }

    @Override
    public void close() {
        for (MemorySegment seg : this.slots) {
            if (seg != null && seg.address() != 0) {
                FlecsAllocator.free(seg);
            }
        }
        FlecsAllocator.free(this.separator);
    }
}
