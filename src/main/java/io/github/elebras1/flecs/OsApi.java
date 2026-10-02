package io.github.elebras1.flecs;

import io.github.elebras1.flecs.internal.FlecsLoader;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class OsApi {
    private final Arena arena;
    private final MemorySegment nativeOsApi;
    private boolean destructed;

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

    private void checkDestructed() {
        if (this.destructed) {
            throw new IllegalStateException("OsApi has been destructed");
        }
    }

    public OsApi taskNew(TaskNewCallback callback) {
        this.checkDestructed();
        MemorySegment nativeCallback = ecs_os_api_thread_new_t.allocate((cb, arg) ->
                callback.run(() -> ecs_os_thread_callback_t.invoke(cb, arg)), this.arena);
        ecs_os_api_t.task_new_(this.nativeOsApi, nativeCallback);
        return this;
    }

    public OsApi taskJoin(TaskJoinCallback callback) {
        this.checkDestructed();
        MemorySegment nativeCallback = ecs_os_api_thread_join_t.allocate((threadId) -> {
            callback.run(threadId);
            return MemorySegment.NULL;
        }, this.arena);
        ecs_os_api_t.task_join_(this.nativeOsApi, nativeCallback);
        return this;
    }

    public void set() {
        this.checkDestructed();
        flecs_h.ecs_os_set_api(this.nativeOsApi);
    }

    public void destruct() {
        if (this.destructed) {
            return;
        }
        this.destructed = true;
        this.arena.close();
    }
}
