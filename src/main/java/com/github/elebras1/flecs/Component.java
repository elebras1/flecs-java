package com.github.elebras1.flecs;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public interface Component<T> {

    MemoryLayout layout();

    void write(MemorySegment segment, long offset, T data);

    T read(MemorySegment segment, long offset);

    T[] createArray(int size);

    long offsetOf(String fieldName);

    default long size() {
        return layout().byteSize();
    }

    default long alignment() {
        return layout().byteAlignment();
    }
}