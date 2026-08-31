package io.github.elebras1.flecs;

import io.github.elebras1.flecs.collection.ClassLongMap;
import io.github.elebras1.flecs.collection.LongClassMap;
import io.github.elebras1.flecs.collection.LongObjectMap;
import io.github.elebras1.flecs.util.Flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.util.List;

public class ComponentRegistry {

    private final World world;
    private final ClassLongMap componentIds;
    private final LongClassMap componentClasses;
    private final LongObjectMap<Component<?>> components;

    protected ComponentRegistry(World world) {
        this.world = world;
        this.componentIds = new ClassLongMap(ComponentMap.size());
        this.componentClasses = new LongClassMap(ComponentMap.size());
        this.components = new LongObjectMap<>(ComponentMap.size());
    }

    protected <T> long register(Class<T> componentClass) {
        long existingId = this.componentIds.get(componentClass);
        if (existingId != -1) {
            return existingId;
        }

        Component<T> component = this.getComponentInstance(componentClass);
        if (component == null) {
            throw new IllegalArgumentException("Component class in the argument is not a component : " + componentClass.getName());
        }
        String simpleName = componentClass.getSimpleName();
        String symbol = componentClass.getName();

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment symbolSegment = tempArena.allocateFrom(symbol);

            long componentId = flecs_h.ecs_lookup_symbol(this.world.worldSeg(), symbolSegment, false, false);

            if (componentId == 0) {
                MemorySegment nameSegment = tempArena.allocateFrom(simpleName);

                MemorySegment entityDesc = ecs_entity_desc_t.allocate(tempArena);
                ecs_entity_desc_t.name(entityDesc, nameSegment);
                ecs_entity_desc_t.symbol(entityDesc, symbolSegment);
                long scope = flecs_h.ecs_get_scope(this.world.worldSeg());
                ecs_entity_desc_t.parent(entityDesc, scope);

                long entityId = flecs_h.ecs_entity_init(this.world.worldSeg(), entityDesc);
                if (scope != 0) {
                    flecs_h.ecs_add_id(this.world.worldSeg(), entityId, flecs_h.ecs_make_pair(Flecs.ChildOf, scope));
                }

                MemorySegment componentDesc = ecs_component_desc_t.allocate(tempArena);
                ecs_component_desc_t.entity(componentDesc, entityId);

                MemorySegment typeInfo = ecs_component_desc_t.type(componentDesc);
                ecs_type_info_t.size(typeInfo, (int) component.size());
                ecs_type_info_t.alignment(typeInfo, (int) component.alignment());

                componentId = flecs_h.ecs_component_init(world.worldSeg(), componentDesc);

                if (componentId == 0) {
                    throw new IllegalStateException("Failed to register component: " + symbol);
                }

                this.registerReflectionData(tempArena, componentId, component.layout());
            }

            this.componentIds.put(componentClass, componentId);
            this.componentClasses.put(componentId, componentClass);
            return componentId;
        }
    }

    private void registerReflectionData(Arena tempArena, long componentId, MemoryLayout layout) {
        if (!(layout instanceof GroupLayout group)) {
            return;
        }

        List<MemoryLayout> memberLayouts = group.memberLayouts();
        long realMemberCount = memberLayouts.stream().filter(memoryLayout -> memoryLayout.name().isPresent()).count();
        if (realMemberCount == 0 || realMemberCount > flecs_h.ECS_MEMBER_DESC_CACHE_SIZE()) {
            return;
        }

        MemorySegment structDesc = ecs_struct_desc_t.allocate(tempArena);
        ecs_struct_desc_t.entity(structDesc, componentId);

        MemorySegment members = ecs_struct_desc_t.members(structDesc);
        long memberDescSize = ecs_member_t.sizeof();

        int i = 0;
        for (MemoryLayout member : memberLayouts) {
            if (member.name().isEmpty()) {
                continue;
            }
            String fieldName = member.name().get();
            long offset = group.byteOffset(PathElement.groupElement(fieldName));

            int count;
            MemoryLayout elementLayout;
            if (member instanceof SequenceLayout sequenceLayout) {
                count = (int) sequenceLayout.elementCount();
                elementLayout = sequenceLayout.elementLayout();
            } else {
                count = 0;
                elementLayout = member;
            }

            MemorySegment memberDesc = members.asSlice(i * memberDescSize, memberDescSize);
            ecs_member_t.name(memberDesc, tempArena.allocateFrom(fieldName));
            ecs_member_t.type(memberDesc, resolveFlecsPrimitive(elementLayout));
            ecs_member_t.count(memberDesc, count);
            ecs_member_t.offset(memberDesc, (int) offset);
            ecs_member_t.use_offset(memberDesc, true);
            i++;
        }

        long structId = flecs_h.ecs_struct_init(this.world.worldSeg(), structDesc);
        if (structId == 0) {
            throw new IllegalStateException("Failed to register reflection data for component id: " + componentId);
        }
    }


    protected <T> long getComponentId(Class<T> componentClass) {
        long id = this.componentIds.get(componentClass);

        if (id <= 0) {
            id = this.register(componentClass);
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

    protected Class<?> getComponentClassById(long componentId) {
        Class<?> componentClass = this.componentClasses.get(componentId);
        if(componentClass == null) {
            throw new IllegalArgumentException("Unknown component ID: " + componentId);
        }
        return componentClass;
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

    private static long resolveFlecsPrimitive(MemoryLayout elementLayout) {
        return switch (elementLayout) {
            case ValueLayout.OfBoolean _ -> flecs_h.FLECS_IDecs_bool_tID_();
            case ValueLayout.OfByte _ -> flecs_h.FLECS_IDecs_byte_tID_();
            case ValueLayout.OfChar _ -> flecs_h.FLECS_IDecs_char_tID_();
            case ValueLayout.OfShort _ -> flecs_h.FLECS_IDecs_i16_tID_();
            case ValueLayout.OfInt _ -> flecs_h.FLECS_IDecs_i32_tID_();
            case ValueLayout.OfLong _ -> flecs_h.FLECS_IDecs_i64_tID_();
            case ValueLayout.OfFloat _ -> flecs_h.FLECS_IDecs_f32_tID_();
            case ValueLayout.OfDouble _ -> flecs_h.FLECS_IDecs_f64_tID_();
            default -> throw new IllegalStateException("Unsupported member layout for flecs reflection: " + elementLayout);
        };
    }
}