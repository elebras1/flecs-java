package com.github.elebras1.flecs;

import com.github.elebras1.flecs.collection.EcsLongList;
import com.github.elebras1.flecs.util.internal.FlecsLoader;

import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static java.lang.foreign.ValueLayout.*;

public class World implements AutoCloseable {
    private final MemorySegment nativeWorld;
    private final Arena arena;
    private final ComponentRegistry componentRegistry;
    private final Map<Long, SystemCallbacks> systemCallbacks;
    private final Map<Long, ObserverCallbacks> observerCallbacks;
    private final FlecsBuffers defaultBuffers;
    private final boolean owned;
    private boolean closed;

    static {
        FlecsLoader.load();
    }

    private record SystemCallbacks(Query.IterCallback iterCallback, Query.RunCallback runCallback, Query.EntityCallback entityCallback) {
    }

    private record ObserverCallbacks(Query.IterCallback iterCallback, Query.RunCallback runCallback, Query.EntityCallback entityCallback) {
    }

    public record FlecsBuffers(NameBuffer nameBuffer, ComponentBuffer componentBuffer, EntityDescBuffer entityDescBuffer) implements AutoCloseable {
        public FlecsBuffers() {
            this(new NameBuffer(64), new ComponentBuffer(256), new EntityDescBuffer());
        }

        @Override
        public void close() {
            this.nameBuffer.close();
            this.componentBuffer.close();
            this.entityDescBuffer.close();
        }
    }

    private static final class NameBuffer implements AutoCloseable {
        private Arena arena;
        private MemorySegment segment;
        private long capacity;

        NameBuffer(long initialCapacity) {
            this.capacity = initialCapacity;
            this.arena = Arena.ofConfined();
            this.segment = this.arena.allocate(initialCapacity);
        }

        private MemorySegment ensure(long needed) {
            if (needed > capacity) {
                this.arena.close();
                this.arena = Arena.ofConfined();
                this.capacity = Math.max(needed, capacity * 2);
                this.segment = this.arena.allocate(this.capacity);
            }
            return segment;
        }

        public MemorySegment set(String value) {
            byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
            long needed = utf8.length + 1;
            MemorySegment seg = ensure(needed);
            MemorySegment.copy(utf8, 0, seg, JAVA_BYTE, 0, utf8.length);
            seg.set(JAVA_BYTE, utf8.length, (byte) 0);
            return seg;
        }

        @Override
        public void close() {
            if (this.arena != null) {
                this.arena.close();
                this.arena = null;
            }
        }
    }

    private static final class ComponentBuffer implements AutoCloseable {
        private Arena arena;
        private MemorySegment segment;
        private long capacity;

        ComponentBuffer(long initialCapacity) {
            this.capacity = initialCapacity;
            this.arena = Arena.ofConfined();
            this.segment = this.arena.allocate(initialCapacity);
        }

        MemorySegment ensure(long needed) {
            if (needed > capacity) {
                long newCapacity = Math.max(needed, capacity * 2);
                this.arena.close();
                this.arena = Arena.ofConfined();
                this.segment = this.arena.allocate(newCapacity);
                this.capacity = newCapacity;
            }
            return segment.asSlice(0, needed);
        }

        @Override
        public void close() {
            if (this.arena != null) {
                this.arena.close();
                this.arena = null;
            }
        }
    }

    private static final class EntityDescBuffer implements AutoCloseable {
        private final Arena arena;
        private final MemorySegment segment;

        EntityDescBuffer() {
            this.arena = Arena.ofConfined();
            this.segment = ecs_entity_desc_t.allocate(this.arena);
        }

        MemorySegment get() {
            return this.segment;
        }

        @Override
        public void close() {
            this.arena.close();
        }
    }

    public World() {
        this.arena = Arena.ofConfined();
        this.nativeWorld = flecs_h.ecs_init();

        if (this.nativeWorld == null || this.nativeWorld.address() == 0) {
            throw new IllegalStateException("Flecs world initialization failed");
        }

        this.componentRegistry = new ComponentRegistry(this);
        this.systemCallbacks = new ConcurrentHashMap<>();
        this.observerCallbacks = new ConcurrentHashMap<>();
        this.defaultBuffers = new FlecsBuffers();
        this.closed = false;
        this.owned = true;
    }

