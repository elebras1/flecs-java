#include "benchmark.h"

#include <stdio.h>
#include <stdlib.h>
typedef struct {
    ecs_world_t *world;
    ecs_entity_t *entities;
    int *order;
    int n;
} EntityCtx;

static ecs_entity_t *bench_entities_alloc(int n) {
    return (ecs_entity_t *)malloc(sizeof(ecs_entity_t) * (size_t)n);
}

static int *bench_order_alloc(int n) {
    return (int *)malloc(sizeof(int) * (size_t)n);
}

static void bench_world_free(ecs_world_t *world) {
    if (world) {
        ecs_fini(world);
    }
}
typedef struct {
    ecs_world_t *world;
    int n;
} CreateEmptyCtx;

static void create_world_before(void *ptr) {
    CreateEmptyCtx *ctx = ptr;
    ctx->world = bench_world_new();
}

static void create_world_after(void *ptr) {
    CreateEmptyCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void createEmpty_run(void *ptr) {
    CreateEmptyCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        acc += (unsigned long long)entity;
    }
    bench_sink_u64(acc);
}

static void run_createEmpty(int n) {
    CreateEmptyCtx ctx = { NULL, n };
    Benchmark benchmark = { "createEmpty", n, NULL, create_world_before, createEmpty_run, create_world_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
}
static void create1_run(void *ptr) {
    CreateEmptyCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { 1.0f, 2.0f });
        acc += (unsigned long long)entity;
    }
    bench_sink_u64(acc);
}

static void run_create1(int n) {
    CreateEmptyCtx ctx = { NULL, n };
    Benchmark benchmark = { "create1", n, NULL, create_world_before, create1_run, create_world_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
}
static void create2_run(void *ptr) {
    CreateEmptyCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { 1.0f, 2.0f });
        ecs_set(ctx->world, entity, Velocity, { 3.0f, 4.0f });
        acc += (unsigned long long)entity;
    }
    bench_sink_u64(acc);
}

static void run_create2(int n) {
    CreateEmptyCtx ctx = { NULL, n };
    Benchmark benchmark = { "create2", n, NULL, create_world_before, create2_run, create_world_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
}
typedef struct {
    ecs_world_t *world;
    ecs_entity_t prefab;
    int n;
} PrefabCtx;

static void createFromPrefab_before(void *ptr) {
    PrefabCtx *ctx = ptr;
    ctx->world = bench_world_new();
    ctx->prefab = ecs_entity(ctx->world, { .add = ecs_ids(EcsPrefab) });
    ecs_set(ctx->world, ctx->prefab, Position, { 1.0f, 2.0f });
    ecs_set(ctx->world, ctx->prefab, Velocity, { 3.0f, 4.0f });
}

static void createFromPrefab_after(void *ptr) {
    PrefabCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void createFromPrefab_run(void *ptr) {
    PrefabCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity =
            ecs_new_w_pair(ctx->world, EcsIsA, ctx->prefab);
        acc += (unsigned long long)entity;
    }
    bench_sink_u64(acc);
}

static void run_createFromPrefab(int n) {
    PrefabCtx ctx = { NULL, 0, n };
    Benchmark benchmark = { "createFromPrefab", n, NULL, createFromPrefab_before, createFromPrefab_run, createFromPrefab_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
}
typedef struct {
    ecs_world_t *world;
    ecs_entity_t *entities;
    int *order;
    int n;
} DestroyCtx;

static void destroy_world_before(void *ptr) {
    DestroyCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ctx->entities[i] = ecs_new(ctx->world);
    }
    bench_shuffle(ctx->order, ctx->n, BENCH_SEED);
}

static void destroy_world_after(void *ptr) {
    DestroyCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void destroy_run(void *ptr) {
    DestroyCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ctx->entities[ctx->order[i]];
        ecs_delete(ctx->world, entity);
        acc ^= (unsigned long long)entity;
    }
    bench_sink_u64(acc);
}

static void run_destroyEmpty(int n) {
    DestroyCtx ctx = { NULL, bench_entities_alloc(n), bench_order_alloc(n), n };
    Benchmark benchmark = { "destroyEmpty", n, NULL, destroy_world_before, destroy_run, destroy_world_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
    free(ctx.order);
}
static void destroy2_before(void *ptr) {
    DestroyCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { 1.0f, 2.0f });
        ecs_set(ctx->world, entity, Velocity, { 3.0f, 4.0f });
        ctx->entities[i] = entity;
    }
    bench_shuffle(ctx->order, ctx->n, BENCH_SEED);
}

