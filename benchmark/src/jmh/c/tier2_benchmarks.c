#include "benchmark.h"

#include <stdlib.h>
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
    ecs_entity_t *entities;
    ecs_entity_t likes;
    ecs_entity_t apples;
    int n;
} PairAddCtx;

static void pairAdd_before(void *ptr) {
    PairAddCtx *ctx = ptr;
    ctx->world = bench_world_new();
    ctx->likes = ecs_entity(ctx->world, { .name = "Likes" });
    ctx->apples = ecs_entity(ctx->world, { .name = "Apples" });
    for (int i = 0; i < ctx->n; i++) {
        ctx->entities[i] = ecs_new(ctx->world);
    }
}

static void pairAdd_after(void *ptr) {
    PairAddCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void pairAdd_run(void *ptr) {
    PairAddCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_add_pair(ctx->world, ctx->entities[i], ctx->likes, ctx->apples);
        acc ^= (unsigned long long)ctx->entities[i];
    }
    bench_sink_u64(acc);
}

static void run_pairAdd(int n) {
    PairAddCtx ctx = { NULL, bench_entities_alloc(n), 0, 0, n };
    Benchmark benchmark = { "pairAdd", n, NULL, pairAdd_before, pairAdd_run, pairAdd_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
}
typedef struct {
    ecs_world_t *world;
    ecs_query_t *query;
    int n;
} PairIterCtx;

static void pairIterate_setup(void *ptr) {
    PairIterCtx *ctx = ptr;
    ctx->world = bench_world_new();
    ecs_entity_t likes = ecs_entity(ctx->world, { .name = "Likes" });
    ecs_entity_t apples = ecs_entity(ctx->world, { .name = "Apples" });
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_add_pair(ctx->world, entity, likes, apples);
    }
    ctx->query = ecs_query(ctx->world, {
        .terms = {
            { .id = ecs_pair(likes, EcsWildcard) }
        }
    });
}

