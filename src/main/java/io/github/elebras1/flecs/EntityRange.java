package io.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class EntityRange {

    private final MemorySegment rangeSeg;

    EntityRange(MemorySegment rangeSeg) {
        this.rangeSeg = rangeSeg.reinterpret(ecs_entity_range_t.sizeof());
    }

    MemorySegment seg() {
        return this.rangeSeg;
    }

    public int min() {
        return ecs_entity_range_t.min(this.rangeSeg);
    }

    public int max() {
        return ecs_entity_range_t.max(this.rangeSeg);
    }

    public int current() {
        return ecs_entity_range_t.cur(this.rangeSeg);
    }

    public long[] recycled() {
        MemorySegment recycledSeg = ecs_entity_range_t.recycled(this.rangeSeg);
        int count = ecs_vec_t.count(recycledSeg);
        return ecs_vec_t.array(recycledSeg)
            .reinterpret((long) count * ValueLayout.JAVA_LONG.byteSize())
            .toArray(ValueLayout.JAVA_LONG);
    }
}
