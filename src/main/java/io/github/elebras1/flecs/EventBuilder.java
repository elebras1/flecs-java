package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;

public class EventBuilder {

    private final World world;
    private final long eventId;
    private long entityId;
    private final List<Long> ids = new ArrayList<>();
    private Object payload;

    EventBuilder(World world, long eventId) {
        this.world = world;
        this.eventId = eventId;
    }

    public EventBuilder id(long id) {
        this.ids.add(id);
        return this;
    }

    public <T> EventBuilder id(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.id(componentId);
    }

    public EventBuilder id(long first, long second) {
        return this.id(flecs_h.ecs_make_pair(first, second));
    }

    public <T> EventBuilder id(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.id(firstId, second);
    }

    public <T> EventBuilder id(Class<T> first, Entity second) {
        return this.id(first, second.id());
    }

    public <A, B> EventBuilder id(Class<A> first, Class<B> second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        long secondId = this.world.componentRegistry().getComponentId(second);
        return this.id(firstId, secondId);
    }

    public EventBuilder entity(long entityId) {
        this.entityId = entityId;
        return this;
    }

    public EventBuilder entity(Entity entity) {
        return this.entity(entity.id());
    }

    public <T> EventBuilder payload(T data) {
        this.payload = data;
        return this;
    }

    public void emit() {
        this.emitEvent(false);
    }

    public void enqueue() {
        this.emitEvent(true);
    }

    @SuppressWarnings("unchecked")
    private void emitEvent(boolean deferred) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment descSeg = ecs_event_desc_t.allocate(tempArena);
            ecs_event_desc_t.event(descSeg, this.eventId);

            if (!this.ids.isEmpty()) {
                MemorySegment typeSeg = ecs_type_t.allocate(tempArena);
                MemorySegment idsSeg = tempArena.allocate(ValueLayout.JAVA_LONG, this.ids.size());
                for (int i = 0; i < this.ids.size(); i++) {
                    idsSeg.setAtIndex(ValueLayout.JAVA_LONG, i, this.ids.get(i));
                }
                ecs_type_t.array(typeSeg, idsSeg);
                ecs_type_t.count(typeSeg, this.ids.size());
                ecs_event_desc_t.ids(descSeg, typeSeg);
            }

            if (this.entityId != 0) {
                ecs_event_desc_t.entity(descSeg, this.entityId);
            }

            if (this.payload != null) {
                @SuppressWarnings("rawtypes")
                Component component = this.world.componentRegistry().getComponent(this.payload.getClass());
                MemorySegment dataSeg = this.world.getComponentBuffer(component.size());
                component.write(dataSeg, 0, this.payload);
                ecs_event_desc_t.param(descSeg, dataSeg);
            }

            if (deferred) {
                flecs_h.ecs_enqueue(this.world.worldSeg(), descSeg);
            } else {
                flecs_h.ecs_emit(this.world.worldSeg(), descSeg);
            }
        }
    }
}
