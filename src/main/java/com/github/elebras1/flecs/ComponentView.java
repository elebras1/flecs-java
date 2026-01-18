package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

public interface ComponentView {
    void setResource(MemorySegment memorySegment, long offset);
}
