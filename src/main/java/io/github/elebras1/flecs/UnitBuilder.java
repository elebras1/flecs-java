package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class UnitBuilder {

    private final World world;
    private final Arena arena;
    private final MemorySegment desc;
    private long entityId;

    UnitBuilder(World world, String name) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_unit_desc_t.allocate(this.arena);

        if (name != null) {
            MemorySegment entityDesc = ecs_entity_desc_t.allocate(this.arena);
            ecs_entity_desc_t.name(entityDesc, this.arena.allocateFrom(name));
            ecs_entity_desc_t.sep(entityDesc, this.arena.allocateFrom("::"));
            ecs_entity_desc_t.root_sep(entityDesc, this.arena.allocateFrom("::"));
            this.entityId = flecs_h.ecs_entity_init(world.worldSeg(), entityDesc);
            ecs_unit_desc_t.entity(this.desc, this.entityId);
        }
    }

    public UnitBuilder symbol(String symbol) {
        ecs_unit_desc_t.symbol(this.desc, this.arena.allocateFrom(symbol));
        return this;
    }

    public UnitBuilder quantity(long quantityId) {
        ecs_unit_desc_t.quantity(this.desc, quantityId);
        return this;
    }

    public UnitBuilder quantity(Entity quantity) {
        return this.quantity(quantity.id());
    }

    public UnitBuilder base(long baseId) {
        ecs_unit_desc_t.base(this.desc, baseId);
        return this;
    }

    public UnitBuilder base(Entity base) {
        return this.base(base.id());
    }

    public UnitBuilder over(long overId) {
        ecs_unit_desc_t.over(this.desc, overId);
        return this;
    }

    public UnitBuilder prefix(long prefixId) {
        ecs_unit_desc_t.prefix(this.desc, prefixId);
        return this;
    }

    public UnitBuilder prefix(Entity prefix) {
        return this.prefix(prefix.id());
    }

    public UnitBuilder translation(int factor, int power) {
        MemorySegment translation = ecs_unit_desc_t.translation(this.desc);
        ecs_unit_translation_t.factor(translation, factor);
        ecs_unit_translation_t.power(translation, power);
        return this;
    }

    public long build() {
        try {
            long id = flecs_h.ecs_unit_init(this.world.worldSeg(), this.desc);
            if (id == 0) {
                throw new IllegalStateException("Failed to create unit");
            }
            return id;
        } finally {
            this.arena.close();
        }
    }

    public Entity buildEntity() {
        return this.world.obtainEntity(this.build());
    }
}