    private World(MemorySegment stagePtr, ComponentRegistry sharedRegistry) {
        this.arena = Arena.ofConfined();
        this.nativeWorld = stagePtr;
        this.componentRegistry = sharedRegistry;
        this.systemCallbacks = new ConcurrentHashMap<>();
        this.observerCallbacks = new ConcurrentHashMap<>();
        this.defaultBuffers = null;
        this.closed = false;
        this.owned = false;
    }

    public long entity() {
        this.checkClosed();
        long entityId = flecs_h.ecs_new(this.nativeWorld);
        return entityId;
    }

    public long entity(String name) {
        this.checkClosed();
        MemorySegment nameSegment = this.defaultBuffers.nameBuffer().set(name);

        MemorySegment desc = this.defaultBuffers.entityDescBuffer().get();
        ecs_entity_desc_t.name(desc, nameSegment);

        return flecs_h.ecs_entity_init(this.nativeWorld, desc);
    }

    public Entity obtainEntity(long entityId) {
        if(entityId < 0) {
            throw new IllegalArgumentException("Invalid entity ID: " + entityId);
        }
        return new Entity(this, entityId);
    }

    MemorySegment getComponentBuffer(long size) {
        return this.defaultBuffers.componentBuffer().ensure(size);
    }

    public EcsLongList entityBulk(int count) {
        this.checkClosed();
        try(Arena tempArena = Arena.ofConfined()) {
            MemorySegment desc = ecs_bulk_desc_t.allocate(tempArena);
            ecs_bulk_desc_t.count(desc, count);
            ecs_bulk_desc_t.entities(desc, MemorySegment.NULL);

            MemorySegment idsSegment = flecs_h.ecs_bulk_init(this.nativeWorld, desc);

            EcsLongList ids = new EcsLongList(count);
            ids.addAll(idsSegment.asSlice(0, (long) count * Long.BYTES).toArray(JAVA_LONG));
            return ids;
        }
    }

    public final EcsLongList entityBulk(int count, Class<?>... componentClasses) {
        this.checkClosed();

        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }

        if (componentClasses == null || componentClasses.length == 0) {
            return this.entityBulk(count);
        }

        if (componentClasses.length > 32) {
            throw new IllegalArgumentException("Cannot have more than 32 components in bulk creation");
        }

        try (Arena tempArena = Arena.ofConfined()) {
            long[] componentIds = new long[componentClasses.length];
            for (int i = 0; i < componentClasses.length; i++) {
                long componentId = this.componentRegistry().getComponentId(componentClasses[i]);
                if (componentId == -1) {
                    throw new IllegalStateException("Component " + componentClasses[i].getSimpleName() + " is not registered. Call world.component() first.");
                }
                componentIds[i] = componentId;
            }

            MemorySegment desc = ecs_bulk_desc_t.allocate(tempArena);

            ecs_bulk_desc_t._canary(desc, 0);
            ecs_bulk_desc_t.entities(desc, MemorySegment.NULL);
            ecs_bulk_desc_t.count(desc, count);

            MemorySegment idsArray = ecs_bulk_desc_t.ids(desc);
            for (int i = 0; i < componentIds.length; i++) {
                idsArray.setAtIndex(ValueLayout.JAVA_LONG, i, componentIds[i]);
            }

            ecs_bulk_desc_t.data(desc, MemorySegment.NULL);
            ecs_bulk_desc_t.table(desc, MemorySegment.NULL);

            MemorySegment entitiesPtr = flecs_h.ecs_bulk_init(this.nativeWorld, desc);

            EcsLongList entities = new EcsLongList(count);
            entities.addAll(entitiesPtr.asSlice(0, (long) count * Long.BYTES).toArray(JAVA_LONG));

            return entities;
        }
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
        MemorySegment segment = this.defaultBuffers.nameBuffer().set(name);

