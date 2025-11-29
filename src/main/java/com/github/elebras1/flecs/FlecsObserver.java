package com.github.elebras1.flecs;

public class FlecsObserver {
    
    private final Flecs world;
    private final long observerId;

    FlecsObserver(Flecs world, long observerId) {
        this.world = world;
        this.observerId = observerId;
    }

    public long id() {
        return this.observerId;
    }

    public Flecs world() {
        return this.world;
    }

    public void enable() {
        flecs_h.ecs_enable(this.world.nativeHandle(), this.observerId, true);
    }

    public void disable() {
        flecs_h.ecs_enable(this.world.nativeHandle(), this.observerId, false);
    }

    public void destruct() {
        flecs_h.ecs_delete(this.world.nativeHandle(), this.observerId);
    }

    public boolean isValid() {
        return flecs_h.ecs_is_valid(this.world.nativeHandle(), this.observerId);
    }

    public boolean isAlive() {
        return flecs_h.ecs_is_alive(this.world.nativeHandle(), this.observerId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FlecsObserver other)) {
            return false;
        }
        return this.observerId == other.observerId && this.world == other.world;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(this.observerId);
    }
    
    @Override
    public String toString() {
        return String.format("Observer[%d]", this.observerId);
    }
}