static void run_destroy2(int n) {
    DestroyCtx ctx = { NULL, bench_entities_alloc(n), bench_order_alloc(n), n };
    Benchmark benchmark = { "destroy2", n, NULL, destroy2_before, destroy_run, destroy_world_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
    free(ctx.order);
}
static void addComponent_before(void *ptr) {
    EntityCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { 1.0f, 2.0f });
        ctx->entities[i] = entity;
    }
    bench_shuffle(ctx->order, ctx->n, BENCH_SEED);
}

static void addComponent_run(void *ptr) {
    EntityCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ctx->entities[ctx->order[i]];
        ecs_add(ctx->world, entity, Velocity);
        acc ^= (unsigned long long)entity;
    }
    bench_sink_u64(acc);
}

static void run_addComponent(int n) {
    EntityCtx ctx = { NULL, bench_entities_alloc(n), bench_order_alloc(n), n };
    Benchmark benchmark = { "addComponent", n, NULL, addComponent_before, addComponent_run, destroy_world_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
    free(ctx.order);
}
static void removeComponent_before(void *ptr) {
    EntityCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { 1.0f, 2.0f });
        ecs_set(ctx->world, entity, Velocity, { 3.0f, 4.0f });
        ctx->entities[i] = entity;
    }
    bench_shuffle(ctx->order, ctx->n, BENCH_SEED);
}

static void removeComponent_run(void *ptr) {
    EntityCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ctx->entities[ctx->order[i]];
        ecs_remove(ctx->world, entity, Velocity);
        acc ^= (unsigned long long)entity;
    }
    bench_sink_u64(acc);
}

static void run_removeComponent(int n) {
    EntityCtx ctx = { NULL, bench_entities_alloc(n), bench_order_alloc(n), n };
    Benchmark benchmark = { "removeComponent", n, NULL, removeComponent_before, removeComponent_run, destroy_world_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
    free(ctx.order);
}
static void access_setup(void *ptr) {
    EntityCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { (float)i, (float)i + 1.0f });
        ctx->entities[i] = entity;
    }
    bench_shuffle(ctx->order, ctx->n, BENCH_SEED);
}

static void access_fini(void *ptr) {
    EntityCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void get_run(void *ptr) {
    EntityCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        const Position *position =
            ecs_get(ctx->world, ctx->entities[ctx->order[i]], Position);
        if (position) {
            acc ^= bench_f32_bits(position->x);
            acc ^= bench_f32_bits(position->y);
        }
    }
    bench_sink_u64(acc);
}

static void run_get(int n) {
    EntityCtx ctx = { NULL, bench_entities_alloc(n), bench_order_alloc(n), n };
    Benchmark benchmark = { "get", n, access_setup, NULL, get_run, NULL, access_fini, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
    free(ctx.order);
}
static void getSet_run(void *ptr) {
    EntityCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ctx->entities[ctx->order[i]];
        Position *position = ecs_get_mut(ctx->world, entity, Position);
        if (position) {
            acc ^= bench_f32_bits(position->x);
            position->x += 1.0f;
            position->y += 0.5f;
        }
    }
    bench_sink_u64(acc);
}

static void run_getSet(int n) {
    EntityCtx ctx = { NULL, bench_entities_alloc(n), bench_order_alloc(n), n };
    Benchmark benchmark = { "getSet", n, access_setup, NULL, getSet_run, NULL, access_fini, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
    free(ctx.order);
}
static void has_run(void *ptr) {
    EntityCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        acc += ecs_has(ctx->world, ctx->entities[ctx->order[i]], Position) ? 1u : 0u;
    }
    bench_sink_u64(acc);
}

static void run_has(int n) {
    EntityCtx ctx = { NULL, bench_entities_alloc(n), bench_order_alloc(n), n };
    Benchmark benchmark = { "has", n, access_setup, NULL, has_run, NULL, access_fini, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
    free(ctx.order);
}
typedef struct {
    ecs_world_t *world;
    ecs_query_t *query;
    int n;
} QueryCtx;

static void query1Read_setup(void *ptr) {
    QueryCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { (float)i, (float)i + 1.0f });
        ecs_set(ctx->world, entity, Velocity, { 1.0f, 0.5f });
        ecs_add(ctx->world, entity, Tag);
    }
    ctx->query = ecs_query(ctx->world, {
        .terms = {
            { .id = ecs_id(Position) }
        }
    });
}

