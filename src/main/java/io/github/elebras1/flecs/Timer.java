package io.github.elebras1.flecs;

public final class Timer extends Entity {

    Timer(World world, long id) {
        super(world, id);
    }

    public Timer interval(float interval) {
        flecs_h.ecs_set_interval(this.world.worldSeg(), this.id, interval);
        return this;
    }

    public float interval() {
        return flecs_h.ecs_get_interval(this.world.worldSeg(), this.id);
    }

    public Timer timeout(float timeout) {
        flecs_h.ecs_set_timeout(this.world.worldSeg(), this.id, timeout);
        return this;
    }

    public float timeout() {
        return flecs_h.ecs_get_timeout(this.world.worldSeg(), this.id);
    }

    public Timer rate(int rate) {
        return this.rate(rate, 0);
    }

    public Timer rate(int rate, long tickSourceId) {
        flecs_h.ecs_set_rate(this.world.worldSeg(), this.id, rate, tickSourceId);
        return this;
    }

    public void start() {
        flecs_h.ecs_start_timer(this.world.worldSeg(), this.id);
    }

    public void stop() {
        flecs_h.ecs_stop_timer(this.world.worldSeg(), this.id);
    }
}
