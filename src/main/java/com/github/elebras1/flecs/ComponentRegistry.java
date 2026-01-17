package com.github.elebras1.flecs;


import com.github.elebras1.flecs.collection.ClassLongMap;
import com.github.elebras1.flecs.collection.LongClassMap;
import com.github.elebras1.flecs.collection.LongObjectMap;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class ComponentRegistry {

    private final World world;
    private final ClassLongMap componentIds;
    private final LongClassMap componentClasses;
    private final LongObjectMap<Component<?>> components;

    protected ComponentRegistry(World world) {
        this.world = world;
        this.componentIds = new ClassLongMap();
        this.componentClasses = new LongClassMap();
        this.components = new LongObjectMap<>();
    }

    protected <T> long register(Class<T> componentClass) {
        long existingId = this.componentIds.get(componentClass);
        if(existingId != -1) {
            return existingId;
        }

        Component<T> component = this.getComponentInstance(componentClass);
        String componentName = componentClass.getName();

        long componentId = this.world.lookup(componentName);
        if(componentId == 0) {
            try (Arena tempArena = Arena.ofConfined()) {
                MemorySegment nameSegment = tempArena.allocateFrom(componentName);

                MemorySegment entityDesc = ecs_entity_desc_t.allocate(tempArena);
                ecs_entity_desc_t.name(entityDesc, nameSegment);

                long entityId = flecs_h.ecs_entity_init(this.world.nativeHandle(), entityDesc);

                MemorySegment componentDesc = ecs_component_desc_t.allocate(tempArena);
                ecs_component_desc_t.entity(componentDesc, entityId);

                MemorySegment typeInfo = ecs_component_desc_t.type(componentDesc);
                ecs_type_info_t.size(typeInfo, (int) component.size());
                ecs_type_info_t.alignment(typeInfo, (int) component.alignment());

                componentId = flecs_h.ecs_component_init(world.nativeHandle(), componentDesc);

                if (componentId == 0) {
                    throw new IllegalStateException("Failed to register component: " + componentName);
                }
            }
        }
        this.componentIds.put(componentClass, componentId);
        this.componentClasses.put(componentId, componentClass);
        return componentId;
    }

    protected <T> long getComponentId(Class<T> componentClass) {
        long id = this.componentIds.get(componentClass);

        if (id <= 0) {
            throw new IllegalArgumentException("Component not registered: " + componentClass.getName());
        }

        return id;
    }

    protected <T> Component<T> getComponent(Class<T> componentClass) {
        return ComponentMap.getInstance(componentClass);
    }

    @SuppressWarnings("unchecked")
    protected <T> Component<T> getComponentById(long componentId) {
        Component<T> component = (Component<T>) this.components.get(componentId);
        if(component == null) {
            return this.getAndCacheComponentInstance(componentId);
        }

        return component;
    }

    @SuppressWarnings("unchecked")
    private <T> Component<T> getAndCacheComponentInstance(long componentId) {
        Class<?> rawClass = this.componentClasses.get(componentId);
        if (rawClass == null) {
            throw new IllegalArgumentException("Unknown component ID: " + componentId);
        }

        Class<T> componentClass = (Class<T>) rawClass;
        Component<T> component = this.getComponentInstance(componentClass);

        this.components.put(componentId, component);
        return component;
    }

    private <T> Component<T> getComponentInstance(Class<T> componentClass) {
        return ComponentMap.getInstance(componentClass);
    }
}