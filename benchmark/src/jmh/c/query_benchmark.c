#include "benchmark.h"
#include "query_benchmark.h"

typedef struct {
    ecs_world_t *world;
    ecs_query_t *query;
} QueryCtx;

static void query_setup(void *ctx) {
    QueryCtx *c = ctx;
    c->world = benchmark_world_new();
    benchmark_world_populate(c->world, BENCH_ITERATIONS);
    c->query = ecs_query(c->world, {
        .terms = {
            { ecs_id(Health)   },
            { ecs_id(Ideology) }
        }
    });
}

static void query_teardown(void *ctx) {
    QueryCtx *c = ctx;
    ecs_query_fini(c->query);
    ecs_fini(c->world);
}

static volatile int sink = 0;

static void query(void *ctx) {
    QueryCtx *c = ctx;

    ecs_iter_t it = ecs_query_iter(c->world, c->query);
    while (ecs_query_next(&it)) {
        Health   *healths    = ecs_field(&it, Health,   0);
        Ideology *ideologies = ecs_field(&it, Ideology, 1);

        for (int i = 0; i < it.count; i++) {
            int v1 = healths[i].value + 1;
            healths[i].value = v1;

            int v2 = ideologies[i].color + 1;
            ideologies[i].color = v2;

            int v3 = ideologies[i].factionDriftingSpeed + 1;
            ideologies[i].factionDriftingSpeed = v3;

            sink ^= v1 ^ v2 ^ v3;
        }
    }
}

void benchmark_query(BenchmarkResult *out) {
    QueryCtx ctx = {0};
    benchmark_run("query", query_setup, query_teardown, query, &ctx, out);
}
