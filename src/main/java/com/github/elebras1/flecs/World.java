package com.github.elebras1.flecs;

import com.github.elebras1.flecs.callback.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.github.elebras1.flecs.util.internal.FlecsAllocator;
import com.github.elebras1.flecs.util.internal.FlecsLoader;

import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.foreign.ValueLayout.*;

public class World {
    public static final MemorySegment WHOLE_MEMORY = MemorySegment.NULL.reinterpret(Long.MAX_VALUE);
    private final MemorySegment worldSeg;
    private final Arena arena;
    private final ComponentRegistry componentRegistry;
    private final Map<Long, SystemCallbacks> systemCallbacks;
    private final Map<Long, ObserverCallbacks> observerCallbacks;
    private final FlecsBuffers defaultBuffers;
    private final FlecsContext contextCache;
    private World[] stages;
    private final boolean owned;
    private boolean destroyed;
    private Object ctx;

    static {
        FlecsLoader.load();
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
        private MemorySegment segment;
        private long capacity;

        NameBuffer(long initialCapacity) {
            this.capacity = initialCapacity;
            this.segment = FlecsAllocator.malloc(initialCapacity);
        }

        private MemorySegment ensure(long needed) {
            if (needed > this.capacity) {
                this.capacity = Math.max(needed, this.capacity * 2);
                FlecsAllocator.free(this.segment);
                this.segment = FlecsAllocator.malloc(this.capacity);
            }
            return segment;
        }

        public MemorySegment set(String name) {
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            long needed = nameBytes.length + 1;
            MemorySegment nameSeg = ensure(needed);
            nameSeg.fill((byte) 0);
            nameSeg.setString(0, name);
            return nameSeg;
        }

        @Override
        public void close() {
            if (this.segment != null && this.segment.address() != 0) {
                FlecsAllocator.free(this.segment);
            }
        }
    }

    private static final class ComponentBuffer implements AutoCloseable {
        private MemorySegment segment;
        private long capacity;

        ComponentBuffer(long initialCapacity) {
            this.capacity = initialCapacity;
            this.segment = FlecsAllocator.malloc(initialCapacity);
        }

        MemorySegment ensure(long needed) {
            if (needed > this.capacity) {
                this.capacity = Math.max(needed, this.capacity * 2);
                FlecsAllocator.free(segment);
                this.segment = FlecsAllocator.malloc(this.capacity);
            }
            return this.segment.fill((byte) 0);
        }

        @Override
        public void close() {
            if (this.segment != null && this.segment.address() != 0) {
                FlecsAllocator.free(this.segment);
            }
        }
    }

    private static final class EntityDescBuffer implements AutoCloseable {
        private final MemorySegment segment;

        EntityDescBuffer() {
            this.segment = FlecsAllocator.malloc(ecs_entity_desc_t.sizeof());
        }

        MemorySegment get() {
            this.segment.fill((byte) 0);
            return this.segment;
        }

        @Override
        public void close() {
            FlecsAllocator.free(this.segment);
        }
    }

    public World() {
        this.arena = Arena.ofConfined();
        this.worldSeg = flecs_h.ecs_init();
        if (this.worldSeg.address() == 0) {
            throw new IllegalStateException("Flecs world initialization failed");
        }
        this.componentRegistry = new ComponentRegistry(this);
        this.systemCallbacks = new HashMap<>();
        this.observerCallbacks = new HashMap<>();
        this.defaultBuffers = new FlecsBuffers();
        this.contextCache = new FlecsContext(this);
        this.stages = new World[] { this };
        this.destroyed = false;
        this.owned = true;
    }

    private World(MemorySegment stageSeg, ComponentRegistry componentRegistry) {
        this.arena = Arena.ofShared();
        this.worldSeg = stageSeg;
        this.componentRegistry = componentRegistry;
        this.systemCallbacks = new HashMap<>();
        this.observerCallbacks = new HashMap<>();
        this.defaultBuffers = new FlecsBuffers();
        this.contextCache = new FlecsContext(this);
        this.destroyed = false;
        this.owned = false;
    }

    public long entity() {
        this.checkDestroyed();
        return flecs_h.ecs_new(this.worldSeg);
    }

