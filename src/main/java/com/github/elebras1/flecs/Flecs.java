package com.github.elebras1.flecs;

import com.github.elebras1.flecs.collection.EcsLongList;
import com.github.elebras1.flecs.generated.ecs_bulk_desc_t;
import com.github.elebras1.flecs.generated.ecs_entity_desc_t;
import com.github.elebras1.flecs.generated.flecs_h;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.JAVA_LONG;

public class Flecs implements AutoCloseable {
    
    private final MemorySegment nativeWorld;
    private final Arena arena;
    private final ComponentRegistry componentRegistry;
    private boolean closed = false;

    public Flecs() {
        this.arena = Arena.ofConfined();
        this.nativeWorld = flecs_h.ecs_init();
        
        if (this.nativeWorld == null || this.nativeWorld.address() == 0) {
            throw new IllegalStateException("Flecs world initialization failed");
        }

        this.componentRegistry = new ComponentRegistry(this);
    }

    public long entity() {
        this.checkClosed();
        long entityId = flecs_h.ecs_new(this.nativeWorld);
        return entityId;
    }

    public long entity(String name) {
        this.checkClosed();
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment nameSegment = tempArena.allocateFrom(name);
            MemorySegment desc = ecs_entity_desc_t.allocate(tempArena);

            ecs_entity_desc_t.name(desc, nameSegment);

            long entityId = flecs_h.ecs_entity_init(this.nativeWorld, desc);
            return entityId;
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
        EcsLongList ids = new EcsLongList(count);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment desc = ecs_bulk_desc_t.allocate(arena);
            ecs_bulk_desc_t.count(desc, count);
            ecs_bulk_desc_t.entities(desc, MemorySegment.NULL);

            MemorySegment idsSegment = flecs_h.ecs_bulk_init(nativeWorld, desc);

            for (int i = 0; i < count; i++) {
                ids.add(idsSegment.get(JAVA_LONG, (long) i * Long.BYTES));
            }
        } catch (Throwable t) {
            throw new RuntimeException("bulk entity creation failed", t);
        }

        return ids;
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
        return query().expr(expr).build();
    }

    public long lookup(String name) {
        this.checkClosed();
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment nameSegment = tempArena.allocateFrom(name);
            long entityId = flecs_h.ecs_lookup(this.nativeHandle(), nameSegment);
            if (entityId == 0) {
                return -1;
            }
            return entityId;
        }
    }

    public ComponentRegistry components() {
        this.checkClosed();
        return this.componentRegistry;
    }

    MemorySegment nativeHandle() {
        this.checkClosed();
        return this.nativeWorld;
    }

    Arena arena() {
        return this.arena;
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

