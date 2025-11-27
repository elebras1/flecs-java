package com.github.elebras1.flecs;

import com.github.elebras1.flecs.generated.ecs_component_desc_t;
import com.github.elebras1.flecs.generated.ecs_entity_desc_t;
import com.github.elebras1.flecs.generated.ecs_type_info_t;
import com.github.elebras1.flecs.generated.flecs_h;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentRegistry {

    private final Flecs world;
    private final Map<Class<?>, ComponentRegistration<?>> registrations;
    private final Map<Long, ComponentRegistration<?>> registrationsById;

    private record ComponentRegistration<T>(long id, Component<T> component) {
    }

    protected ComponentRegistry(Flecs world) {
        this.world = world;
        this.registrations = new ConcurrentHashMap<>();
        this.registrationsById = new ConcurrentHashMap<>();
    }

    protected <T extends EcsComponent<T>> long register(Class<T> componentClass) {
        if (this.registrations.containsKey(componentClass)) {
            return this.registrations.get(componentClass).id;
        }

        Component<T> component = this.createComponentInstance(componentClass);
        String componentName = componentClass.getName();

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

            long componentId = flecs_h.ecs_component_init(world.nativeHandle(), componentDesc);

            if (componentId == 0) {
                throw new IllegalStateException("Failed to register component: " + componentName);
            }

            ComponentRegistration<T> registration = new ComponentRegistration<>(componentId, component);
            this.registrations.put(componentClass, registration);
            this.registrationsById.put(componentId, registration);
            return componentId;
        }
    }

    protected <T extends EcsComponent<T>> long getComponentId(Class<T> componentClass) {
        if(!this.registrations.containsKey(componentClass)) {
            throw new IllegalStateException("Component " + componentClass.getName() + " is not registered.");
        }

        return this.get(componentClass).id;
    }

    protected <T extends EcsComponent<T>> Component<T> getComponent(Class<T> componentClass) {
        if(!this.registrations.containsKey(componentClass)) {
            throw new IllegalStateException("Component " + componentClass.getName() + " is not registered.");
        }

        return this.get(componentClass).component;
    }

    @SuppressWarnings("unchecked")
    protected <T extends EcsComponent<T>> Component<T> getComponentById(long componentId) {
        ComponentRegistration<?> reg = this.registrationsById.get(componentId);
        if (reg == null) {
            throw new IllegalStateException("Component with ID " + componentId + " is not registered.");
        }
        return (Component<T>) reg.component;
    }

    @SuppressWarnings("unchecked")
    private <T extends EcsComponent<T>> Component<T> createComponentInstance(Class<T> componentClass) {
        try {
            var method = componentClass.getDeclaredMethod("component");
            return (Component<T>) method.invoke(null);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot create Component instance for " + componentClass.getName() + ". Class must either have a no-arg constructor or a static component() method.", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ComponentRegistration<T> get(Class<T> componentClass) {
        return (ComponentRegistration<T>) this.registrations.get(componentClass);
    }
}