    public long entity(long parentId) {
        long id = flecs_h.ecs_new(this.worldSeg);
        MemorySegment dataSegment = this.getComponentBuffer(EcsParent.sizeof());
        EcsParent.value(dataSegment, parentId);
        flecs_h.ecs_set_id(this.worldSeg, id, flecs_h_1.FLECS_IDEcsParentID_(), EcsParent.sizeof(), dataSegment);
        return id;
    }

    public long entity(String name) {
        this.checkDestroyed();
        MemorySegment nameSegment = this.defaultBuffers.nameBuffer().set(name);

        MemorySegment descSeg = this.defaultBuffers.entityDescBuffer().get();
        ecs_entity_desc_t.name(descSeg, nameSegment);

        return flecs_h.ecs_entity_init(this.worldSeg, descSeg);
    }

    public long entity(long parentId, String name) {
        long id = this.entity(name);
        MemorySegment dataSeg = this.getComponentBuffer(EcsParent.sizeof());
        EcsParent.value(dataSeg, parentId);
        flecs_h.ecs_set_id(this.worldSeg, id, flecs_h_1.FLECS_IDEcsParentID_(), EcsParent.sizeof(), dataSeg);
        return id;
    }

    public Entity obtainEntity(long entityId) {
        assert entityId >= 0 : "Invalid entity ID: " + entityId;
        return new Entity(this, entityId);
    }

    public EntityView obtainEntityView(long entityId) {
        assert entityId >= 0 : "Invalid entity ID: " + entityId;

        return this.contextCache.getEntityView(entityId);
    }

    MemorySegment getComponentBuffer(long size) {
        return this.defaultBuffers.componentBuffer().ensure(size);
    }

    public long[] entityBulk(int count) {
        this.checkDestroyed();
        try(Arena tempArena = Arena.ofConfined()) {
            MemorySegment descSeg = ecs_bulk_desc_t.allocate(tempArena);
            ecs_bulk_desc_t.count(descSeg, count);
            ecs_bulk_desc_t.entities(descSeg, MemorySegment.NULL);

            MemorySegment idsSegment = flecs_h.ecs_bulk_init(this.worldSeg, descSeg);

            return idsSegment.asSlice(0, (long) count * Long.BYTES).toArray(JAVA_LONG);
        }
    }

