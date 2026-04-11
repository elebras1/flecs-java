package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

public interface ComponentRowView {
    void setSegment(MemorySegment segment);

    void setCount(int count);

    MemorySegment segment();

    long size();

    int count();
}
