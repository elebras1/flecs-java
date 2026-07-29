package io.github.elebras1.flecs;

public class Id {

    protected final World world;
    protected long id;

    Id(World world, long id) {
        this.world = world;
        this.id = id;
    }

    public long id() {
        return this.id;
    }

    public long entity() {
        assert !this.isPair() : "Invalid entity : " + this.id;
        assert this.flags() == 0 : "Invalid entity (has flags) : " + this.id;
        return this.id;
    }

    public long flags() {
        return this.id & flecs_h.ECS_ID_FLAGS_MASK();
    }

    public long first() {
        assert isPair() : "Invalid pair : " + this.id;
        return (this.id & flecs_h.ECS_COMPONENT_MASK())  >>> 32;
    }

    public long second() {
        assert isPair() : "Invalid pair : " + this.id;
        return (this.id & flecs_h.ECS_COMPONENT_MASK()) & 0xFFFFFFFFL;
    }

    public long addFlags(long flags) {
        return this.id | flags;
    }

    public long removeFlags(long flags) {
        assert (this.id & flecs_h.ECS_ID_FLAGS_MASK()) == flags : "Invalid pair : " + this.id;
        return this.id & flecs_h.ECS_COMPONENT_MASK();
    }

    public long removeFlags() {
        return this.id & flecs_h.ECS_COMPONENT_MASK();
    }

    public long removeGeneration() {
        return this.id & 0xFFFFFFFFL;
    }

    public boolean isPair() {
        return (this.id & flecs_h.ECS_ID_FLAGS_MASK()) == flecs_h.ECS_PAIR();
    }

    public boolean isWildCard() {
        return flecs_h.ecs_id_is_wildcard(this.id);
    }

    public boolean isEntity() {
        return (this.id & flecs_h.ECS_ID_FLAGS_MASK()) == 0;
    }

    public long typeId() {
        return flecs_h.ecs_get_typeid(this.world.worldSeg(), this.id);
    }
}