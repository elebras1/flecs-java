package io.github.elebras1.flecs.util.internal;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public final class FlecsMemoryStack implements SegmentAllocator, AutoCloseable {

    private MemorySegment segment;
    private long capacity;
    private long offset;
    private final long[] frames = new long[32];
    private int frameIndex;

    public FlecsMemoryStack(long initialCapacity) {
        this.capacity = initialCapacity;
        this.segment = FlecsAllocator.malloc(initialCapacity);
    }

    public FlecsMemoryStack push() {
        this.frames[this.frameIndex++] = this.offset;
        return this;
    }

    @Override
    public void close() {
        this.offset = this.frames[--this.frameIndex];
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        long alignedOffset = alignUp(this.offset, byteAlignment);
        ensure(alignedOffset + byteSize);
        MemorySegment slice = this.segment.asSlice(alignedOffset, byteSize);
        this.offset = alignedOffset + byteSize;
        return slice;
    }

    public MemorySegment allocateUtf8(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        MemorySegment slice = allocate(bytes.length + 1, 1);
        MemorySegment.copy(bytes, 0, slice, ValueLayout.JAVA_BYTE, 0, bytes.length);
        slice.set(ValueLayout.JAVA_BYTE, bytes.length, (byte) 0);
        return slice;
    }

    private static long alignUp(long value, long alignment) {
        return (value + alignment - 1) & -alignment;
    }

    private void ensure(long needed) {
        if (needed > this.capacity) {
            long newCapacity = Math.max(needed, this.capacity * 2);
            MemorySegment newSeg = FlecsAllocator.malloc(newCapacity);
            MemorySegment.copy(this.segment, 0, newSeg, 0, this.offset);
            FlecsAllocator.free(this.segment);
            this.segment = newSeg;
            this.capacity = newCapacity;
        }
    }

    public void free() {
        if (this.segment != null && this.segment.address() != 0) {
            FlecsAllocator.free(this.segment);
        }
    }
}