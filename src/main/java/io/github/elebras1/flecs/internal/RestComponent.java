package io.github.elebras1.flecs.internal;

import io.github.elebras1.flecs.Component;
import io.github.elebras1.flecs.Rest;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class RestComponent implements Component<Rest> {

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_SHORT.withName("port"),
        MemoryLayout.paddingLayout(6),
        ValueLayout.JAVA_LONG.withName("ipaddr"),
        ValueLayout.JAVA_LONG.withName("impl"));

    private static final long OFFSET_PORT = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("port"));
    private static final long OFFSET_IPADDR = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("ipaddr"));
    private static final long OFFSET_IMPL = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("impl"));

    private static final class Holder {
        static final RestComponent INSTANCE = new RestComponent();
    }

    @Override
    public MemoryLayout layout() {
        return LAYOUT;
    }

    @Override
    public void write(MemorySegment segment, long offset, Rest data) {
        MemoryAccess.set(segment, offset + OFFSET_PORT, data.port());
        segment.set(ValueLayout.JAVA_LONG, offset + OFFSET_IPADDR, 0L);
        segment.set(ValueLayout.JAVA_LONG, offset + OFFSET_IMPL, 0L);
    }

    @Override
    public Rest read(MemorySegment segment, long offset) {
        return new Rest().port(MemoryAccess.getShort(segment, offset + OFFSET_PORT));
    }

    @Override
    public Rest[] createArray(int size) {
        return new Rest[size];
    }

    public static RestComponent getInstance() {
        return Holder.INSTANCE;
    }
}
