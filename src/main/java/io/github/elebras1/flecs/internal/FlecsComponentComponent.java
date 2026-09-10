package io.github.elebras1.flecs.internal;

import io.github.elebras1.flecs.Component;
import io.github.elebras1.flecs.FlecsComponent;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public final class FlecsComponentComponent implements Component<FlecsComponent> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        MemoryAccess.intLayout().withName("size"),
        MemoryAccess.intLayout().withName("alignment"));

    private static final long OFFSET_SIZE = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("size"));
    private static final long OFFSET_ALIGNMENT = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("alignment"));

    private static final class Holder {
        static final FlecsComponentComponent INSTANCE = new FlecsComponentComponent();
    }

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, long offset, FlecsComponent data) {
        MemoryAccess.set(segment, offset + OFFSET_SIZE, data.size());
        MemoryAccess.set(segment, offset + OFFSET_ALIGNMENT, data.alignment());
    }

    @Override
    public FlecsComponent read(MemorySegment segment, long offset) {
        int size = MemoryAccess.getInt(segment, offset + OFFSET_SIZE);
        int alignment = MemoryAccess.getInt(segment, offset + OFFSET_ALIGNMENT);
        return new FlecsComponent(size, alignment);
    }

    @Override
    public FlecsComponent[] createArray(int size) {
        return new FlecsComponent[size];
    }

    public static FlecsComponentComponent getInstance() {
        return Holder.INSTANCE;
    }
}
