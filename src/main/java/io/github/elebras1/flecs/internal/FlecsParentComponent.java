package io.github.elebras1.flecs.internal;

import io.github.elebras1.flecs.Component;
import io.github.elebras1.flecs.FlecsParent;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public final class FlecsParentComponent implements Component<FlecsParent> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        MemoryAccess.longLayout().withName("value"));

    private static final long OFFSET_VALUE = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("value"));

    private static final class Holder {
        static final FlecsParentComponent INSTANCE = new FlecsParentComponent();
    }

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, long offset, FlecsParent data) {
        MemoryAccess.set(segment, offset + OFFSET_VALUE, data.value());
    }

    @Override
    public FlecsParent read(MemorySegment segment, long offset) {
        return new FlecsParent(MemoryAccess.getLong(segment, offset + OFFSET_VALUE));
    }

    @Override
    public FlecsParent[] createArray(int size) {
        return new FlecsParent[size];
    }

    public static FlecsParentComponent getInstance() {
        return Holder.INSTANCE;
    }
}