        long entityId = flecs_h.ecs_lookup(this.nativeWorld, segment);
        return entityId == 0 ? -1 : entityId;
    }

    public <T> long component(Class<T> componentClass) {
        this.checkClosed();
        return this.componentRegistry.register(componentClass);
    }

    public <T> long component(Class<T> componentClass, Consumer<ComponentHooks<T>> configuration) {
        this.checkClosed();
        long id = this.component(componentClass);
        Component<T> component = this.componentRegistry.getComponent(componentClass);
        ComponentHooks<T> hooks = new ComponentHooks<>(this, component);
        configuration.accept(hooks);
        hooks.install(this.nativeWorld, id);
        return id;
    }

    public void deleteWith(Class<?> componentClass) {
        this.checkClosed();
        long componentId = this.componentRegistry.getComponentId(componentClass);
        flecs_h.ecs_delete_with(this.nativeWorld, componentId);
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

    public <T> ObserverBuilder observer(Class<T> componentClass) {
        this.checkClosed();
        return new ObserverBuilder(this).with(componentClass);
    }

    public TimerBuilder timer() {
        this.checkClosed();
        return new TimerBuilder(this);
    }

    public ScriptBuilder script(String code) {
        this.checkClosed();
        return new ScriptBuilder(this, code);
    }

    public void setThreads(int threads) {
        this.checkClosed();
        flecs_h.ecs_set_threads(this.nativeWorld, threads);
    }

    public void setPipeline(long pipelineId) {
        this.checkClosed();
        flecs_h.ecs_set_pipeline(this.nativeWorld, pipelineId);
    }

    public long getMaxId() {
        this.checkClosed();
        return flecs_h.ecs_get_max_id(this.nativeWorld);
    }

    public EcsLongList getEntities() {
        this.checkClosed();
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment entitiesStruct = flecs_h.ecs_get_entities(tempArena, this.nativeWorld);
            MemorySegment idsPointer = entitiesStruct.get(ValueLayout.ADDRESS, 0);
            int count = entitiesStruct.get(ValueLayout.JAVA_INT, ValueLayout.ADDRESS.byteSize());

            EcsLongList entities = new EcsLongList(count);
            if (count > 0 && !idsPointer.equals(MemorySegment.NULL)) {
                MemorySegment idsArray = idsPointer.reinterpret((long) count * Long.BYTES);
                entities.addAll(idsArray.toArray(ValueLayout.JAVA_LONG));
            }

            return entities;
        }
    }

    public void exclusiveAccessBegin(String threadName) {
        this.checkClosed();

        if (threadName == null) {
            flecs_h.ecs_exclusive_access_begin(this.nativeWorld, MemorySegment.NULL);
        } else {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment nameSegment = arena.allocateFrom(threadName);
                flecs_h.ecs_exclusive_access_begin(this.nativeWorld, nameSegment);
            }
        }
    }

    public void exclusiveAccessBegin() {
        this.checkClosed();
        flecs_h.ecs_exclusive_access_begin(this.nativeWorld, MemorySegment.NULL);
    }

    public void exclusiveAccessEnd(boolean lockWorld) {
        this.checkClosed();
        flecs_h.ecs_exclusive_access_end(this.nativeWorld, lockWorld);
    }

    public void shrink() {
        this.checkClosed();
        flecs_h.ecs_shrink(this.nativeWorld);
    }

    public void dim(int numberEntities) {
        this.checkClosed();
        flecs_h.ecs_dim(this.nativeWorld, numberEntities);
    }

    public void frameBegin(float deltaTime) {
        this.checkClosed();
        flecs_h.ecs_frame_begin(this.nativeWorld, deltaTime);
    }

    public void frameEnd() {
        this.checkClosed();
        flecs_h.ecs_frame_end(this.nativeWorld);
    }

    public void quit() {
        this.checkClosed();
        flecs_h.ecs_quit(this.nativeWorld);
    }

    public void shouldQuit() {
        this.checkClosed();
        flecs_h.ecs_should_quit(this.nativeWorld);
    }

    public void measureFrameTime(boolean enable) {
        this.checkClosed();
        flecs_h.ecs_measure_frame_time(this.nativeWorld, enable);
    }

    public void measureSystemTime(boolean enable) {
        this.checkClosed();
        flecs_h.ecs_measure_system_time(this.nativeWorld, enable);
    }

    public void setTargetFps(float fps) {
        this.checkClosed();
        flecs_h.ecs_set_target_fps(this.nativeWorld, fps);
    }

    public FlecsInfo getInfo() {
        this.checkClosed();

        MemorySegment infoPtr = flecs_h.ecs_get_world_info(this.nativeWorld);
        if (infoPtr.equals(MemorySegment.NULL)) {
            throw new IllegalStateException("Failed to get world info");
        }

        MemorySegment info = infoPtr.reinterpret(ecs_world_info_t.layout().byteSize());

        return new FlecsInfo(
                ecs_world_info_t.last_component_id(info),
                ecs_world_info_t.min_id(info),
                ecs_world_info_t.max_id(info),
                ecs_world_info_t.delta_time_raw(info),
                ecs_world_info_t.delta_time(info),
                ecs_world_info_t.time_scale(info),
                ecs_world_info_t.target_fps(info),
                ecs_world_info_t.frame_time_total(info),
                ecs_world_info_t.system_time_total(info),
                ecs_world_info_t.emit_time_total(info),
                ecs_world_info_t.merge_time_total(info),
                ecs_world_info_t.rematch_time_total(info),
                ecs_world_info_t.world_time_total(info),
                ecs_world_info_t.world_time_total_raw(info),
                ecs_world_info_t.frame_count_total(info),
                ecs_world_info_t.merge_count_total(info),
                ecs_world_info_t.eval_comp_monitors_total(info),
                ecs_world_info_t.rematch_count_total(info),
                ecs_world_info_t.id_create_total(info),
                ecs_world_info_t.id_delete_total(info),
                ecs_world_info_t.table_create_total(info),
                ecs_world_info_t.table_delete_total(info),
                ecs_world_info_t.pipeline_build_count_total(info),
                ecs_world_info_t.systems_ran_total(info),
                ecs_world_info_t.observers_ran_total(info),
                ecs_world_info_t.queries_ran_total(info),
                ecs_world_info_t.tag_id_count(info),
                ecs_world_info_t.component_id_count(info),
                ecs_world_info_t.pair_id_count(info),
                ecs_world_info_t.table_count(info),
                ecs_world_info_t.creation_time(info),
                extractCommandStats(info),
                extractNamePrefix(info)
        );
    }

    private FlecsInfo.CommandStats extractCommandStats(MemorySegment info) {
        MemorySegment cmd = ecs_world_info_t.cmd(info);
        return new FlecsInfo.CommandStats(
                ecs_world_info_t.cmd.add_count(cmd),
                ecs_world_info_t.cmd.remove_count(cmd),
                ecs_world_info_t.cmd.delete_count(cmd),
                ecs_world_info_t.cmd.clear_count(cmd),
                ecs_world_info_t.cmd.set_count(cmd),
                ecs_world_info_t.cmd.ensure_count(cmd),
                ecs_world_info_t.cmd.modified_count(cmd),
                ecs_world_info_t.cmd.discard_count(cmd),
                ecs_world_info_t.cmd.event_count(cmd),
                ecs_world_info_t.cmd.other_count(cmd),
                ecs_world_info_t.cmd.batched_entity_count(cmd),
                ecs_world_info_t.cmd.batched_command_count(cmd)
        );
    }

    private String extractNamePrefix(MemorySegment info) {
        MemorySegment namePrefixPtr = ecs_world_info_t.name_prefix(info);
        if (namePrefixPtr.equals(MemorySegment.NULL)) {
            return null;
        }
        return namePrefixPtr.reinterpret(Long.MAX_VALUE).getString(0);
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

    public void setStageCount(int stages) {
        this.checkClosed();
        flecs_h.ecs_set_stage_count(this.nativeWorld, stages);
    }

    public int getStageCount() {
        this.checkClosed();
        return flecs_h.ecs_get_stage_count(this.nativeWorld);
    }

    public World getStage(int stageId) {
        this.checkClosed();
        MemorySegment stagePtr = flecs_h.ecs_get_stage(this.nativeWorld, stageId);
        return new World(stagePtr, this.componentRegistry);
    }

    public int getStageId() {
        this.checkClosed();
        return flecs_h.ecs_stage_get_id(this.nativeWorld);
    }

    public World stage() {
        this.checkClosed();
        MemorySegment stagePtr = flecs_h.ecs_stage_new(this.nativeWorld);
        return new World(stagePtr, this.componentRegistry);
    }

    public void freeStage() {
        if (this.nativeWorld != null && this.nativeWorld.address() != 0) {
            flecs_h.ecs_stage_free(this.nativeWorld);
        }
    }

    public void merge() {
        this.checkClosed();
        flecs_h.ecs_merge(this.nativeWorld);
    }

    public boolean readonlyBegin(boolean multiThreaded) {
        this.checkClosed();
        return flecs_h.ecs_readonly_begin(this.nativeWorld, multiThreaded);
    }

    public boolean readonlyBegin() {
        return this.readonlyBegin(false);
    }

    public void readonlyEnd() {
        this.checkClosed();
        flecs_h.ecs_readonly_end(this.nativeWorld);
    }

    public boolean isReadonly() {
        this.checkClosed();
        return flecs_h.ecs_stage_is_readonly(this.nativeWorld);
    }

    public boolean isDeferred() {
        this.checkClosed();
        return flecs_h.ecs_is_deferred(this.nativeWorld);
    }

    public long setScope(long scopeId) {
        this.checkClosed();
        return flecs_h.ecs_set_scope(this.nativeWorld, scopeId);
    }

    public long getScope() {
        this.checkClosed();
        return flecs_h.ecs_get_scope(this.nativeWorld);
    }

    public PipelineBuilder pipeline() {
        this.checkClosed();
        return new PipelineBuilder(this);
    }

    public PipelineBuilder pipeline(String name) {
        this.checkClosed();
        return new PipelineBuilder(this, name);
    }

    public void runPipeline(long pipelineId, float deltaTime) {
        this.checkClosed();
        flecs_h.ecs_run_pipeline(this.nativeWorld, pipelineId, deltaTime);
    }

    public void runPipeline(Pipeline pipeline, float deltaTime) {
        this.runPipeline(pipeline.id(), deltaTime);
    }

    MemorySegment nativeHandle() {
        this.checkClosed();
        return this.nativeWorld;
    }

    public Arena arena() {
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

    public String toJson(boolean serializeBuiltin, boolean serializeModules) {
        this.checkClosed();

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment options = ecs_world_to_json_desc_t.allocate(tempArena);
            ecs_world_to_json_desc_t.serialize_builtin(options, serializeBuiltin);
            ecs_world_to_json_desc_t.serialize_modules(options, serializeModules);

            MemorySegment jsonSegment = flecs_h.ecs_world_to_json(this.nativeWorld, options);
            if (jsonSegment == null || jsonSegment.address() == 0) {
                return null;
            }

            return jsonSegment.getString(0);
        }
    }

    public String toJson() {
        return this.toJson(false, false);
    }

    public void fromJson(String json) {
        this.checkClosed();
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("JSON cannot be null or empty");
        }

        try (Arena tempArena = Arena.ofConfined()) {
            byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
            MemorySegment jsonSegment = tempArena.allocate(jsonBytes.length + 1);
            jsonSegment.asSlice(0, jsonBytes.length).copyFrom(MemorySegment.ofArray(jsonBytes));
            jsonSegment.set(ValueLayout.JAVA_BYTE, jsonBytes.length, (byte)0);

            MemorySegment ptrSegment = flecs_h.ecs_world_from_json(this.nativeWorld, jsonSegment, MemorySegment.NULL);
        }
    }

    public void enableRest() {
        this.enableRest((short) 27750);
    }

    public void enableRest(short port) {
        this.checkClosed();

        flecs_h.FlecsDocImport(this.nativeWorld);
        flecs_h.FlecsRestImport(this.nativeWorld);
        flecs_h.FlecsAlertsImport(this.nativeWorld);
        flecs_h.FlecsStatsImport(this.nativeWorld);
        flecs_h.FlecsMetricsImport(this.nativeWorld);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment restCompName = arena.allocateFrom("flecs.rest.Rest");
            long restCompId = flecs_h.ecs_lookup(this.nativeWorld, restCompName);

            if (restCompId == 0) {
                throw new IllegalStateException("Failed to find flecs.rest.Rest component.");
            }

            MemorySegment restData = arena.allocate(32);
            restData.set(ValueLayout.JAVA_SHORT, 0, port);

            flecs_h.ecs_set_id(this.nativeWorld, restCompId, restCompId, 32, restData);
        }
    }

    public void disableRest() {
        this.checkClosed();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment restCompName = arena.allocateFrom("flecs.rest.Rest");
            long restCompId = flecs_h.ecs_lookup(this.nativeWorld, restCompName);

            if( restCompId == 0) {
                throw new IllegalStateException("Failed to find flecs.rest.Rest component. Make sure FlecsRest module is imported.");
            }

            flecs_h.ecs_remove_id(this.nativeWorld, restCompId, restCompId);
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            if (this.owned && this.nativeWorld != null && this.nativeWorld.address() != 0) {
                flecs_h.ecs_fini(this.nativeWorld);
            }

            if (this.arena != null) {
                this.arena.close();
            }
        }
        this.closed = true;
    }

    @Override
    public String toString() {
        if (this.closed) {
            return "World[closed]";
        }
        return String.format("World[0x%x]", this.nativeWorld.address());
    }
}

