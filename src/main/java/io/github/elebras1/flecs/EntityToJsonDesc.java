package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class EntityToJsonDesc {

    private boolean serializeEntityId;
    private boolean serializeDoc;
    private boolean serializeFullPaths;
    private boolean serializeInherited;
    private boolean serializeValues;
    private boolean serializeBuiltin;
    private boolean serializeTypeInfo;
    private boolean serializeAlerts;
    private long serializeRefs;
    private boolean serializeMatches;

    public EntityToJsonDesc() {
        this.serializeEntityId = false;
        this.serializeDoc = false;
        this.serializeFullPaths = true;
        this.serializeInherited = false;
        this.serializeValues = true;
        this.serializeBuiltin = false;
        this.serializeTypeInfo = false;
        this.serializeAlerts = false;
        this.serializeRefs = 0;
        this.serializeMatches = false;
    }

    public EntityToJsonDesc serializeEntityId(boolean value) {
        this.serializeEntityId = value;
        return this;
    }

    public EntityToJsonDesc serializeDoc(boolean value) {
        this.serializeDoc = value;
        return this;
    }

    public EntityToJsonDesc serializeFullPaths(boolean value) {
        this.serializeFullPaths = value;
        return this;
    }

    public EntityToJsonDesc serializeInherited(boolean value) {
        this.serializeInherited = value;
        return this;
    }

    public EntityToJsonDesc serializeValues(boolean value) {
        this.serializeValues = value;
        return this;
    }

    public EntityToJsonDesc serializeBuiltin(boolean value) {
        this.serializeBuiltin = value;
        return this;
    }

    public EntityToJsonDesc serializeTypeInfo(boolean value) {
        this.serializeTypeInfo = value;
        return this;
    }

    public EntityToJsonDesc serializeAlerts(boolean value) {
        this.serializeAlerts = value;
        return this;
    }

    public EntityToJsonDesc serializeRefs(long value) {
        this.serializeRefs = value;
        return this;
    }

    public EntityToJsonDesc serializeMatches(boolean value) {
        this.serializeMatches = value;
        return this;
    }

    MemorySegment allocate(Arena arena) {
        MemorySegment desc = ecs_entity_to_json_desc_t.allocate(arena);
        ecs_entity_to_json_desc_t.serialize_entity_id(desc, this.serializeEntityId);
        ecs_entity_to_json_desc_t.serialize_doc(desc, this.serializeDoc);
        ecs_entity_to_json_desc_t.serialize_full_paths(desc, this.serializeFullPaths);
        ecs_entity_to_json_desc_t.serialize_inherited(desc, this.serializeInherited);
        ecs_entity_to_json_desc_t.serialize_values(desc, this.serializeValues);
        ecs_entity_to_json_desc_t.serialize_builtin(desc, this.serializeBuiltin);
        ecs_entity_to_json_desc_t.serialize_type_info(desc, this.serializeTypeInfo);
        ecs_entity_to_json_desc_t.serialize_alerts(desc, this.serializeAlerts);
        ecs_entity_to_json_desc_t.serialize_refs(desc, this.serializeRefs);
        ecs_entity_to_json_desc_t.serialize_matches(desc, this.serializeMatches);
        return desc;
    }
}
