#define _POSIX_C_SOURCE 199309L
#include <flecs.h>
#include <time.h>
#include <stdio.h>
#define ITERATIONS 100000
#define BENCH_RUNS 50

typedef struct {
    float value;
} Health;

typedef struct {
    int color;
    int speed;
    int stability;
} Ideology;

ECS_COMPONENT_DECLARE(Health);
ECS_COMPONENT_DECLARE(Ideology);

ecs_world_t* setup() {
    ecs_world_t *world = ecs_init();
    ECS_COMPONENT_DEFINE(world, Health);
    ECS_COMPONENT_DEFINE(world, Ideology);
    return world;
}

void teardown(ecs_world_t *world) {
    ecs_fini(world);
}

void bench_create(ecs_world_t *world) {
    for (int i = 0; i < ITERATIONS; i++) {
        ecs_new(world);
    }
}

void bench_create_2_components(ecs_world_t *world) {
    for (int i = 0; i < ITERATIONS; i++) {
        ecs_entity_t entity = ecs_new(world);
        ecs_set(world, entity, Health, { 100 });
        ecs_set(world, entity, Ideology, { 0xFF0000, 10, 50 });
    }
}

double get_time_us() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (ts.tv_sec * 1e6) + (ts.tv_nsec / 1e3);
}

void run_benchmark(const char* name, void (*func)(ecs_world_t*)) {
    double total_time_us = 0;
    for (int run = 0; run < BENCH_RUNS; run++) {
        ecs_world_t *world = setup();
        double start = get_time_us();
        func(world);
        double end = get_time_us();
        total_time_us += (end - start);
        teardown(world);
    }
    double avg_time_us = total_time_us / BENCH_RUNS;
    double time_per_op_us = avg_time_us / ITERATIONS;
    printf("Benchmark: %-25s\n", name);
    printf("  Mode: AverageTime, Time: us\n");
    printf("  Score: %.3f us/batch (100k) | %.6f us/op\n", avg_time_us, time_per_op_us);
    printf("---------------------------------------------------\n");
}

typedef struct {
    ecs_world_t *world;
    ecs_query_t *query;
} QueryBenchState;

static volatile int sink = 0;

QueryBenchState query_setup() {
    QueryBenchState s;
    s.world = ecs_init();
    ECS_COMPONENT_DEFINE(s.world, Health);
    ECS_COMPONENT_DEFINE(s.world, Ideology);

    for (int i = 0; i < ITERATIONS; i++) {
        ecs_entity_t entity = ecs_new(s.world);
        ecs_set(s.world, entity, Health, { 100 });
        ecs_set(s.world, entity, Ideology, { 0xFF0000, 10, 50 });
    }

    s.query = ecs_query(s.world, {
        .terms = {
            { ecs_id(Health) },
            { ecs_id(Ideology) }
        }
    });

    return s;
}

void query_teardown(QueryBenchState *s) {
    ecs_query_fini(s->query);
    ecs_fini(s->world);
}

void bench_query(QueryBenchState *s) {
    ecs_iter_t it = ecs_query_iter(s->world, s->query);
    while (ecs_query_next(&it)) {
        Health   *healths   = ecs_field(&it, Health,   0);
        Ideology *ideologies = ecs_field(&it, Ideology, 1);
        for (int i = 0; i < it.count; i++) {
            int v1 = (int)healths[i].value + 1;
            healths[i].value = (float)v1;

            int v2 = ideologies[i].color + 1;
            ideologies[i].color = v2;

            int v3 = ideologies[i].speed + 1;
            ideologies[i].speed = v3;

            sink ^= v1 ^ v2 ^ v3;
        }
    }
}

void run_query_benchmark() {
    double total_time_us = 0;

    for (int run = 0; run < BENCH_RUNS; run++) {
        QueryBenchState s = query_setup();

        double start = get_time_us();
        bench_query(&s);
        double end = get_time_us();

        total_time_us += (end - start);
        query_teardown(&s);
    }

    double avg_time_us = total_time_us / BENCH_RUNS;
    double time_per_op_us = avg_time_us / ITERATIONS;
    printf("Benchmark: %-25s\n", "query");
    printf("  Mode: AverageTime, Time: us\n");
    printf("  Score: %.3f us/batch (100k) | %.6f us/op\n", avg_time_us, time_per_op_us);
    printf("---------------------------------------------------\n");
}

int main(int argc, char *argv[]) {
    printf("== Flecs C Benchmark ==\n");
    run_benchmark("create", bench_create);
    run_benchmark("createWith2Components", bench_create_2_components);
    run_query_benchmark();
    return 0;
}
