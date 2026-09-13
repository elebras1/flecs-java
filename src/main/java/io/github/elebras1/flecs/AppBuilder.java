package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class AppBuilder {

    private final World world;
    private float targetFps;
    private float deltaTime;
    private int threads;
    private int frames;
    private boolean enableStats;
    private boolean enableRest;
    private short port = 27750;

    AppBuilder(World world) {
        this.world = world;
    }

    public AppBuilder targetFps(float targetFps) {
        this.targetFps = targetFps;
        return this;
    }

    public AppBuilder deltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
        return this;
    }

    public AppBuilder threads(int threads) {
        this.threads = threads;
        return this;
    }

    public AppBuilder frames(int frames) {
        this.frames = frames;
        return this;
    }

    public AppBuilder enableStats() {
        this.enableStats = true;
        return this;
    }

    public AppBuilder enableRest() {
        this.enableRest = true;
        return this;
    }

    public AppBuilder enableRest(short port) {
        this.enableRest = true;
        this.port = port;
        return this;
    }

    public void run() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment desc = ecs_app_desc_t.allocate(arena);
            ecs_app_desc_t.target_fps(desc, this.targetFps);
            ecs_app_desc_t.delta_time(desc, this.deltaTime);
            ecs_app_desc_t.threads(desc, this.threads);
            ecs_app_desc_t.frames(desc, this.frames);
            ecs_app_desc_t.enable_stats(desc, this.enableStats);
            ecs_app_desc_t.enable_rest(desc, this.enableRest);
            if (this.enableRest) {
                ecs_app_desc_t.port(desc, this.port);
            }

            int result = flecs_h.ecs_app_run(this.world.worldSeg(), desc);
            if (result != 0) {
                throw new IllegalStateException("Application exited with error code " + result);
            }
        }
    }
}
