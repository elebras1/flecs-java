package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class AlertBuilder {

    private static final int MAX_SEVERITY_FILTERS = 4;

    private final World world;
    private final Arena arena;
    private final MemorySegment desc;
    private int termCount;
    private int severityFilterCount;

    AlertBuilder(World world, String name) {
        this.world = world;
        this.arena = Arena.ofConfined();
        this.desc = ecs_alert_desc_t.allocate(this.arena);
        this.termCount = 0;
        this.severityFilterCount = 0;

        if (name != null) {
            MemorySegment entityDesc = ecs_entity_desc_t.allocate(this.arena);
            ecs_entity_desc_t.name(entityDesc, this.arena.allocateFrom(name));
            ecs_entity_desc_t.sep(entityDesc, this.arena.allocateFrom("::"));
            ecs_entity_desc_t.root_sep(entityDesc, this.arena.allocateFrom("::"));
            long entityId = flecs_h.ecs_entity_init(world.worldSeg(), entityDesc);
            ecs_alert_desc_t.entity(this.desc, entityId);
        }
    }

    public AlertBuilder with(long componentId) {
        if (this.termCount >= 32) {
            throw new IllegalStateException("Maximum number of terms (32) reached");
        }
        MemorySegment termSeg = ecs_query_desc_t.terms(ecs_alert_desc_t.query(this.desc), this.termCount);
        ecs_term_t.id(termSeg, componentId);
        this.termCount++;
        return this;
    }

    public <T> AlertBuilder with(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.with(componentId);
    }

    public AlertBuilder with(long first, long second) {
        return this.with(flecs_h.ecs_make_pair(first, second));
    }

    public AlertBuilder without(long componentId) {
        return this.with(componentId).not();
    }

    public <T> AlertBuilder without(Class<T> componentClass) {
        long componentId = this.world.componentRegistry().getComponentId(componentClass);
        return this.without(componentId);
    }

    public AlertBuilder not() {
        if (this.termCount == 0) {
            throw new IllegalStateException("No term to apply 'not' modifier to");
        }
        MemorySegment termSeg = ecs_query_desc_t.terms(ecs_alert_desc_t.query(this.desc), this.termCount - 1);
        ecs_term_t.oper(termSeg, (short) Flecs.Not);
        return this;
    }

    public AlertBuilder message(String message) {
        ecs_alert_desc_t.message(this.desc, this.arena.allocateFrom(message));
        return this;
    }

    public AlertBuilder brief(String brief) {
        ecs_alert_desc_t.brief(this.desc, this.arena.allocateFrom(brief));
        return this;
    }

    public AlertBuilder docName(String docName) {
        ecs_alert_desc_t.doc_name(this.desc, this.arena.allocateFrom(docName));
        return this;
    }

    public AlertBuilder severity(long severityId) {
        ecs_alert_desc_t.severity(this.desc, severityId);
        return this;
    }

    public AlertBuilder retainPeriod(float period) {
        ecs_alert_desc_t.retain_period(this.desc, period);
        return this;
    }

    public AlertBuilder severityFilter(long severityId, long withId, String var) {
        if (this.severityFilterCount >= MAX_SEVERITY_FILTERS) {
            throw new IllegalStateException("Maximum number of severity filters (" + MAX_SEVERITY_FILTERS + ") reached");
        }
        MemorySegment filter = ecs_alert_desc_t.severity_filters(this.desc, this.severityFilterCount);
        ecs_alert_severity_filter_t.severity(filter, severityId);
        ecs_alert_severity_filter_t.with(filter, withId);
        if (var != null) {
            ecs_alert_severity_filter_t.var_(filter, this.arena.allocateFrom(var));
        }
        this.severityFilterCount++;
        return this;
    }

    public AlertBuilder member(long memberId) {
        ecs_alert_desc_t.member(this.desc, memberId);
        return this;
    }

    public AlertBuilder id(long id) {
        ecs_alert_desc_t.id(this.desc, id);
        return this;
    }

    public AlertBuilder var(String var) {
        ecs_alert_desc_t.var_(this.desc, this.arena.allocateFrom(var));
        return this;
    }

    public Entity build() {
        try {
            long id = flecs_h.ecs_alert_init(this.world.worldSeg(), this.desc);
            if (id == 0) {
                throw new IllegalStateException("Failed to create alert");
            }
            return this.world.obtainEntity(id);
        } finally {
            this.arena.close();
        }
    }
}