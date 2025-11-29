package com.github.elebras1.flecs;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public interface Component<T> {

    MemoryLayout layout();

    void write(MemorySegment segment, T data);

    T read(MemorySegment segment);

    T[] createArray(int size);

    default long size() {
        return layout().byteSize();
    }

    default long alignment() {
        return layout().byteAlignment();
    }
}