package com.github.elebras1.flecs;

public class Pipeline {

    private final World world;
    private final long id;

    Pipeline(World world, long id) {
        this.world = world;
        this.id = id;
    }

    public long id() {
        return this.id;
    }

    public Entity entity() {
        return new Entity(this.world, this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pipeline other)) {
            return false;
        }
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }

    @Override
    public String toString() {
        return String.format("Pipeline[%d]", this.id);
    }
}