static void query_fini(void *ptr) {
    QueryCtx *ctx = ptr;
    ecs_query_fini(ctx->query);
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void query1Read_run(void *ptr) {
    QueryCtx *ctx = ptr;
    double sum = 0.0;
    ecs_iter_t it = ecs_query_iter(ctx->world, ctx->query);
    while (ecs_query_next(&it)) {
        const Position *positions = ecs_field(&it, Position, 0);
        for (int i = 0; i < it.count; i++) {
            sum += positions[i].x;
        }
    }
    bench_sink_f32((float)sum);
}

static void run_query1Read(int n) {
    QueryCtx ctx = { NULL, NULL, n };
    Benchmark benchmark = { "query1Read", n, query1Read_setup, NULL, query1Read_run, NULL, query_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
static void query2ReadWrite_setup(void *ptr) {
    QueryCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { (float)i, (float)i + 1.0f });
        ecs_set(ctx->world, entity, Velocity, { 1.0f, 0.5f });
        ecs_add(ctx->world, entity, Tag);
    }
    ctx->query = ecs_query(ctx->world, {
        .terms = {
            { .id = ecs_id(Position) },
            { .id = ecs_id(Velocity) }
        }
    });
}

static void query2ReadWrite_run(void *ptr) {
    QueryCtx *ctx = ptr;
    double sum = 0.0;
    ecs_iter_t it = ecs_query_iter(ctx->world, ctx->query);
    while (ecs_query_next(&it)) {
        Position *positions = ecs_field(&it, Position, 0);
        const Velocity *velocities = ecs_field(&it, Velocity, 1);
        for (int i = 0; i < it.count; i++) {
            positions[i].x += velocities[i].dx;
            positions[i].y += velocities[i].dy;
            sum += positions[i].x;
        }
    }
    bench_sink_f32((float)sum);
}

static void run_query2ReadWrite(int n) {
    QueryCtx ctx = { NULL, NULL, n };
    Benchmark benchmark = { "query2ReadWrite", n, query2ReadWrite_setup, NULL, query2ReadWrite_run, NULL, query_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
static void queryFiltered_setup(void *ptr) {
    QueryCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { (float)i, (float)i + 1.0f });
        ecs_set(ctx->world, entity, Velocity, { 1.0f, 0.5f });
        ecs_add(ctx->world, entity, Tag);
    }
    ctx->query = ecs_query(ctx->world, {
        .terms = {
            { .id = ecs_id(Position) },
            { .id = ecs_id(Tag) }
        }
    });
}

static void queryFiltered_run(void *ptr) {
    QueryCtx *ctx = ptr;
    double sum = 0.0;
    ecs_iter_t it = ecs_query_iter(ctx->world, ctx->query);
    while (ecs_query_next(&it)) {
        const Position *positions = ecs_field(&it, Position, 0);
        for (int i = 0; i < it.count; i++) {
            sum += positions[i].x;
        }
    }
    bench_sink_f32((float)sum);
}

static void run_queryFiltered(int n) {
    QueryCtx ctx = { NULL, NULL, n };
    Benchmark benchmark = { "queryFiltered", n, queryFiltered_setup, NULL, queryFiltered_run, NULL, query_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
static void systemRun_callback(ecs_iter_t *it) {
    Position *positions = ecs_field(it, Position, 0);
    const Velocity *velocities = ecs_field(it, Velocity, 1);
    double sum = 0.0;
    for (int i = 0; i < it->count; i++) {
        positions[i].x += velocities[i].dx;
        positions[i].y += velocities[i].dy;
        sum += positions[i].x;
    }
    bench_sink_f32((float)sum);
}

typedef struct {
    ecs_world_t *world;
    ecs_entity_t system;
    int n;
} SystemCtx;

static void systemRun_setup(void *ptr) {
    SystemCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { (float)i, (float)i + 1.0f });
        ecs_set(ctx->world, entity, Velocity, { 1.0f, 0.5f });
    }
    ctx->system = ecs_system(ctx->world, {
        .entity = ecs_entity(ctx->world, {
            .name = "Move",
            .add = ecs_ids(ecs_dependson(EcsOnUpdate))
        }),
        .query.terms = {
            { .id = ecs_id(Position) },
            { .id = ecs_id(Velocity) }
        },
        .callback = systemRun_callback
    });
}

static void systemRun_fini(void *ptr) {
    SystemCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void systemRun_run(void *ptr) {
    SystemCtx *ctx = ptr;
    ecs_run(ctx->world, ctx->system, 0.0f, NULL);
}

static void run_systemRun(int n) {
    SystemCtx ctx = { NULL, 0, n };
    Benchmark benchmark = { "systemRun", n, systemRun_setup, NULL, systemRun_run, NULL, systemRun_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
typedef struct {
    ecs_world_t *world;
    int n;
} QueryCreateCtx;

static void queryCreate_setup(void *ptr) {
    QueryCreateCtx *ctx = ptr;
    ctx->world = bench_world_new();
}

static void queryCreate_fini(void *ptr) {
    QueryCreateCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void queryCreate_run(void *ptr) {
    QueryCreateCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_query_t *query = ecs_query(ctx->world, {
            .terms = {
                { .id = ecs_id(Position) },
                { .id = ecs_id(Velocity) }
            }
        });
        acc ^= (unsigned long long)(uintptr_t)query;
        ecs_query_fini(query);
    }
    bench_sink_u64(acc);
}

static void run_queryCreate(int n) {
    QueryCreateCtx ctx = { NULL, n };
    Benchmark benchmark = { "queryCreate", n, queryCreate_setup, NULL, queryCreate_run, NULL, queryCreate_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
typedef struct {
    ecs_world_t *world;
    char *name_buffer;
    char **names;
    int *order;
    int n;
} LookupCtx;

static void lookup_setup(void *ptr) {
    LookupCtx *ctx = ptr;
    ctx->world = bench_world_new();
    ctx->name_buffer = (char *)malloc(16 * (size_t)ctx->n);
    ctx->names = (char **)malloc(sizeof(char *) * (size_t)ctx->n);
    for (int i = 0; i < ctx->n; i++) {
        char *name = ctx->name_buffer + (size_t)i * 16;
        snprintf(name, 16, "e%d", i);
        ctx->names[i] = name;
        ecs_entity(ctx->world, { .name = name });
    }
    bench_shuffle(ctx->order, ctx->n, BENCH_SEED);
}

static void lookup_fini(void *ptr) {
    LookupCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
    free(ctx->name_buffer);
    ctx->name_buffer = NULL;
    free(ctx->names);
    ctx->names = NULL;
}

static void lookup_run(void *ptr) {
    LookupCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_lookup(ctx->world, ctx->names[ctx->order[i]]);
        acc ^= (unsigned long long)entity;
    }
    bench_sink_u64(acc);
}

static void run_lookup(int n) {
    LookupCtx ctx = { NULL, NULL, NULL, bench_order_alloc(n), n };
    Benchmark benchmark = { "lookup", n, lookup_setup, NULL, lookup_run, NULL, lookup_fini, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.order);
}
static void systemRun5_callback(ecs_iter_t *it) {
    Position *positions = ecs_field(it, Position, 0);
    const Velocity *velocities = ecs_field(it, Velocity, 1);
    Health *healths = ecs_field(it, Health, 2);
    Mass *masses = ecs_field(it, Mass, 3);
    Age *ages = ecs_field(it, Age, 4);
    double sum = 0.0;
    for (int i = 0; i < it->count; i++) {
        positions[i].x += velocities[i].dx;
        positions[i].y += velocities[i].dy;
        healths[i].value += 1;
        masses[i].value += 0.5f;
        ages[i].value += 1;
        sum += positions[i].x;
    }
    bench_sink_f32((float)sum);
}

typedef struct {
    ecs_world_t *world;
    ecs_entity_t system;
    int n;
} System5Ctx;

static void systemRun5_setup(void *ptr) {
    System5Ctx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { 1.0f, 2.0f });
        ecs_set(ctx->world, entity, Velocity, { 0.5f, 0.25f });
        ecs_set(ctx->world, entity, Health, { 100 });
        ecs_set(ctx->world, entity, Mass, { 1.0f });
        ecs_set(ctx->world, entity, Age, { 10 });
    }
    ctx->system = ecs_system(ctx->world, {
        .entity = ecs_entity(ctx->world, {
            .name = "Move5",
            .add = ecs_ids(ecs_dependson(EcsOnUpdate))
        }),
        .query.terms = {
            { .id = ecs_id(Position) },
            { .id = ecs_id(Velocity) },
            { .id = ecs_id(Health) },
            { .id = ecs_id(Mass) },
            { .id = ecs_id(Age) }
        },
        .callback = systemRun5_callback
    });
}

static void systemRun5_fini(void *ptr) {
    System5Ctx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void systemRun5_run(void *ptr) {
    System5Ctx *ctx = ptr;
    ecs_run(ctx->world, ctx->system, 0.0f, NULL);
}

static void run_systemRun5(int n) {
    System5Ctx ctx = { NULL, 0, n };
    Benchmark benchmark = { "systemRun5", n, systemRun5_setup, NULL, systemRun5_run, NULL, systemRun5_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
const BenchEntry tier1_benchmarks[] = {
    { "createEmpty", run_createEmpty },
    { "create1", run_create1 },
    { "create2", run_create2 },
    { "createFromPrefab", run_createFromPrefab },
    { "destroyEmpty", run_destroyEmpty },
    { "destroy2", run_destroy2 },
    { "addComponent", run_addComponent },
    { "removeComponent", run_removeComponent },
    { "get", run_get },
    { "getSet", run_getSet },
    { "has", run_has },
    { "query1Read", run_query1Read },
    { "query2ReadWrite", run_query2ReadWrite },
    { "queryFiltered", run_queryFiltered },
    { "systemRun", run_systemRun },
    { "queryCreate", run_queryCreate },
    { "lookup", run_lookup },
    { "systemRun5", run_systemRun5 },
};

const int tier1_benchmark_count =
    (int)(sizeof(tier1_benchmarks) / sizeof(tier1_benchmarks[0]));
