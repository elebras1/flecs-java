#include "benchmark.h"

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

ECS_COMPONENT_DECLARE(Health);
ECS_COMPONENT_DECLARE(Ideology);

double benchmark_time_us(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (ts.tv_sec * 1e6) + (ts.tv_nsec / 1e3);
}

ecs_world_t *benchmark_world_new(void) {
    ecs_world_t *world = ecs_init();
    ECS_COMPONENT_DEFINE(world, Health);
    ECS_COMPONENT_DEFINE(world, Ideology);
    return world;
}

void benchmark_world_populate(ecs_world_t *world, int count) {
    for (int i = 0; i < count; i++) {
        ecs_entity_t e = ecs_new(world);
        ecs_set(world, e, Health,   { 100 });
        ecs_set(world, e, Ideology, { 0xFF0000, 10, 50 });
    }
}

void benchmark_run(
    const char  *name,
    void (*setup)(void *ctx),
    void (*teardown)(void *ctx),
    void (*fn)(void *ctx),
    void *ctx,
    BenchmarkResult *out
) {
    for (int i = 0; i < BENCH_WARMUP_RUNS; i++) {
        if (setup) {
            setup(ctx);
        }
        fn(ctx);
        if (teardown) {
            teardown(ctx);
        }
    }

    double *samples = malloc(sizeof(double) * BENCH_RUNS);

    for (int run = 0; run < BENCH_RUNS; run++) {
        if (setup) {
            setup(ctx);
        }
        double start = benchmark_time_us();
        fn(ctx);
        double end = benchmark_time_us();
        if (teardown) {
            teardown(ctx);
        }
        samples[run] = end - start;
    }

    double sum = 0.0;
    for (int i = 0; i < BENCH_RUNS; i++) sum += samples[i];
    double avg = sum / BENCH_RUNS;

    double var = 0.0;
    for (int i = 0; i < BENCH_RUNS; i++) {
        double d = samples[i] - avg;
        var += d * d;
    }
    var /= (BENCH_RUNS - 1);
    double stddev = sqrt(var);

    double error = (3.291 * stddev / sqrt(BENCH_RUNS)) / BENCH_ITERATIONS;

    free(samples);

    out->name = name;
    out->avg_us  = avg;
    out->per_op_us = avg / BENCH_ITERATIONS;
    out->stddev_us = stddev;
    out->error_us = error;
    out->runs = BENCH_RUNS;
    out->iterations = BENCH_ITERATIONS;
}


void benchmark_print(const BenchmarkResult *r) {
    printf("Benchmark: %-25s\n", r->name);
    printf("  Mode: AverageTime, Time: us\n");
    printf("  Score: %.6f ± %.6f us/op  (batch avg: %.3f us, stddev: %.3f us)\n",
           r->per_op_us, r->error_us, r->avg_us, r->stddev_us);
    printf("---------------------------------------------------\n");
}
