package com.github.elebras1.flecs;

import com.github.elebras1.flecs.generated.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class ComponentRegistry {

    private final Flecs world;

    ComponentRegistry(Flecs world) {
        this.world = world;
    }

    public <C extends Component<?>> long register(Class<C> clazz, C component) {
        try (Arena arena = Arena.ofConfined()) {
            String name = clazz.getName();
            MemorySegment nameSegment = arena.allocateFrom(clazz.getName());
            MemorySegment desc = ecs_component_desc_t.allocate(arena);

            MemorySegment entityDesc = ecs_entity_desc_t.allocate(arena);
            ecs_entity_desc_t.name(entityDesc, nameSegment);

            long entityId = flecs_h.ecs_entity_init(this.world.nativeHandle(), entityDesc);
            ecs_component_desc_t.entity(desc, entityId);

            MemorySegment typeInfo = ecs_component_desc_t.type(desc);
            ecs_type_info_t.size(typeInfo, (int) component.size());
            ecs_type_info_t.alignment(typeInfo, (int) component.layout().byteAlignment());

            long componentId = flecs_h.ecs_component_init(this.world.nativeHandle(), desc);

            if (componentId == 0) {
                throw new IllegalStateException("Échec de l'enregistrement du composant: " + name);
            }

            return componentId;
        }
    }

    public long registerTag(String name) {
        return this.world.entity(name).id();
    }
}