static void pairIterate_fini(void *ptr) {
    PairIterCtx *ctx = ptr;
    ecs_query_fini(ctx->query);
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void pairIterate_run(void *ptr) {
    PairIterCtx *ctx = ptr;
    unsigned long long count = 0;
    ecs_iter_t it = ecs_query_iter(ctx->world, ctx->query);
    while (ecs_query_next(&it)) {
        for (int i = 0; i < it.count; i++) {
            count += (unsigned long long)it.entities[i];
        }
    }
    bench_sink_u64(count);
}

static void run_pairIterate(int n) {
    PairIterCtx ctx = { NULL, NULL, n };
    Benchmark benchmark = { "pairIterate", n, pairIterate_setup, NULL, pairIterate_run, NULL, pairIterate_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
typedef struct {
    ecs_world_t *world;
    int n;
} HierarchyBuildCtx;

static void hierarchyBuild_before(void *ptr) {
    HierarchyBuildCtx *ctx = ptr;
    ctx->world = bench_world_new();
}

static void hierarchyBuild_after(void *ptr) {
    HierarchyBuildCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void hierarchyBuild_run(void *ptr) {
    HierarchyBuildCtx *ctx = ptr;
    unsigned long long acc = 0;
    int pairs = ctx->n / 2;
    for (int i = 0; i < pairs; i++) {
        ecs_entity_t parent = ecs_new(ctx->world);
        ecs_entity_t child = ecs_new(ctx->world);
        ecs_add_pair(ctx->world, child, EcsChildOf, parent);
        acc ^= (unsigned long long)parent;
        acc ^= (unsigned long long)child;
    }
    bench_sink_u64(acc);
}

static void run_hierarchyBuild(int n) {
    HierarchyBuildCtx ctx = { NULL, n };
    Benchmark benchmark = { "hierarchyBuild", n, NULL, hierarchyBuild_before, hierarchyBuild_run, hierarchyBuild_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
}
typedef struct {
    ecs_world_t *world;
    ecs_query_t *query;
    int n;
} HierarchyTraverseCtx;

static void hierarchyTraverse_setup(void *ptr) {
    HierarchyTraverseCtx *ctx = ptr;
    ctx->world = bench_world_new();
    int pairs = ctx->n / 2;
    for (int i = 0; i < pairs; i++) {
        ecs_entity_t parent = ecs_new(ctx->world);
        ecs_set(ctx->world, parent, Position, { 1.0f, 2.0f });
        ecs_entity_t child = ecs_new(ctx->world);
        ecs_add_pair(ctx->world, child, EcsChildOf, parent);
    }
    ctx->query = ecs_query(ctx->world, {
        .terms = {
            { .id = ecs_id(Position), .src.id = EcsUp, .trav = EcsChildOf }
        }
    });
}

static void hierarchyTraverse_fini(void *ptr) {
    HierarchyTraverseCtx *ctx = ptr;
    ecs_query_fini(ctx->query);
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void hierarchyTraverse_run(void *ptr) {
    HierarchyTraverseCtx *ctx = ptr;
    double sum = 0.0;
    ecs_iter_t it = ecs_query_iter(ctx->world, ctx->query);
    while (ecs_query_next(&it)) {
        const Position *positions = ecs_field(&it, Position, 0);
        for (int i = 0; i < it.count; i++) {
            sum += positions[0].x;
        }
    }
    bench_sink_f32((float)sum);
}

static void run_hierarchyTraverse(int n) {
    HierarchyTraverseCtx ctx = { NULL, NULL, n };
    Benchmark benchmark = { "hierarchyTraverse", n, hierarchyTraverse_setup, NULL, hierarchyTraverse_run, NULL, hierarchyTraverse_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
typedef struct {
    ecs_world_t *world;
    ecs_entity_t *entities;
    int *order;
    int n;
} PrefabInheritCtx;

static void prefabInheritGet_setup(void *ptr) {
    PrefabInheritCtx *ctx = ptr;
    ctx->world = bench_world_new();
    ecs_add_pair(ctx->world, ecs_id(Position), EcsOnInstantiate, EcsInherit);
    ecs_entity_t prefab = ecs_entity(ctx->world, { .add = ecs_ids(EcsPrefab) });
    ecs_set(ctx->world, prefab, Position, { 1.0f, 2.0f });
    for (int i = 0; i < ctx->n; i++) {
        ctx->entities[i] =
            ecs_new_w_pair(ctx->world, EcsIsA, prefab);
    }
    bench_shuffle(ctx->order, ctx->n, BENCH_SEED);
}

static void prefabInheritGet_fini(void *ptr) {
    PrefabInheritCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void prefabInheritGet_run(void *ptr) {
    PrefabInheritCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        const Position *position =
            ecs_get(ctx->world, ctx->entities[ctx->order[i]], Position);
        if (position) {
            acc ^= bench_f32_bits(position->x);
        }
    }
    bench_sink_u64(acc);
}

static void run_prefabInheritGet(int n) {
    PrefabInheritCtx ctx = { NULL, bench_entities_alloc(n), bench_order_alloc(n), n };
    Benchmark benchmark = { "prefabInheritGet", n, prefabInheritGet_setup, NULL, prefabInheritGet_run, NULL, prefabInheritGet_fini, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
    free(ctx.order);
}
static void observerAdd_callback(ecs_iter_t *it) {
    unsigned long long count = (unsigned long long)it->count;
    bench_sink_u64(count);
}

typedef struct {
    ecs_world_t *world;
    ecs_entity_t *entities;
    int n;
} ObserverCtx;

static void observerAdd_before(void *ptr) {
    ObserverCtx *ctx = ptr;
    ctx->world = bench_world_new();
    ecs_observer(ctx->world, {
        .query.terms = {
            { .id = ecs_id(Position) }
        },
        .events = { EcsOnAdd },
        .callback = observerAdd_callback
    });
    for (int i = 0; i < ctx->n; i++) {
        ctx->entities[i] = ecs_new(ctx->world);
    }
}

static void observerAdd_after(void *ptr) {
    ObserverCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void observerAdd_run(void *ptr) {
    ObserverCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_add(ctx->world, ctx->entities[i], Position);
        acc ^= (unsigned long long)ctx->entities[i];
    }
    bench_sink_u64(acc);
}

static void run_observerAdd(int n) {
    ObserverCtx ctx = { NULL, bench_entities_alloc(n), n };
    Benchmark benchmark = { "observerAdd", n, NULL, observerAdd_before, observerAdd_run, observerAdd_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
}
typedef struct {
    ecs_world_t *world;
    int n;
} SingletonCtx;

static void singletonGetSet_setup(void *ptr) {
    SingletonCtx *ctx = ptr;
    ctx->world = bench_world_new();
}

static void singletonGetSet_fini(void *ptr) {
    SingletonCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void singletonGetSet_run(void *ptr) {
    SingletonCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_singleton_set(ctx->world, Position, { 1.0f, 2.0f });
        const Position *position = ecs_singleton_get(ctx->world, Position);
        if (position) {
            acc ^= bench_f32_bits(position->x);
        }
    }
    bench_sink_u64(acc);
}

static void run_singletonGetSet(int n) {
    SingletonCtx ctx = { NULL, n };
    Benchmark benchmark = { "singletonGetSet", n, singletonGetSet_setup, NULL, singletonGetSet_run, NULL, singletonGetSet_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
typedef struct {
    ecs_world_t *world;
    ecs_entity_t *entities;
    int n;
} DeferCtx;

static void deferAdd_before(void *ptr) {
    DeferCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ctx->entities[i] = ecs_new(ctx->world);
    }
}

static void deferAdd_after(void *ptr) {
    DeferCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void deferAdd_run(void *ptr) {
    DeferCtx *ctx = ptr;
    ecs_defer_begin(ctx->world);
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_set(ctx->world, ctx->entities[i], Position, { 1.0f, 2.0f });
        ecs_set(ctx->world, ctx->entities[i], Velocity, { 3.0f, 4.0f });
        acc ^= (unsigned long long)ctx->entities[i];
    }
    ecs_defer_end(ctx->world);
    bench_sink_u64(acc);
}

static void run_deferAdd(int n) {
    DeferCtx ctx = { NULL, bench_entities_alloc(n), n };
    Benchmark benchmark = { "deferAdd", n, NULL, deferAdd_before, deferAdd_run, deferAdd_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
}
static void multiThreadedProgress_callback(ecs_iter_t *it) {
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
    int n;
} ProgressCtx;

static void multiThreadedProgress_setup(void *ptr) {
    ProgressCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ecs_new(ctx->world);
        ecs_set(ctx->world, entity, Position, { (float)i, (float)i + 1.0f });
        ecs_set(ctx->world, entity, Velocity, { 1.0f, 0.5f });
    }
    ecs_system(ctx->world, {
        .entity = ecs_entity(ctx->world, {
            .name = "MultiThreadedMove",
            .add = ecs_ids(ecs_dependson(EcsOnUpdate))
        }),
        .query.terms = {
            { .id = ecs_id(Position) },
            { .id = ecs_id(Velocity) }
        },
        .callback = multiThreadedProgress_callback,
        .multi_threaded = true
    });
    ecs_set_threads(ctx->world, 4);
}

static void multiThreadedProgress_fini(void *ptr) {
    ProgressCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void multiThreadedProgress_run(void *ptr) {
    ProgressCtx *ctx = ptr;
    ecs_progress(ctx->world, 0.0f);
}

static void run_multiThreadedProgress(int n) {
    ProgressCtx ctx = { NULL, n };
    Benchmark benchmark = { "multiThreadedProgress", n, multiThreadedProgress_setup, NULL, multiThreadedProgress_run, NULL, multiThreadedProgress_fini, &ctx };
    bench_run_benchmark(&benchmark);
}
typedef struct {
    ecs_world_t *world;
    ecs_entity_t *entities;
    int *order;
    int n;
} SetValueCtx;

static void setValue_before(void *ptr) {
    SetValueCtx *ctx = ptr;
    ctx->world = bench_world_new();
    for (int i = 0; i < ctx->n; i++) {
        ctx->entities[i] = ecs_new(ctx->world);
    }
    bench_shuffle(ctx->order, ctx->n, BENCH_SEED);
}

static void setValue_after(void *ptr) {
    SetValueCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void setValue_run(void *ptr) {
    SetValueCtx *ctx = ptr;
    unsigned long long acc = 0;
    for (int i = 0; i < ctx->n; i++) {
        ecs_entity_t entity = ctx->entities[ctx->order[i]];
        ecs_set(ctx->world, entity, Position, { 1.0f, 2.0f });
        acc ^= (unsigned long long)entity;
    }
    bench_sink_u64(acc);
}

static void run_setValue(int n) {
    SetValueCtx ctx = { NULL, bench_entities_alloc(n), bench_order_alloc(n), n };
    Benchmark benchmark = { "setValue", n, NULL, setValue_before, setValue_run, setValue_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
    free(ctx.entities);
    free(ctx.order);
}
typedef struct {
    ecs_world_t *world;
    int n;
} BulkCreateCtx;

static void bulkCreate_before(void *ptr) {
    BulkCreateCtx *ctx = ptr;
    ctx->world = bench_world_new();
}

static void bulkCreate_after(void *ptr) {
    BulkCreateCtx *ctx = ptr;
    bench_world_free(ctx->world);
    ctx->world = NULL;
}

static void bulkCreate_run(void *ptr) {
    BulkCreateCtx *ctx = ptr;
    ecs_bulk_desc_t desc = { 0 };
    desc.count = ctx->n;
    desc.ids[0] = ecs_id(Position);
    desc.ids[1] = ecs_id(Velocity);
    const ecs_entity_t *entities = ecs_bulk_init(ctx->world, &desc);
    bench_sink_u64((unsigned long long)entities[ctx->n - 1]);
}

static void run_bulkCreate(int n) {
    BulkCreateCtx ctx = { NULL, n };
    Benchmark benchmark = { "bulkCreate", n, NULL, bulkCreate_before, bulkCreate_run, bulkCreate_after, NULL, &ctx };
    bench_run_benchmark(&benchmark);
}
const BenchEntry tier2_benchmarks[] = {
    { "pairAdd", run_pairAdd },
    { "pairIterate", run_pairIterate },
    { "hierarchyBuild", run_hierarchyBuild },
    { "hierarchyTraverse", run_hierarchyTraverse },
    { "prefabInheritGet", run_prefabInheritGet },
    { "observerAdd", run_observerAdd },
    { "singletonGetSet", run_singletonGetSet },
    { "deferAdd", run_deferAdd },
    { "multiThreadedProgress", run_multiThreadedProgress },
    { "setValue", run_setValue },
    { "bulkCreate", run_bulkCreate },
};

const int tier2_benchmark_count =
    (int)(sizeof(tier2_benchmarks) / sizeof(tier2_benchmarks[0]));
