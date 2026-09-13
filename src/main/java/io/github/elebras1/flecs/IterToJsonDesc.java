package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class IterToJsonDesc {

    private boolean serializeEntityIds;
    private boolean serializeValues;
    private boolean serializeBuiltin;
    private boolean serializeDoc;
    private boolean serializeFullPaths;
    private boolean serializeFields;
    private boolean serializeInherited;
    private boolean serializeTable;
    private boolean serializeTypeInfo;
    private boolean serializeFieldInfo;
    private boolean serializeQueryInfo;
    private boolean serializeQueryPlan;
    private boolean serializeQueryProfile;
    private boolean serializeResults;
    private boolean serializeAlerts;
    private long serializeRefs;
    private boolean serializeMatches;
    private boolean serializeParentsBeforeChildren;
    private MemorySegment query;

    public IterToJsonDesc() {
        this.serializeEntityIds = false;
        this.serializeValues = true;
        this.serializeBuiltin = false;
        this.serializeDoc = false;
        this.serializeFullPaths = true;
        this.serializeFields = true;
        this.serializeInherited = false;
        this.serializeTable = false;
        this.serializeTypeInfo = false;
        this.serializeFieldInfo = false;
        this.serializeQueryInfo = false;
        this.serializeQueryPlan = false;
        this.serializeQueryProfile = false;
        this.serializeResults = true;
        this.serializeAlerts = false;
        this.serializeRefs = 0;
        this.serializeMatches = false;
        this.serializeParentsBeforeChildren = false;
        this.query = MemorySegment.NULL;
    }

    public IterToJsonDesc serializeEntityIds(boolean value) {
        this.serializeEntityIds = value;
        return this;
    }

    public IterToJsonDesc serializeValues(boolean value) {
        this.serializeValues = value;
        return this;
    }

    public IterToJsonDesc serializeBuiltin(boolean value) {
        this.serializeBuiltin = value;
        return this;
    }

    public IterToJsonDesc serializeDoc(boolean value) {
        this.serializeDoc = value;
        return this;
    }

    public IterToJsonDesc serializeFullPaths(boolean value) {
        this.serializeFullPaths = value;
        return this;
    }

    public IterToJsonDesc serializeFields(boolean value) {
        this.serializeFields = value;
        return this;
    }

    public IterToJsonDesc serializeInherited(boolean value) {
        this.serializeInherited = value;
        return this;
    }

    public IterToJsonDesc serializeTable(boolean value) {
        this.serializeTable = value;
        return this;
    }

    public IterToJsonDesc serializeTypeInfo(boolean value) {
        this.serializeTypeInfo = value;
        return this;
    }

    public IterToJsonDesc serializeFieldInfo(boolean value) {
        this.serializeFieldInfo = value;
        return this;
    }

    public IterToJsonDesc serializeQueryInfo(boolean value) {
        this.serializeQueryInfo = value;
        return this;
    }

    public IterToJsonDesc serializeQueryPlan(boolean value) {
        this.serializeQueryPlan = value;
        return this;
    }

    public IterToJsonDesc serializeQueryProfile(boolean value) {
        this.serializeQueryProfile = value;
        return this;
    }

    public IterToJsonDesc serializeResults(boolean value) {
        this.serializeResults = value;
        return this;
    }

    public IterToJsonDesc serializeAlerts(boolean value) {
        this.serializeAlerts = value;
        return this;
    }

    public IterToJsonDesc serializeRefs(long value) {
        this.serializeRefs = value;
        return this;
    }

    public IterToJsonDesc serializeMatches(boolean value) {
        this.serializeMatches = value;
        return this;
    }

    public IterToJsonDesc serializeParentsBeforeChildren(boolean value) {
        this.serializeParentsBeforeChildren = value;
        return this;
    }

    IterToJsonDesc query(MemorySegment query) {
        this.query = query;
        return this;
    }

    MemorySegment allocate(Arena arena) {
        MemorySegment desc = ecs_iter_to_json_desc_t.allocate(arena);
        ecs_iter_to_json_desc_t.serialize_entity_ids(desc, this.serializeEntityIds);
        ecs_iter_to_json_desc_t.serialize_values(desc, this.serializeValues);
        ecs_iter_to_json_desc_t.serialize_builtin(desc, this.serializeBuiltin);
        ecs_iter_to_json_desc_t.serialize_doc(desc, this.serializeDoc);
        ecs_iter_to_json_desc_t.serialize_full_paths(desc, this.serializeFullPaths);
        ecs_iter_to_json_desc_t.serialize_fields(desc, this.serializeFields);
        ecs_iter_to_json_desc_t.serialize_inherited(desc, this.serializeInherited);
        ecs_iter_to_json_desc_t.serialize_table(desc, this.serializeTable);
        ecs_iter_to_json_desc_t.serialize_type_info(desc, this.serializeTypeInfo);
        ecs_iter_to_json_desc_t.serialize_field_info(desc, this.serializeFieldInfo);
        ecs_iter_to_json_desc_t.serialize_query_info(desc, this.serializeQueryInfo);
        ecs_iter_to_json_desc_t.serialize_query_plan(desc, this.serializeQueryPlan);
        ecs_iter_to_json_desc_t.serialize_query_profile(desc, this.serializeQueryProfile);
        ecs_iter_to_json_desc_t.dont_serialize_results(desc, !this.serializeResults);
        ecs_iter_to_json_desc_t.serialize_alerts(desc, this.serializeAlerts);
        ecs_iter_to_json_desc_t.serialize_refs(desc, this.serializeRefs);
        ecs_iter_to_json_desc_t.serialize_matches(desc, this.serializeMatches);
        ecs_iter_to_json_desc_t.serialize_parents_before_children(desc, this.serializeParentsBeforeChildren);
        ecs_iter_to_json_desc_t.query(desc, this.query);
        return desc;
    }
}
