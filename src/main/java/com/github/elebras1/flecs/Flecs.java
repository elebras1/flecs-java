package com.github.elebras1.flecs;

import com.github.elebras1.flecs.collection.EcsLongList;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public class Flecs implements AutoCloseable {
    
    private final MemorySegment nativeWorld;
    private final Arena arena;
    private final ComponentRegistry componentRegistry;
    private boolean closed = false;
    private final ThreadLocal<NameBuffer> threadLocalNameBuffer;
    private final Map<Long, SystemCallbacks> systemCallbacks;
    private final Map<Long, ObserverCallbacks> observerCallbacks;

    private static final class SystemCallbacks {
        final Query.IterCallback iterCallback;
        final Query.RunCallback runCallback;
        final Query.EntityCallback entityCallback;

        SystemCallbacks(Query.IterCallback iterCallback, Query.RunCallback runCallback, Query.EntityCallback entityCallback) {
            this.iterCallback = iterCallback;
            this.runCallback = runCallback;
            this.entityCallback = entityCallback;
        }
    }

    private static final class ObserverCallbacks {
        final Query.IterCallback iterCallback;
        final Query.RunCallback runCallback;
        final Query.EntityCallback entityCallback;

        ObserverCallbacks(Query.IterCallback iterCallback, Query.RunCallback runCallback, Query.EntityCallback entityCallback) {
            this.iterCallback = iterCallback;
            this.runCallback = runCallback;
            this.entityCallback = entityCallback;
        }
    }

    private static final class NameBuffer {
        MemorySegment segment;
        int capacity;

        NameBuffer(int initialCapacity) {
            this.capacity = initialCapacity;
            this.segment = Arena.ofAuto().allocate(initialCapacity);
        }

        MemorySegment ensure(int needed) {
            if (needed > capacity) {
                Arena arena = Arena.ofAuto();
                segment = arena.allocate(needed);
                capacity = needed;
            }
            return segment;
        }
    }

    public Flecs() {
        this.arena = Arena.ofConfined();
        this.nativeWorld = flecs_h.ecs_init();
        
        if (this.nativeWorld == null || this.nativeWorld.address() == 0) {
            throw new IllegalStateException("Flecs world initialization failed");
        }

        this.componentRegistry = new ComponentRegistry(this);
        this.threadLocalNameBuffer = ThreadLocal.withInitial(() -> new NameBuffer(64));
        this.systemCallbacks = new ConcurrentHashMap<>();
        this.observerCallbacks = new ConcurrentHashMap<>();
    }

    public long entity() {
        this.checkClosed();
        long entityId = flecs_h.ecs_new(this.nativeWorld);
        return entityId;
    }

    public long entity(String name) {
        this.checkClosed();

        byte[] utf8 = name.getBytes(StandardCharsets.UTF_8);
        int len = utf8.length;

        NameBuffer nameBuffer = this.threadLocalNameBuffer.get();
        MemorySegment nameSegment = nameBuffer.ensure(len + 1);
        nameSegment.asSlice(0, len).copyFrom(MemorySegment.ofArray(utf8));
        nameSegment.set(java.lang.foreign.ValueLayout.JAVA_BYTE, len, (byte)0);

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment desc = ecs_entity_desc_t.allocate(tempArena);
            ecs_entity_desc_t.name(desc, nameSegment);
            return flecs_h.ecs_entity_init(this.nativeWorld, desc);
        }
    }

    public Entity obtainEntity(long entityId) {
        if(entityId < 0) {
            throw new IllegalArgumentException("Invalid entity ID: " + entityId);
        }
        return new Entity(this, entityId);
    }

    public EcsLongList entityBulk(int count) {
        this.checkClosed();

        MemorySegment desc = ecs_bulk_desc_t.allocate(this.arena);
        ecs_bulk_desc_t.count(desc, count);
        ecs_bulk_desc_t.entities(desc, MemorySegment.NULL);

        MemorySegment idsSegment = flecs_h.ecs_bulk_init(nativeWorld, desc);

        EcsLongList ids = new EcsLongList(count);
        ids.addAll(idsSegment.asSlice(0, (long) count * Long.BYTES).toArray(JAVA_LONG));
        return ids;
    }

    public long makeAlive(long entityId) {
        this.checkClosed();
        flecs_h.ecs_make_alive(this.nativeWorld, entityId);
        return entityId;
    }

    public void setVersion(int entityId) {
        this.checkClosed();
        flecs_h.ecs_set_version(this.nativeWorld, entityId);
    }

    public void setEntityRange(long idStart, long idEnd) {
        this.checkClosed();
        flecs_h.ecs_set_entity_range(this.nativeWorld, idStart, idEnd);
    }

    public void enableRangeCheck(boolean enable) {
        this.checkClosed();
        flecs_h.ecs_enable_range_check(this.nativeWorld, enable);
    }

    public long prefab() {
        this.checkClosed();
        long entityId = flecs_h.ecs_new_w_id(this.nativeWorld, FlecsConstants.EcsPrefab);
        return entityId;
    }

    public boolean progress(float deltaTime) {
        this.checkClosed();
        return flecs_h.ecs_progress(this.nativeWorld, deltaTime);
    }

    public boolean progress() {
        return progress(0.0f);
    }

    public QueryBuilder query() {
        this.checkClosed();
        return new QueryBuilder(this);
    }

    public Query query(String expr) {
        this.checkClosed();
        return this.query().expr(expr).build();
    }

    public long lookup(String name) {
        this.checkClosed();

        byte[] utf8 = name.getBytes(StandardCharsets.UTF_8);
        int len = utf8.length;

        NameBuffer buffer = this.threadLocalNameBuffer.get();
        MemorySegment segment = buffer.ensure(len + 1);

        segment.asSlice(0, len).copyFrom(MemorySegment.ofArray(utf8));
        segment.set(java.lang.foreign.ValueLayout.JAVA_BYTE, len, (byte)0);

        long entityId = flecs_h.ecs_lookup(this.nativeWorld, segment);
        return entityId == 0 ? -1 : entityId;
    }

    public <T extends FlecsComponent<T>> long component(Class<T> componentClass) {
        this.checkClosed();
        return this.componentRegistry.register(componentClass);
    }

    public <T extends FlecsComponent<T>> long component(Class<T> componentClass, Consumer<ComponentHooks<T>> configuration) {
        long id = this.component(componentClass);
        Component<T> component = this.componentRegistry.getComponent(componentClass);
        ComponentHooks<T> hooks = new ComponentHooks<>(this, component, componentClass);
        configuration.accept(hooks);
        hooks.install(this.nativeWorld, id);
        return id;
    }

    public int deleteEmptyTables(int limit) {
        this.checkClosed();
        try (Arena tempArena = Arena.ofConfined()) {
            MemoryLayout memoryLayout = MemoryLayout.structLayout(JAVA_INT.withName("limit"), JAVA_INT.withName("flags"));
            MemorySegment desc = tempArena.allocate(memoryLayout);
            desc.set(JAVA_INT, memoryLayout.byteOffset(MemoryLayout.PathElement.groupElement("limit")), limit);
            desc.set(JAVA_INT, memoryLayout.byteOffset(MemoryLayout.PathElement.groupElement("flags")), 0);

            return flecs_h.ecs_delete_empty_tables(this.nativeWorld, desc);
        }
    }

    public void deferBegin() {
        this.checkClosed();
        flecs_h.ecs_defer_begin(this.nativeWorld);
    }

    public void deferEnd() {
        this.checkClosed();
        flecs_h.ecs_defer_end(this.nativeWorld);
    }

    public void deferSuspend() {
        this.checkClosed();
        flecs_h.ecs_defer_suspend(this.nativeWorld);
    }

    public void deferResume() {
        this.checkClosed();
        flecs_h.ecs_defer_resume(this.nativeWorld);
    }

    public SystemBuilder system() {
        this.checkClosed();
        return new SystemBuilder(this);
    }

    public SystemBuilder system(String name) {
        this.checkClosed();
        return new SystemBuilder(this, name);
    }

    public ObserverBuilder observer() {
        this.checkClosed();
        return new ObserverBuilder(this);
    }

    public ObserverBuilder observer(String name) {
        this.checkClosed();
        return new ObserverBuilder(this, name);
    }

    public <T extends FlecsComponent<T>> ObserverBuilder observer(Class<T> componentClass) {
        this.checkClosed();
        return new ObserverBuilder(this).with(componentClass);
    }

    public TimerBuilder timer() {
        this.checkClosed();
        return new TimerBuilder(this);
    }

    public void setThreads(int threads) {
        this.checkClosed();
        flecs_h.ecs_set_threads(this.nativeWorld, threads);
    }

    public void setPipeline(long pipelineId) {
        this.checkClosed();
        flecs_h.ecs_set_pipeline(this.nativeWorld, pipelineId);
    }

    void registerSystemCallbacks(long systemId, Query.IterCallback iterCallback, Query.RunCallback runCallback, Query.EntityCallback entityCallback) {
        if (iterCallback != null || runCallback != null || entityCallback != null) {
            this.systemCallbacks.put(systemId, new SystemCallbacks(iterCallback, runCallback, entityCallback));
        }
    }

    void registerObserverCallbacks(long observerId, Query.IterCallback iterCallback, Query.RunCallback runCallback, Query.EntityCallback entityCallback) {
        if (iterCallback != null || runCallback != null || entityCallback != null) {
            this.observerCallbacks.put(observerId, new ObserverCallbacks(iterCallback, runCallback, entityCallback));
        }
    }

    MemorySegment nativeHandle() {
        this.checkClosed();
        return this.nativeWorld;
    }

    Arena arena() {
        return this.arena;
    }

    ComponentRegistry componentRegistry() {
        this.checkClosed();
        return this.componentRegistry;
    }

    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("The Flecs world has already been closed");
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            if (this.nativeWorld != null && this.nativeWorld.address() != 0) {
                flecs_h.ecs_fini(this.nativeWorld);
            }
            this.arena.close();
        }
    }

    @Override
    public String toString() {
        if (this.closed) {
            return "World[closed]";
        }
        return String.format("World[0x%x]", this.nativeWorld.address());
    }
}