    public final long[] entityBulk(int count, Class<?>... componentClasses) {
        this.checkDestroyed();

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

            MemorySegment descSeg = ecs_bulk_desc_t.allocate(tempArena);

            ecs_bulk_desc_t._canary(descSeg, 0);
            ecs_bulk_desc_t.entities(descSeg, MemorySegment.NULL);
            ecs_bulk_desc_t.count(descSeg, count);

            MemorySegment idsArray = ecs_bulk_desc_t.ids(descSeg);
            for (int i = 0; i < componentIds.length; i++) {
                idsArray.setAtIndex(JAVA_LONG, i, componentIds[i]);
            }

            ecs_bulk_desc_t.data(descSeg, MemorySegment.NULL);
            ecs_bulk_desc_t.table(descSeg, MemorySegment.NULL);

            MemorySegment entitiesSeg = flecs_h.ecs_bulk_init(this.worldSeg, descSeg);

            return entitiesSeg.asSlice(0, (long) count * Long.BYTES).toArray(JAVA_LONG);
        }
    }

    public void makeAlive(long entityId) {
        this.checkDestroyed();
        flecs_h.ecs_make_alive(this.worldSeg, entityId);
    }

    public void setVersion(long entityId) {
        this.checkDestroyed();
        flecs_h.ecs_set_version(this.worldSeg, entityId);
    }

    public int getVersion(long entityId) {
        this.checkDestroyed();
        return flecs_h.ecs_get_version(entityId);
    }

    public void setEntityRange(long idStart, long idEnd) {
        this.checkDestroyed();
        flecs_h.ecs_set_entity_range(this.worldSeg, idStart, idEnd);
    }

    public boolean enableRangeCheck(boolean enable) {
        this.checkDestroyed();
        return flecs_h.ecs_enable_range_check(this.worldSeg, enable);
    }

    public long prefab() {
        this.checkDestroyed();
        return flecs_h.ecs_new_w_id(this.worldSeg, FlecsConstants.EcsPrefab);
    }

    public boolean progress(float deltaTime) {
        this.checkDestroyed();
        return flecs_h.ecs_progress(this.worldSeg, deltaTime);
    }

    public boolean progress() {
        return progress(0.0f);
    }

    public QueryBuilder query() {
        this.checkDestroyed();
        return new QueryBuilder(this);
    }

    public Query query(String expr) {
        this.checkDestroyed();
        return this.query().expr(expr).build();
    }

    public long lookup(String name) {
        this.checkDestroyed();
        MemorySegment segment = this.defaultBuffers.nameBuffer().set(name);

        return flecs_h.ecs_lookup(this.worldSeg, segment);
    }

    public <T> long component(Class<T> componentClass) {
        this.checkDestroyed();
        return this.componentRegistry.register(componentClass);
    }

    public <T> long component(Class<T> componentClass, Consumer<ComponentHooks<T>> configuration) {
        this.checkDestroyed();
        long id = this.component(componentClass);
        Component<T> component = this.componentRegistry.getComponent(componentClass);
        ComponentHooks<T> hooks = new ComponentHooks<>(this, component);
        configuration.accept(hooks);
        hooks.install(this.worldSeg, id);
        return id;
    }

    public long getComponentId(Class<?> componentClass) {
        this.checkDestroyed();
        return this.componentRegistry.getComponentId(componentClass);
    }

    public void deleteWith(Class<?> componentClass) {
        this.checkDestroyed();
        long componentId = this.componentRegistry.getComponentId(componentClass);
        flecs_h.ecs_delete_with(this.worldSeg, componentId);
    }

    public int deleteEmptyTables(int limit) {
        this.checkDestroyed();
        try (Arena tempArena = Arena.ofConfined()) {
            MemoryLayout memoryLayout = MemoryLayout.structLayout(JAVA_INT.withName("limit"), JAVA_INT.withName("flags"));
            MemorySegment desc = tempArena.allocate(memoryLayout);
            desc.set(JAVA_INT, memoryLayout.byteOffset(MemoryLayout.PathElement.groupElement("limit")), limit);
            desc.set(JAVA_INT, memoryLayout.byteOffset(MemoryLayout.PathElement.groupElement("flags")), 0);

            return flecs_h.ecs_delete_empty_tables(this.worldSeg, desc);
        }
    }

    public void deferBegin() {
        this.checkDestroyed();
        flecs_h.ecs_defer_begin(this.worldSeg);
    }

    public void deferEnd() {
        this.checkDestroyed();
        flecs_h.ecs_defer_end(this.worldSeg);
    }

    public void deferSuspend() {
        this.checkDestroyed();
        flecs_h.ecs_defer_suspend(this.worldSeg);
    }

    public void deferResume() {
        this.checkDestroyed();
        flecs_h.ecs_defer_resume(this.worldSeg);
    }

    public SystemBuilder system() {
        this.checkDestroyed();
        return new SystemBuilder(this);
    }

    public SystemBuilder system(String name) {
        this.checkDestroyed();
        return new SystemBuilder(this, name);
    }

    public ObserverBuilder observer() {
        this.checkDestroyed();
        return new ObserverBuilder(this);
    }

    public ObserverBuilder observer(String name) {
        this.checkDestroyed();
        return new ObserverBuilder(this, name);
    }

    public <T> ObserverBuilder observer(Class<T> componentClass) {
        this.checkDestroyed();
        return new ObserverBuilder(this).with(componentClass);
    }

    public TimerBuilder timer() {
        this.checkDestroyed();
        return new TimerBuilder(this);
    }

    public ScriptBuilder script(String code) {
        this.checkDestroyed();
        return new ScriptBuilder(this, code);
    }

    public void setThreads(int threads) {
        this.checkDestroyed();
        flecs_h.ecs_set_threads(this.worldSeg, threads);

        this.resetStages();
    }

    public void setTaskThreads(int taskThreads) {
        this.checkDestroyed();
        flecs_h.ecs_set_task_threads(this.worldSeg, taskThreads);

        this.resetStages();
    }

    public void setPipeline(long pipelineId) {
        this.checkDestroyed();
        flecs_h.ecs_set_pipeline(this.worldSeg, pipelineId);
    }

    public long getMaxId() {
        this.checkDestroyed();
        return flecs_h.ecs_get_max_id(this.worldSeg);
    }

    public long[] getEntities() {
        this.checkDestroyed();
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment entitiesSeg = flecs_h.ecs_get_entities(tempArena, this.worldSeg);
            int count = entitiesSeg.get(JAVA_INT, ADDRESS.byteSize());

            long[] entities = new long[0];
            if (count > 0) {
                return ecs_entities_t.ids(entitiesSeg).reinterpret((long) count * Long.BYTES).toArray(JAVA_LONG);
            }

            return entities;
        }
    }

    public void exclusiveAccessBegin(String threadName) {
        this.checkDestroyed();

        if (threadName == null) {
            flecs_h.ecs_exclusive_access_begin(this.worldSeg, MemorySegment.NULL);
        } else {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment nameSegment = arena.allocateFrom(threadName);
                flecs_h.ecs_exclusive_access_begin(this.worldSeg, nameSegment);
            }
        }
    }

    public void exclusiveAccessBegin() {
        this.checkDestroyed();
        flecs_h.ecs_exclusive_access_begin(this.worldSeg, MemorySegment.NULL);
    }

    public void exclusiveAccessEnd(boolean lockWorld) {
        this.checkDestroyed();
        flecs_h.ecs_exclusive_access_end(this.worldSeg, lockWorld);
    }

    public void shrink() {
        this.checkDestroyed();
        flecs_h.ecs_shrink(this.worldSeg);
    }

    public void dim(int numberEntities) {
        this.checkDestroyed();
        flecs_h.ecs_dim(this.worldSeg, numberEntities);
    }

    public void frameBegin(float deltaTime) {
        this.checkDestroyed();
        flecs_h.ecs_frame_begin(this.worldSeg, deltaTime);
    }

    public void frameEnd() {
        this.checkDestroyed();
        flecs_h.ecs_frame_end(this.worldSeg);
    }

    public void quit() {
        this.checkDestroyed();
        flecs_h.ecs_quit(this.worldSeg);
    }

    public void shouldQuit() {
        this.checkDestroyed();
        flecs_h.ecs_should_quit(this.worldSeg);
    }

    public void measureFrameTime(boolean enable) {
        this.checkDestroyed();
        flecs_h.ecs_measure_frame_time(this.worldSeg, enable);
    }

    public void measureSystemTime(boolean enable) {
        this.checkDestroyed();
        flecs_h.ecs_measure_system_time(this.worldSeg, enable);
    }

    public void setTargetFps(float fps) {
        this.checkDestroyed();
        flecs_h.ecs_set_target_fps(this.worldSeg, fps);
    }

    public FlecsInfo getInfo() {
        this.checkDestroyed();

        MemorySegment infoSeg = flecs_h.ecs_get_world_info(this.worldSeg);
        if (infoSeg.equals(MemorySegment.NULL)) {
            throw new IllegalStateException("Failed to get world info");
        }

        infoSeg = infoSeg.reinterpret(ecs_world_info_t.layout().byteSize());

        return new FlecsInfo(
                ecs_world_info_t.last_component_id(infoSeg),
                ecs_world_info_t.min_id(infoSeg),
                ecs_world_info_t.max_id(infoSeg),
                ecs_world_info_t.delta_time_raw(infoSeg),
                ecs_world_info_t.delta_time(infoSeg),
                ecs_world_info_t.time_scale(infoSeg),
                ecs_world_info_t.target_fps(infoSeg),
                ecs_world_info_t.frame_time_total(infoSeg),
                ecs_world_info_t.system_time_total(infoSeg),
                ecs_world_info_t.emit_time_total(infoSeg),
                ecs_world_info_t.merge_time_total(infoSeg),
                ecs_world_info_t.rematch_time_total(infoSeg),
                ecs_world_info_t.world_time_total(infoSeg),
                ecs_world_info_t.world_time_total_raw(infoSeg),
                ecs_world_info_t.frame_count_total(infoSeg),
                ecs_world_info_t.merge_count_total(infoSeg),
                ecs_world_info_t.eval_comp_monitors_total(infoSeg),
                ecs_world_info_t.rematch_count_total(infoSeg),
                ecs_world_info_t.id_create_total(infoSeg),
                ecs_world_info_t.id_delete_total(infoSeg),
                ecs_world_info_t.table_create_total(infoSeg),
                ecs_world_info_t.table_delete_total(infoSeg),
                ecs_world_info_t.pipeline_build_count_total(infoSeg),
                ecs_world_info_t.systems_ran_total(infoSeg),
                ecs_world_info_t.observers_ran_total(infoSeg),
                ecs_world_info_t.queries_ran_total(infoSeg),
                ecs_world_info_t.tag_id_count(infoSeg),
                ecs_world_info_t.component_id_count(infoSeg),
                ecs_world_info_t.pair_id_count(infoSeg),
                ecs_world_info_t.table_count(infoSeg),
                ecs_world_info_t.creation_time(infoSeg),
                extractCommandStats(infoSeg),
                extractNamePrefix(infoSeg)
        );
    }

    private FlecsInfo.CommandStats extractCommandStats(MemorySegment info) {
        MemorySegment commandSeg = ecs_world_info_t.cmd(info);
        return new FlecsInfo.CommandStats(
                ecs_world_info_t.cmd.add_count(commandSeg),
                ecs_world_info_t.cmd.remove_count(commandSeg),
                ecs_world_info_t.cmd.delete_count(commandSeg),
                ecs_world_info_t.cmd.clear_count(commandSeg),
                ecs_world_info_t.cmd.set_count(commandSeg),
                ecs_world_info_t.cmd.ensure_count(commandSeg),
                ecs_world_info_t.cmd.modified_count(commandSeg),
                ecs_world_info_t.cmd.discard_count(commandSeg),
                ecs_world_info_t.cmd.event_count(commandSeg),
                ecs_world_info_t.cmd.other_count(commandSeg),
                ecs_world_info_t.cmd.batched_entity_count(commandSeg),
                ecs_world_info_t.cmd.batched_command_count(commandSeg)
        );
    }

    private String extractNamePrefix(MemorySegment info) {
        MemorySegment namePrefixSeg = ecs_world_info_t.name_prefix(info);
        if (namePrefixSeg.equals(MemorySegment.NULL)) {
            return null;
        }
        return namePrefixSeg.reinterpret(Long.MAX_VALUE).getString(0);
    }

    void registerSystemCallbacks(long systemId, IterCallback iterCallback, RunCallback runCallback, EntityCallback entityCallback) {
        if (iterCallback != null || runCallback != null || entityCallback != null) {
            this.systemCallbacks.put(systemId, new SystemCallbacks(iterCallback, runCallback, entityCallback));
        }
    }

    void registerObserverCallbacks(long observerId, IterCallback iterCallback, RunCallback runCallback, EntityCallback entityCallback) {
        if (iterCallback != null || runCallback != null || entityCallback != null) {
            this.observerCallbacks.put(observerId, new ObserverCallbacks(iterCallback, runCallback, entityCallback));
        }
    }

    FlecsContext viewCache() {
        return this.contextCache;
    }

    public void setStageCount(int count) {
        this.checkDestroyed();
        flecs_h.ecs_set_stage_count(this.worldSeg, count);

        this.resetStages();
    }

    private void resetStages() {
        int stageCount = flecs_h.ecs_get_stage_count(this.worldSeg);
        World[] newStages = new World[stageCount];
        newStages[0] = this;
        for (int i = 1; i < this.stages.length; i++) {
            if (this.stages[i] != null) {
                this.stages[i].arena.close();
            }
        }
        this.stages = newStages;
    }

    public int getStageCount() {
        this.checkDestroyed();
        return flecs_h.ecs_get_stage_count(this.worldSeg);
    }

    public World getStage(int stageId) {
        this.checkDestroyed();
        if( stageId < 0 || stageId >= this.getStageCount()) {
            throw new IllegalArgumentException("Invalid stage ID: " + stageId);
        }

        World stage = this.stages[stageId];
        if(stage == null) {
            MemorySegment stageSeg = flecs_h.ecs_get_stage(this.worldSeg, stageId);
            stage = new World(stageSeg, this.componentRegistry);
            this.stages[stageId] = stage;
        }
        return stage;
    }

    public int getStageId() {
        this.checkDestroyed();
        return flecs_h.ecs_stage_get_id(this.worldSeg);
    }

    public World stage() {
        this.checkDestroyed();
        MemorySegment stageSeg = flecs_h.ecs_stage_new(this.worldSeg);
        return new World(stageSeg, this.componentRegistry);
    }

    public World asyncStage() {
        this.checkDestroyed();
        MemorySegment stageSeg = flecs_h.ecs_stage_new(this.worldSeg);
        flecs_h.flecs_poly_release_(stageSeg);
        return new World(stageSeg, this.componentRegistry);
    }

    public void freeStage() {
        if (this.worldSeg != null && this.worldSeg.address() != 0) {
            flecs_h.ecs_stage_free(this.worldSeg);
        }
    }

    public void merge() {
        this.checkDestroyed();
        flecs_h.ecs_merge(this.worldSeg);
    }

    public boolean readonlyBegin(boolean multiThreaded) {
        this.checkDestroyed();
        return flecs_h.ecs_readonly_begin(this.worldSeg, multiThreaded);
    }

    public boolean readonlyBegin() {
        return this.readonlyBegin(false);
    }

    public void readonlyEnd() {
        this.checkDestroyed();
        flecs_h.ecs_readonly_end(this.worldSeg);
    }

    public boolean isReadonly() {
        this.checkDestroyed();
        return flecs_h.ecs_stage_is_readonly(this.worldSeg);
    }

    public void setCtx(Object ctx) {
        this.ctx = ctx;
    }

    public Object getCtx() {
        return this.ctx;
    }

    public boolean isDeferred() {
        this.checkDestroyed();
        return flecs_h.ecs_is_deferred(this.worldSeg);
    }

    public long setScope(long scopeId) {
        this.checkDestroyed();
        return flecs_h.ecs_set_scope(this.worldSeg, scopeId);
    }

    public long getScope() {
        this.checkDestroyed();
        return flecs_h.ecs_get_scope(this.worldSeg);
    }

    public long[] setLookupPath(long[] searchPath) {
        this.checkDestroyed();
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment pathSeg = tempArena.allocate(JAVA_LONG, searchPath.length + 1);
            MemorySegment.copy(searchPath, 0, pathSeg, JAVA_LONG, 0, searchPath.length);
            MemorySegment oldPathSeg = flecs_h.ecs_set_lookup_path(this.worldSeg, pathSeg);
            if (oldPathSeg.address() == 0) {
                return new long[0];
            }
            int len = 0;
            while (oldPathSeg.getAtIndex(JAVA_LONG, len) != 0L) {
                len++;
            }

            long[] oldPath = new long[len];
            MemorySegment.copy(oldPathSeg, JAVA_LONG, 0, oldPath, 0, len);
            return oldPath;
        }
    }

    public long lookup(String name, String sep, String rootSep, boolean recursive) {
        this.checkDestroyed();
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment nameSeg = tempArena.allocateFrom(name);
            MemorySegment sepSeg = tempArena.allocateFrom(sep);
            MemorySegment rootSepSeg = tempArena.allocateFrom(rootSep);
            return flecs_h.ecs_lookup_path_w_sep(this.worldSeg, 0, nameSeg, sepSeg, rootSepSeg, recursive);
        }
    }

    public PipelineBuilder pipeline() {
        this.checkDestroyed();
        return new PipelineBuilder(this);
    }

    public PipelineBuilder pipeline(String name) {
        this.checkDestroyed();
        return new PipelineBuilder(this, name);
    }

    public void runPipeline(long pipelineId, float deltaTime) {
        this.checkDestroyed();
        flecs_h.ecs_run_pipeline(this.worldSeg, pipelineId, deltaTime);
    }

    public void runPipeline(Pipeline pipeline, float deltaTime) {
        this.runPipeline(pipeline.id(), deltaTime);
    }

    MemorySegment worldSeg() {
        this.checkDestroyed();
        return this.worldSeg;
    }

    public Arena arena() {
        return this.arena;
    }

    ComponentRegistry componentRegistry() {
        this.checkDestroyed();
        return this.componentRegistry;
    }

    private void checkDestroyed() {
        if (this.destroyed) {
            throw new IllegalStateException("The Flecs world has already been destroyed");
        }
    }

    public String toJson(boolean serializeBuiltin, boolean serializeModules) {
        this.checkDestroyed();

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment options = ecs_world_to_json_desc_t.allocate(tempArena);
            ecs_world_to_json_desc_t.serialize_builtin(options, serializeBuiltin);
            ecs_world_to_json_desc_t.serialize_modules(options, serializeModules);

            MemorySegment jsonSeg = flecs_h.ecs_world_to_json(this.worldSeg, options);
            if (jsonSeg.address() == 0) {
                return null;
            }

            return jsonSeg.getString(0);
        }
    }

    public String toJson() {
        return this.toJson(false, false);
    }

    public void fromJson(String json) {
        this.checkDestroyed();
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("JSON cannot be null or empty");
        }

        try (Arena tempArena = Arena.ofConfined()) {
            byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
            MemorySegment jsonSeg = tempArena.allocate(jsonBytes.length + 1);
            jsonSeg.asSlice(0, jsonBytes.length).copyFrom(MemorySegment.ofArray(jsonBytes));
            jsonSeg.set(JAVA_BYTE, jsonBytes.length, (byte)0);

            MemorySegment resultSeg = flecs_h.ecs_world_from_json(this.worldSeg, jsonSeg, MemorySegment.NULL);
            if (resultSeg.address() == 0) {
                throw new RuntimeException("Failed to parse JSON into world");
            }
        }
    }

    public void enableRest() {
        this.enableRest((short) 27750);
    }

    public void enableRest(short port) {
        this.checkDestroyed();

        flecs_h.FlecsDocImport(this.worldSeg);
        flecs_h.FlecsRestImport(this.worldSeg);
        flecs_h.FlecsAlertsImport(this.worldSeg);
        flecs_h.FlecsStatsImport(this.worldSeg);
        flecs_h.FlecsMetricsImport(this.worldSeg);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment restCompName = arena.allocateFrom("flecs.rest.Rest");
            long restCompId = flecs_h.ecs_lookup(this.worldSeg, restCompName);

            if (restCompId == 0) {
                throw new IllegalStateException("Failed to find flecs.rest.Rest component.");
            }

            MemorySegment restDataSeg = arena.allocate(32);
            restDataSeg.set(JAVA_SHORT, 0, port);

            flecs_h.ecs_set_id(this.worldSeg, restCompId, restCompId, 32, restDataSeg);
        }
    }

    public void disableRest() {
        this.checkDestroyed();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment restCompNameSeg = arena.allocateFrom("flecs.rest.Rest");
            long restCompId = flecs_h.ecs_lookup(this.worldSeg, restCompNameSeg);

            if( restCompId == 0) {
                throw new IllegalStateException("Failed to find flecs.rest.Rest component. Make sure FlecsRest module is imported.");
            }

            flecs_h.ecs_remove_id(this.worldSeg, restCompId, restCompId);
        }
    }

    public void destroy() {
        if (!this.destroyed) {
            if (this.owned && this.worldSeg != null && this.worldSeg.address() != 0) {
                flecs_h.ecs_fini(this.worldSeg);
                for(World stage : this.stages) {
                    if (stage != null && stage != this) {
                        stage.arena.close();
                    }
                }
            }

            this.defaultBuffers.close();

            if (this.arena != null) {
                this.arena.close();
            }
        }
        this.destroyed = true;
    }

    @Override
    public String toString() {
        if (this.destroyed) {
            return "World[destroyed]";
        }
        return String.format("World[0x%x]", this.worldSeg.address());
    }
}

