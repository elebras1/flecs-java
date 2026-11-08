package io.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

final class FieldRef {
    static final long EMPTY_BASE = 1L;

    private final MemorySegment iterSeg;
    private final long size;
    private final byte index;
    private final boolean isRow;
    private final long base;
    private final long stride;

    FieldRef(MemorySegment iterSeg, long size, int index) {
        this.iterSeg = iterSeg;
        this.size = size;
        this.index = (byte) index;
        this.isRow = size != 0 && (ecs_iter_t.row_fields(iterSeg) & (1 << index)) != 0;
        if (size == 0) {
            this.base = EMPTY_BASE;
            this.stride = 0;
        } else if (this.isRow) {
            this.base = 0;
            this.stride = 0;
        } else {
            this.base = flecs_h.ecs_base_field_w_size(iterSeg, size, this.index).address();
            this.stride = this.base != 0 && flecs_h.ecs_field_is_self(iterSeg, this.index)
                    ? flecs_h.ecs_field_stride(iterSeg, this.index)
                    : 0;
        }
    }

    static boolean requiredByQuery(MemorySegment querySeg) {
        return ecs_query_t.row_fields(querySeg) != 0
                || (ecs_query_t.flags(querySeg) & flecs_h.EcsQueryHasComponentInheritance()) != 0;
    }

    static boolean requiredByIter(MemorySegment iterSeg) {
        return ecs_iter_t.row_fields(iterSeg) != 0
                || (ecs_iter_t.flags(iterSeg) & flecs_h.EcsIterComponentInheritance()) != 0;
    }

    long address(int row) {
        return this.isRow
                ? flecs_h.ecs_field_at_w_size(this.iterSeg, this.size, this.index, row).address()
                : this.base + row * this.stride;
    }
}
