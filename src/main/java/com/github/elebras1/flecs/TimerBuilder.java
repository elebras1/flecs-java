package com.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class TimerBuilder {
    
    private final World world;
    private final Arena arena;
    private final MemorySegment desc;
    
    TimerBuilder(World world) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_system_desc_t.allocate(this.arena);
    }

    public TimerBuilder interval(float interval) {
        ecs_system_desc_t.interval(this.desc, interval);
        return this;
    }

    public TimerBuilder rate(int rate) {
        ecs_system_desc_t.rate(this.desc, rate);
        return this;
    }

    public TimerBuilder tickSource(long tickSourceId) {
        ecs_system_desc_t.tick_source(this.desc, tickSourceId);
        return this;
    }

    public Entity build() {
        long timerId = flecs_h.ecs_system_init(this.world.nativeHandle(), this.desc);
        
        if (timerId == 0) {
            throw new IllegalStateException("Failed to create timer");
        }
        
        return this.world.obtainEntity(timerId);
    }
}

