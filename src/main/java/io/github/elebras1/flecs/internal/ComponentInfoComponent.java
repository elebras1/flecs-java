package io.github.elebras1.flecs.internal;

import io.github.elebras1.flecs.Component;
import io.github.elebras1.flecs.ComponentInfo;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public final class ComponentInfoComponent implements Component<ComponentInfo> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        MemoryAccess.intLayout().withName("size"),
        MemoryAccess.intLayout().withName("alignment"));

    private static final long OFFSET_SIZE = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("size"));
    private static final long OFFSET_ALIGNMENT = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("alignment"));

    private static final class Holder {
        static final ComponentInfoComponent INSTANCE = new ComponentInfoComponent();
    }

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, long offset, ComponentInfo data) {
        MemoryAccess.set(segment, offset + OFFSET_SIZE, data.size());
        MemoryAccess.set(segment, offset + OFFSET_ALIGNMENT, data.alignment());
    }

    @Override
    public ComponentInfo read(MemorySegment segment, long offset) {
        int size = MemoryAccess.getInt(segment, offset + OFFSET_SIZE);
        int alignment = MemoryAccess.getInt(segment, offset + OFFSET_ALIGNMENT);
        return new ComponentInfo(size, alignment);
    }

    @Override
    public ComponentInfo[] createArray(int size) {
        return new ComponentInfo[size];
    }

    public static ComponentInfoComponent getInstance() {
        return Holder.INSTANCE;
    }
}
