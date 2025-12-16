package com.github.elebras1.flecs;


import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentRegistry {

    private final World world;
    private final Map<Class<?>, Long> componentIds;
    private final Map<Long, Class<?>> componentClasses;

    protected ComponentRegistry(World world) {
        this.world = world;
        this.componentIds = new ConcurrentHashMap<>();
        this.componentClasses = new ConcurrentHashMap<>();
    }

    protected <T> long register(Class<T> componentClass) {
        if (this.componentIds.containsKey(componentClass)) {
            return this.componentIds.get(componentClass);
        }

        Component<T> component = this.getComponentInstance(componentClass);
        String componentName = componentClass.getName();

        long componentId = this.world.lookup(componentName);
        if(componentId == -1) {
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
        Long componentId = this.componentIds.get(componentClass);
        if (componentId == null) {
            throw new IllegalStateException("Component " + componentClass.getName() + " is not registered.");
        }
        return componentId;
    }

    protected <T> Component<T> getComponent(Class<T> componentClass) {
        if (!this.componentIds.containsKey(componentClass)) {
            throw new IllegalStateException("Component " + componentClass.getName() + " is not registered.");
        }
        return ComponentMap.getInstance(componentClass);
    }

    @SuppressWarnings("unchecked")
    protected <T> Component<T> getComponentById(long componentId) {
        Class<?> componentClass = this.componentClasses.get(componentId);
        if (componentClass == null) {
            throw new IllegalStateException("Component with ID " + componentId + " is not registered.");
        }
        return ComponentMap.getInstance((Class<T>) componentClass);
    }

    private <T> Component<T> getComponentInstance(Class<T> componentClass) {
        Component<T> component = ComponentMap.getInstance(componentClass);

        if (component == null) {
            throw new IllegalStateException("Component not found for " + componentClass.getName() + ". Make sure the record is annotated with @FlecsComponent and annotation processing is enabled.");
        }

        return component;
    }
}