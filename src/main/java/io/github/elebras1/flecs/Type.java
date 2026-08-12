package io.github.elebras1.flecs;

import io.github.elebras1.flecs.util.internal.FlecsAllocator;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class Type {
    private final World world;
    private final MemorySegment typeSeg;

    Type(World world, MemorySegment typeSeg) {
        this.world = world;
        this.typeSeg = typeSeg;
    }

    public int count() {
        this.world.worldSeg();
        if (this.typeSeg.address() == 0) {
            return 0;
        }
        return ecs_type_t.count(this.typeSeg);
    }

    public Id get(int index) {
        int count = this.count();
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("index: " + index + ", count: " + count);
        }

        MemorySegment arraySeg = ecs_type_t.array(this.typeSeg);
        if (arraySeg.address() == 0) {
            throw new IllegalStateException("Flecs type has no ID array");
        }
        long id = arraySeg.getAtIndex(ValueLayout.JAVA_LONG, index);
        return new Id(this.world, id);
    }

    public long[] array() {
        int count = this.count();
        if (count == 0) {
            return new long[0];
        }

        MemorySegment arraySeg = ecs_type_t.array(this.typeSeg);
        if (arraySeg.address() == 0) {
            return new long[0];
        }
        return arraySeg.reinterpret((long) count * Long.BYTES).toArray(ValueLayout.JAVA_LONG);
    }

    public String str() {
        this.world.worldSeg();
        if (this.typeSeg.address() == 0) {
            return "";
        }

        MemorySegment strSeg = flecs_h.ecs_type_str(this.world.worldSeg(), this.typeSeg);
        if (strSeg.address() == 0) {
            return "";
        }

        try {
            return strSeg.reinterpret(Long.MAX_VALUE).getString(0);
        } finally {
            FlecsAllocator.free(strSeg);
        }
    }
}
