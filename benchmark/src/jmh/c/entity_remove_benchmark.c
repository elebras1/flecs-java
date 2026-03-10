#include "benchmark.h"
#include "flecs.h"
#include "flecs/private/api_defines.h"
#include "entity_remove_benchmark.h"

typedef struct {
    ecs_world_t *world;
    ecs_entity_t entities[BENCH_ITERATIONS];
} EntityCtx;

static void entity_setup(void *ctx) {
    EntityCtx *c = ctx;
    c->world = benchmark_world_new();
    for (int i = 0; i < BENCH_ITERATIONS; i++) {
        c->entities[i] = ecs_new(c->world);
        ecs_set(c->world, c->entities[i], Health, { 100 });
        ecs_set(c->world, c->entities[i], Ideology, { 0xFF0000, 10, 50 });
    }
}

static void entity_teardown(void *ctx) {
    EntityCtx *c = ctx;
    ecs_fini(c->world);
}

static void destruct_with_2_components(void *ctx) {
    EntityCtx *c = ctx;
    for (int i = 0; i < BENCH_ITERATIONS; i++) {
        ecs_delete(c->world, c->entities[i]);
    }
}

static void remove_1_components(void *ctx) {
    EntityCtx *c = ctx;
    for (int i = 0; i < BENCH_ITERATIONS; i++) {
        ecs_remove_id(c->world, c->entities[i], ecs_id(Health));
    }
}

static void remove_2_components(void *ctx) {
    EntityCtx *c = ctx;
    for (int i = 0; i < BENCH_ITERATIONS; i++) {
        ecs_remove_id(c->world, c->entities[i], ecs_id(Health));
        ecs_remove_id(c->world, c->entities[i], ecs_id(Ideology));
    }
}

void benchmark_destruct_with_2_components(BenchmarkResult *out) {
    EntityCtx ctx = { 0 };
    benchmark_run("destruct_with_2_components", entity_setup, entity_teardown, destruct_with_2_components, &ctx, out);
}

void benchmark_remove_1_components(BenchmarkResult *out) {
    EntityCtx ctx = { 0 };
    benchmark_run("remove_1_components", entity_setup, entity_teardown, remove_1_components, &ctx, out);
}

void benchmark_remove_2_components(BenchmarkResult *out) {
    EntityCtx ctx = { 0 };
    benchmark_run("remove_2_components", entity_setup, entity_teardown, remove_2_components, &ctx, out);
}
