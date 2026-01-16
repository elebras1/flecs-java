package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

public interface ComponentView {
    void setMemorySegment(MemorySegment memorySegment);
}
