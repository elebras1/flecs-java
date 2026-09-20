package io.github.elebras1.flecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class AlertBuilder extends QueryTermBuilder<AlertBuilder> {

    private static final int MAX_SEVERITY_FILTERS = 4;

    private final Arena arena;
    private final MemorySegment desc;
    private int severityFilterCount;

    AlertBuilder(World world, String name) {
        this(world, name, Arena.ofConfined());
    }

    private AlertBuilder(World world, String name, Arena arena) {
        this(world, name, arena, ecs_alert_desc_t.allocate(arena));
    }

    private AlertBuilder(World world, String name, Arena arena, MemorySegment desc) {
        super(world, arena, ecs_alert_desc_t.query(desc));
        this.arena = arena;
        this.desc = desc;
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

    @Override
    public AlertBuilder var(String var) {
        ecs_alert_desc_t.var_(this.desc, this.arena.allocateFrom(var));
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

    public <T> AlertBuilder severity(Class<T> severityClass) {
        return severity(this.world.componentRegistry().getComponentId(severityClass));
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
            ecs_alert_severity_filter_t.var_(filter, this.world.arena().allocateFrom(var));
        }
        this.severityFilterCount++;
        return this;
    }

    public <T> AlertBuilder member(Class<T> type, String member) {
        return member(type, member, null);
    }

    public <T> AlertBuilder member(Class<T> type, String member, String var) {
        long typeId = this.world.componentRegistry().getComponentId(type);
        long memberId = flecs_h.ecs_lookup_path_w_sep(
                this.world.worldSeg(),
                typeId,
                this.arena.allocateFrom(member),
                this.arena.allocateFrom("::"),
                this.arena.allocateFrom("::"),
                false);
        if (memberId == 0) {
            throw new IllegalArgumentException("member not found: " + member);
        }
        ecs_alert_desc_t.var_(this.desc, var == null ? MemorySegment.NULL : this.world.arena().allocateFrom(var));
        return member(memberId);
    }

    public AlertBuilder member(long memberId) {
        ecs_alert_desc_t.member(this.desc, memberId);
        return this;
    }

    public AlertBuilder id(long id) {
        ecs_alert_desc_t.id(this.desc, id);
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
