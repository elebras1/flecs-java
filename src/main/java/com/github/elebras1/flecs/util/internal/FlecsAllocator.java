package com.github.elebras1.flecs.util.internal;

import com.github.elebras1.flecs.ecs_os_api_free_t;
import com.github.elebras1.flecs.ecs_os_api_malloc_t;
import com.github.elebras1.flecs.ecs_os_api_t;
import com.github.elebras1.flecs.flecs_h;

import java.lang.foreign.MemorySegment;

public final class FlecsAllocator {

    public static MemorySegment malloc(long size) {
        MemorySegment mallocSeg = ecs_os_api_t.malloc_(flecs_h.ecs_os_api());
        return ecs_os_api_malloc_t.invoke(mallocSeg, (int) size).reinterpret(size);
    }

    public static void free(MemorySegment segment) {
        MemorySegment freeSeg = ecs_os_api_t.free_(flecs_h.ecs_os_api());
        ecs_os_api_free_t.invoke(freeSeg, segment);
    }

}
