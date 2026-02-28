package com.github.elebras1.flecs;

import com.github.elebras1.flecs.util.internal.FlecsLoader;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class OsApi implements AutoCloseable {
    private final Arena arena;
    private final MemorySegment nativeOsApi;

    static {
        FlecsLoader.load();
    }

    @FunctionalInterface
    public interface TaskNewCallback {
        long run(Runnable task);
    }

    @FunctionalInterface
    public interface TaskJoinCallback {
        void run(long threadId);
    }

    public OsApi() {
        this.arena = Arena.ofConfined();
        flecs_h.ecs_os_set_api_defaults();
        this.nativeOsApi = flecs_h.ecs_os_get_api(this.arena);
    }

    public OsApi taskNew(TaskNewCallback callback) {
        MemorySegment nativeCallback = ecs_os_api_thread_new_t.allocate((cb, arg) ->
                callback.run(() -> ecs_os_thread_callback_t.invoke(cb, arg)), this.arena);
        ecs_os_api_t.task_new_(this.nativeOsApi, nativeCallback);
        return this;
    }

    public OsApi taskJoin(TaskJoinCallback callback) {
        MemorySegment nativeCallback = ecs_os_api_thread_join_t.allocate((threadId) -> {
            callback.run(threadId);
            return MemorySegment.NULL;
        }, this.arena);
        ecs_os_api_t.task_join_(this.nativeOsApi, nativeCallback);
        return this;
    }

    public void set() {
        flecs_h.ecs_os_set_api(this.nativeOsApi);
    }

    @Override
    public void close() throws Exception {
        this.arena.close();
    }
}
