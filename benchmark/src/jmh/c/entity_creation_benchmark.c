#include "benchmark.h"
#include "entity_creation_benchmark.h"

typedef struct {
    ecs_world_t *world;
} EntityCtx;

static void entity_setup(void *ctx) {
    EntityCtx *c = ctx;
    c->world = benchmark_world_new();
}

static void entity_teardown(void *ctx) {
    EntityCtx *c = ctx;
    ecs_fini(c->world);
}

static void entity_reset(void *ctx) {
    EntityCtx *c = ctx;
    ecs_fini(c->world);
    c->world = benchmark_world_new();
}

static void create(void *ctx) {
    EntityCtx *c = ctx;
    for (int i = 0; i < BENCH_ITERATIONS; i++) {
        ecs_new(c->world);
    }
    entity_reset(ctx);
}

static void create_with_2_components(void *ctx) {
    EntityCtx *c = ctx;
    for (int i = 0; i < BENCH_ITERATIONS; i++) {
        ecs_entity_t e = ecs_new(c->world);
        ecs_set(c->world, e, Health, { 100 });
        ecs_set(c->world, e, Ideology, { 0xFF0000, 10, 50 });
    }
    entity_reset(ctx);
}

void benchmark_create(BenchmarkResult *out) {
    EntityCtx ctx = { 0 };
    benchmark_run("create", entity_setup, entity_teardown, create, &ctx, out);
}

void benchmark_create_with_2_components(BenchmarkResult *out) {
    EntityCtx ctx = { 0 };
    benchmark_run("createWith2Components", entity_setup, entity_teardown, create_with_2_components, &ctx, out);
}
