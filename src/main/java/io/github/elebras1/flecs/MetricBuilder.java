package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class MetricBuilder {

    private final World world;
    private final Arena arena;
    private final MemorySegment desc;

    MetricBuilder(World world, long entityId) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_metric_desc_t.allocate(this.arena);
        ecs_metric_desc_t.entity(this.desc, entityId);
    }

    public MetricBuilder member(long memberId) {
        ecs_metric_desc_t.member(this.desc, memberId);
        return this;
    }

    public MetricBuilder member(String name) {
        long memberId;
        if (ecs_metric_desc_t.id(this.desc) != 0) {
            long typeId = flecs_h.ecs_get_typeid(this.world.worldSeg(), ecs_metric_desc_t.id(this.desc));
            memberId = this.lookupChild(typeId, name);
        } else {
            memberId = this.world.lookup(name);
        }
        if (memberId == 0) {
            throw new IllegalArgumentException("Member not found: " + name);
        }
        return this.member(memberId);
    }

    public MetricBuilder dotmember(String expr) {
        ecs_metric_desc_t.dotmember(this.desc, this.arena.allocateFrom(expr));
        return this;
    }

    public MetricBuilder id(long id) {
        ecs_metric_desc_t.id(this.desc, id);
        return this;
    }

    public MetricBuilder id(long first, long second) {
        return this.id(flecs_h.ecs_make_pair(first, second));
    }

    public <T> MetricBuilder id(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.id(componentId);
    }

    public <T> MetricBuilder id(Class<T> first, long second) {
        long firstId = this.world.componentRegistry().getComponentId(first);
        return this.id(firstId, second);
    }

    public MetricBuilder targets(boolean value) {
        ecs_metric_desc_t.targets(this.desc, value);
        return this;
    }

    public MetricBuilder kind(long kindId) {
        ecs_metric_desc_t.kind(this.desc, kindId);
        return this;
    }

    public MetricBuilder brief(String brief) {
        ecs_metric_desc_t.brief(this.desc, this.arena.allocateFrom(brief));
        return this;
    }

    public long build() {
        try {
            long id = flecs_h.ecs_metric_init(this.world.worldSeg(), this.desc);
            if (id == 0) {
                throw new IllegalStateException("Failed to create metric");
            }
            return id;
        } finally {
            this.arena.close();
        }
    }

    public Entity buildEntity() {
        return this.world.obtainEntity(this.build());
    }

    private long lookupChild(long entityId, String name) {
        try (Arena tempArena = Arena.ofConfined()) {
            return flecs_h.ecs_lookup_child(this.world.worldSeg(), entityId, tempArena.allocateFrom(name));
        }
    }
}