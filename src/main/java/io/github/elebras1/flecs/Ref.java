package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class Ref<T> {

    private final World world;
    private final long entityId;
    private final long id;
    private final Class<T> componentClass;
    private final Component<T> component;
    private final MemorySegment refSeg;
    private final Arena arena;
    private boolean destructed;

    public Ref(World world, long entityId, Class<T> componentClass) {
        this(world, entityId, world.componentRegistry().getComponentId(componentClass), componentClass);
    }

    Ref(World world, long entityId, long id, Class<T> componentClass) {
        this.world = world;
        this.entityId = entityId;
        this.id = id;
        this.componentClass = componentClass;
        this.component = world.componentRegistry().getComponent(componentClass);
        this.arena = Arena.ofConfined();
        this.refSeg = flecs_h.ecs_ref_init_id(this.arena, world.worldSeg(), entityId, this.id);
    }

    public long entity() {
        return this.entityId;
    }

    public long component() {
        return this.id;
    }

    public T get() {
        this.checkDestructed();
        long address = flecs_h.ecs_ref_get_id(this.world.worldSeg(), this.refSeg, this.id);
        if (address == 0) {
            return null;
        }
        MemorySegment dataSeg = MemorySegment.ofAddress(address).reinterpret(this.component.size());
        return this.component.read(dataSeg, 0);
    }

    @SuppressWarnings("unchecked")
    public <A extends ComponentView> A getMutView() {
        this.checkDestructed();
        ComponentView view = this.world.viewCache().getComponentView(this.componentClass);
        long address = flecs_h.ecs_ref_get_id(this.world.worldSeg(), this.refSeg, this.id);
        if (address == 0) {
            return null;
        }

        view.setBaseAddress(address);
        return (A) view;
    }

    public T tryGet() {
        this.checkDestructed();
        long address = flecs_h.ecs_ref_get_id(this.world.worldSeg(), this.refSeg, this.id);
        if (address == 0) {
            return null;
        }
        MemorySegment dataSeg = MemorySegment.ofAddress(address).reinterpret(this.component.size());
        return this.component.read(dataSeg, 0);
    }

    public boolean has() {
        return this.tryGet() != null;
    }

    private void checkDestructed() {
        if (this.destructed) {
            throw new IllegalStateException("Ref has been destructed");
        }
    }

    public void destruct() {
        if (this.destructed) {
            return;
        }
        this.destructed = true;
        this.arena.close();
    }
}