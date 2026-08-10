package io.github.elebras1.flecs.util.internal.buffer;

public record FlecsBuffers(StringRing stringRing, ComponentBuffer componentBuffer, EntityDescBuffer entityDescBuffer) implements AutoCloseable {
    public FlecsBuffers() {
        this(new StringRing(8, 64), new ComponentBuffer(256), new EntityDescBuffer());
    }

    @Override
    public void close() {
        this.stringRing.close();
        this.componentBuffer.close();
        this.entityDescBuffer.close();
    }
}
